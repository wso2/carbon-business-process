/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.core.ode.integration.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.engine.BpelDatabase;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.pmapi.ManagementException;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.namespace.QName;

/**
 * Utility methods for the process store
 */
public final class Utils {
    private static Log log = LogFactory.getLog(Utils.class);
    private static final String HEXES = "0123456789ABCDEF";


    private Utils() {
    }

    public static final Comparator<String> BY_VERSION =
            new Comparator<String>() {
                public int compare(String o1, String o2) {
                    String[] nameParts1 = o1.split("/");
                    String version1 = nameParts1[0].substring(nameParts1[0].lastIndexOf('-') + 1);
                    String[] nameParts2 = o2.split("/");
                    String version2 = nameParts2[0].substring(nameParts2[0].lastIndexOf('-') + 1);

                    return Integer.parseInt(version1) - Integer.parseInt(version2);
                }
            };

    /**
     * Create a property mapping based on the initial values in the deployment descriptor.
     *
     * @param properties properties
     * @param dd         deployment descriptor
     * @return property map
     */
    public static Map<QName, Node> calcInitialProperties(final Properties properties,
                                                         final TDeployment.Process dd) {
        HashMap<QName, Node> ret = new HashMap<QName, Node>();

        for (Object key1 : properties.keySet()) {
            String key = (String) key1;
            Document doc = DOMUtils.newDocument();
            doc.appendChild(doc.createElementNS(null, "temporary-simple-type-wrapper"));
            doc.getDocumentElement().appendChild(doc.createTextNode(properties.getProperty(key)));

            ret.put(new QName(key), doc.getDocumentElement());
        }

        if (dd.getPropertyList().size() > 0) {
            for (TDeployment.Process.Property property : dd.getPropertyList()) {
                Element elementContent = DOMUtils.getElementContent(property.getDomNode());
                if (elementContent != null) {
                    // We'll need DOM Level 3
                    Document doc = DOMUtils.newDocument();
                    doc.appendChild(doc.importNode(elementContent, true));
                    ret.put(property.getName(), doc.getDocumentElement());
                } else {
                    ret.put(property.getName(), property.getDomNode().getFirstChild());
                }

            }
        }
        return ret;
    }

    /**
     * Figure out the initial process state from the state in the deployment descriptor.
     *
     * @param dd deployment descriptor
     * @return process state
     */
    public static ProcessState calcInitialState(final TDeployment.Process dd) {
        ProcessState state = ProcessState.ACTIVE;

        if (dd.isSetActive() && !dd.getActive()) {
            state = ProcessState.DISABLED;
        }
        if (dd.isSetRetired() && dd.getRetired()) {
            state = ProcessState.RETIRED;
        }

        return state;
    }

    public static QName toPid(final QName processType, final long version) {
        return new QName(processType.getNamespaceURI(), processType.getLocalPart() + "-" + version);
    }

    public static QName getProcessType(final TDeployment.Process processDescriptor) {
        return processDescriptor.getType() != null ? processDescriptor.getType() :
                processDescriptor.getName();
    }

    /**
     * Extract BPEL archive to tenant's BPEL file system repository.
     *
     * @param deploymentContext BPEL deployment information
     * @throws Exception on BPEL archive extraction error
     */
    public static void extractBPELArchive(final BPELDeploymentContext deploymentContext)
            throws Exception {
        try {

            ZipInputStream zipStream = new ZipInputStream(
                    new FileInputStream(deploymentContext.getBpelArchive()));
            ZipEntry entry;

            String canonicalDescPath = new File(deploymentContext.getBpelPackageLocationInFileSystem()).
                    getCanonicalPath();
            while ((entry = zipStream.getNextEntry()) != null) {
                String canonicalEntryPath = new File(deploymentContext.getBpelPackageLocationInFileSystem() +
                        File.separator + entry.getName()).getCanonicalPath();
                if(!canonicalEntryPath.startsWith(canonicalDescPath)){
                    throw new Exception("Entry is outside of the target dir: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Extracting directory " + entry.getName());
                    }

                    if (!new File(deploymentContext.getBpelPackageLocationInFileSystem(),
                            entry.getName()).mkdirs()) {
                        throw new Exception("Archive extraction failed. Cannot create directory: "
                                + new File(deploymentContext.getBpelPackageLocationInFileSystem(),
                                entry.getName()).getAbsolutePath() + ".");
                    }
                    continue;
                }

                if (entry.getName().endsWith(".cbp")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipped extracting file: " + entry.getName());
                    }
                    continue;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Extracting file " + entry.getName());
                }

                File destFile = new File(deploymentContext.getBpelPackageLocationInFileSystem(),
                        entry.getName());
                if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                    throw new Exception("Archive extraction failed. Cannot create directory: "
                            + destFile.getParentFile().getAbsolutePath());
                }
                copyInputStream(zipStream,
                        new BufferedOutputStream(new FileOutputStream(destFile)));
            }

            zipStream.close();
        } catch (IOException e) {
            String errMsg = "Error occurred during extracting the archive: " +
                    deploymentContext.getArchiveName();
            log.error(errMsg, e);
            throw new Exception(errMsg, e);
        }
    }

    private static void copyInputStream(final InputStream in, final OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        out.close();
    }

    public static void deleteInstances(Collection<QName> processes) {
        if (processes != null) {
            String filter = null;
            for (QName q : processes) {
                if (filter == null) {
                    filter = "pid=" + q.toString();
                } else {
                    filter += "|" + q.toString();
                }
            }
            List<Long> iids = delete(filter);
            logIids(processes, iids);
        } else {
            //This very much unlikely to happen. How to handle this situation?
            log.warn("No processes found to delete instances");
        }
    }

    public static List<Long> delete(String filter) {
        log.info("Instance filter for instance deletion:" + filter);
        final InstanceFilter instanceFilter = new InstanceFilter(filter);

        final List<Long> ret = new LinkedList<Long>();
        try {
            dbexec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
                    for (ProcessInstanceDAO instance : instances) {
                        instance.delete(EnumSet.allOf(ProcessConf.CLEANUP_CATEGORY.class), true);
                        ret.add(instance.getInstanceId());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            String errMsg = "Exception during instance deletion. Filter: " + filter;
            log.error(errMsg, e);
            throw new ManagementException(errMsg, e);
        }

        return ret;
    }

    private static void logIids(Collection<QName> pids, List<Long> ids) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting instances of processes: ");
            for (QName q : pids) {
                log.debug(q);
            }
            log.debug("Instance IDs:");
            for (Long l : ids) {
                log.debug(l);
            }
        }
    }

    /**
     * Execute a database transaction, unwrapping nested
     * {@link org.apache.ode.bpel.pmapi.ManagementException}s.
     *
     * @param callable action to run
     * @return object of type T
     * @throws org.apache.ode.bpel.pmapi.ManagementException if exception occurred during transaction
     */
    private static <T> T dbexec(BpelDatabase.Callable<T> callable) throws ManagementException {
        try {
            BPELServerImpl bpelServer = (BPELServerImpl) BPELServiceComponent.getBPELServer();
            BpelDatabase bpelDb = bpelServer.getODEBPELServer().getBpelDb();
            return bpelDb.exec(callable);
        } catch (ManagementException me) {
            // Passthrough.
            throw me;
        } catch (Exception ex) {
            log.error("Exception during database operation", ex);
            throw new ManagementException("Exception during database operation", ex);
        }
    }


    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static byte[] createChecksum(File fileToCalculateMD5)
            throws IOException, NoSuchAlgorithmException {
        InputStream fis = new FileInputStream(fileToCalculateMD5);
        try {
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return complete.digest();
        } finally {
            fis.close();
        }
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string

    public static String getMD5Checksum(File file) throws IOException, NoSuchAlgorithmException {
        byte[] b = createChecksum(file);
        return getHex(b);
    }

    /**
     * Return the instances in the given processes list
     *
     * @param processesInPackage
     * @return Instance count
     * @throws ManagementException
     */
    public static long getInstanceCountForProcess(List<QName> processesInPackage) throws ManagementException {
        if (processesInPackage != null) {
            String filter = null;
            for (QName q : processesInPackage) {
                if (filter == null) {
                    filter = "pid=" + q.toString();
                } else {
                    filter += "|" + q.toString();
                }
            }
            try {
                final InstanceFilter instanceFilter = new InstanceFilter(filter);
                long count = (long) dbexec(new BpelDatabase.Callable<Object>() {

                    public Object run(BpelDAOConnection conn) {
                        return conn.instanceCount(instanceFilter);
                    }
                });
                return count;
            } catch (Exception e) {
                String errMsg = "Exception during instance count for deletion. Filter: " + filter;
                throw new ManagementException(errMsg, e);
            }

        } else {
            return 0;
        }
    }
}



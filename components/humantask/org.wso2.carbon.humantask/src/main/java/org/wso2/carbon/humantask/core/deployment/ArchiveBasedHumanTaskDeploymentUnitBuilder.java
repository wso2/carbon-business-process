/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.deployment;

import org.apache.axis2.util.XMLUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.humantask.HumanInteractionsDocument;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.deployment.config.HTDeploymentConfigDocument;
import org.wso2.carbon.humantask.core.utils.FileUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Process HumanTask zip archive to get humanTask related files
 * Are we doing this? This class is no longer used since a Registry Handler is used to get the job done
 */
public class ArchiveBasedHumanTaskDeploymentUnitBuilder extends HumanTaskDeploymentUnitBuilder {
    private static Log log = LogFactory.getLog(ArchiveBasedHumanTaskDeploymentUnitBuilder.class);

    private File humantaskDir;

    private String fileName;

    private int tenantId;

    private long version;

    private String md5sum;

    private List<Definition> wsdlDefinitions = new ArrayList<Definition>();

    private InputStream hiDefinition;

    private InputStream hiConfiguration;

    private File humanTaskDefinitionFile;

    private Map<String, InputStream> wsdlsMap = new HashMap<String, InputStream>();

    private Map<String, InputStream> schemasMap = new HashMap<String, InputStream>();

    private static final FileFilter wsdlFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".wsdl") && path.isFile();
        }
    };

    private static final FileFilter xsdFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".xsd") && path.isFile();
        }
    };

    private static final FileFilter humantaskFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(HumanTaskConstants.HUMANTASK_FILE_EXT) && path.isFile();
        }
    };

    // Build human task deployment unit with unextracted archive
    public ArchiveBasedHumanTaskDeploymentUnitBuilder(File hiArchiveZip, int tenantId, long version, String md5sum)
            throws HumanTaskDeploymentException {
        String hiArchiveZipName = hiArchiveZip.getName();
        this.fileName = FilenameUtils.removeExtension(hiArchiveZipName);
        this.tenantId = tenantId;
        this.version = version;
        this.md5sum = md5sum;
        humantaskDir = extractHumanTaskArchive(hiArchiveZip, tenantId, version);
        buildHumanInteractionDocuments();
        buildDeploymentConfiguration();
        buildWSDLs();
        buildSchemas();
    }

    // Build human task deployment unit with the
    public ArchiveBasedHumanTaskDeploymentUnitBuilder(File extractedTaskArchive, int tenantId, long version,
                                                      String packageName,
                                                      String md5sum) throws HumanTaskDeploymentException {
        this.fileName = packageName;
        this.version = version;
        this.humantaskDir = extractedTaskArchive;
        this.tenantId = tenantId;
        this.md5sum = md5sum;
        buildHumanInteractionDocuments();
        buildDeploymentConfiguration();
        buildWSDLs();
        buildSchemas();
    }

    @Override
    public void buildHumanInteractionDocuments() throws HumanTaskDeploymentException {
        if (hiDefinition == null) {
            List<File> hiDefinitionFiles = FileUtils.directoryEntriesInPath(humantaskDir,
                    humantaskFilter);
            if (hiDefinitionFiles.size() != 1) {
                String errMsg;
                if (hiDefinitionFiles.size() == 0) {
                    errMsg = "No human task definition files were found in " + fileName;
                } else {
                    errMsg = hiDefinitionFiles.size() +
                            " human task definition files were found in " + fileName;
                }
                log.error(errMsg);
                throw new HumanTaskDeploymentException(errMsg);
            }

            try {
                hiDefinition = new FileInputStream(hiDefinitionFiles.get(0));
                humanTaskDefinitionFile = hiDefinitionFiles.get(0);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
                throw new HumanTaskDeploymentException("Error building humantask archive; " +
                        fileName, e);
            }
        }
    }

    @Override
    public void buildDeploymentConfiguration() throws HumanTaskDeploymentException {
        if (hiConfiguration == null) {
            File humantaskConfFile = new File(humantaskDir, "htconfig.xml");
            if (!humantaskConfFile.exists()) {
                String errMsg = "htconfig.xml file not found for the " + fileName;
                log.error(errMsg);
                throw new HumanTaskDeploymentException(errMsg);
            }
            try {
                hiConfiguration = new FileInputStream(humantaskConfFile);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
                throw new HumanTaskDeploymentException("Error building humantask archive: " +
                        fileName, e);
            }
        }
    }

    @Override
    public void buildWSDLs() throws HumanTaskDeploymentException {
        URI baseUri = humantaskDir.toURI();
        for (File file : FileUtils.directoryEntriesInPath(humantaskDir, wsdlFilter)) {

            try {
                URI uri = baseUri.relativize(file.toURI());
                if(!uri.isAbsolute()) {
                    File f = new File(baseUri.getPath() + File.separator + uri.getPath());
                    URI abUri = f.toURI();
                    if(abUri.isAbsolute()){
                        uri = abUri;
                    }
                }

                WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
                reader.setFeature(HumanTaskConstants.JAVAX_WSDL_VERBOSE_MODE_KEY, false);
                reader.setFeature("javax.wsdl.importDocuments", true);
                Definition definition = reader.readWSDL(new HumanTaskWSDLLocator(uri));
                wsdlDefinitions.add(definition);

            } catch (WSDLException e) {
                log.error("Error processing wsdl " + file.getName());
                throw  new HumanTaskDeploymentException(" Error processing wsdl ", e);
            } catch (URISyntaxException e) {
                log.error("Invalid uri in reading wsdl " , e);
                throw new HumanTaskDeploymentException(" Invalid uri in reading wsdl " , e);
            }
//            wsdlsMap.put(file.getName(), is);
        }
    }

    @Override
    public void buildSchemas() throws HumanTaskDeploymentException {
        for (File file : FileUtils.directoryEntriesInPath(humantaskDir, xsdFilter)) {
            InputStream is;
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
                throw new HumanTaskDeploymentException("Error building humantask archive: " +
                        fileName, e);
            }
            schemasMap.put(file.getName(), is);
        }
    }

    @Override
    public HumanInteractionsDocument getHumanInteractionsDocument()
            throws HumanTaskDeploymentException {
        HumanInteractionsDocument hiDoc;
        try {
            hiDoc = HumanInteractionsDocument.Factory.parse(hiDefinition);
        } catch (Exception e) {
            String errMsg = "Error occurred while parsing the human interaction definition";
            log.error(errMsg, e);
            throw new HumanTaskDeploymentException(errMsg, e);
        }
        return hiDoc;
    }

    @Override
    public HTDeploymentConfigDocument getHTDeploymentConfigDocument()
            throws HumanTaskDeploymentException {
        HTDeploymentConfigDocument hiConf;
        try {
            hiConf = HTDeploymentConfigDocument.Factory.parse(hiConfiguration);
        } catch (Exception e) {
            String errMsg = "Error occurred while parsing the human interaction configuration " +
                    "file: htconfig.xml";
            log.error(errMsg, e);
            throw new HumanTaskDeploymentException(errMsg, e);
        }

        return hiConf;
    }

    @Override
    public String getArchiveName() {
        return fileName;
    }

    public List<Definition> getWsdlDefinitions() throws HumanTaskDeploymentException {
//        if (wsdlDefinitions.size() == 0) {
//
//
//
//            for (Map.Entry<String, InputStream> wsdl : wsdlsMap.entrySet()) {
//                try {
//                    wsdlDefinitions.add(readInTheWSDLFile(wsdl.getValue(),
//                                                          CarbonUtils.getCarbonHome() + File.separator +
//                                                          "repository" + File.separator +
//                                                          HumanTaskConstants.HUMANTASK_REPO_DIRECTORY +
//                                                          File.separator + this.tenantId +
//                                                          File.separator + this.fileName +
//                                                          File.separator + wsdl.getKey(), false));
//                } catch (WSDLException e) {
//                    String errMsg = "Error occurred while converting the wsdl input stream to " +
//                                    "wsdl definition";
//                    throw new HumanTaskDeploymentException(errMsg, e);
//                }
//            }
//        }
        return wsdlDefinitions;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public String getMd5sum() {
        return this.md5sum;
    }

    /**
     * Read the WSDL file given the input stream for the WSDL source
     *
     * @param in           WSDL input stream
     * @param entryName    ZIP file entry name
     * @param fromRegistry whether the wsdl is read from registry
     * @return WSDL Definition
     * @throws javax.wsdl.WSDLException at parser error
     */
    public static Definition readInTheWSDLFile(InputStream in, String entryName,
                                               boolean fromRegistry) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // switch off the verbose mode for all usecases
        reader.setFeature(HumanTaskConstants.JAVAX_WSDL_VERBOSE_MODE_KEY, false);
        reader.setFeature("javax.wsdl.importDocuments", true);

        Definition def;
        Document doc;
        try {
            doc = XMLUtils.newDocument(in);
        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser Configuration Error", e);
        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser SAX Error", e);

        } catch (IOException e) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error",
                    e);
        }

        // Log when and from where the WSDL is loaded.
        if (log.isDebugEnabled()) {
            log.debug("Reading 1.1 WSDL with base uri = " + entryName);
            log.debug("  the document base uri = " + entryName);
        }
        if (fromRegistry) {
            throw new UnsupportedOperationException("This operation is not currently " +
                    "supported in this version of WSO2 BPS.");
        } else {
            def = reader.readWSDL(entryName, doc.getDocumentElement());
        }
        def.setDocumentBaseURI(entryName);
        return def;

    }

    public File getHumanTaskDefinitionFile() {
        return humanTaskDefinitionFile;
    }



    //    public void persist() throws HumanTaskDeploymentException {
//        //create a collection for the DU n create relevent associations
//        Registry configRegistry;
//        try {
//            configRegistry = HumanTaskServiceComponent.getRegistryService().getConfigSystemRegistry();
//        } catch (RegistryException e) {
//            String errMsg = "Error while getting config registry";
//            log.error(errMsg, e);
//            throw new HumanTaskDeploymentException(errMsg, e);
//        }
//
//        String zipName = getFileName();
//        Collection du;
//        try {
//            if (configRegistry.resourceExists(HumanTaskConstants.HT_DEP_UNITS_REPO_LOCATION + zipName)) {
//                String errMsg = zipName + " is already exist.";
//                log.error(errMsg);
//                throw new HumanTaskDeploymentException(errMsg);
//            }
//            du = configRegistry.newCollection();
//        } catch (RegistryException e) {
//            //TODO Roleback WSDLS n Schemas in Governance Registry
//            String errMsg = "Error accessing registry";
//            log.error(errMsg, e);
//            throw new HumanTaskDeploymentException(errMsg, e);
//        }
//
//        if (du != null) {
//            String errMsg = "Error puting the collection to registry";
//            try {
//                du.addProperty("WSDL_COUNT", Integer.toString(wsdlsMap.size()));
//                //du.addProperty("STATUS", "DEPLOYED");
//                configRegistry.put(HumanTaskConstants.HT_DEP_UNITS_REPO_LOCATION + zipName + "/", du);
//            } catch (RegistryException e) {
//                //TODO Roleback WSDLS n Schemas in Governance Registry
//                log.error(errMsg, e);
//                throw new HumanTaskDeploymentException(errMsg, e);
//            }
//
//            try {
//                for (Map.Entry<String, InputStream> wsdlEntry : wsdlsMap.entrySet()) {
//                    Resource wsdlResource = configRegistry.newResource();
//                    wsdlResource.setContentStream(getWsdlInputStream(wsdlEntry));
//                    configRegistry.put(HumanTaskConstants.HT_DEP_UNITS_REPO_LOCATION +
//                                       this.fileName + "/" + wsdlEntry.getKey(), wsdlResource);
//                }
//
//                for (Map.Entry<String, InputStream> schemaEntry : schemasMap.entrySet()) {
//                    Resource schemaResource = configRegistry.newResource();
//                    schemaResource.setContentStream(getSchemaInputStream(schemaEntry));
//                    configRegistry.put(HumanTaskConstants.HT_DEP_UNITS_REPO_LOCATION +
//                                       this.fileName + "/" + schemaEntry.getKey(), schemaResource);
//                }
//            } catch (RegistryException e) {
//                log.error(errMsg, e);
//                throw new HumanTaskDeploymentException(errMsg, e);
//            } catch (IOException e) {
//                log.error(errMsg, e);
//                throw new HumanTaskDeploymentException(errMsg, e);
//            }
//
//            Resource fileResource;
//            try {
//                fileResource = configRegistry.newResource();
//            } catch (RegistryException e) {
//                String erMsg = "Error creating new resource";
//                log.error(erMsg, e);
//                throw new HumanTaskDeploymentException(erMsg, e);
//            }
//
//            if (fileResource != null) {
//                try {
//                    fileResource.setContent(hiDefinition);
//                    configRegistry.put(HumanTaskConstants.HT_DEP_UNITS_REPO_LOCATION + zipName +
//                                       "/" + "htDefinition.ht", fileResource);
//                    fileResource.setContentStream(hiConfiguration);
//                    configRegistry.put(HumanTaskConstants.HT_DEP_UNITS_REPO_LOCATION + zipName +
//                                       "/" + "htconfig.xml", fileResource);
//                } catch (RegistryException e) {
//                    String erMsg = "Error puting resource to registry";
//                    log.error(erMsg, e);
//                    throw new HumanTaskDeploymentException(erMsg, e);
//                }
//            }
//        }
//    }

//    public InputStream getWsdlInputStream(Map.Entry<String, InputStream> wsdlEntry)
//            throws IOException {
//        InputStream wsdl = wsdlEntry.getValue();
//        if (wsdl.markSupported()) {
//            wsdl.reset();
//        }
//        return wsdl;
//    }
//
//    public InputStream getSchemaInputStream(Map.Entry<String, InputStream> schemaEntry)
//            throws IOException {
//        InputStream schema = schemaEntry.getValue();
//        schema.reset();
//        return schema;
//    }

    /**
     * Extract HumanTask archive to tenant's HumanTask file system repository.
     * Version is passed to the method and the final extract directory will be {taskPackageName}-{version}.
     * Task  version is unique for all task packages.
     * Example. If the task package name is ClaimsApproval and this is the first task to be deployed in the server
     * resulting in task version being 1, then the extracted directory will be
     * ClaimsApproval-1 located in the corresponding tenants humantasks directory
     * @param archiveFile zip file
     * @param tenantId    Tenant ID
     * @return Extracted directory
     * @throws HumanTaskDeploymentException If an error occured
     */
    public static File extractHumanTaskArchive(final File archiveFile, int tenantId, long version)
            throws HumanTaskDeploymentException {
        ZipInputStream zipStream = null;

        try {
            String humanTaskExtractionLocation = CarbonUtils.getCarbonHome() + File.separator +
                    "repository" + File.separator +
                    HumanTaskConstants.HUMANTASK_REPO_DIRECTORY + File.separator +
                    tenantId + File.separator + FilenameUtils.removeExtension(archiveFile.getName()) + "-" + version;

            zipStream = new ZipInputStream(new FileInputStream(archiveFile));
            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Extracting directory " + entry.getName());
                    }
                    if (!new File(humanTaskExtractionLocation, entry.getName()).mkdirs() &&
                            !new File(humanTaskExtractionLocation, entry.getName()).exists()) {
                        throw new HumanTaskDeploymentException("Archive extraction failed. " +
                                "Cannot create directory: "
                                + new File(humanTaskExtractionLocation,
                                entry.getName()).getAbsolutePath() + ".");
                    }
                    continue;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Extracting file " + entry.getName());
                }

                File destFile = new File(humanTaskExtractionLocation, entry.getName());

                if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                    throw new HumanTaskDeploymentException("Archive extraction failed. " +
                            "Cannot create directory: "
                            + destFile.getParentFile().getAbsolutePath());
                }
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
                copyInputStream(zipStream, outputStream);
            }


            return new File(humanTaskExtractionLocation);
        } catch (IOException e) {
            String errMsg = "Error occurred during extracting the archive: " + archiveFile;
            log.error(errMsg, e);
            throw new HumanTaskDeploymentException(errMsg, e);
        } finally {
            if(zipStream != null){
                try {
                    zipStream.close();

                } catch (IOException e) {
                    String errMsg = "Error occurred during extracting the archive: " + archiveFile;
                    log.error(errMsg+ e);
                    throw new HumanTaskDeploymentException(errMsg, e);
                }
            }
        }
    }

    private static void copyInputStream(final InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        out.close();
    }
}

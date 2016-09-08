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

package org.wso2.carbon.bpel.ui.fileupload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.store.DeploymentUnitDir;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/**
 *
 */
public class BPELUploadExecutor extends AbstractFileUploadExecutor {
    private static final String[] ALLOWED_FILE_EXTENSIONS =
            new String[]{".zip"};
    private static Log log = LogFactory.getLog(BPELUploadExecutor.class);

    private static void addDir(File dirObj, ZipOutputStream out, int basePathLen) throws Exception {
        if (dirObj != null && out != null) {
            File[] files = dirObj.listFiles();
            byte[] tmpBuf = new byte[2048];
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        addDir(file, out, basePathLen);
                        continue;
                    }
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(file.getAbsolutePath());
                        if (log.isDebugEnabled()) {
                            log.debug("Adding: " + file.getAbsolutePath());
                        }
                        out.putNextEntry(new ZipEntry(file.getAbsolutePath().substring(basePathLen)));
                        int len;
                        while ((len = in.read(tmpBuf)) > 0) {
                            out.write(tmpBuf, 0, len);
                        }
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
            }
        }
    }

    public boolean execute(HttpServletRequest request,
                           HttpServletResponse response) throws CarbonException, IOException {
        String errMsg;

        response.setContentType("text/html; charset=utf-8");

        PrintWriter out = response.getWriter();
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();

        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed.";
            log.error(msg);
            out.write("<textarea>" +
                    "(function(){i18n.fileUplodedFailed();})();" +
                    "</textarea>");
            return true;
        }
        BPELUploaderClient uploaderClient = new BPELUploaderClient(configurationContext,
                serverURL + "BPELUploader", cookie);

        SaveExtractReturn uploadedFiles = null;
        ArrayList<String> extractedFiles = new ArrayList<String>();
        try {
            for (FileItemData fieldData : fileItemsMap.get("bpelFileName")) {
                String fileName = getFileName(fieldData.getFileItem().getName());
                //Check filename for \ charactors. This cannot be handled at the lower stages.
                if (fileName.matches("(.*[\\\\].*[/].*|.*[/].*[\\\\].*)")) {
                    log.error("BPEL Package Validation Failure: one or many of the following illegal characters are " +
                            "in " +
                            "the package.\n ~!@#$;%^*()+={}[]| \\<>");
                    throw new Exception("BPEL Package Validation Failure: one or many of the following illegal " +
                            "characters " +
                            "are in the package. ~!@#$;%^*()+={}[]| \\<>");
                }
                //Check file extension.
                checkServiceFileExtensionValidity(fileName, ALLOWED_FILE_EXTENSIONS);

                if (fileName.lastIndexOf('\\') != -1) {
                    int indexOfColon = fileName.lastIndexOf('\\') + 1;
                    fileName = fileName.substring(indexOfColon, fileName.length());
                }
                if (fieldData.getFileItem().getFieldName().equals("bpelFileName")) {
                    uploadedFiles = saveAndExtractUploadedFile(fieldData.getFileItem());
                    extractedFiles.add(uploadedFiles.extractedFile);
                    validateBPELPackage(uploadedFiles.extractedFile);
                    DataSource dataSource = new FileDataSource(uploadedFiles.zipFile);
                    uploaderClient.addUploadedFileItem(new DataHandler(dataSource), fileName, "zip");

                }
            }
            uploaderClient.uploadFileItems();
            String msg = "Your BPEL package been uploaded successfully. Please refresh this page in a" +
                    " while to see the status of the new process.";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                    response, getContextRoot(request) + "/" + webContext + "/bpel/process_list.jsp");

            return true;
        } catch (Exception e) {
            errMsg = "File upload failed :" + e.getMessage();
            log.error(errMsg, e);
            CarbonUIMessage.sendCarbonUIMessage(errMsg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/bpel/upload_bpel.jsp");
        } finally {
            for (String s : extractedFiles) {
                File extractedFile = new File(s);
                if (log.isDebugEnabled()) {
                    log.debug("Cleaning temporarily extracted BPEL artifacts in " + extractedFile.getParent());
                }
                try {
                    FileUtils.cleanDirectory(new File(extractedFile.getParent()));
                } catch (IOException ex) {
                    log.warn("Failed to clean temporary extractedFile.", ex);
                }
            }
        }

        return false;
    }

    public SaveExtractReturn saveAndExtractUploadedFile(FileItem fileItem) throws Exception {
        String serviceUploadDir = getTempUploadDir();
        File servicesDir = new File(serviceUploadDir);
        if (!servicesDir.exists() && !servicesDir.mkdirs()) {
            throw new IOException("Fail to create the directory: " + servicesDir.getAbsolutePath());
        }

        // Writing BPEL archive to file system
        String fileItemName = getFileName(fileItem.getName());
        File uploadedFile = new File(servicesDir, fileItemName);

        if (log.isDebugEnabled()) {
            log.debug("[BPELUI]BPEL Archive Path: " + uploadedFile.getAbsolutePath());
        }

        try {
            fileItem.write(uploadedFile);
        } catch (Exception e) {
            log.error("Error occurred while writing file item to file system.", e);
            throw new Exception("Erorr occurred while writing file item to file system.", e);
        }

        String destinationDir = serviceUploadDir + File.separator +
                fileItemName.substring(0, fileItemName.lastIndexOf('.'));
        if (log.isDebugEnabled()) {
            log.debug("[BPELUI]Bpel package location: " + destinationDir);
        }
        try {
            ArchiveExtractor.extract(uploadedFile, destinationDir);
        } catch (Exception e) {
            log.error("Error extracting archive.", e);
            throw new Exception(e);
        }

        // Handling backward compatibility issues. If user upload BPEL archive which follows the BPS 1.0.1 archive
        // format
        // we need to convert it to new format and upload.
        File deployXml = new File(destinationDir, "deploy.xml");
        if (!deployXml.exists()) {
            String depXmlSrc = fileItemName.substring(0, fileItemName.lastIndexOf('.')) +
                    File.separator + "deploy.xml";
            deployXml = new File(destinationDir, depXmlSrc);
            if (deployXml.exists() &&
                    onlyOneChildDir(destinationDir, fileItemName.substring(0,
                            fileItemName.lastIndexOf('.')))) {
                String tempUploadDir = getTempUploadDir();
                File tempDir = new File(tempUploadDir);
                if (!tempDir.exists() && !tempDir.mkdirs()) {
                    throw new IOException("Fail to create the directory: " + tempDir.getAbsolutePath());
                }

                String filesToZipParent = destinationDir + File.separator +
                        fileItemName.substring(0, fileItemName.lastIndexOf('.'));
                String zipLocation = tempDir.getAbsolutePath() + File.separator + fileItemName;
                try {
                    zip(zipLocation, filesToZipParent);
                } catch (Exception e) {
                    throw new Exception(e);
                }

                return new SaveExtractReturn(zipLocation, filesToZipParent);

            }

            throw new Exception("BPEL Archive format error.Please confirm that the file being uploaded is a " +
                    "valid BPEL archive.");
        }

        return new SaveExtractReturn(uploadedFile.getAbsolutePath(), destinationDir);
    }

    private void zip(String zipFile, String sourceDir) throws Exception {
        File dirObj = new File(sourceDir);
        int len = dirObj.getAbsolutePath().length() + 1;
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            if (log.isDebugEnabled()) {
                log.debug("Creating: " + zipFile);
            }
            addDir(dirObj, out, len);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private String getTempUploadDir() {
        String uuid = generateUUID();
        String tmpDir = "bpelTemp";
        return getWorkingDir() + File.separator + tmpDir + File.separator + uuid + File.separator;
    }

    private boolean onlyOneChildDir(String location, String dirNameToCheck) {
        if (location != null && dirNameToCheck != null) {
            File parentDir = new File(location);
            if (parentDir.isDirectory()) {
                String[] entries = parentDir.list();
                if (entries != null && entries.length == 1 && entries[0].equals(dirNameToCheck)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void validateBPELPackage(String directoryPath) throws Exception {
        DeploymentUnitDir du;
        try {
            du = new DeploymentUnitDir(new File(directoryPath));
        } catch (IllegalArgumentException iae) {
            log.error("BPEL Package Validation Failure.", iae);
            throw new Exception("BPEL Package Validation Failure.", iae);
        }

        //check package for illegal charactors which registry does not support. (~!@#$;%^*()+={}[]|\<>)
        List<File> packageFiles = du.allFiles();
        for (File packageFile : packageFiles) {
            if (!packageFile.getName().matches("[^\\~\\!\\@\\#\\$\\;\\%\\^\\*\\(\\)\\+ " +
                    "/\\=\\{\\}\\[\\]\\\\|\\<\\>\"\\'\\`]+")) {
                log.error("BPEL Package Validation Failure: one or many of the following illegal characters are in " +
                        "the package.\n ~!@#$;%^*()+={}[]| \\<>\"'`");
                throw new Exception("BPEL Package Validation Failure: one or many of the following illegal characters" +
                        " " +
                        "are in the package. ~!@#$;%^*()+={}[]| \\<>\"'`");
            }
        }

        try {
            du.compile();
        } catch (RuntimeException ce) {
            log.error("BPEL Process Compilation Failure.", ce);
            throw new Exception("BPEL Compilation Failure!", ce);
        } catch (Exception e) {
            log.error("BPEL Process Compilation Failure.", e);
            throw new Exception("BPEL Compilation Failure!", e);
        }

        du.scan();
        DeployDocument dd = du.getDeploymentDescriptor();
        for (TDeployment.Process processDD : dd.getDeploy().getProcessList()) {
            QName processType = processDD.getType() != null ? processDD.getType() : processDD.getName();

            DeploymentUnitDir.CBPInfo cbpInfo = du.getCBPInfo(processType);
            if (cbpInfo == null) {
                //removeDeploymentArtifacts(deploymentContext, du);
                String logMessage = "Aborting deployment. Cannot find Process definition for type "
                        + processType + ".";
                log.error(logMessage);
                throw new Exception(logMessage);
            }

            for (TProvide tProvide : processDD.getProvideList()) {
                if (tProvide.getService() == null) {
                    String errMsg = "Service element missing for the provide element in deploy.xml";
                    log.error(errMsg);
                    throw new Exception(errMsg);
                }
            }
            for (TInvoke tInvoke : processDD.getInvokeList()) {
                if (tInvoke.getService() == null) {
                    String errMsg = "Service element missing for the invoke element in deploy.xml";
                    log.error(errMsg);
                    throw new Exception(errMsg);
                }
            }
        }
    }

    static class SaveExtractReturn {
        private String zipFile;
        private String extractedFile;

        public SaveExtractReturn(String zipFile, String extractedFile) {
            this.zipFile = zipFile;
            this.extractedFile = extractedFile;
        }
    }
}


/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.ui.fileupload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.humantask.ui.constants.HumanTaskUIConstants;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

/**
 * HumanTask archive upload executor
 */
public class HumanTaskUploadExecutor extends AbstractFileUploadExecutor {

    private static Log log = LogFactory.getLog(HumanTaskUploadExecutor.class);

    private static final String[] ALLOWED_FILE_EXTENSIONS =
            new String[]{".zip"};


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
        HIUploaderClient uploaderClient = new HIUploaderClient(configurationContext,
                serverURL +
                        HumanTaskUIConstants.SERVICE_NAMES.HUMANTASK_UPLOADER_SERVICE_NAME,
                cookie);

        try {

            for (FileItemData fieldData : fileItemsMap.get("humantaskFileName")) {
                String fileName = getFileName(fieldData.getFileItem().getName());
                //Check filename for \ charactors. This cannot be handled at the lower stages.
                if (fileName.matches("(.*[\\\\].*[/].*|.*[/].*[\\\\].*)")) {
                    log.error("HumanTask Package Validation Failure: one or many of the following illegal characters are in " +
                            "the package.\n ~!@#$;%^*()+={}[]| \\<>");
                    throw new Exception("HumanTask Package Validation Failure: one or many of the following illegal characters " +
                            "are in the package. ~!@#$;%^*()+={}[]| \\<>");
                }
                //Check file extension.
                checkServiceFileExtensionValidity(fileName, ALLOWED_FILE_EXTENSIONS);

                if (fileName.lastIndexOf('\\') != -1) {
                    int indexOfColon = fileName.lastIndexOf('\\') + 1;
                    fileName = fileName.substring(indexOfColon, fileName.length());
                }
                if ("humantaskFileName".equals(fieldData.getFileItem().getFieldName())) {
                    SaveExtractReturn uploadedFiles = saveAndExtractUploadedFile(fieldData.getFileItem());

                    validateHumanTaskPackage(uploadedFiles.extractedFile);
                    DataSource dataSource = new FileDataSource(uploadedFiles.zipFile);
                    uploaderClient.addUploadedFileItem(new DataHandler(dataSource), fileName, "zip");
                }
            }

            uploaderClient.uploadFileItems();

            String msg = "Your HumanTask package been uploaded successfully. Please refresh this page in a" +
                    " while to see the status of the new package.";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                    response, getContextRoot(request) + "/" + webContext +
                    HumanTaskUIConstants.PAGES.PACKAGE_LIST_PAGE);

            return true;
        } catch (Exception e) {
            errMsg = "File upload failed :" + e.getMessage();
            log.error(errMsg, e);
            CarbonUIMessage.sendCarbonUIMessage(errMsg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext +
                    HumanTaskUIConstants.PAGES.UPLOAD_PAGE);
        }

        return false;
    }

    public SaveExtractReturn saveAndExtractUploadedFile(FileItem fileItem) throws Exception {
        String serviceUploadDir = getTempUploadDir();
        File servicesDir = new File(serviceUploadDir);
        if (!servicesDir.exists() && !servicesDir.mkdirs()) {
            throw new IOException("Fail to create the directory: " + servicesDir.getAbsolutePath());
        }

        // Writing HumanTask archive to file system
        String fileItemName = getFileName(fileItem.getName());
        File uploadedFile = new File(servicesDir, fileItemName);

        if (log.isDebugEnabled()) {
            log.debug("[HumanTaskUI]HumanTask Archive Path: " + uploadedFile.getAbsolutePath());
        }

        try {
            fileItem.write(uploadedFile);
        } catch (Exception e) {
            log.error("Error occurred while writing file item to file system.", e);
            throw new Exception("Error occurred while writing file item to file system.", e);
        }

        String destinationDir = serviceUploadDir + fileItemName.substring(0, fileItemName.lastIndexOf('.'));
        if (log.isDebugEnabled()) {
            log.debug("[HumanTaskUI]HumanTask package location: " + destinationDir);
        }
        try {
            ArchiveExtractor.extract(uploadedFile, destinationDir);
        } catch (Exception e) {
            log.error("Error extracting archive.", e);
            throw new Exception(e);
        }

        return new SaveExtractReturn(uploadedFile.getAbsolutePath(), destinationDir);
    }

//    private static void addDir(File dirObj, ZipOutputStream out, int basePathLen) throws Exception {
//        File[] files = dirObj.listFiles();
//        byte[] tmpBuf = new byte[2048];
//
//        for (File file : files) {
//            if (file.isDirectory()) {
//                addDir(file, out, basePathLen);
//                continue;
//            }
//            FileInputStream in = null;
//            try {
//                in = new FileInputStream(file.getAbsolutePath());
//                if (log.isDebugEnabled()) {
//                    log.debug("Adding: " + file.getAbsolutePath());
//                }
//                out.putNextEntry(new ZipEntry(file.getAbsolutePath().substring(basePathLen)));
//                int len;
//                while ((len = in.read(tmpBuf)) > 0) {
//                    out.write(tmpBuf, 0, len);
//                }
//            } finally {
//                if (in != null) {
//                    in.close();
//                }
//            }
//        }
//    }

    private String getTempUploadDir() {
        String uuid = generateUUID();
        String tmpDir = "humantaskTemp";
        return getWorkingDir() + File.separator + tmpDir + File.separator + uuid + File.separator;
    }

    public void validateHumanTaskPackage(String directoryPath) throws Exception {

        //We have to check whether the htconfig.xml file is in the root level.
        //otherwise we cannot accept this as a valid human task package.
        String htConfigFilePathString = directoryPath + File.separator + HumanTaskUIConstants.FILE_NAMES.HT_CONFIG_NAME;

        File htConfigFile = new File(htConfigFilePathString);

        if (!htConfigFile.exists()) {
            throw new Exception("The uploaded task definition zip file does not contain a htconfig.xml" +
                    "file. Please check the package and re-upload.");
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

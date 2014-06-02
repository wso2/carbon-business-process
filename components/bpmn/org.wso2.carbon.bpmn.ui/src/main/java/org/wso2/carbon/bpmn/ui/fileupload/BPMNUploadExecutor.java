/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.ui.fileupload;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.CarbonUtils;
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

public class BPMNUploadExecutor extends AbstractFileUploadExecutor {
    private static final String[] ALLOWED_FILE_EXTENSIONS =
            new String[]{".bar"};
    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {
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
        BPMNUploaderClient uploaderClient = new BPMNUploaderClient(configurationContext,
                serverURL + "BPMNUploaderService", cookie);

        File uploadedTempFile;
        try {
            for (FileItemData fileData : fileItemsMap.get("bpmnFileName")) {
                String fileName = getFileName(fileData.getFileItem().getName());
                //Check filename for \ charactors. This cannot be handled at the lower stages.
                if (fileName.matches("(.*[\\\\].*[/].*|.*[/].*[\\\\].*)")) {
                    log.error("BPMN Package Validation Failure: one or more of the following illegal characters are in " +
                            "the package.\n ~!@#$;%^*()+={}[]| \\<>");
                    throw new Exception("BPMN Package Validation Failure: one or more of the following illegal characters " +
                            "are in the package. ~!@#$;%^*()+={}[]| \\<>");
                }
                //Check file extension.
                checkServiceFileExtensionValidity(fileName, ALLOWED_FILE_EXTENSIONS);

                if (fileName.lastIndexOf('\\') != -1) {
                    int indexOfColon = fileName.lastIndexOf('\\') + 1;
                    fileName = fileName.substring(indexOfColon, fileName.length());
                }
                if (fileData.getFileItem().getFieldName().equals("bpmnFileName")) {
                    uploadedTempFile = new File(CarbonUtils.getTmpDir(), fileName);
                    fileData.getFileItem().write(uploadedTempFile);
                    DataSource dataSource = new FileDataSource(uploadedTempFile);
                    uploaderClient.addUploadedFileItem(new DataHandler(dataSource), fileName, "bar");
                }
            }
            uploaderClient.uploadFileItems();
            String msg = "Your BPMN package has been uploaded successfully. Please refresh this page in a" +
                    " while to see the status of the new process.";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                    response, getContextRoot(request) + "/" + webContext + "/bpmn/process_list_view.jsp");

            return true;
        } catch (Exception e) {
            errMsg = "File upload failed :" + e.getMessage();
            log.error(errMsg, e);
            CarbonUIMessage.sendCarbonUIMessage(errMsg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/bpmn/process_list_view.jsp");
        }

        return false;
    }
}



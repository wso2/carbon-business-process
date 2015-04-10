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

package org.wso2.carbon.humantask.ui.fileupload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.humantask.ui.clients.HumanTaskClientAPIServiceClient;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class AttachmentUploadExecutor extends AbstractFileUploadExecutor {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentUploadExecutor.class);

    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        Map<String, ArrayList<String>> formFieldsMap = getFormFieldsMap();
        String taskID = null;
        String redirect = null;

        try {
            if (formFieldsMap.get("taskID") != null) {
                taskID = formFieldsMap.get("taskID").get(0);
            }
            if (formFieldsMap.get("redirect") != null) {
                redirect = formFieldsMap.get("redirect").get(0);
            }

            ArrayList<FileItemData> fileItemsMap = getFileItemsMap().get("fileToUpload");

            FileItemData fileToBeUpload = fileItemsMap.get(0);

            AttachmentUploadClient attachmentUploadClient = new AttachmentUploadClient(configurationContext,
                    serverURL + "AttachmentMgtService", cookie);
            HumanTaskClientAPIServiceClient taskOperationClient = new HumanTaskClientAPIServiceClient(cookie,
                    serverURL, configurationContext);

            response.setContentType("text/html; charset=utf-8");

            String attachmentID = attachmentUploadClient.addUploadedFileItem(fileToBeUpload);

            String attachmentName = fileToBeUpload.getDataHandler().getName();
            String contentType = fileToBeUpload.getDataHandler().getContentType();
            boolean isAdded = taskOperationClient.addAttachment(taskID, attachmentName, contentType, attachmentID);

            String msg = "Your attachment has been uploaded successfully.";

            if (!isAdded) {
                throw new Exception("Attachment was added successfully with id:" + attachmentID + ". But the task " +
                        "with id: " + taskID + " was not associated with it correctly.");
            } else {
                if (redirect != null) {
                    CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request, response,
                            getContextRoot(request) + "/" + webContext + "/" + redirect);
                } else {
                    CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
                }

                return true;
            }
        } catch (Exception ex) {
            String errMsg = "File upload failed. Reason :" + ex.getLocalizedMessage();
            log.error(errMsg, ex);
            if (redirect != null) {
                CarbonUIMessage.sendCarbonUIMessage(errMsg, CarbonUIMessage.ERROR, request,
                        response, getContextRoot(request) + "/" + webContext + "/" + redirect);
            } else {
                CarbonUIMessage.sendCarbonUIMessage(errMsg, CarbonUIMessage.ERROR, request);
            }
        }
        return false;

    }
}

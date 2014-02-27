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

package org.wso2.carbon.attachment.mgt.ui.fileupload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
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

@Deprecated
/**
 * This impl was moved to human task bundle. So recommended to remove.
 */
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
        String redirect = null;

        try {
            if (formFieldsMap.get("redirect") != null) {
                redirect = formFieldsMap.get("redirect").get(0);
            }

            ArrayList<FileItemData> fileItemsMap = getFileItemsMap().get("fileToUpload");

            FileItemData fileToBeUpload = fileItemsMap.get(0);

            if (fileItemsMap == null || fileItemsMap.isEmpty() || fileItemsMap.size() != 1) {
                String msg = "File uploading failed.";
                log.error(msg);
                out.write("<textarea>" +
                          "(function(){i18n.fileUplodedFailed();})();" +
                          "</textarea>");
                return true;
            }

            AttachmentUploadClient client = new AttachmentUploadClient(configurationContext,
                                                                       serverURL + "AttachmentMgtService", cookie);
            response.setContentType("text/html; charset=utf-8");

            client.addUploadedFileItem(fileToBeUpload);

            String msg = "Your attachment has been uploaded successfully. Please refresh this page in a while to see " +
                         "the status of the new process.";
            if (redirect != null) {
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request, response,
                                                    getContextRoot(request) + "/" + webContext + "/" + redirect);
            } else {
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
            }

            return true;
        } catch (Exception ex) {
            String errMsg = "File upload failed.";
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

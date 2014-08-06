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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.stub.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.stub.AttachmentMgtServiceStub;
import org.wso2.carbon.attachment.mgt.stub.types.TAttachment;
import org.wso2.carbon.utils.FileItemData;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

@Deprecated
/**
 * This impl was moved to human task bundle. So recommended to remove.
 */
public class AttachmentUploadClient {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentUploadClient.class);

    private AttachmentMgtServiceStub stub;

    public AttachmentUploadClient(ConfigurationContext configurationContext, String serverURL, String cookie) throws AxisFault {
        stub = new AttachmentMgtServiceStub(configurationContext, serverURL);
        Options options = stub._getServiceClient().getOptions();
        options.setManageSession(true);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        //Increase the time out when sending large attachments
        options.setTimeOutInMilliSeconds(60 * 1000);
    }

    public void addUploadedFileItem(FileItemData fileItemData) throws AttachmentMgtException, RemoteException {
        DataHandler handler = fileItemData.getDataHandler();
        TAttachment attachment = new TAttachment();
        attachment.setName(handler.getName());
        attachment.setContentType(handler.getContentType());

        attachment.setCreatedBy("DummyUser");  //TODO: Remove this hard-coded value
        attachment.setContent(fileItemData.getDataHandler());
        String attachmentID = stub.add(attachment);
        log.info("Attachment was uploaded with id:" + attachmentID);
    }
}

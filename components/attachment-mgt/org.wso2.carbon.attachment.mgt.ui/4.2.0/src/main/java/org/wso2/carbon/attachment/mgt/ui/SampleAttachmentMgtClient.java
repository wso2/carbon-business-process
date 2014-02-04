/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.attachment.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.wso2.carbon.attachment.mgt.stub.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.stub.AttachmentMgtServiceStub;
import org.wso2.carbon.attachment.mgt.stub.types.TAttachment;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.rmi.RemoteException;

public class SampleAttachmentMgtClient {
    public static void main(String[] args) throws RemoteException, AttachmentMgtException {
        String id = uploadAttachment();
        getAttachmentInfo(id);
        removeAttachment(id);

    }

    private static void removeAttachment (String id) throws RemoteException, AttachmentMgtException {
        AttachmentMgtServiceStub stub = new AttachmentMgtServiceStub();

        Options options = new Options();
        options.setTo(new EndpointReference("http://127.0.0.1:9763/services/AttachmentMgtService/"));
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Boolean.TRUE);
        stub._getServiceClient().setOptions(options);

        System.out.println("Attachment removed status:" + stub.remove(id));
    }

    private static void getAttachmentInfo (String id) throws RemoteException,
                                                           AttachmentMgtException {
        AttachmentMgtServiceStub stub = new AttachmentMgtServiceStub();

        Options options = new Options();
        options.setTo(new EndpointReference("http://127.0.0.1:9763/services/AttachmentMgtService/"));
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Boolean.TRUE);
        stub._getServiceClient().setOptions(options);

        TAttachment attachment = stub.getAttachmentInfo(id);

        System.out.println("Attachment details received. Id: " + attachment.getId());
    }

    private static String uploadAttachment() throws RemoteException, AttachmentMgtException {
        AttachmentMgtServiceStub stub = new AttachmentMgtServiceStub();

        Options options = new Options();
        options.setTo(new EndpointReference("http://127.0.0.1:9763/services/AttachmentMgtService/"));
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Boolean.TRUE);
        stub._getServiceClient().setOptions(options);

        TAttachment att = new TAttachment();
        //att.setId("ContentId");
        //att.setCreatedTime(Calendar.getInstance());
        att.setName("ContentName");
        att.setCreatedBy("DenisAuthor");
        att.setContentType("text/plain");

        //FileDataSource dataSource = new FileDataSource(new File("/home/denis/Desktop/note.txt"));
        FileDataSource dataSource = new FileDataSource(new File("/home/denis/Desktop/fromSoapUI.xml"));
        DataHandler fileDataHandler = new DataHandler(dataSource);
        att.setContent(fileDataHandler);

        String id = stub.add(att);
        System.out.println("Attachment uploaded with id: " + id);

        return id;

    }
}
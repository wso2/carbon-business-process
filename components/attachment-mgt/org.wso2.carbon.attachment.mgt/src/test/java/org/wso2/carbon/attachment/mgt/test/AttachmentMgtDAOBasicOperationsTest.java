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

package org.wso2.carbon.attachment.mgt.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.core.service.AttachmentManagerService;
import org.wso2.carbon.attachment.mgt.server.internal.AttachmentServerHolder;
import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;

public class AttachmentMgtDAOBasicOperationsTest extends TestCase {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtDAOBasicOperationsTest.class);

    protected MockAttachmentServer server;
    protected AttachmentServerHolder attachmentServerHolder;

    /**
     * This maintains an attachment id across multiple test cases written in this Test class. So
     * different test cases can refer to the same attachments.
     */
    private String attachmentID = "1";

    /**
     * This maintains a dummy attachment which is used to verify test-cases
     */
    private TAttachment dummyAttachment;

    private static final String AXIS2_XML_FILE_PATH =  "src" + File.separator + "test" + File
            .separator + "resources";

    @Override
    protected void setUp() throws Exception {
        //Setup the MockAttachment-Server
        try {
            ConfigurationContext context  =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(AXIS2_XML_FILE_PATH, "axis2.xml");
            ConfigurationContextService service = new ConfigurationContextService(context, null);
            AttachmentServerHolder.getInstance().setConfigurationContextService(service);

        }catch (Exception e) {

        }
        server = new MockAttachmentServer();

        attachmentServerHolder = AttachmentServerHolder.getInstance();
        attachmentServerHolder.setAttachmentServer(server);

        server.init();
    }

    /**
     * This method tests the attachment upload functionality
     */
    public void testAttachmentDAOAddTest() {
        AttachmentManagerService service = new AttachmentManagerService();
        try {
            attachmentID = service.add(createAttachment());
            log.info("Attachment added with id : " + attachmentID);
            assertNotNull(attachmentID);
        } catch (AttachmentMgtException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            Assert.fail("Attachment upload failed due to reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * This method tests whether it's possible to get the attachment-info for a given attachment id
     */
    public void testAttachmentDAOGetInfoTest() {
        AttachmentManagerService service = new AttachmentManagerService();

        try {
            TAttachment attachment = service.getAttachmentInfo(attachmentID);

            dummyAttachment = createAttachment();
            assertEquals(dummyAttachment.getName(), attachment.getName());
            assertEquals(dummyAttachment.getCreatedBy(), attachment.getCreatedBy());
            assertEquals(dummyAttachment.getContentType(), attachment.getContentType());

            log.info("Attachment information retrieved for id : " + attachment.getId());

        } catch (AttachmentMgtException e) {
            log.error(e.getLocalizedMessage(), e);
            Assert.fail("Attachment information retrieval failed due to reason: " + e.getLocalizedMessage());
        }
    }

    /**
     * This method tests whether it's possible to get the attachment-info for a given attachment url
     */
    public void testAttachmentDAOGetInfoFromURLTest() {
        AttachmentManagerService service = new AttachmentManagerService();

        try {
            //Request for the attachment using the attachment id
            TAttachment attachmentFromID = service.getAttachmentInfo(attachmentID);

            //request for the attachment using attachment url
            TAttachment attachmentFromURL = service.getAttachmentInfoFromURL(attachmentFromID.getUrl().toString());

            assertEquals(attachmentFromID.getId(), attachmentFromURL.getId());
            log.info("Attachment information retrieved for uri : " + attachmentFromURL.getUrl().toString());
        } catch (AttachmentMgtException e) {
            log.error(e.getLocalizedMessage(), e);
            Assert.fail("Attachment information retrieval failed due to reason: " + e.getLocalizedMessage());
        }
    }

    /**
     * This method tests the attachment removal functionality
     */
    public void testAttachmentDAORemoveTest() {
        AttachmentManagerService service = new AttachmentManagerService();
        try {
            if (service.remove(attachmentID)) {
                log.info("Attachment with id: " + attachmentID + " was successfully removed from data-source.");
                assertTrue("Attachment successfully has been removed from data-source.", true);
            } else {
                 Assert.fail("Attachment with id: " + attachmentID + " couldn't be removed.");
            }
        } catch (AttachmentMgtException e) {
            log.error(e.getLocalizedMessage(), e);
            Assert.fail("Attachment removal failed due to reason: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        server.shutdown();
    }

    /**
     * Creates an attachment stub bean which is consumable by the Back-End server interface
     * {@link org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtServiceSkeletonInterface}
     *
     * @return an attachment stub bean which is consumable by the Back-End server interface
     */
    private TAttachment createAttachment() {
        dummyAttachment = new TAttachment();
        dummyAttachment.setName("DummyName");
        dummyAttachment.setContentType("DummyContentType");
        dummyAttachment.setCreatedBy("DummyUser");

        DataHandler handler = new DataHandler(new FileDataSource(new File("src" + File.separator + "test" + File
                .separator + "resources" + File.separator + "dbConfig.xml")));
        dummyAttachment.setContent(handler);

        return dummyAttachment;
    }
}

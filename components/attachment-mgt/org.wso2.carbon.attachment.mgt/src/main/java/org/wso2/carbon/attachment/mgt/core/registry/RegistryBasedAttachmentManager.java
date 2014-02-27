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
package org.wso2.carbon.attachment.mgt.core.registry;

import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtServiceSkeletonInterface;
import org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment;
import org.wso2.carbon.registry.core.Registry;

/**
  * Registry based Attachment-Mgt service implementation.
  * This class in deprecated state as attachments are stored in a relational data
 */

@Deprecated
public class RegistryBasedAttachmentManager implements AttachmentMgtServiceSkeletonInterface {
    private Registry configRegistry;

    public String add(TAttachment attachment) throws org.wso2.carbon.attachment.mgt.skeleton
                                                             .AttachmentMgtException {

        /*this.configRegistry = AttachmentServiceComponent.getRegistryService().getConfigSystemRegistry();
       //Add resource
       Resource resource = configRegistry.newResource();
       resource.setContent(attachment.getContent());
       configRegistry.put(attachment.getName(), resource);


       //Get getHTTPPermalink
       //return resource.getPermanentPath();
       return "responseReceived";*/

        throw new UnsupportedOperationException("Still not impled.");
    }

    public TAttachment getAttachmentInfo(String s) throws org.wso2.carbon.attachment.mgt.skeleton
                                                                  .AttachmentMgtException {
        throw new UnsupportedOperationException("Still not impled.");
    }

    @Override
    public TAttachment getAttachmentInfoFromURL(String s) throws AttachmentMgtException {
        throw new UnsupportedOperationException("Still not impled.");
    }

    public boolean remove(String s) throws org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtException {
        throw new UnsupportedOperationException("Still not impled.");
    }
}
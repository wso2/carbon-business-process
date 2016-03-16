/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.model.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.io.CachedOutputStream;

import java.io.IOException;

public class AttachmentDataHolder {

    private static final Log log = LogFactory.getLog(AttachmentDataHolder.class);

    private String name = null;
    private String description = null;
    private String type = null;
    private String contentType = null;
    private String scope= null;
    private byte[] attachmentArray = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getAttachmentArray() {
        return attachmentArray;
    }

    public void setAttachmentArray(byte[] attachmentArray) {
        this.attachmentArray = attachmentArray;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void printDebug(){
        boolean debugLogEnabled = log.isDebugEnabled();

        if(log.isDebugEnabled()){
            log.debug("name:" + name);
            log.debug("description:" + description);
            log.debug("type:" + type);
            log.debug("contentType:" + type);
            log.debug("scope:" + scope);

            CachedOutputStream bos = new CachedOutputStream();
            try {
                String fileName = bos.getOut().toString();
                log.debug("fileName:"+fileName);
                bos.close();
            } catch (IOException e) {
                log.error("Exception occured during reading the file name", e);
            }
            log.debug("Stream String:" + new String(attachmentArray));
        }
    }
}

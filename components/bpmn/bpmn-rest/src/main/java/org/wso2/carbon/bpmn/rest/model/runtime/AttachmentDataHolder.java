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

import org.apache.cxf.io.CachedOutputStream;

import java.io.IOException;

public class AttachmentDataHolder {

    private String name;
    private String description;
    private String type;
    private String contentType;
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

    public void printDebug() throws IOException {
        System.out.println("name:" + name);
        System.out.println("description:" + description);
        System.out.println("type:" + type);
        System.out.println("contentType:" + type);
        CachedOutputStream bos = new CachedOutputStream();
        /*IOUtils.copy(inputStream, bos);
        inputStream.close();*/
        bos.close();
        String fileName = bos.getOut().toString();
        System.out.println("Stream String:"+new String(attachmentArray));
    }
}

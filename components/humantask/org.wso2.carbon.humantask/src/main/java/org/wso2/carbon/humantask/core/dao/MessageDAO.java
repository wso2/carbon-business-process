/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * The message representation.
 */
public interface MessageDAO {

    /**
     * The message types.
     */
    public static enum MessageType {
        INPUT,
        OUTPUT,
        FAILURE
    }

    /**
     * @param task : the task dao to set.
     */
    void setTask(TaskDAO task);

    /**
     * @return : The message name.
     */
    QName getName();

    /**
     * @param name : The message name to set.
     */
    void setName(QName name);

    /**
     * @param message : The data of the messsage to set.
     */
    void setData(Element message);

    /**
     * @return : the message data.
     */
    Element getBodyData();

    /**
     * @param header : The message header to set.
     */
    void setHeader(Element header);

    /**
     * @return : The message header element.
     */
    Element getHeader();

    /**
     * @return : The message id.
     */
    Long getId();

    /**
     * @param id : The message id to set.
     */
    void setId(Long id);

    /**
     * @return : The message type.
     */
    MessageType getMessageType();

    /**
     * @param messageType : The message type to set.
     */
    void setMessageType(MessageType messageType);

    /**
     * Adds the provided element to message body parts.
     * @param partName : The part name of the element.
     * @param part : the element to be added.
     */
    void addBodyPart(String partName, Element part);

    /**
     * Returns the matching message body element for the given part name.
     *
     * @param partName : The part name.
     * @return : The matching part element.
     */
    Element getBodyPart(String partName);

    /**
     * @return : The message body parts.
     */
    Map<String, Element> getBodyParts();

    /**
     * Adds the provided message part to the hear parts.
     * @param partName : the message part name.
     * @param part : The message part element to add.
     */
    void addHeaderPart(String partName, Element part);

    /**
     * Returns the header part of the message with the given name.
     * @param partName : part name.
     *
     * @return : The matching header part.
     */
    Element getHeaderPart(String partName);

    /**
     *
     * @return : The header parts of the message.
     */
    Map<String, Element> getHeaderParts();
}

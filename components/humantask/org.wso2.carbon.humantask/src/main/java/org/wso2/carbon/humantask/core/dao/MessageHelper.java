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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.humantask.core.utils.DOMUtils;

import java.util.Map;

/**
 * This class implements the message interface for the humantask input and output messages.
 * <p/>
 * allows to add new message parts and header parts
 */
public class MessageHelper {

    private MessageDAO messageDao;

    /**
     * @param messageDao : The message dao object to be constructed.
     */
    public MessageHelper(MessageDAO messageDao) {
        this.messageDao = messageDao;
    }

    /**
     * Create the task message with the provided task creation context.
     *
     * @param taskCreationContext : The task creation context.
     *
     * @return : The created MessageDAO object.
     */
    public MessageDAO createMessage(TaskCreationContext taskCreationContext) {
        for (Map.Entry<String, Element> part : taskCreationContext.getMessageBodyParts().entrySet()) {
            setPart(part.getKey(), part.getValue());
        }

        for (Map.Entry<String, Element> part : taskCreationContext.getMessageHeaderParts().entrySet()) {
            setHeaderPart(part.getKey(), part.getValue());
        }

        return messageDao;
    }

    /**
     * Return the part with the given name.
     *
     * @param partName : The part name
     * @return : The matching name.
     */
    public Element getPart(String partName) {
        Element message = messageDao.getBodyData();
        NodeList eltList = message.getElementsByTagName(partName);
        if (eltList.getLength() == 0) {
            return null;
        } else {
            return (Element) eltList.item(0);
        }
    }

    /**
     * Sets the provided element to the task message.
     *
     * @param partName : The part name.
     * @param content  : The message content.
     */
    public void setPart(String partName, Element content) {
        Element message = messageDao.getBodyData();
        if (message == null) {
            Document doc = DOMUtils.newDocument();
            message = doc.createElement("message");
            doc.appendChild(message);
        }

        Element partElement = message.getOwnerDocument().createElement(partName);
        partElement.appendChild(partElement.getOwnerDocument().importNode(content, true));
        message.appendChild(partElement);
        messageDao.setData(message);
    }

    /**
     * Return the header part of the message with the provided part name.
     *
     * @param partName : The part name.
     * @return : The matching header element.
     */
    public Element getHeaderPart(String partName) {
        Element header = messageDao.getHeader();
        if (header == null) {
            return null;
        }

        NodeList eltList = header.getElementsByTagName(partName);
        if (eltList.getLength() == 0) {
            return null;
        } else {
            return (Element) eltList.item(0);
        }
    }

    /**
     * Sets the header part of the message dao.
     *
     * @param name    : parent element
     * @param content : The content.
     */
    public void setHeaderPart(String name, Element content) {
        Element header = messageDao.getHeader();
        if (header == null) {
            Document doc = DOMUtils.newDocument();
            header = doc.createElement("header");
            doc.appendChild(header);
        }
        Element part = header.getOwnerDocument().createElement(name);
        header.appendChild(part);
        part.appendChild(header.getOwnerDocument().importNode(content, true));
        messageDao.setHeader(header);
    }

    /**
     * @return : The message DAO.
     */
    public MessageDAO getMessageDao() {
        return messageDao;
    }
}

/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.humantask.core.dao.MessageDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.utils.DOMUtils;

import javax.persistence.*;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain object which holds information related to WSDL messages.
 * This will be used as the domain object for both document literal and RPC literal massages.
 */
@Entity
@Table(name = "HT_MESSAGE")
@NamedQueries(
        @NamedQuery(name = Message.DELETE_MESSAGE_BY_TASK, query ="delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Message as m where m.task = :task")
)
public class Message implements MessageDAO, Serializable {

    public static final String DELETE_MESSAGE_BY_TASK = "DELETE_MESSAGE_BY_TASK";
    @Id
    @Column(name = "MESSAGE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "MESSAGE_NAME", length = 512)
    private String name;

    @Column(name = "MESSAGE_DATA", columnDefinition = "CLOB")
    @Lob
    private String data;

    @Column(name = "MESSAGE_HEADER", columnDefinition = "CLOB")
    @Lob
    private String header;

    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_TYPE")
    private MessageType messageType;

    @ManyToOne(targetEntity = Task.class)
    private Task task;

    public Message() {
    }

    public Message(QName name) {
        //name does not get set when the notifications are created as a result of escalations.
        //name is required for fault messages only.
        if (name != null) {
            this.name = name.toString();
        }
    }

    
    public Element getBodyData() {
        try {
            return data == null ? null : DOMUtils.stringToDOM(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    public void setData(Element data) {
        if (data == null) {
            return;
        }
        this.data = DOMUtils.domToString(data);
    }

    
    public Element getHeader() {
        try {
            return header == null ? null : DOMUtils.stringToDOM(header);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    public void setHeader(Element header) {
        if (header == null) {
            return;
        }
        this.header = DOMUtils.domToString(header);
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    
    public void setTask(TaskDAO task) {
        this.task = (Task) task;
    }

    
    public QName getName() {
        if (name != null) {
            return QName.valueOf(name);
        }

        return null;
    }

    
    public void setName(QName name) {
        this.name = name.toString();
    }

    
    public Long getId() {
        return id;
    }

    
    public void setId(Long id) {
        this.id = id;
    }

    
    public MessageType getMessageType() {
        return messageType;
    }

    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    
    public void addBodyPart(String partName, Element part) {
        Element message = getBodyData();
        if (message == null) {
            Document doc = DOMUtils.newDocument();
            message = doc.createElement("message");
            doc.appendChild(message);
        }

        Element partElement = message.getOwnerDocument().createElement(partName);
        partElement.appendChild(partElement.getOwnerDocument().importNode(part,true));
        message.appendChild(partElement);
        setData(message);
    }

    
    public Element getBodyPart(String partName) {
        Element message = getBodyData();
        NodeList eltList = message.getElementsByTagName(partName);
        if (eltList.getLength() == 0) {
            return null;
        } else {
            return (Element) eltList.item(0);
        }
    }

    
    public Map<String, Element> getBodyParts() {
        Map<String, Element> bodyParts = new HashMap<String, Element>();

        Element message = getBodyData();
        NodeList eltList = message.getChildNodes();
        for (int i = 0; i < eltList.getLength(); i++) {
            Node part = eltList.item(i);
            bodyParts.put(part.getLocalName(), (Element)part.getFirstChild());
        }

        return bodyParts;
    }

    
    public void addHeaderPart(String partName, Element part) {
        Element message = getBodyData();
        if (message == null) {
            Document doc = DOMUtils.newDocument();
            message = doc.createElement("message");
            doc.appendChild(message);
        }

        Element partElement = message.getOwnerDocument().createElement(partName);
        partElement.appendChild(partElement.getOwnerDocument().importNode(part,true));
        message.appendChild(partElement);
        setData(message);
    }

    
    public Element getHeaderPart(String partName) {
        Element message = getBodyData();
        NodeList eltList = message.getElementsByTagName(partName);
        if (eltList.getLength() == 0) {
            return null;
        } else {
            return (Element) eltList.item(0);
        }
    }

    
    public Map<String, Element> getHeaderParts() {
        Map<String, Element> bodyParts = new HashMap<String, Element>();

        Element message = getBodyData();
        NodeList eltList = message.getChildNodes();
        for (int i = 0; i < eltList.getLength(); i++) {
            Node part = eltList.item(i);
            bodyParts.put(part.getLocalName(), (Element)part.getFirstChild());
        }

        return bodyParts;
    }
}

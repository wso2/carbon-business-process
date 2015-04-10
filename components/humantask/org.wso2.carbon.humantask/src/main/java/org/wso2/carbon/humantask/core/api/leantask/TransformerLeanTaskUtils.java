/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.api.leantask;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.ADBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;
import org.wso2.carbon.humantask.LeanTaskDocument;
import org.wso2.carbon.humantask.client.api.leantask.humantask.TLeanTask;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * Data transformer util. Contains methods transforming adb data types
 * to human task engine's objects and vice versa related to leantask.
 */
public class TransformerLeanTaskUtils {
    private static Log log = LogFactory.getLog(TransformerLeanTaskUtils.class);

    Element e;

    public LeanTaskDocument transformLeanTask(TLeanTask adbLeantask) throws ADBException, XmlException {

        org.wso2.carbon.humantask.TLeanTask xmlbLeantask = org.wso2.carbon.humantask.TLeanTask.Factory.newInstance();


        QName ns = new QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/leantask/api/200803", "taskDefinition");


        OMElement taskDef = adbLeantask.getOMElement(ns, OMAbstractFactory.getOMFactory());
        xmlbLeantask = org.wso2.carbon.humantask.TLeanTask.Factory.parse(taskDef.toString());

        //OMElement p = adbLeantask.getPresentationElements().getOMElement(new QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/200803", "presentationElements"), OMAbstractFactory.getOMFactory());
        //xmlbLeantask.setPresentationElements(org.wso2.carbon.humantask.TPresentationElements.Factory.parse(p.toString()));

       /*xmlbLeantask.setPriority(adbLeantask.getPriority());
        xmlbLeantask.setPeopleAssignments((TPeopleAssignments) adbLeantask.getPeopleAssignments());
        xmlbLeantask.setDelegation((TDelegation) adbLeantask.getDelegation());
        xmlbLeantask.setPresentationElements((TPresentationElements) adbLeantask.getPresentationElements());
        xmlbLeantask.setRenderings((TRenderings) adbLeantask.getRenderings());
        xmlbLeantask.setOutcome((TQuery) adbLeantask.getOutcome());
        xmlbLeantask.setSearchBy((TExpression) adbLeantask.getSearchBy());
        xmlbLeantask.setDeadlines((TDeadlines) adbLeantask.getDeadlines());*/

        LeanTaskDocument document = LeanTaskDocument.Factory.newInstance();
        document.setLeanTask(xmlbLeantask);
        try {
            e = DOMUtils.stringToDOM(taskDef.toString());
        } catch (SAXException e) {
            log.error("A SAX exception occurred " + e);
        } catch (IOException e) {
            log.error("An IO exception occurred " + e);
        }

        return document;
    }
}




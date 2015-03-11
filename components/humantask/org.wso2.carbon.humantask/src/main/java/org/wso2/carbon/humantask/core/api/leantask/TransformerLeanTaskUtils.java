package org.wso2.carbon.humantask.core.api.leantask;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.ADBException;
import org.apache.xmlbeans.XmlException;
import org.wso2.carbon.humantask.leantask.client.api.humantask.TLeanTask;
import org.wso2.carbon.humantask.LeanTaskDocument;

import javax.xml.namespace.QName;

/**
 * Created with IntelliJ IDEA.
 * User: suba
 * Date: 2/25/15
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformerLeanTaskUtils {

    public static LeanTaskDocument transformLeanTask(TLeanTask adbLeantask) throws ADBException, XmlException {

        org.wso2.carbon.humantask.TLeanTask xmlbLeantask = org.wso2.carbon.humantask.TLeanTask.Factory.newInstance();


        QName ns = new QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/leantask/api/200803", "taskDefinition");


        OMElement taskDef = adbLeantask.getOMElement(ns, OMAbstractFactory.getOMFactory());
        xmlbLeantask = org.wso2.carbon.humantask.TLeanTask.Factory.parse(taskDef.toString());
        //optional
//
//        xmlbLeantask.setPriority(adbLeantask.getPriority());
//        xmlbLeantask.setPeopleAssignments((TPeopleAssignments) adbLeantask.getPeopleAssignments());
//        xmlbLeantask.setDelegation((TDelegation) adbLeantask.getDelegation());
//        xmlbLeantask.setPresentationElements((TPresentationElements) adbLeantask.getPresentationElements());
//        xmlbLeantask.setRenderings((TRenderings) adbLeantask.getRenderings());
//        xmlbLeantask.setOutcome((TQuery) adbLeantask.getOutcome());
//        xmlbLeantask.setSearchBy((TExpression) adbLeantask.getSearchBy());
//        xmlbLeantask.setDeadlines((TDeadlines) adbLeantask.getDeadlines());

        LeanTaskDocument document = LeanTaskDocument.Factory.newInstance();
        document.setLeanTask(xmlbLeantask);

        return document;
    }
}




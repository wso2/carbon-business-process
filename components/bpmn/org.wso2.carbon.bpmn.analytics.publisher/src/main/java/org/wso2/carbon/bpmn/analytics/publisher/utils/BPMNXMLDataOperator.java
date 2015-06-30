package org.wso2.carbon.bpmn.analytics.publisher.utils;

import org.w3c.dom.*;
import org.wso2.carbon.bpmn.analytics.publisher.BPMNConstants;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by isuruwi on 6/26/15.
 */
public class BPMNXMLDataOperator {
    private final String PROCESS_INSTANCE_FILE_PATH = "/home/isuruwi/bps/carbon-business-process/components/bpmn/org.wso2.carbon.bpmn.analytics.publisher/src/main/java/org/wso2/carbon/bpmn/analytics/publisher/BPMNCompletedProcessInstances";
    private final String TASK_INSTANCE_FILE_PATH = "/home/isuruwi/bps/carbon-business-process/components/bpmn/org.wso2.carbon.bpmn.analytics.publisher/src/main/java/org/wso2/carbon/bpmn/analytics/publisher/BPMNCompletedTaskInstances";
    private static BPMNXMLDataOperator processInstanceDataOperator, taskDataOperator;
    private DocumentBuilderFactory processInstanceDocFactory, taskInstanceDocFactory;
    private DocumentBuilder processInstanceDocBuilder, taskInstanceDocBuilder;
    private Document processInstanceDoc, taskInstanceDoc;

    private BPMNXMLDataOperator(String instanceType) {
        try {
            if (instanceType.equals(BPMNConstants.PROCESS_INSTANCE)) {
                processInstanceDocFactory = DocumentBuilderFactory.newInstance();
                processInstanceDocBuilder = processInstanceDocFactory.newDocumentBuilder();
                processInstanceDoc = processInstanceDocBuilder.parse(PROCESS_INSTANCE_FILE_PATH);
            } else {
                taskInstanceDocFactory = DocumentBuilderFactory.newInstance();
                taskInstanceDocBuilder = taskInstanceDocFactory.newDocumentBuilder();
                taskInstanceDoc = taskInstanceDocBuilder.parse(TASK_INSTANCE_FILE_PATH);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Process Instance or Activity Instance
    public static BPMNXMLDataOperator getInstance(String instanceType) {
        if (instanceType.equals(BPMNConstants.PROCESS_INSTANCE)) {
            if (processInstanceDataOperator == null) {
                processInstanceDataOperator = new BPMNXMLDataOperator(BPMNConstants.PROCESS_INSTANCE);
            }
            return processInstanceDataOperator;
        } else {
            if (taskDataOperator == null) {
                taskDataOperator = new BPMNXMLDataOperator(BPMNConstants.TASK_INSTANCE);
            }
            return taskDataOperator;
        }
    }

    public String timeReadFromXML(String elementName) {
        Node lastInstanceTime = getNode(elementName);
        System.out.println("Get text content: " + lastInstanceTime.getTextContent());
        return lastInstanceTime.getTextContent();
    }

    public void timeWriteToXML(Date date, String elementName) {
        Node lastInstanceTime = getNode(elementName);
        lastInstanceTime.setTextContent(date.toString());
        if (elementName.equals(BPMNConstants.LAST_PROCESS_INSTANCE_END_TIME)) {
            writeContent(processInstanceDoc, PROCESS_INSTANCE_FILE_PATH);
        } else {
            writeContent(taskInstanceDoc, TASK_INSTANCE_FILE_PATH);
        }
    }

    private Node getNode(String nodeName) {
        Node lastInstanceTime = null;
        if(nodeName.equals(BPMNConstants.LAST_PROCESS_INSTANCE_END_TIME)){
            lastInstanceTime = processInstanceDoc.getElementsByTagName(nodeName).item(0);
        }else{
            lastInstanceTime = taskInstanceDoc.getElementsByTagName(nodeName).item(0);
        }
        return lastInstanceTime;
    }

    private void writeContent(Document doc, String filePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            System.out.println("Change date");
            Date date = df.parse("Mon Jun 22 14:47:14 IST 2015");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

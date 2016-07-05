/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNDeployment;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNInstance;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNProcess;
import org.wso2.carbon.bpmn.stub.*;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class WorkflowServiceClient {

    private BPMNInstanceServiceStub instanceServiceStub = null;
    private BPMNDeploymentServiceStub deploymentServiceStub = null;

    private static Log log = LogFactory.getLog(WorkflowServiceClient.class);
    public WorkflowServiceClient(String cookie,
                                 String backendServerURL,
                                 ConfigurationContext configContext) throws AxisFault {

        String deploymentServiceURL = backendServerURL + "BPMNDeploymentService";
        deploymentServiceStub = new BPMNDeploymentServiceStub(configContext, deploymentServiceURL);
        ServiceClient deploymentServiceClient = deploymentServiceStub._getServiceClient();
        Options option2 = deploymentServiceClient.getOptions();
        option2.setManageSession(true);
        option2.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        String instanceServiceURL = backendServerURL + "BPMNInstanceService";
        instanceServiceStub = new BPMNInstanceServiceStub(configContext, instanceServiceURL);
        ServiceClient instanceServiceClient = instanceServiceStub._getServiceClient();
        Options option1 = instanceServiceClient.getOptions();
        option1.setManageSession(true);
        option1.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void startProcess(String processId) {
        try {
            instanceServiceStub.startProcess(processId);
        } catch (RemoteException e) {
            log.error("Error starting process, RemoteException", e);
        } catch (BPMNInstanceServiceBPSFaultException e) {
            log.error("Error starting process, BPMNInstanceServiceBPSFaultException", e);
        }
    }

    public BPMNDeployment[] getDeployments() throws Exception{
        return deploymentServiceStub.getDeployments();
    }

    public BPMNDeployment[] getPaginatedDeploymentsByFilter(String method, String filter, int start, int size) {
        try {
            return  deploymentServiceStub.getPaginatedDeploymentsByFilter(method, filter, start, size);
        } catch (RemoteException e) {
            log.error("Error getting paginated deployments, RemoteException", e);
        }
        return null;
    }

    public int getDeploymentCount() {
        try {
            return deploymentServiceStub.getDeploymentCount();
        } catch (RemoteException e) {
            log.error("Error getting deployments count, RemoteException", e);
        } catch (BPMNDeploymentServiceBPSFaultException e) {
            log.error("Error getting deployments count, BPMNDeploymentServiceBPSFaultException", e);
        }
        return 0;
    }

    public int getInstanceCount() throws Exception  {
        return instanceServiceStub.getInstanceCount();
    }

    public BPMNProcess getProcessById(String processId) throws Exception {
        return  deploymentServiceStub.getProcessById(processId);
    }

    public BPMNProcess[] getProcessList() {
        try {
            return deploymentServiceStub.getDeployedProcesses();
        } catch (RemoteException e) {
            log.error("Error getting process list, RemoteException", e);
        } catch (BPMNDeploymentServiceBPSFaultException e) {
            log.error("Error getting process list, BPMNDeploymentServiceBPSFaultException", e);
        }
        return null;
    }

    public BPMNInstance[] getProcessInstances() throws Exception {
        return instanceServiceStub.getProcessInstances();
    }

    public BPMNInstance[] getPaginatedInstanceByFilter(boolean finished, String instanceId,  String startAfter,
                                                       String startBefore, String processId, boolean isActive,
                                                       String variables, int start, int size) {
        BPMNInstance[] bpmnInstances = null;

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        Date after = null;
        if (startAfter != null && !startAfter.equals("")) {
            try {
                after = formatter.parse(startAfter);
            } catch (ParseException e) {
                log.error("Error converting date filter string, ParseException", e);
            }
        }
        Date before = null;
        if (startBefore != null && !startBefore.equals("")) {
            try {
                before = formatter.parse(startBefore);
            } catch (ParseException e) {
                log.error("Error converting date filter string, ParseException", e);
            }
        }
        try {
            bpmnInstances = instanceServiceStub.getPaginatedInstanceByFilter(finished, instanceId, after, before, processId,
                    isActive, variables, start, size);
        } catch (RemoteException e) {
            log.error("Error getting process list by filter, RemoteException", e);
        }
        return bpmnInstances;
    }

    public BPMNInstance[] getPaginatedHistoryInstances(int start, int size) throws Exception {
        return instanceServiceStub.getPaginatedHistoryInstances(start, size);
    }

    public int getHistoryInstanceCount() throws Exception {
        return instanceServiceStub.getHistoryInstanceCount();
    }

    public BPMNInstance getProcessInstanceById(String instanceId) throws Exception {
        BPMNInstance[] bpmnInstances = instanceServiceStub.getPaginatedInstanceByFilter(true, instanceId, null,
                null, null, true, null, 0, 1);
        if (bpmnInstances == null || bpmnInstances.length <= 0) {
            bpmnInstances = instanceServiceStub.getPaginatedInstanceByFilter(false, instanceId, null, null, null,
                    true, null, 0, 1);
        }
        return bpmnInstances[0];
    }

    public void deleteProcessInstance(String instanceID) throws Exception {
        instanceServiceStub.deleteProcessInstance(instanceID);
    }

    public void deleteCompletedInstance(String instanceID) throws Exception {
        instanceServiceStub.deleteHistoryInstance(instanceID);
    }

    public void deleteAllCompletedInstances() throws Exception {
        instanceServiceStub.deleteAllCompletedInstances();
    }

    public void deleteAllProcessInstances() throws Exception {
        BPMNInstance[] instances = getPaginatedInstanceByFilter(true, null, null, null, null, true, null, 0, 100);
        List<String> instanceIds = new ArrayList<String>();
        for(BPMNInstance instance : instances){
            instanceIds.add(instance.getInstanceId());
        }
        instanceServiceStub.deleteProcessInstanceSet(instanceIds.toArray(new String[instanceIds.size()]));
    }

    public void suspendProcessInstance(String instanceID) throws Exception {
        instanceServiceStub.suspendProcessInstance(instanceID);
    }

    public void activateProcessInstance(String instanceID) throws Exception {
        instanceServiceStub.activateProcessInstance(instanceID);
    }

    public BPMNProcess[] getProcessListByDeploymentID(String deploymentID) {
        try {
            return deploymentServiceStub.getProcessesByDeploymentId(deploymentID);
        } catch (RemoteException e) {
            log.error("Error getting process list for deployment id: " + deploymentID, e);
        }
        return null;
    }

    public String getProcessDiagram(String processId) {
        ByteArrayOutputStream baos = null;
        String dataUri = null;
        try {
        String imageString = deploymentServiceStub.getProcessDiagram(processId);
        BufferedImage bufferedImage = decodeToImage(imageString);
        baos = new ByteArrayOutputStream();
        ImageIO.write( bufferedImage, "png", baos );
        baos.flush();
        dataUri = "data:image/png;base64," +
                DatatypeConverter.printBase64Binary(baos.toByteArray());
        }catch (IOException e) {
            log.error("IO error while writing image " + e);
        } catch (Exception e) {
            //TODO Fix skeleton and rename above exception to correct type
            log.error(" Error while obtaining the process diagram " + e);
        } finally {
            if(baos != null ){
                try {
                    baos.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing io stream " + e);
                }
            }
        }
        return dataUri;
    }

    public String getProcessInstanceDiagram(String instanceId) throws Exception {
        ByteArrayOutputStream baos = null;
        String dataUri;
        try {
            String imageString = instanceServiceStub.getProcessInstanceDiagram(instanceId);
            BufferedImage bufferedImage = decodeToImage(imageString);
            baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            baos.flush();
            dataUri = "data:image/png;base64," +
                    DatatypeConverter.printBase64Binary(baos.toByteArray());
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.error("IO error occurred while closing the stream " + e);
                }
            }
        }
        return dataUri;
    }

    public String getProcessModel(String processId) {
        String tRawXML = null;
        try {
            tRawXML = deploymentServiceStub.getProcessModel(processId);
        } catch (RemoteException e) {
            log.error("BPMN Error getting process model, RemoteException", e);
        } catch (BPMNDeploymentServiceBPSFaultException e) {
            log.error("BPMN Error getting process model, BPMNDeploymentServiceBPSFaultException", e);
        }
        tRawXML = tRawXML.replaceAll("\n|\\r|\\f|\\t", "");
        tRawXML = tRawXML.replaceAll("> +<", "><");
        InputStream xmlIn = new ByteArrayInputStream(tRawXML.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
        tRawXML = xmlPrettyPrinter.xmlFormat().replaceAll("<", "&lt").replaceAll(">", "&gt");
        return tRawXML;
    }

    public  void undeploy(String deploymentName) throws RemoteException, BPMNDeploymentServiceBPSFaultException{
        deploymentServiceStub.undeploy(deploymentName);
    }

    private BufferedImage decodeToImage(String imageString) throws IOException{
        BufferedImage image = null;
        ByteArrayInputStream bis = null;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
        } finally {
            if(bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing the input stream", e);
                }
            }
        }
        return image;
    }
}

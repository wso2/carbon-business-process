/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.coordination.event.listeners;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.b4p.coordination.CoordinationTask;
import org.wso2.carbon.bpel.b4p.coordination.configuration.CoordinationConfiguration;
import org.wso2.carbon.bpel.b4p.coordination.context.ExitProtocolMessage;
import org.wso2.carbon.bpel.b4p.coordination.context.WSConstants;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTProtocolHandlerDAO;
import org.wso2.carbon.bpel.b4p.coordination.dao.TaskProtocolHandler;
import org.wso2.carbon.bpel.b4p.internal.B4PContentHolder;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Thread that used to terminate tasks.
 */
public class TerminationTask implements CoordinationTask {

    private static final Log log = LogFactory.getLog(TerminationTask.class);
    private static final String REG_TASK_COORDINATION = "TaskCoordination";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private String instanceID;
    private int tenantID;

    public TerminationTask(String instanceID) {
        this.instanceID = instanceID;
    }

    public void setTenantID(int tenantID) {
        this.tenantID = tenantID;
    }

    @Override
    public void run() {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(tenantID, true);

        try {
            List<TaskProtocolHandler> taskProtocolHandlers = null;
            try {
                taskProtocolHandlers = getHTProtocolHandlerURLWithTasks(instanceID);
            } catch (Exception e) {
                log.error("Error occurred while retrieving coordination data", e);
            }

            String currentProtocolHandler = "";
            ExitProtocolMessage currentExitMessage = null;
            for (TaskProtocolHandler taskProtocolHandler : taskProtocolHandlers) {

                // Task Protocol Handler and Task ID should be not null
                if (taskProtocolHandler.getProtocolHandlerURL() != null && taskProtocolHandler.getTaskID() != null) {
                    // Note: Result set is pre-sorted using protocolHandler URL.
                    // So we can retrieve a list of task id for that particular protocol handler URL using few
                    // iterations.
                    // Here we are building that taskID list and store them in exitMessage.
                    // If it found new URL, do invoke using old exit message and create a new exit message

                    if (!currentProtocolHandler.equals(taskProtocolHandler.getProtocolHandlerURL())) {
                        // found a new protocol handler URL, so invoking old exit message.
                        if (currentExitMessage != null) {   // To ignore initial condition, we do null check here.
                            invokeProtocolHandler(currentExitMessage);
                        }
                        currentExitMessage = new ExitProtocolMessage(taskProtocolHandler.getProtocolHandlerURL());
                        currentProtocolHandler = taskProtocolHandler.getProtocolHandlerURL();
                    }
                    currentExitMessage.getTaskIDs().add(taskProtocolHandler.getTaskID());
                    if (log.isDebugEnabled()) {
                        log.debug("building exit protocol message for task id:" + taskProtocolHandler.getTaskID());
                    }
                }
            }
            if (currentExitMessage != null) {   // Here we do last invocation.
                invokeProtocolHandler(currentExitMessage);
            }

            //Cleaning coordination data.
            boolean deleted = false;
            try {
                deleted = deleteCoordinationData(instanceID);
            } catch (Exception e) {
                log.error("Error occurred while cleaning coordination data for process instance id " + instanceID, e);
            }

            if (deleted && log.isDebugEnabled()) {
                log.debug("Coordination data are removed from database for process instance id " + instanceID);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private List<HTProtocolHandlerDAO> getHtProtocolHandlerDAOList(final String instanceID) throws Exception {
        List<HTProtocolHandlerDAO> htProtocolHandlerDAOList = ((BPELServerImpl) B4PContentHolder.getInstance()
                .getBpelServer())
                .getScheduler().execTransaction(new Callable<List<HTProtocolHandlerDAO>>() {
                    @Override
                    public List<HTProtocolHandlerDAO> call() throws Exception {
                        HTCoordinationDAOConnection daoConnection = B4PContentHolder.getInstance()
                                .getCoordinationController()
                                .getDaoConnectionFactory().getConnection();
                        return daoConnection.getProtocolHandlers(instanceID);
                    }
                });
        return htProtocolHandlerDAOList;
    }

    private List<TaskProtocolHandler> getHTProtocolHandlerURLWithTasks(final String instanceID) throws Exception {
        List<TaskProtocolHandler> htProtocolURLWithTasks = ((BPELServerImpl) B4PContentHolder.getInstance()
                .getBpelServer())
                .getScheduler().execTransaction(new Callable<List<TaskProtocolHandler>>() {
                    @Override
                    public List<TaskProtocolHandler> call() throws Exception {
                        HTCoordinationDAOConnection daoConnection = B4PContentHolder.getInstance()
                                .getCoordinationController()
                                .getDaoConnectionFactory().getConnection();
                        return daoConnection.getProtocolHandlerURLsWithTasks(instanceID);
                    }
                });
        return htProtocolURLWithTasks;
    }

    private boolean deleteCoordinationData(final String instanceID) throws Exception {
        boolean success = (Boolean) ((BPELServerImpl) B4PContentHolder.getInstance().getBpelServer()).getScheduler()
                .execTransaction(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        HTCoordinationDAOConnection daoConnection = B4PContentHolder.getInstance()
                                .getCoordinationController()
                                .getDaoConnectionFactory().getConnection();
                        return daoConnection.deleteCoordinationData(instanceID);
                    }
                });
        return success;
    }

    private synchronized void invokeProtocolHandler(ExitProtocolMessage message) {
        OMElement payload = message.toOM();
        Options options = new Options();
        options.setTo(new EndpointReference(message.getTaskProtocolHandlerURL()));
        options.setAction(WSConstants.WS_HT_COORDINATION_PROTOCOL_EXIT_ACTION);
        options.setTransportInProtocol(org.apache.axis2.Constants.TRANSPORT_HTTPS);
        ServiceClient serviceClient = null;
        try {
            serviceClient = new ServiceClient();
            serviceClient.setOptions(options);
            //Setting basic auth headers.
            String tenantDomain = MultitenantUtils.getTenantDomainFromUrl(message.getTaskProtocolHandlerURL());
            if (message.getTaskProtocolHandlerURL().equals(tenantDomain)) {
                //this is a Super tenant registration service
                CarbonUtils.setBasicAccessSecurityHeaders(CoordinationConfiguration.getInstance()
                        .getProtocolHandlerAdminUser()
                        , CoordinationConfiguration.getInstance().getProtocolHandlerAdminPassword(), serviceClient);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Sending exit protocol message to tenant domain: " + tenantDomain);
                }
                // Tenant's registration service
                String username = "";
                String password = "";
                try {
                    UserRegistry configSystemRegistry = B4PContentHolder.getInstance().getRegistryService()
                            .getConfigSystemRegistry(tenantID);
                    Resource taskCoordination = configSystemRegistry.get(REG_TASK_COORDINATION);
                    if (taskCoordination != null) {
                        username = taskCoordination.getProperty(USERNAME);
                        password = taskCoordination.getProperty(PASSWORD);
                    } else {
                        log.error("Task coordination is not configured for tenant : " + tenantDomain +
                                ". Dropping Exit Coordination message. Affected Tasks ids : " + message.getTaskIDs());
                        return;
                    }
                } catch (RegistryException e) {
                    log.warn("Error while accessing Registry Service for tenant : " + tenantDomain +
                            ". Dropping Exit Coordination message. Affected Tasks ids : " + message.getTaskIDs(), e);
                    return;
                }
                CarbonUtils.setBasicAccessSecurityHeaders(username, password, serviceClient);
            }
            serviceClient.fireAndForget(payload);
            if (log.isDebugEnabled()) {
                log.debug("Sent exit protocol message to " + message.getTaskProtocolHandlerURL());
            }
        } catch (AxisFault axisFault) {
            log.error("Error occurred while invoking HT Protocol Handler " + message.getTaskProtocolHandlerURL() +
                    ". Affected Tasks ids : " + message.getTaskIDs(), axisFault);
        }
    }

}

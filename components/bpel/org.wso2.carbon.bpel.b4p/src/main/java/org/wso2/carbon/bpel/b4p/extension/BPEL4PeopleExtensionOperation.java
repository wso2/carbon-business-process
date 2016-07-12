/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.b4p.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.extension.AbstractLongRunningExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.b4p.coordination.configuration.CoordinationConfiguration;
import org.wso2.carbon.bpel.b4p.coordination.context.WSConstants;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection;
import org.wso2.carbon.bpel.b4p.internal.B4PContentHolder;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;

import java.util.concurrent.Callable;
import javax.xml.namespace.QName;

/**
 * Class that implements <code>&lt;peopleActivity&gt;</code> related to BPEL4People.
 */
public class BPEL4PeopleExtensionOperation extends AbstractLongRunningExtensionOperation {
    private static Log log = LogFactory.getLog(BPEL4PeopleExtensionOperation.class);

    private static Log messageTraceLog = LogFactory.getLog(BPEL4PeopleConstants.MESSAGE_TRACE);

    private ExtensionContext extensionContext;

    private String cid;

    private String outputVarName;

    private PeopleActivity peopleActivity;

    /**
     * Initial stuff and calling an external service which causes to send back a response in an indefinite time.
     * Correlation values should be set within this method.
     *
     * @param extensionContext ExtensionContext
     * @param cid              cid
     * @param element          ExtensionActivity
     */
    @Override
    public void runAsync(ExtensionContext extensionContext, String cid, Element element)
            throws FaultException {
        this.extensionContext = extensionContext;
        this.cid = cid;
        peopleActivity = new PeopleActivity(extensionContext, element);
        String taskID = peopleActivity.invoke(extensionContext);
        extensionContext.setCorrelationValues(new String[]{taskID});
        extensionContext.setCorrelatorId(peopleActivity.inferCorrelatorId(extensionContext));
        outputVarName = peopleActivity.getOutputVarName();
        if (log.isDebugEnabled()) {
            log.debug("B4P extension invoked by Process " + peopleActivity.getProcessId() + ", pid:" +
                    extensionContext.getInternalInstance().getPid() + ", task:" + taskID);
        }
    }

    /**
     * Called when the response for the above service is received
     *
     * @param mexId MessageExchange id
     */
    @Override
    public void onRequestReceived(String mexId) throws FaultException {
        if (log.isDebugEnabled()) {
            log.debug("People Activity Response received : mexId " + mexId);
        }

        Element notificationMessageEle = extensionContext.getInternalInstance().getMyRequest(mexId);

        if ("".equals(outputVarName) || outputVarName == null) {
            // if output variable is null or empty, these is no way to process the response from the task as we do
            // not have
            // a variable from bpel file to assign the values to. Hence what what we can do is to return a fault and
            // exit.
            log.error("Output variable not specified correctly for the remoteTask activity.Hence the error condition." +
                    "Please verify and correct your BPEL process remoteTask");

            extensionContext.completeWithFault(
                    cid,
                    new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                            BPEL4PeopleConstants.NON_RECOVERABLE_ERROR));
        } else {
            Node part = extensionContext.getPartData(notificationMessageEle,
                    outputVarName);

            if (messageTraceLog.isTraceEnabled()) {
                messageTraceLog.trace("B4P Response Message: " +
                        DOMUtils.domToString(notificationMessageEle));
                messageTraceLog.trace("B4P Response Part: " +
                        DOMUtils.domToString(part));

            }

            if (CoordinationConfiguration.getInstance().isHumantaskCoordinationEnabled() && notificationMessageEle
                    .hasChildNodes()) {
                String taskID = "";
                Element correlationHeader = DOMUtils.findChildByName(notificationMessageEle, new QName
                        (BPEL4PeopleConstants.B4P_NAMESPACE, BPEL4PeopleConstants.B4P_CORRELATION_HEADER), true);
                if (correlationHeader != null) {
                    taskID = correlationHeader.getAttributeNS(BPEL4PeopleConstants.B4P_NAMESPACE,
                            BPEL4PeopleConstants.B4P_CORRELATION_HEADER_ATTRIBUTE);
                    try {
                        deleteCoordinationTaskData(taskID);
                    } catch (Exception e) {
                        log.error("Error occurred while cleaning coordination data for task id " + taskID, e);
                    }
                }

                //Checking for fault
                Element fault = DOMUtils.findChildByName(notificationMessageEle, new QName(WSConstants
                        .WS_HT_COORDINATION_PROTOCOL_FAULT));
                if (fault != null) {
                    if (fault.hasAttribute("headerPart")) {
                        if (log.isDebugEnabled()) {
                            log.debug("Throwing Fault to People Activity Scope since received Fault Protocol Message " +
                                    "for task" + taskID + ".");
                        }
                        extensionContext.completeWithFault(cid,
                                new FaultException(BPEL4PeopleConstants.B4P_FAULT, BPEL4PeopleConstants
                                        .NON_RECOVERABLE_ERROR));
                        ;
                    }
                }
                //Checking for Skip
                Element skipped = DOMUtils.findChildByName(notificationMessageEle, new QName(WSConstants
                        .WS_HT_COORDINATION_PROTOCOL_SKIPPED));
                if (skipped != null) {
                    if (skipped.hasAttribute("headerPart")) {
                        if (log.isDebugEnabled()) {
                            log.debug("Skipping People Activity since received Skipped Protocol Message for task " +
                                    taskID + ".");
                        }
                        //Set extension as complete, since task is skipped. No value write to output variable.
                        extensionContext.complete(cid);
                        return;
                    }
                }
            }
            extensionContext.writeVariable(outputVarName, notificationMessageEle);
            extensionContext.complete(cid);
        }
    }

    private boolean deleteCoordinationTaskData(final String taskID) throws Exception {
        boolean success = (Boolean) ((BPELServerImpl) B4PContentHolder.getInstance().getBpelServer()).getScheduler()
                .execTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                HTCoordinationDAOConnection daoConnection = B4PContentHolder.getInstance().getCoordinationController
                        ().getDaoConnectionFactory().getConnection();
                return daoConnection.deleteTaskData(taskID);
            }
        });
        return success;
    }
}

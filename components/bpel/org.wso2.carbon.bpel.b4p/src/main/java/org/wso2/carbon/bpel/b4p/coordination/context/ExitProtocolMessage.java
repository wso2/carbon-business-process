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

package org.wso2.carbon.bpel.b4p.coordination.context;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Exit Protocol Message of ws-humantask coordination.
 */
public class ExitProtocolMessage {

    private static final OMFactory omFactory = OMAbstractFactory.getOMFactory();

    private List<String> taskIDs;
    private String taskProtocolHandlerURL;

    private OMElement payLoad;

    public ExitProtocolMessage(String taskProtocolHandlerURL) {
        this.taskProtocolHandlerURL = taskProtocolHandlerURL;
        this.taskIDs = new ArrayList<String>();
    }

    public List<String> getTaskIDs() {
        return taskIDs;
    }

    public String getTaskProtocolHandlerURL() {
        return taskProtocolHandlerURL;
    }

    public void setTaskProtocolHandlerURL(String taskProtocolHandlerURL) {
        this.taskProtocolHandlerURL = taskProtocolHandlerURL;
    }

    public OMElement toOM() {
        if (this.payLoad != null) {
            return this.payLoad;
        }
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace htCoordinationNS = factory.createOMNamespace(WSConstants.WS_HT_COORDINATION_PROTOCOL_NAMESPACE,
                WSConstants.WS_HT_COORDINATION_PROTOCOL_DEFAULT_PREFIX);
        payLoad = factory.createOMElement(WSConstants.WS_HT_COORDINATION_PROTOCOL_EXIT, htCoordinationNS);
        for (String taskID : taskIDs) {
            payLoad.addChild(createTaskIDElement(factory, htCoordinationNS, taskID));
        }

        return payLoad;
    }

    private OMElement createTaskIDElement(OMFactory factory, OMNamespace htCoordinationNS, String taskID) {
        OMElement omElement = factory.createOMElement(WSConstants.WS_HT_COORDINATION_PROTOCOL_EXIT_TASK_ID,
                htCoordinationNS);
        omElement.setText(taskID);
        return omElement;
    }
}

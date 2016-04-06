/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.core;


import org.activiti.engine.ProcessEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Content holder for BPMN Server component.
 */
public final class BPMNServerHolder {

    private static final Log log = LogFactory.getLog(BPMNServerHolder.class);

    private static BPMNServerHolder bpmnServerHolder = new BPMNServerHolder();

    private ProcessEngine engine = null;


    private BPMNServerHolder() {
    }

    public static BPMNServerHolder getInstance() {
        return bpmnServerHolder;
    }

    public ProcessEngine getEngine() {
        return engine;
    }

    public void setEngine(ProcessEngine engine) {
        this.engine = engine;
    }

}

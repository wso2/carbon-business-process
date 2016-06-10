/**
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.internal;

import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.rest.BPMNRestService;

/**
 * BPMN Rest Content holder.
 */
public class RestServiceContentHolder {

    private static volatile RestServiceContentHolder instance;
    private BPMNEngineService engineService;
    private BPMNRestService restService;

    private RestServiceContentHolder() {
        engineService = null;
    }

    /**
     * Get RestServiceContentHolder instance.
     *
     * @return RestServiceContentHolder
     */
    public static RestServiceContentHolder getInstance() {
        if (instance == null) {
            instance = new RestServiceContentHolder();
        }
        return instance;
    }

    /**
     * Return BPMNEngineService.
     *
     * @return BPMNEngineService
     */
    public BPMNEngineService getBpmnEngineService() {
        return this.engineService;
    }

    /**
     * Set BPMNEngineService.
     *
     * @param bpmnEngineService Activiti BPMNEngineService.
     */
    public void setBpmnEngineService(BPMNEngineService bpmnEngineService) {
        this.engineService = bpmnEngineService;
    }


    public BPMNRestService getRestService() {
        return restService;
    }

    public void setRestService(BPMNRestService restService) {
        this.restService = restService;
    }
}

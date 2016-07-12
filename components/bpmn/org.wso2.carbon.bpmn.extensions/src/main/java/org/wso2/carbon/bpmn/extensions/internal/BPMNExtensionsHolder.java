/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.extensions.internal;


import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.registry.core.service.RegistryService;

public class BPMNExtensionsHolder {
    private static final BPMNExtensionsHolder holder = new BPMNExtensionsHolder();

    private RegistryService registryService;
    private BPMNEngineService engineService;

    public static BPMNExtensionsHolder getInstance() {
        return holder;
    }

    public BPMNEngineService getEngineService() {
        return engineService;
    }

    public void setEngineService(BPMNEngineService engineService) {
        this.engineService = engineService;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }


}

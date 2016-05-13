
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
package org.wso2.carbon.bpmn.core.internal;


import org.wso2.carbon.security.caas.user.core.service.RealmService;


/**
 * Data holder for identityservice
 */
public class IdentityDataHolder {

    private RealmService carbonRealmService;
    private static volatile IdentityDataHolder instance;

    /**
     * Get IdentityDataHolder instance.
     *
     * @return IdentityDataHolder
     */
    private IdentityDataHolder() {
        carbonRealmService = null;
    }

    public static IdentityDataHolder getInstance() {
        if (instance == null) {
            instance = new IdentityDataHolder();
        }
        return instance;
    }

    public void registerCarbonRealmService(RealmService carbonRealmService) {
        this.carbonRealmService = carbonRealmService;
    }


    public RealmService getCarbonRealmService() {
        return this.carbonRealmService;
    }
}

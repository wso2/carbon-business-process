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

package org.wso2.carbon.humantask.core.internal;

import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.ui.util.UIResourceProvider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Human task ui rendering support :
 */
public class HumanTaskUIResourceProvider implements UIResourceProvider {

    public HumanTaskUIResourceProvider() {
    }

    /**
     * @param name : The ui resource path.
     * @return : The URL of the resource location if this request is a matching human task ui resource.
     *         null otherwise.
     */
    public URL getUIResource(String name) {
        if (name.contains("humantaskui")) {
            String[] nameElements = name.split("/");
            if (nameElements != null && nameElements.length == 5) {
                int tenantId = Integer.parseInt(nameElements[2]);
                String packageName = nameElements[3];
                String fileName = nameElements[4];
                HumanTaskStore taskStore = HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager().getHumanTaskStore(tenantId);
                String jspFilePath = taskStore.getHumanTaskDeploymentRepo().getAbsolutePath() + File.separator + tenantId + File.separator + packageName + File.separator
                        + "web" + File.separator + fileName;
                File jspFile = new File(jspFilePath);

                if (jspFile.exists()) {
                    try {
                        return jspFile.toURI().toURL();
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public Set<String> getUIResourcePaths(String s) {
        return null;
    }
}

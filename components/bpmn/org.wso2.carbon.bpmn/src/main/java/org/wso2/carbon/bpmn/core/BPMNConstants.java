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

import java.io.File;

/**
 * Class to store the static final variables of BPMN
 */

public class BPMNConstants {

    public static final String BPMN_YAML_CONFIGURATION_FILE_NAME = "bpmn.yml";
    public static final String LATEST_CHECKSUM_PROPERTY = "latestChecksum";
    public static final String BPMN_REGISTRY_PATH = "/bpmn/deployments";
    public static final String REGISTRY_PATH_SEPARATOR = "/";
    public static final String BPMN_REPO_NAME = "bpmn";
    public static final String SELECT_META_MODEL_QUERY = "selectDeploymentMetaDataModel";
    public static final String SELECT_META_MODELS_QUERY = "selectDeploymentMetaDataModels";
    public static final String UPDATE_META_MODEL_QUERY = "updateDeploymentMetaDataModel";
    public static final String DELETE_META_MODEL_QUERY = "deleteDeploymentMetaDataModel";

    public static final String BPMN_PACKAGE_EXTENSION = "bar";
    public static final String BPMN_PACKAGE_TEMP_DIRECTORY =
            File.separator + "tmp" + File.separator + "bpmnuploads";
    public static final int SUPER_TENANT_ID = -1234;

    public static final String BPS_BPMN_DEPLOYMENT_METADATA_TABLE = "BPS_BPMN_DEPLOYMENT_METADATA";
    public static final String SPRING_NAMESPACE = "http://www.springframework.org/schema/beans";
    public static final String BEAN = "bean";
    public static final String BEAN_ID = "id";
    public static final String BEAN_CLASS = "class";
    public static final String PROPERTY = "property";
    public static final String NAME = "name";
    public static final String VALUE = "value";

    public static final String NOT_DEFINED_VAR = "NOT_DEFINED";

    public static final int ACTIVITI_INSTANCE_MAX_DELETE_COUNT = 1000;
    public static final String ACTIVITI_INSTANCE_MAX_DELETE_CONFIG = "activitiInstanceDeleteConfig";
    public static final String ACTIVITI_INSTANCE_MAX_DELETE_CONFIG_MAX_COUNT_PROPERTY = "maxCount";

    public static final String REST_CLIENT_CONFIG_ELEMENT = "restClientConfiguration";
    public static final String REST_CLIENT_MAX_TOTAL_CONNECTIONS = "maxTotalConnections";
    public static final String REST_CLIENT_MAX_CONNECTIONS_PER_ROUTE = "maxConnectionsPerRoute";

    public static final String BPMN_DB_NAME = "ACTIVITI_DB";
    public static final String BPMN_DB_CONTEXT_NAME = "ActivitiDB";
}

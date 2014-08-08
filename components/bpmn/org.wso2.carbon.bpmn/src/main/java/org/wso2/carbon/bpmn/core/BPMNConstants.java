package org.wso2.carbon.bpmn.core;

import java.io.File;

public class BPMNConstants {

    public static final String ACTIVITI_CONFIGURATION_FILE_NAME = "activiti.xml";

    public static final String LATEST_CHECKSUM_PROPERTY = "latestChecksum";
    public static final String BPMN_REGISTRY_PATH = "/bpmn/deployments";
    public static final String REGISTRY_PATH_SEPARATOR = "/";
    public static final String BPMN_REPO_NAME = "bpmn";

    public static final String BPMN_PACKAGE_EXTENSION = "bar";
    public static final String BPMN_PACKAGE_TEMP_DIRECTORY = File.separator + "tmp" + File.separator + "bpmnuploads";
    public static final String BPMN_DISTRIBUTED_DEPLOYMENT_ID_SET = "deploymentIDs";
    public static final String BPMN_DISTRIBUTED_PROCESS_DEFINITION_ID_SET = "processDefinitionIDs";
    public static final int SUPER_TENANT_ID = -1234;
}
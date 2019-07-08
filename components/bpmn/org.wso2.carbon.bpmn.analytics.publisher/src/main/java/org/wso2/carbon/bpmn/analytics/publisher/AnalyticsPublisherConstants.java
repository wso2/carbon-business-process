/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.analytics.publisher;

/**
 * AnalyticsPublisherConstants class holds all the constants which are used in the Analytics Publisher
 */
public class AnalyticsPublisherConstants {

    public static final String PROCESS_STREAM_NAME = "BPMN_Process_Instance_Data_Publish";
    public static final String TASK_STREAM_NAME = "BPMN_Task_Instance_Data_Publish";
    public static final String SERVICE_TASK_STREAM_NAME = "BPMN_Service_Task_Instance_Data_Publish";
    public static final String STREAM_VERSION = "1.0.0";
    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    // Data publisher related constants.
    public static final String SERVICE_TASK = "serviceTask";
    public static final String TRUE = "true";

    // Process variables publisher related constants
    public static final String REG_PATH_BPMN_ANALYTICS = "/bpmn/analytics/";
    public static final String ANALYTICS_CONFIG_FILE_NAME = "das_analytics_config_details.json";
    public static final String PROCESS_VARIABLES_JSON_ENTRY_NAME = "processVariables";
    
    // BPS Analytics Configurations related constants
    public static final String BPS_BPMN_ANALYTICS_SERVER_PASSWORD_SECRET_ALIAS = "BPS.Analytics.Server.Password";
    public static final String BPS_ANALYTICS_CONFIGURATION_FILE_NAME = "bps-analytics.xml";

    public static final String BPS_ANALYTIC_NAMESPACE = "http://wso2.org/bps/analytics/config";
    public static final String BPS_ANALYTIC_SERVER_KEY = "AnalyticServer";
    public static final String BPS_ANALYTIC_PASSWORD_KEY = "Password";
}

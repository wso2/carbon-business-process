package org.wso2.carbon.bpmn.analytics.publisher;

/**
 * Created by isuruwi on 6/28/15.
 */
public class DASPublisherConstants {
    public static final String HOST = "localhost";
    public static final String PORT = "7711";
    public static final String USER_NAME = "admin";
    public static final String PASSWORD = "admin";

    public static final String PROCESS_STREAM_NAME = "BPMNProcessInstanceDataPublish";
    public static final String TASK_STREAM_NAME = "BPMNTaskDataPublish";
    public static final String STREAM_ID = "id";
    public static final String STREAM_VERSION = "1.0.0";
    public static final String STREAM_NICK_NAME = "nickName";
    public static final String PROCESS_STREAM_DESCRIPTION = "BPMN process instances data";
    public static final String TASK_STREAM_DESCRIPTION = "BPMN user tasks data";

    public static final  long DELAY = 1000;
}

package org.wso2.carbon.bpmn.core;

public class BPSException extends Exception {

    private static final long serialVersionUID = 1L;

    public BPSException(String msg, Exception e) {
        super(msg, e);
    }

    public BPSException(String msg) {
        super(msg);
    }
}

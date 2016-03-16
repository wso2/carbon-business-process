package org.wso2.carbon.bpmn.core.exception;

public class BPMNAuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public BPMNAuthenticationException(String message) {
        super(message);
    }

    public BPMNAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }


}

package org.wso2.carbon.bpmn.stats.rest.Exception;

/**
 * Created by natasha on 12/2/15.
 */
public class BPMNRestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BPMNRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BPMNRestException(String message) {
        super(message);
    }
}
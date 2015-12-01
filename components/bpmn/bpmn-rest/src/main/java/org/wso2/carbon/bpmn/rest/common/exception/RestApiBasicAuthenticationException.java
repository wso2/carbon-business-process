package org.wso2.carbon.bpmn.rest.common.exception;

public class RestApiBasicAuthenticationException extends BPMNRestException {
    public RestApiBasicAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestApiBasicAuthenticationException(String message) {
        super(message);
    }
}

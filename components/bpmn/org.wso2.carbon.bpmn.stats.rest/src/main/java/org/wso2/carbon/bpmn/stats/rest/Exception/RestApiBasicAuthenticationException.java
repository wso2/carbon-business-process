package org.wso2.carbon.bpmn.stats.rest.Exception;

/**
 * Created by natasha on 12/2/15.
 */
public class RestApiBasicAuthenticationException extends BPMNRestException {
    public RestApiBasicAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestApiBasicAuthenticationException(String message) {
        super(message);
    }
}
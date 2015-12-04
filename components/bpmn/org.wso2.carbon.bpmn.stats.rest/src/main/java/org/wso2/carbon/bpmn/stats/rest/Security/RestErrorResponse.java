package org.wso2.carbon.bpmn.stats.rest.Security;

/**
 * Created by natasha on 12/4/15.
 */
public class RestErrorResponse {

    private String errorMessage;
    private int statusCode;


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}

package org.wso2.carbon.bpmn.rest.common.provider.ExceptionMapper;

import org.wso2.carbon.bpmn.rest.common.RestErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class BPMNErrorHandler implements ExceptionMapper<Error> {
    @Override
    public Response toResponse(Error error) {
        RestErrorResponse restErrorResponse = new RestErrorResponse();
        restErrorResponse.setStatusCode(400);
        restErrorResponse.setErrorMessage(error.getMessage());

        return Response.status(400).type(MediaType.APPLICATION_JSON).entity(restErrorResponse).build();
    }
}

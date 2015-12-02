/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package org.wso2.carbon.bpmn.rest.common.provider.ExceptionMapper;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;
import org.wso2.carbon.bpmn.rest.common.security.RestErrorResponse;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class BPMNExceptionHandler implements ExceptionMapper<Exception> {

    private final Log log = LogFactory.getLog(BPMNExceptionHandler.class);
    @Override
    public Response toResponse(Exception e) {

        RestErrorResponse restErrorResponse = new RestErrorResponse();

        if(e instanceof ActivitiIllegalArgumentException){
            log.error("Exception during service invocation ", e);
            restErrorResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            restErrorResponse.setErrorMessage(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(restErrorResponse).build();
        } else if(e instanceof ActivitiTaskAlreadyClaimedException){
            restErrorResponse.setStatusCode(Response.Status.CONFLICT.getStatusCode());
            restErrorResponse.setErrorMessage(e.getMessage());
            log.error("Exception during Task claiming ", e);
            return Response.status(Response.Status.CONFLICT).entity(restErrorResponse).build();
        } else if(e instanceof ActivitiObjectNotFoundException){
            restErrorResponse.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
            restErrorResponse.setErrorMessage(e.getMessage());
            log.error("Exception due to Activiti Object Not Found ", e);
            return Response.status(Response.Status.NOT_FOUND).entity(restErrorResponse).build();
        } else if(e instanceof BPMNOSGIServiceException){
            restErrorResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            restErrorResponse.setErrorMessage("Service Failed");
            log.error("Exception due to issues on osgi service ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(restErrorResponse).build();
        } else if(e instanceof NotFoundException){
            restErrorResponse.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
            restErrorResponse.setErrorMessage("No Resource Found");
            log.error("unsupported operation not found ", e);
            return Response.status(Response.Status.NOT_FOUND).entity(restErrorResponse).build();
        } else if(e instanceof ClientErrorException){
            restErrorResponse.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
            restErrorResponse.setErrorMessage("Failed to hit the request target due to unsupported operation");
            log.error("unsupported operation not found ", e);
            return Response.status(Response.Status.NOT_FOUND).entity(restErrorResponse).build();
        } else if(e instanceof WebApplicationException){
            restErrorResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            restErrorResponse.setErrorMessage("Web application exception thrown");
            log.error("Web application exception thrown ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(restErrorResponse).build();
        }
        else {
            restErrorResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            restErrorResponse.setErrorMessage(e.getMessage());
            log.error("Unknown Exception occurred ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(restErrorResponse).build();
        }
    }
}

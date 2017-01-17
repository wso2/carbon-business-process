/*
 * Copyright (c) 2016. WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.wso2.carbon.bpmn.rest.service.runtime;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.runtime.SignalEventReceivedRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Notifies the engine that a signal event has been received, not explicitly related to a specific execution.
 */
@Path("/signals")
public class SignalService {

    @Context
    protected UriInfo uriInfo;

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response signalEventReceived(SignalEventReceivedRequest signalRequest) {
        RestResponseFactory restResponseFactory = new RestResponseFactory();
        RuntimeService runtimeService = BPMNOSGIService.getRuntimeService();

        if (signalRequest.getSignalName() == null) {
            throw new ActivitiIllegalArgumentException("signalName is required");
        }

        Map<String, Object> signalVariables = null;
        if (signalRequest.getVariables() != null) {
            signalVariables = new HashMap<String, Object>();
            for (RestVariable variable : signalRequest.getVariables()) {
                if (variable.getName() == null) {
                    throw new ActivitiIllegalArgumentException("Variable name is required.");
                }
                signalVariables.put(variable.getName(), restResponseFactory.getVariableValue(variable));
            }
        }

        if (signalRequest.isAsync()) {
            if (signalVariables != null) {
                throw new ActivitiIllegalArgumentException("Async signals cannot take variables as payload");
            }
            if (signalRequest.isCustomTenantSet()) {
                runtimeService.signalEventReceivedAsyncWithTenantId(signalRequest.getSignalName(),
                                                                    signalRequest.getTenantId());
            } else {
                runtimeService.signalEventReceivedAsync(signalRequest.getSignalName());
            }
            return Response.ok().status(Response.Status.ACCEPTED).build();
        } else {
            if (signalRequest.isCustomTenantSet()) {
                runtimeService.signalEventReceivedWithTenantId(signalRequest.getSignalName(), signalVariables,
                                                               signalRequest.getTenantId());
            } else {
                runtimeService.signalEventReceived(signalRequest.getSignalName(), signalVariables);
            }
            return Response.ok().status(Response.Status.NO_CONTENT).build();
        }
    }
}

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

package org.wso2.carbon.bpmn.rest.service.form;


import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.FormService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.runtime.ProcessInstance;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.form.FormDataResponse;
import org.wso2.carbon.bpmn.rest.model.form.RestFormProperty;
import org.wso2.carbon.bpmn.rest.model.form.SubmitFormRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceResponse;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("/form-data")
public class FormDataService {

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getFormData() {

        String taskId = uriInfo.getQueryParameters().getFirst("taskId");
        String processDefinitionId = uriInfo.getQueryParameters().getFirst("processDefinitionId");

        if (taskId == null && processDefinitionId == null) {
            throw new ActivitiIllegalArgumentException("The taskId or processDefinitionId parameter has to be provided");
        }

        if (taskId != null && processDefinitionId != null) {
            throw new ActivitiIllegalArgumentException("Not both a taskId and a processDefinitionId parameter can be provided");
        }

        FormData formData = null;
        String id = null;
        FormService formService = BPMNOSGIService.getFormService();
        if (taskId != null) {
            formData = formService.getTaskFormData(taskId);
            id = taskId;
        } else {
            formData = formService.getStartFormData(processDefinitionId);
            id = processDefinitionId;
        }

        if (formData == null) {
            throw new ActivitiObjectNotFoundException("Could not find a form data with id '" + id + "'.", FormData.class);
        }

        return Response.ok().entity(new RestResponseFactory().createFormDataResponse(formData, uriInfo.getBaseUri()
                .toString())).build();
    }

    @POST
    @Path("/form-data")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response submitForm(SubmitFormRequest submitRequest) {

        if (submitRequest == null) {
            throw new ActivitiException("A request body was expected when executing the form submit.");
        }

        if (submitRequest.getTaskId() == null && submitRequest.getProcessDefinitionId() == null) {
            throw new ActivitiIllegalArgumentException("The taskId or processDefinitionId property has to be provided");
        }

        Map<String, String> propertyMap = new HashMap<String, String>();
        if (submitRequest.getProperties() != null) {
            for (RestFormProperty formProperty : submitRequest.getProperties()) {
                propertyMap.put(formProperty.getId(), formProperty.getValue());
            }
        }
        FormService formService = BPMNOSGIService.getFormService();
        Response.ResponseBuilder response = Response.ok();
        if (submitRequest.getTaskId() != null) {
            formService.submitTaskFormData(submitRequest.getTaskId(), propertyMap);
            response.status(Response.Status.NO_CONTENT);
            return response.build();

        } else {
            ProcessInstance processInstance = null;
            if (submitRequest.getBusinessKey() != null) {
                processInstance = formService.submitStartFormData(submitRequest.getProcessDefinitionId(),
                        submitRequest.getBusinessKey(), propertyMap);
            } else {
                processInstance = formService.submitStartFormData(submitRequest.getProcessDefinitionId(), propertyMap);
            }
            ProcessInstanceResponse processInstanceResponse = new RestResponseFactory().createProcessInstanceResponse
                    (processInstance, uriInfo.getBaseUri().toString());
            return response.entity(processInstanceResponse).build();
        }
    }
}

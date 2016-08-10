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
package org.wso2.carbon.bpmn.rest.service.history;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricVariableInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.service.base.BaseHistoricVariableInstanceService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

@Path("/historic-variable-instances")
public class HistoricVariableInstanceService extends BaseHistoricVariableInstanceService{

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getHistoricActivityInstances() {
        HistoricVariableInstanceQueryRequest query = new HistoricVariableInstanceQueryRequest();

        // Populate query based on request
        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }
        if (allRequestParams.get("excludeTaskVariables") != null) {
            query.setExcludeTaskVariables(Boolean.valueOf(allRequestParams.get("excludeTaskVariables")));
        }

        if (allRequestParams.get("taskId") != null) {
            query.setTaskId(allRequestParams.get("taskId"));
        }

        if(allRequestParams.get("executionId") != null)
        {
            query.setExecutionId(allRequestParams.get("executionId"));
        }

        if (allRequestParams.get("processInstanceId") != null) {
            query.setProcessInstanceId(allRequestParams.get("processInstanceId"));
        }

        if (allRequestParams.get("variableName") != null) {
            query.setVariableName(allRequestParams.get("variableName"));
        }

        if (allRequestParams.get("variableNameLike") != null) {
            query.setVariableNameLike(allRequestParams.get("variableNameLike"));
        }

        DataResponse dataResponse = getQueryResponse(query, allRequestParams);
        return Response.ok().entity(dataResponse).build();
    }

    @GET
    @Path("/{varInstanceId}/data")
    public  byte[] getVariableData(@PathParam("varInstanceId") String varInstanceId){

        Response.ResponseBuilder response = Response.ok();
        try {
            byte[] result = null;
            RestVariable variable = getVariableFromRequest(true, varInstanceId);
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                response.type("application/octet-stream");

            } else if(RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
                outputStream.writeObject(variable.getValue());
                outputStream.close();
                result = buffer.toByteArray();
                response.type("application/x-java-serialized-object");

            } else {
                throw new ActivitiObjectNotFoundException("The variable does not have a binary data stream.", null);
            }
            return result;

        } catch(IOException ioe) {
            // Re-throw IOException
            throw new ActivitiException("Unexpected exception getting variable data", ioe);
        }
    }

    protected RestVariable getVariableFromRequest(boolean includeBinary, String varInstanceId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricVariableInstance varObject = historyService.createHistoricVariableInstanceQuery().id(varInstanceId).singleResult();

        if (varObject == null) {
            throw new ActivitiObjectNotFoundException("Historic variable instance '" + varInstanceId + "' couldn't be found.", VariableInstanceEntity.class);
        } else {
            return new RestResponseFactory().createRestVariable(varObject.getVariableName(), varObject.getValue(), null, varInstanceId,
                                                          RestResponseFactory.VARIABLE_HISTORY_VARINSTANCE, includeBinary,uriInfo.getBaseUri().toString());
        }
    }


}

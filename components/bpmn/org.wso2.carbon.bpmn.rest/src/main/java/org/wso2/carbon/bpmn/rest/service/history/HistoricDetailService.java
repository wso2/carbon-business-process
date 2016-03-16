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
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.history.HistoricDetailQueryRequest;
import org.wso2.carbon.bpmn.rest.service.base.BaseHistoricDetailService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

@Path("/historic-detail")
public class HistoricDetailService extends BaseHistoricDetailService {



    @Context
    UriInfo uriInfo;

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getHistoricDetailInfo() {

        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }

        // Populate query based on request
        HistoricDetailQueryRequest queryRequest = new HistoricDetailQueryRequest();

        if (allRequestParams.get("id") != null) {
            queryRequest.setId(allRequestParams.get("id"));
        }

        if (allRequestParams.get("processInstanceId") != null) {
            queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
        }

        if (allRequestParams.get("executionId") != null) {
            queryRequest.setExecutionId(allRequestParams.get("executionId"));
        }

        if (allRequestParams.get("activityInstanceId") != null) {
            queryRequest.setActivityInstanceId(allRequestParams.get("activityInstanceId"));
        }

        if (allRequestParams.get("taskId") != null) {
            queryRequest.setTaskId(allRequestParams.get("taskId"));
        }

        if (allRequestParams.get("selectOnlyFormProperties") != null) {
            queryRequest.setSelectOnlyFormProperties(Boolean.valueOf(allRequestParams.get("selectOnlyFormProperties")));
        }

        if (allRequestParams.get("selectOnlyVariableUpdates") != null) {
            queryRequest.setSelectOnlyVariableUpdates(Boolean.valueOf(allRequestParams.get("selectOnlyVariableUpdates")));
        }
        DataResponse dataResponse = getQueryResponse(queryRequest, allRequestParams, uriInfo);
        return Response.ok().entity(dataResponse).build();
    }

    @GET
    @Path("/{detailId}/data")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getVariableData(@PathParam("detailId") String detailId) {
        try {
            byte[] result = null;
            RestVariable variable = getVariableFromRequest(true, detailId);
            Response.ResponseBuilder response = Response.ok();
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
            return response.entity(result).build();

        } catch(IOException ioe) {
            // Re-throw IOException
            throw new ActivitiException("Unexpected exception getting variable data", ioe);
        }
    }

    public RestVariable getVariableFromRequest(boolean includeBinary, String detailId) {
        Object value = null;
        HistoricVariableUpdate variableUpdate = null;
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricDetail detailObject = historyService.createHistoricDetailQuery().id(detailId).singleResult();
        if (detailObject instanceof HistoricVariableUpdate) {
            variableUpdate = (HistoricVariableUpdate) detailObject;
            value = variableUpdate.getValue();
        }

        if (value == null) {
            throw new ActivitiObjectNotFoundException("Historic detail '" + detailId + "' doesn't have a variable value.", VariableInstanceEntity.class);
        } else {
            return new RestResponseFactory().createRestVariable(variableUpdate.getVariableName(), value, null, detailId,
                    RestResponseFactory.VARIABLE_HISTORY_DETAIL, includeBinary, uriInfo.getBaseUri().toString());
        }
    }
}

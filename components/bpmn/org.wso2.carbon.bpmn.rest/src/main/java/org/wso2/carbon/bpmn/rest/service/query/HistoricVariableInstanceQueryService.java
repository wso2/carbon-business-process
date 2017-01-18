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
package org.wso2.carbon.bpmn.rest.service.query;

import org.activiti.engine.impl.HistoricVariableInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricVariableInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.service.base.BaseHistoricVariableInstanceService;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/historic-variable-instances")
public class HistoricVariableInstanceQueryService extends BaseHistoricVariableInstanceService {

    protected static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();
    protected static final List<String> allPropertiesList  = new ArrayList<>();



    static {
        allowedSortProperties.put("processInstanceId", HistoricVariableInstanceQueryProperty.PROCESS_INSTANCE_ID);
        allowedSortProperties.put("variableName", HistoricVariableInstanceQueryProperty.VARIABLE_NAME);
    }

    static {
        allPropertiesList.add("processInstanceId");
        allPropertiesList.add("taskId");
        allPropertiesList.add("excludeTaskVariables");
        allPropertiesList.add("variableName");
        allPropertiesList.add("variableNameLike");
        allPropertiesList.add("start");
        allPropertiesList.add("size");
        allPropertiesList.add("order");
        allPropertiesList.add("sort");
    }
    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response queryVariableInstances(HistoricVariableInstanceQueryRequest queryRequest) {

        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }
        DataResponse dataResponse = getQueryResponse(queryRequest, allRequestParams);
        return Response.ok().entity(dataResponse).build();
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return Response.ok("").build();
    }
}

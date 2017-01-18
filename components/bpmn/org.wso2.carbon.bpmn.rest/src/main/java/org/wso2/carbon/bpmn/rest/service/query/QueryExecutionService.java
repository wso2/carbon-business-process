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

import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionQueryRequest;
import org.wso2.carbon.bpmn.rest.service.base.BaseExecutionService;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("/executions")
public class QueryExecutionService extends BaseExecutionService {

    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response queryProcessInstances(ExecutionQueryRequest queryRequest, @Context UriInfo uriInfo) {
        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }
        DataResponse dataResponse = getQueryResponse(queryRequest, allRequestParams, uriInfo);
        return Response.ok().entity(dataResponse).build();
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return Response.ok("").build();
    }

}

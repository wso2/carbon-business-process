package org.wso2.carbon.bpmn.rest.service.query;

import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionQueryRequest;
import org.wso2.carbon.bpmn.rest.service.base.BaseExecutionService;

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

}

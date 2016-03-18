package org.wso2.carbon.bpmn.rest.service.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONString;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by samithac on 17/3/16.
 */


@Path("/publish-process-variables")
public class PublishProcessVariablesService {
    private static final Log log = LogFactory.getLog(PublishProcessVariablesService.class);

    @PUT
    @Path("/{processId}")///{processVariables}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public void publishProcessVariables(@PathParam("processId") String processId,@QueryParam("processVariables") String processVariablesJson){//},@PathParam("processVariables") JSONString processVariablesJson){
        log.info("Process ID:"+processId+"\nvariables:"+processVariablesJson);//+processVariablesJson);
        log.info("sssssssssss");
        //https://192.168.43.51:9443/bpmn/analytics/publish-process-variables/ffff?processVariables=aaaaaaabbbbbbbb
        //https://192.168.43.51:9443/bpmn/analytics/publish-process-variables/ffff?processVariables={"dddd":"FFF"}

    }
}

//////////
/*
@Path("/historic-activity-instances")
public class HistoricActivitiInstanceService extends BaseHistoricActivitiInstanceService {

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getHistoricActivityInstances(@Context UriInfo uriInfo) {

        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }

        HistoricActivityInstanceQueryRequest query = getHistoricActivityInstanceQueryRequest(uriInfo, allRequestParams);

        DataResponse dataResponse = getQueryResponse(query, allRequestParams, uriInfo);
        return Response.ok().entity(dataResponse).build();
    }


}
*/

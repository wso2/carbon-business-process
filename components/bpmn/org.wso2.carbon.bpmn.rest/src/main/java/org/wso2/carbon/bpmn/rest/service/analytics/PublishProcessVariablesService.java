package org.wso2.carbon.bpmn.rest.service.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.wso2.carbon.bpmn.analytics.publisher.utils.AnalyticsPublishServiceUtils;
/**
 * Created by samithac on 17/3/16.
 */


@Path("/publish-process-variables")
public class PublishProcessVariablesService {
    private static final Log log = LogFactory.getLog(PublishProcessVariablesService.class);

    @POST
    @Path("/{processId}")///{processVariables}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response publishProcessVariables(@PathParam("processId") String processId, String processVariablesJson){//},@PathParam("processVariables") JSONString processVariablesJson){
        log.info("Process ID:"+processId+"\nvariables:"+processVariablesJson);//+processVariablesJson);
        log.info("sssssssssss");
        //https://192.168.43.51:9443/bpmn/analytics/publish-process-variables/ffff?processVariables=aaaaaaabbbbbbbb
        //https://192.168.43.51:9443/bpmn/analytics/publish-process-variables/ffff?processVariables={"dddd":"FFF"}
        AnalyticsPublishServiceUtils.saveDASconfiguredProcessVariablesinRegistry(processId,processVariablesJson);
        return Response.ok().entity("Success").build();
    }

    public void saveProcVariablePublishDataInGovReg(){

    }
}



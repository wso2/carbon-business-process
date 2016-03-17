package org.wso2.carbon.bpmn.rest.service.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONString;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by samithac on 17/3/16.
 */

@Path("/publish-process-variables")
public class PublishProcessVariablesService {
    private static final Log log = LogFactory.getLog(PublishProcessVariablesService.class);

    @Path("/{processId}/{processVariables}")
    Response publishProcessVariables(@PathParam("processId") String processId,@PathParam("processVariables") JSONString processVariablesJson){

        log.info("Process ID:"+processId+"\nvariables:"+processVariablesJson);

        return null;
    }
}

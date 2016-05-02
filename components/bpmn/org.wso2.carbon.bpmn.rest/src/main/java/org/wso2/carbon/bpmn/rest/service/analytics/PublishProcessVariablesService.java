/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.rest.service.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.wso2.carbon.bpmn.analytics.publisher.utils.AnalyticsPublishServiceUtils;

/**
 * Enables the proces variable publishing from BPS to DAS
 */
@Path("/publish-process-variables")
public class PublishProcessVariablesService {
    private static final Log log = LogFactory.getLog(PublishProcessVariablesService.class);

    /**
     * Enables the proces variable publishing from BPS to DAS by, receiving and saving analytics configuration details
     * in config registry
     *
     * @param processId
     * @param processVariablesJson
     * @return
     */
    @POST
    @Path("/{processId}")///{processVariables}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response publishProcessVariables(@PathParam("processId") String processId, String processVariablesJson){//},@PathParam("processVariables") JSONString processVariablesJson){
        if (log.isDebugEnabled()) {
            log.debug("Recieved analytics configuration details to from PC to BPS for Process ID:"+processId+"\nRecieved Date:"+processVariablesJson);
        }
        //https://192.168.43.51:9443/bpmn/analytics/publish-process-variables/ffff?processVariables=aaaaaaabbbbbbbb
        //https://192.168.43.51:9443/bpmn/analytics/publish-process-variables/ffff?processVariables={"dddd":"FFF"}
        AnalyticsPublishServiceUtils.saveDASconfigInfoInConfigRegistry(processId,processVariablesJson);
        return Response.ok().entity("Success").build();
    }
}



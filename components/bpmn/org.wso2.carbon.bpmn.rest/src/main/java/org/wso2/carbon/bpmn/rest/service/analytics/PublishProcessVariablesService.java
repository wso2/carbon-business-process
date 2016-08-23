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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisherConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.io.IOException;

/**
 * Enables the process variable publishing from BPS to DAS
 */
@Path("/publish-process-variables")
public class PublishProcessVariablesService {
    private static final Log log = LogFactory.getLog(PublishProcessVariablesService.class);

    /**
     * Enables the process variable publishing from BPS to DAS by, receiving (from WSO2 PC) and saving analytics
     * configuration details in config registry.
     *
     * @param processId
     * @param dasConfigDetailsJson
     * @return
     */
    @POST
    @Path("/{processId}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response publishProcessVariables(@PathParam("processId") String processId, String dasConfigDetailsJson)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Recieved analytics configuration details to from PC to BPS for Process ID:" + processId
                    + "\nRecieved Date:" + dasConfigDetailsJson);
        }

        try {
            saveDASconfigInfo(dasConfigDetailsJson);
        } catch (RegistryException e) {
            String errMsg =
                    "Error in saving DAS Analytics Configuratios in BPS Config-Registry for process :" + processId
                            + "\n Details tried to save:" + dasConfigDetailsJson;
            log.error(errMsg, e);
            return Response.status(500).entity(errMsg + " : " + e.getMessage()).build();
        }
        return Response.ok().build();
    }

    /**
     * Removing published process variable publishing from BPS to DAS
     *
     * @param processId process name_version
     * @param processDefinitionIdJSONString JSON string contains process definition id
     * @return
     */
    @DELETE
    @Path("/{processId}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response deleteProcessVariables(@PathParam("processId") String processId, String processDefinitionIdJSONString)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Recieved analytics configuration delete request from PC to BPS for Process ID:" + processId);
        }

        try {
            deleteDASConfigInfo(processDefinitionIdJSONString);
        } catch (RegistryException e) {
            String errMsg =
                    "Error in deleting DAS Analytics Configuratios from BPS Config-Registry for process :" + processId;
            log.error(errMsg, e);
            return Response.status(500).entity(errMsg + " : " + e.getMessage()).build();
        }
        return Response.ok().build();
    }

    /**
     * Save DAS configuration details (received from PC), in config registry
     *
     * @param dasConfigDetailsJSONString
     * @throws RegistryException
     */
    private void saveDASconfigInfo(String dasConfigDetailsJSONString) throws RegistryException, IOException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Registry configRegistry = carbonContext.getRegistry(RegistryType.SYSTEM_CONFIGURATION);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dasConfigDetailsJOb = objectMapper.readTree(dasConfigDetailsJSONString);
        String processDefinitionId = dasConfigDetailsJOb.get(AnalyticsPublisherConstants.PROCESS_DEFINITION_ID)
                .textValue();

        //create a new resource (text file) to keep process variables
        Resource procVariableJsonResource = configRegistry.newResource();
        procVariableJsonResource.setContent(dasConfigDetailsJSONString);
        procVariableJsonResource.setMediaType(MediaType.APPLICATION_JSON);
        configRegistry.put(AnalyticsPublisherConstants.REG_PATH_BPMN_ANALYTICS + processDefinitionId + "/"
                + AnalyticsPublisherConstants.ANALYTICS_CONFIG_FILE_NAME, procVariableJsonResource);
    }

    /**
     * Delete DAS configuration details (received from PC), from config registry
     *
     * @param processDefinitionIdJSONString JSON string contains process definition id
     * @throws RegistryException
     */
    private void deleteDASConfigInfo(String processDefinitionIdJSONString) throws RegistryException, IOException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Registry configRegistry = carbonContext.getRegistry(RegistryType.SYSTEM_CONFIGURATION);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dasConfigDetailsJOb = objectMapper.readTree(processDefinitionIdJSONString);
        String processDefinitionId = dasConfigDetailsJOb.get(AnalyticsPublisherConstants.PROCESS_DEFINITION_ID)
                .textValue();
        configRegistry.delete(AnalyticsPublisherConstants.REG_PATH_BPMN_ANALYTICS + processDefinitionId + "/"
                + AnalyticsPublisherConstants.ANALYTICS_CONFIG_FILE_NAME);
    }
}
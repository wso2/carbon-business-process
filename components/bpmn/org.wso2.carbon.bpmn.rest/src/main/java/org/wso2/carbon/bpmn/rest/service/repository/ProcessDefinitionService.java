/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.service.repository;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.RestUrls;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ProcessDefinitionResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ProcessDefinitionsPaginateList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/process-definitions")
public class ProcessDefinitionService {

    @Context
    UriInfo uriInfo;

    private static final Map<String, QueryProperty> properties = new HashMap<>();
    private static final List<String> allPropertiesList = new ArrayList<>();

    static {
        properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
        properties.put("key", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
        properties.put("category", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
        properties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
        properties.put("version", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
        properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
        properties.put("tenantId", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_TENANT_ID);
    }

    static {
        allPropertiesList.add("version");
        allPropertiesList.add("name");
        allPropertiesList.add("nameLike");
        allPropertiesList.add("key");
        allPropertiesList.add("keyLike");
        allPropertiesList.add("resourceName");
        allPropertiesList.add("resourceNameLike");
        allPropertiesList.add("category");
        allPropertiesList.add("categoryLike");
        allPropertiesList.add("categoryNotEquals");
        allPropertiesList.add("deploymentId");
        allPropertiesList.add("startableByUser");
        allPropertiesList.add("latest");
        allPropertiesList.add("suspended");
        allPropertiesList.add("tenantId");
        allPropertiesList.add("start");
        allPropertiesList.add("size");
        allPropertiesList.add("order");
        allPropertiesList.add("sort");
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitions() {
        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        Map<String, String> allRequestParams = new HashMap<>();

        for (String property : allPropertiesList) {
            String value = uriInfo.getQueryParameters().getFirst(property);

            if (value != null) {
                allRequestParams.put(property, value);
            }
        }
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        // Populate filter-parameters
        if (allRequestParams.containsKey("category")) {
            processDefinitionQuery.processDefinitionCategory(allRequestParams.get("category"));
        }
        if (allRequestParams.containsKey("categoryLike")) {
            processDefinitionQuery.processDefinitionCategoryLike(allRequestParams.get("categoryLike"));
        }
        if (allRequestParams.containsKey("categoryNotEquals")) {
            processDefinitionQuery.processDefinitionCategoryNotEquals(allRequestParams.get("categoryNotEquals"));
        }
        if (allRequestParams.containsKey("key")) {
            processDefinitionQuery.processDefinitionKey(allRequestParams.get("key"));
        }
        if (allRequestParams.containsKey("keyLike")) {
            processDefinitionQuery.processDefinitionKeyLike(allRequestParams.get("keyLike"));
        }
        if (allRequestParams.containsKey("name")) {
            processDefinitionQuery.processDefinitionName(allRequestParams.get("name"));
        }
        if (allRequestParams.containsKey("nameLike")) {
            processDefinitionQuery.processDefinitionNameLike(allRequestParams.get("nameLike"));
        }
        if (allRequestParams.containsKey("resourceName")) {
            processDefinitionQuery.processDefinitionResourceName(allRequestParams.get("resourceName"));
        }
        if (allRequestParams.containsKey("resourceNameLike")) {
            processDefinitionQuery.processDefinitionResourceNameLike(allRequestParams.get("resourceNameLike"));
        }
        if (allRequestParams.containsKey("version")) {
            processDefinitionQuery.processDefinitionVersion(Integer.valueOf(allRequestParams.get("version")));
        }
        if (allRequestParams.containsKey("suspended")) {
            Boolean suspended = Boolean.valueOf(allRequestParams.get("suspended"));
            if (suspended != null) {
                if (suspended) {
                    processDefinitionQuery.suspended();
                } else {
                    processDefinitionQuery.active();
                }
            }
        }
        if (allRequestParams.containsKey("latest")) {
            Boolean latest = Boolean.valueOf(allRequestParams.get("latest"));
            if (latest != null && latest) {
                processDefinitionQuery.latestVersion();
            }
        }
        if (allRequestParams.containsKey("deploymentId")) {
            processDefinitionQuery.deploymentId(allRequestParams.get("deploymentId"));
        }
        if (allRequestParams.containsKey("startableByUser")) {
            processDefinitionQuery.startableByUser(allRequestParams.get("startableByUser"));
        }
        if (allRequestParams.containsKey("tenantId")) {
            processDefinitionQuery.processDefinitionTenantId(allRequestParams.get("tenantId"));
        }
        if (allRequestParams.containsKey("tenantIdLike")) {
            processDefinitionQuery.processDefinitionTenantIdLike(allRequestParams.get("tenantIdLike"));
        }

        DataResponse response = new ProcessDefinitionsPaginateList(new RestResponseFactory(), uriInfo)
                .paginateList(allRequestParams, processDefinitionQuery, "name", properties);

        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/{processDefinitionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ProcessDefinitionResponse getProcessDefinition(@PathParam("processDefinitionId") String processDefinitionId) {
        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
        return new RestResponseFactory().createProcessDefinitionResponse(processDefinition, uriInfo.getBaseUri().toString());
    }


    @GET
    @Path("/{processDefinitionId}/resourcedata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitionResource(@PathParam("processDefinitionId") String processDefinitionId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
        String resourceID = processDefinition.getResourceName();
        String contentType = Utils.resolveContentType(processDefinition.getResourceName());
        return Response.ok().type(contentType).entity(getDeploymentResourceData(processDefinition.getDeploymentId(),
                resourceID, repositoryService)).build();
    }

    @GET
    @Path("/{processDefinitionId}/identitylinks")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdentityLinks(@PathParam("processDefinitionId") String processDefinitionId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);

        return Response.ok().entity(new RestResponseFactory().createRestIdentityLinks(repositoryService
                .getIdentityLinksForProcessDefinition(processDefinition.getId()), uriInfo.getBaseUri().toString())).build();
    }

    @GET
    @Path("/{processDefinitionId}/identitylinks/{family}/{identityId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdentityLink(@PathParam("processDefinitionId") String processDefinitionId, @PathParam("family") String family,
                                    @PathParam("identityId") String identityId) {

        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
        validateIdentityLinkArguments(family, identityId);

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();

        // Check if identitylink to get exists
        IdentityLink link = getIdentityLink(family, identityId, processDefinition.getId(), repositoryService);
        return Response.ok().entity(new RestResponseFactory().createRestIdentityLink(link, uriInfo.getBaseUri().toString()))
                .build();
    }


    private ProcessDefinition getProcessDefinitionFromRequest(String processDefinitionId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

        if (processDefinition == null) {
            throw new ActivitiObjectNotFoundException("Could not find a process definition with id '" + processDefinitionId + "'.", ProcessDefinition.class);
        }
        return processDefinition;
    }

    private byte[] getDeploymentResourceData(String deploymentId, String resourceId, RepositoryService
            repositoryService) {

        if (deploymentId == null) {
            throw new ActivitiIllegalArgumentException("No deployment id provided");
        }
        if (resourceId == null) {
            throw new ActivitiIllegalArgumentException("No resource id provided");
        }

        // Check if deployment exists
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        if (deployment == null) {
            throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", Deployment.class);
        }

        List<String> resourceList = repositoryService.getDeploymentResourceNames(deploymentId);

        if (resourceList.contains(resourceId)) {
            final InputStream resourceStream = repositoryService.getResourceAsStream(deploymentId, resourceId);

            try {
                return IOUtils.toByteArray(resourceStream);
            } catch (Exception e) {
                throw new ActivitiException("Error converting resource stream", e);
            }
        } else {
            // Resource not found in deployment
            throw new ActivitiObjectNotFoundException("Could not find a resource with id '" + resourceId + "' in deployment '" + deploymentId + "'.", String.class);
        }
    }

    protected void validateIdentityLinkArguments(String family, String identityId) {
        if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family) && !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
            throw new ActivitiIllegalArgumentException("Identity link family should be 'users' or 'groups'.");
        }
        if (identityId == null) {
            throw new ActivitiIllegalArgumentException("IdentityId is required.");
        }
    }

    protected IdentityLink getIdentityLink(String family, String identityId, String processDefinitionId,
                                           RepositoryService repositoryService) {
        boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);

        // Perhaps it would be better to offer getting a single identitylink from
        // the API
        List<IdentityLink> allLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinitionId);
        for (IdentityLink link : allLinks) {
            boolean rightIdentity;
            if (isUser) {
                rightIdentity = identityId.equals(link.getUserId());
            } else {
                rightIdentity = identityId.equals(link.getGroupId());
            }

            if (rightIdentity && link.getType().equals(IdentityLinkType.CANDIDATE)) {
                return link;
            }
        }
        throw new ActivitiObjectNotFoundException("Could not find the requested identity link.", IdentityLink.class);
    }
}

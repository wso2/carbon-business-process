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

import io.netty.handler.codec.http.QueryStringDecoder;
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
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.RestUrls;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.internal.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ProcessDefinitionResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ProcessDefinitionsPaginateList;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.service.repository.ProcessDefinitionService",
        service = Microservice.class,
        immediate = true)
@Path("/process-definitions")
public class ProcessDefinitionService implements Microservice {

    private static final Map<String, QueryProperty> properties = new HashMap<>();
    private static final List<String> allPropertiesList = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionService.class);

    @Reference(
            name = "org.wso2.carbon.bpmn.core.BPMNEngineService",
            service = BPMNEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterBPMNEngineService")
    public void setBpmnEngineService(BPMNEngineService engineService) {
        log.info("Setting BPMN engine " + engineService);

    }

    protected void unRegisterBPMNEngineService(BPMNEngineService engineService) {
        log.info("Unregister BPMNEngineService..");
    }

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

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitions(@Context Request request) {
        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        ProcessDefinitionQuery processDefinitionQuery =
                repositoryService.createProcessDefinitionQuery();

        if (decoder.parameters().size() > 0) {
            for (String property : allPropertiesList) {
                String value = decoder.parameters().get(property).get(0);

                if (value != null) {
                    allRequestParams.put(property, value);
                }
            }

            // Populate filter-parameters
            if (allRequestParams.containsKey("category")) {
                processDefinitionQuery.processDefinitionCategory(allRequestParams.get("category"));
            }
            if (allRequestParams.containsKey("categoryLike")) {
                processDefinitionQuery
                        .processDefinitionCategoryLike(allRequestParams.get("categoryLike"));
            }
            if (allRequestParams.containsKey("categoryNotEquals")) {
                processDefinitionQuery
                        .processDefinitionCategoryNotEquals(allRequestParams.get("categoryNotEquals"));
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
                processDefinitionQuery
                        .processDefinitionResourceName(allRequestParams.get("resourceName"));
            }
            if (allRequestParams.containsKey("resourceNameLike")) {
                processDefinitionQuery
                        .processDefinitionResourceNameLike(allRequestParams.get("resourceNameLike"));
            }
            if (allRequestParams.containsKey("version")) {
                processDefinitionQuery
                        .processDefinitionVersion(Integer.valueOf(allRequestParams.get("version")));
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
                processDefinitionQuery
                        .processDefinitionTenantIdLike(allRequestParams.get("tenantIdLike"));
            }
        }

        DataResponse response =
                new ProcessDefinitionsPaginateList(new RestResponseFactory(), request.getUri())
                        .paginateList(allRequestParams, processDefinitionQuery, "name", properties);

        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/{process-definition-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ProcessDefinitionResponse getProcessDefinition(
            @PathParam("process-definition-id") String processDefinitionId,
            @Context Request request) {
        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
        return new RestResponseFactory()
                .createProcessDefinitionResponse(processDefinition, request.getUri());
    }

    @GET
    @Path("/{process-definition-id}/resource-data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitionResource(
            @PathParam("process-definition-id") String processDefinitionId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
        String resourceID = processDefinition.getResourceName();
        String contentType = Utils.resolveContentType(processDefinition.getResourceName());
        return Response.ok().type(contentType)
                .entity(getDeploymentResourceData(processDefinition.getDeploymentId(),
                        resourceID, repositoryService)).build();
    }

    @GET
    @Path("/{process-definition-id}/identity-links")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdentityLinks(@PathParam("process-definition-id") String processDefinitionId,
                                     @Context Request request) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);

        return Response.ok().entity(new RestResponseFactory().createRestIdentityLinks(
                repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId()),
                request.getUri())).build();
    }

    @GET
    @Path("/{process-definition-id}/identity-links/{family}/{identity-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdentityLinks(@PathParam("process-definition-id") String processDefinitionId,
                                     @PathParam("family") String family,
                                     @PathParam("identity-id") String identityId,
                                     @Context Request request) {

        ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
        validateIdentityLinkArguments(family, identityId);

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();

        // Check if identitylink to get exists
        IdentityLink link =
                getIdentityLink(family, identityId, processDefinition.getId(), repositoryService);
        return Response.ok().entity(new RestResponseFactory()
                .createRestIdentityLink(link, request.getUri())).build();
    }

    private ProcessDefinition getProcessDefinitionFromRequest(String processDefinitionId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinition processDefinition =
                repositoryService.getProcessDefinition(processDefinitionId);

        if (processDefinition == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a process definition with id '" + processDefinitionId + "'.",
                    ProcessDefinition.class);
        }
        return processDefinition;
    }

    private byte[] getDeploymentResourceData(String deploymentId, String resourceId,
                                             RepositoryService repositoryService) {

        if (deploymentId == null) {
            throw new ActivitiIllegalArgumentException("No deployment id provided");
        }
        if (resourceId == null) {
            throw new ActivitiIllegalArgumentException("No resource id provided");
        }

        // Check if deployment exists
        Deployment deployment =
                repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        if (deployment == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a deployment with id '" + deploymentId + "'.",
                    Deployment.class);
        }

        List<String> resourceList = repositoryService.getDeploymentResourceNames(deploymentId);

        if (resourceList.contains(resourceId)) {
            final InputStream resourceStream =
                    repositoryService.getResourceAsStream(deploymentId, resourceId);

            try {
                return IOUtils.toByteArray(resourceStream);
            } catch (Exception e) {
                throw new ActivitiException("Error converting resource stream", e);
            }
        } else {
            // Resource not found in deployment
            throw new ActivitiObjectNotFoundException(
                    "Could not find a resource with id '" + resourceId + "' in deployment '" +
                            deploymentId + "'.", String.class);
        }
    }

    protected void validateIdentityLinkArguments(String family, String identityId) {
        if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family) &&
                !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
            throw new ActivitiIllegalArgumentException(
                    "Identity link family should be 'users' or 'groups'.");
        }
        if (identityId == null) {
            throw new ActivitiIllegalArgumentException("IdentityId is required.");
        }
    }

    protected IdentityLink getIdentityLink(String family, String identityId,
                                           String processDefinitionId,
                                           RepositoryService repositoryService) {
        boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);

        // Perhaps it would be better to offer getting a single identitylink from
        // the API
        List<IdentityLink> allLinks =
                repositoryService.getIdentityLinksForProcessDefinition(processDefinitionId);
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
        throw new ActivitiObjectNotFoundException("Could not find the requested identity link.",
                IdentityLink.class);
    }
}

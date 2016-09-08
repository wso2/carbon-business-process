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
import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResourceResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResourceResponseCollection;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentsPaginateList;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
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

@Path("/deployments")
public class DeploymentService {

    private static final Log log = LogFactory.getLog(DeploymentService.class);


    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();
    private static final List<String> allPropertiesList = new ArrayList<>();

    static {
        allPropertiesList.add("name");
        allPropertiesList.add("nameLike");
        allPropertiesList.add("category");
        allPropertiesList.add("categoryNotEquals");
        allPropertiesList.add("tenantId");
        allPropertiesList.add("tenantIdLike");
        allPropertiesList.add("withoutTenantId");
        allPropertiesList.add("sort");
        allPropertiesList.add("start");
        allPropertiesList.add("size");
        allPropertiesList.add("order");

    }


    static {
        allowedSortProperties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
        allowedSortProperties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
        allowedSortProperties.put("deployTime", DeploymentQueryProperty.DEPLOY_TIME);
        allowedSortProperties.put("tenantId", DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeployments() {
        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

        // Apply filters
        Map<String, String> allRequestParams = new HashMap<>();

        for (String property : allPropertiesList) {
            String value = uriInfo.getQueryParameters().getFirst(property);

            if (value != null) {
                allRequestParams.put(property, value);
            }
        }

        String name = uriInfo.getQueryParameters().getFirst("name");
        if (name != null) {
            deploymentQuery.deploymentName(name);
        }

        String nameLike = uriInfo.getQueryParameters().getFirst("nameLike");
        if (nameLike != null) {
            deploymentQuery.deploymentNameLike(nameLike);
        }

        String category = uriInfo.getQueryParameters().getFirst("category");
        if (category != null) {
            deploymentQuery.deploymentCategory(category);
        }

        String categoryNotEquals = uriInfo.getQueryParameters().getFirst("categoryNotEquals");
        if (categoryNotEquals != null) {
            deploymentQuery.deploymentCategoryNotEquals(categoryNotEquals);
        }

        String tenantId = uriInfo.getQueryParameters().getFirst("tenantId");
        if (tenantId != null) {
            deploymentQuery.deploymentTenantId(tenantId);
        }

        String tenantIdLike = uriInfo.getQueryParameters().getFirst("tenantIdLike");
        if (tenantIdLike != null) {
            deploymentQuery.deploymentTenantIdLike(tenantIdLike);
        }


        String sWithoutTenantId = uriInfo.getQueryParameters().getFirst("withoutTenantId");
        if (sWithoutTenantId != null) {
            Boolean withoutTenantId = Boolean.valueOf(sWithoutTenantId);
            if (withoutTenantId) {
                deploymentQuery.deploymentWithoutTenantId();
            }
        }


        DeploymentsPaginateList deploymentsPaginateList = new DeploymentsPaginateList(new RestResponseFactory(), uriInfo);
        DataResponse dataResponse = deploymentsPaginateList.paginateList(allRequestParams, deploymentQuery, "id",
                allowedSortProperties);

        return Response.ok().entity(dataResponse).build();
    }

    @GET
    @Path("/{deploymentId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeployment(@PathParam("deploymentId") String deploymentId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

        if (deployment == null) {
            throw new ActivitiObjectNotFoundException("Could not find a deployment with deploymentId '" + deploymentId + "'.",
                    Deployment.class);
        }

        DeploymentResponse deploymentResponse = new RestResponseFactory().createDeploymentResponse(deployment, uriInfo.getBaseUri().toString());
        return Response.ok().entity(deploymentResponse).build();
    }

    @GET
    @Path("/{deploymentId}/resources/{resourcePath:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeploymentResourceForDifferentUrl(@PathParam("deploymentId") String deploymentId, @PathParam("resourcePath") String resourcePath) {

        if (log.isDebugEnabled()) {
            log.debug("deploymentId:" + deploymentId + " resourcePath:" + resourcePath);
        }
        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        // Check if deployment exists
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        if (deployment == null) {
            throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", Deployment.class);
        }

        List<String> resourceList = repositoryService.getDeploymentResourceNames(deploymentId);

        if (resourceList.contains(resourcePath)) {
            // Build resource representation
            DeploymentResourceResponse deploymentResourceResponse = new RestResponseFactory()
                    .createDeploymentResourceResponse(deploymentId, resourcePath,
                            Utils.resolveContentType(resourcePath), uriInfo.getBaseUri().toString());
            return Response.ok().entity(deploymentResourceResponse).build();

        } else {
            // Resource not found in deployment
            throw new ActivitiObjectNotFoundException("Could not find a resource with id '" + resourcePath
                    + "' in deployment '" + deploymentId + "'.", Deployment.class);
        }

    }

    @GET
    @Path("/{deploymentId}/resources")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeploymentResources(@PathParam("deploymentId") String deploymentId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        // Check if deployment exists
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        if (deployment == null) {
            throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", Deployment.class);
        }

        List<String> resourceList = repositoryService.getDeploymentResourceNames(deploymentId);
        DeploymentResourceResponseCollection deploymentResourceResponseCollection = new RestResponseFactory().createDeploymentResourceResponseList(deploymentId, resourceList, uriInfo.getBaseUri().toString());

        return Response.ok().entity(deploymentResourceResponseCollection).build();
    }


    @GET
    @Path("/{deploymentId}/resourcedata/{resourceId}")
    public Response getDeploymentResource(@PathParam("deploymentId") String deploymentId,
                                          @PathParam("resourceId") String resourceId) {
        String contentType = Utils.resolveContentType(resourceId);
        return Response.ok().type(contentType).entity(getDeploymentResourceData(deploymentId, resourceId)).build();
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return Response.ok("").build();
    }


    private byte[] getDeploymentResourceData(String deploymentId, String resourceId) {

        if (deploymentId == null) {
            throw new ActivitiIllegalArgumentException("No deployment id provided");
        }
        if (resourceId == null) {
            throw new ActivitiIllegalArgumentException("No resource id provided");
        }

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
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
}

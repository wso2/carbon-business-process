/**
 * Copyright (c) 2015-2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResourceResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResourceResponseCollection;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentsPaginateList;
import org.wso2.msf4j.Request;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

//import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;

/**
 *
 */
//@Component(
//        name = "org.wso2.carbon.bpmn.rest.service.repository.DeploymentService",
//        service = Microservice.class,
//        immediate = true)
//@Path("/deployments")
public class DeploymentService { //implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);

    private static final List<String> allPropertiesList = new ArrayList<>();
    private static Map<String, QueryProperty> allowedSortProperties =
            new HashMap<String, QueryProperty>();

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

    public Response getDeployments(Request request) {
        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();
        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

        // Apply filters
        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        if (decoder.parameters().size() > 0) {
            for (String property : allPropertiesList) {
                String value = decoder.parameters().get(property).get(0);

                if (value != null) {
                    allRequestParams.put(property, value);
                }
            }
            Map<String, List<String>> parameters = decoder.parameters();

            if (parameters.containsKey("name")) {
                String name = decoder.parameters().get("name").get(0);
                if (name != null) {
                    deploymentQuery.deploymentName(name);
                }
            }
            if (parameters.containsKey("nameLike")) {
                String nameLike = decoder.parameters().get("nameLike").get(0);
                if (nameLike != null) {
                    deploymentQuery.deploymentNameLike(nameLike);
                }
            }
            if (parameters.containsKey("category")) {
                String category = decoder.parameters().get("category").get(0);
                if (category != null) {
                    deploymentQuery.deploymentCategory(category);
                }
            }

            if (parameters.containsKey("categoryNotEquals")) {
                String categoryNotEquals = decoder.parameters().get("categoryNotEquals").get(0);
                if (categoryNotEquals != null) {
                    deploymentQuery.deploymentCategoryNotEquals(categoryNotEquals);
                }
            }
            if (parameters.containsKey("tenantId")) {
                String tenantId = decoder.parameters().get("tenantId").get(0);
                if (tenantId != null) {
                    deploymentQuery.deploymentTenantId(tenantId);
                }
            }
            if (parameters.containsKey("tenantIdLike")) {
                String tenantIdLike = decoder.parameters().get("tenantIdLike").get(0);
                if (tenantIdLike != null) {
                    deploymentQuery.deploymentTenantIdLike(tenantIdLike);
                }
            }

            if (parameters.containsKey("withoutTenantId")) {
                String sWithoutTenantId = decoder.parameters().get("withoutTenantId").get(0);
                if (sWithoutTenantId != null) {
                    Boolean withoutTenantId = Boolean.valueOf(sWithoutTenantId);
                    if (withoutTenantId) {
                        deploymentQuery.deploymentWithoutTenantId();
                    }
                }
            }
        }
        DeploymentsPaginateList deploymentsPaginateList =
                new DeploymentsPaginateList(new RestResponseFactory(), request.getUri());
        DataResponse dataResponse = deploymentsPaginateList
                .paginateList(allRequestParams, deploymentQuery, "id", allowedSortProperties);

        return Response.ok().entity(dataResponse).build();
    }

    public Response getDeployment(String deploymentId, Request request) {

        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();
        Deployment deployment =
                repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

        if (deployment == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a deployment with deploymentId '" + deploymentId + "'.",
                    Deployment.class);
        }

        DeploymentResponse deploymentResponse =
                new RestResponseFactory().createDeploymentResponse(deployment, request.getUri());
        return Response.ok().entity(deploymentResponse).build();
    }

    public Response getDeploymentResourceForDifferentUrl(String deploymentId, String resourcePath, Request request) {

        if (log.isDebugEnabled()) {
            log.debug("deployment-id:" + deploymentId + " resource-path:" + resourcePath);
        }
        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();
        // Check if deployment exists
        Deployment deployment =
                repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        if (deployment == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a deployment with id '" + deploymentId + "'.",
                    Deployment.class);
        }

        List<String> resourceList = repositoryService.getDeploymentResourceNames(deploymentId);

        if (resourceList.contains(resourcePath)) {
            // Build resource representation
            DeploymentResourceResponse deploymentResourceResponse = new RestResponseFactory()
                    .createDeploymentResourceResponse(deploymentId, resourcePath,
                            Utils.resolveContentType(resourcePath),
                            request.getUri());
            return Response.ok().entity(deploymentResourceResponse).build();

        } else {
            // Resource not found in deployment
            throw new ActivitiObjectNotFoundException(
                    "Could not find a resource with id '" + resourcePath + "' in deployment '" +
                            deploymentId + "'.", Deployment.class);
        }

    }

    public Response getDeploymentResources(String deploymentId, Request request) {

        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();
        // Check if deployment exists
        Deployment deployment =
                repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
        if (deployment == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a deployment with id '" + deploymentId + "'.",
                    Deployment.class);
        }

        List<String> resourceList = repositoryService.getDeploymentResourceNames(deploymentId);
        DeploymentResourceResponseCollection deploymentResourceResponseCollection =
                new RestResponseFactory()
                        .createDeploymentResourceResponseList(deploymentId, resourceList,
                                request.getUri());

        return Response.ok().entity(deploymentResourceResponseCollection).build();
    }

    public Response getDeploymentResource(String deploymentId, String resourceId) {
        String contentType = Utils.resolveContentType(resourceId);
        return Response.ok().type(contentType)
                .entity(getDeploymentResourceData(deploymentId, resourceId)).build();
    }

    private byte[] getDeploymentResourceData(String deploymentId, String resourceId) {

        if (deploymentId == null) {
            throw new ActivitiIllegalArgumentException("No deployment id provided");
        }
        if (resourceId == null) {
            throw new ActivitiIllegalArgumentException("No resource id provided");
        }

        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();
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

            try (final InputStream resourceStream =
                         repositoryService.getResourceAsStream(deploymentId, resourceId)) {
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
}

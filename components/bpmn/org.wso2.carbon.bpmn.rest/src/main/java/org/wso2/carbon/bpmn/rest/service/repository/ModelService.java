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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ModelQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ModelQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ModelResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ModelsPaginateList;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/models")
public class ModelService {

    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();
    private static final Log log = LogFactory.getLog(ModelService.class);
    private static final List<String> allPropertiesList = new ArrayList<>();

    @Context
    UriInfo uriInfo;

    static {
        allowedSortProperties.put("id", ModelQueryProperty.MODEL_ID);
        allowedSortProperties.put("category", ModelQueryProperty.MODEL_CATEGORY);
        allowedSortProperties.put("createTime", ModelQueryProperty.MODEL_CREATE_TIME);
        allowedSortProperties.put("key", ModelQueryProperty.MODEL_KEY);
        allowedSortProperties.put("lastUpdateTime", ModelQueryProperty.MODEL_LAST_UPDATE_TIME);
        allowedSortProperties.put("name", ModelQueryProperty.MODEL_NAME);
        allowedSortProperties.put("version", ModelQueryProperty.MODEL_VERSION);
        allowedSortProperties.put("tenantId", ModelQueryProperty.MODEL_TENANT_ID);
    }

    static {
        allPropertiesList.add("id");
        allPropertiesList.add("category");
        allPropertiesList.add("categoryLike");
        allPropertiesList.add("categoryNotEquals");
        allPropertiesList.add("name");
        allPropertiesList.add("nameLike");
        allPropertiesList.add("key");
        allPropertiesList.add("deploymentId");
        allPropertiesList.add("version");
        allPropertiesList.add("latestVersion");
        allPropertiesList.add("deployed");
        allPropertiesList.add("tenantId");
        allPropertiesList.add("tenantIdLike");
        allPropertiesList.add("tenantIdLike");
        allPropertiesList.add("withoutTenantId");
        allPropertiesList.add("start");
        allPropertiesList.add("size");
        allPropertiesList.add("order");
        allPropertiesList.add("sort");
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModels() {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();

        // Apply filters
        Map<String, String> allRequestParams = new HashMap<>();

        for (String property : allPropertiesList) {
            String value = uriInfo.getQueryParameters().getFirst(property);

            if (value != null) {
                allRequestParams.put(property, value);
            }
        }

        ModelQuery modelQuery = repositoryService.createModelQuery();

        String id = uriInfo.getQueryParameters().getFirst("id");
        if (id != null) {
            modelQuery.modelId(id);
        }

        String category = uriInfo.getQueryParameters().getFirst("category");
        if (category != null) {
            modelQuery.modelCategory(category);
        }

        String categoryLike = uriInfo.getQueryParameters().getFirst("categoryLike");
        if (categoryLike != null) {
            modelQuery.modelCategoryLike(categoryLike);
        }

        String categoryNotEquals = uriInfo.getQueryParameters().getFirst("categoryNotEquals");
        if (categoryNotEquals != null) {
            modelQuery.modelCategoryNotEquals(categoryNotEquals);
        }

        String name = uriInfo.getQueryParameters().getFirst("name");
        if (name != null) {
            modelQuery.modelName(name);
        }

        String nameLike = uriInfo.getQueryParameters().getFirst("nameLike");
        if (nameLike != null) {
            modelQuery.modelNameLike(nameLike);
        }

        String key = uriInfo.getQueryParameters().getFirst("key");
        if (key != null) {
            modelQuery.modelKey(key);
        }

        String version = uriInfo.getQueryParameters().getFirst("version");
        if (version != null) {
            modelQuery.modelVersion(Integer.valueOf(version));
        }

        String latestVersion = uriInfo.getQueryParameters().getFirst("latestVersion");
        if (latestVersion != null) {
            boolean isLatestVersion = Boolean.valueOf(latestVersion);
            if (isLatestVersion) {
                modelQuery.latestVersion();
            }
        }

        String deploymentId = uriInfo.getQueryParameters().getFirst("deploymentId");
        if (deploymentId != null) {
            modelQuery.deploymentId(deploymentId);
        }

        String deployed = uriInfo.getQueryParameters().getFirst("deployed");
        if (deployed != null) {
            boolean isDeployed = Boolean.valueOf(deployed);
            if (isDeployed) {
                modelQuery.deployed();
            } else {
                modelQuery.notDeployed();
            }
        }

        String tenantId = uriInfo.getQueryParameters().getFirst("tenantId");
        if (tenantId != null) {
            modelQuery.modelTenantId(tenantId);
        }

        String tenantIdLike = uriInfo.getQueryParameters().getFirst("tenantIdLike");
        if (tenantIdLike != null) {
            modelQuery.modelTenantIdLike(tenantIdLike);
        }

        String sWithoutTenantId = uriInfo.getQueryParameters().getFirst("withoutTenantId");
        if (sWithoutTenantId != null) {
            boolean withoutTenantId = Boolean.valueOf(sWithoutTenantId);
            if (withoutTenantId) {
                modelQuery.modelWithoutTenantId();
            }
        }

        DataResponse response = new ModelsPaginateList(new RestResponseFactory(), uriInfo).paginateList
                (allRequestParams, modelQuery, "id", allowedSortProperties);

        List<ModelResponse> modelResponseList = (List<ModelResponse>) response.getData();

        if (log.isDebugEnabled()) {
            log.debug("modelResponseList: " + modelResponseList.size());
        }

        return Response.ok().entity(response).build();
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return Response.ok("").build();
    }
}

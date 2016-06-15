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
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ModelQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ModelQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ModelResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ModelsPaginateList;
import org.wso2.msf4j.Request;

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
//        name = "org.wso2.carbon.bpmn.rest.service.repository.ModelService",
//        service = Microservice.class,
//        immediate = true)
//@Path("/models")
public class ModelService { //} implements Microservice {

    private static final List<String> allPropertiesList = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ModelService.class);
    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();

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


    public Response getModels(Request request) {

        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();

        // Apply filters
        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        ModelQuery modelQuery = repositoryService.createModelQuery();
        Map<String, List<String>> parameters = decoder.parameters();

        if (decoder.parameters().size() > 0) {
            for (String property : allPropertiesList) {
                String value = decoder.parameters().get(property).get(0);

                if (value != null) {
                    allRequestParams.put(property, value);
                }
            }


            if (parameters.containsKey("id")) {
                String id = decoder.parameters().get("id").get(0);
                if (id != null) {
                    modelQuery.modelId(id);
                }
            }
            if (parameters.containsKey("category")) {
                String category = decoder.parameters().get("category").get(0);
                if (category != null) {
                    modelQuery.modelCategory(category);
                }
            }
            if (parameters.containsKey("categoryLike")) {
                String categoryLike = decoder.parameters().get("categoryLike").get(0);
                if (categoryLike != null) {
                    modelQuery.modelCategoryLike(categoryLike);
                }
            }

            if (parameters.containsKey("categoryNotEquals")) {
                String categoryNotEquals = decoder.parameters().get("categoryNotEquals").get(0);
                if (categoryNotEquals != null) {
                    modelQuery.modelCategoryNotEquals(categoryNotEquals);
                }
            }

            if (parameters.containsKey("name")) {
                String name = decoder.parameters().get("name").get(0);
                if (name != null) {
                    modelQuery.modelName(name);
                }
            }
            if (parameters.containsKey("nameLike")) {
                String nameLike = decoder.parameters().get("nameLike").get(0);
                if (nameLike != null) {
                    modelQuery.modelNameLike(nameLike);
                }
            }
            if (parameters.containsKey("key")) {
                String key = decoder.parameters().get("key").get(0);
                if (key != null) {
                    modelQuery.modelKey(key);
                }
            }
            if (parameters.containsKey("version")) {
                String version = decoder.parameters().get("version").get(0);
                if (version != null) {
                    modelQuery.modelVersion(Integer.valueOf(version));
                }
            }
            if (parameters.containsKey("latestVersion")) {
                String latestVersion = decoder.parameters().get("latestVersion").get(0);
                if (latestVersion != null) {
                    boolean isLatestVersion = Boolean.valueOf(latestVersion);
                    if (isLatestVersion) {
                        modelQuery.latestVersion();
                    }
                }
            }

            if (parameters.containsKey("deploymentId")) {
                String deploymentId = decoder.parameters().get("deploymentId").get(0);
                if (deploymentId != null) {
                    modelQuery.deploymentId(deploymentId);
                }
            }
            if (parameters.containsKey("deployed")) {
                String deployed = decoder.parameters().get("deployed").get(0);
                if (deployed != null) {
                    boolean isDeployed = Boolean.valueOf(deployed);
                    if (isDeployed) {
                        modelQuery.deployed();
                    } else {
                        modelQuery.notDeployed();
                    }
                }
            }
            if (parameters.containsKey("tenantId")) {
                String tenantId = decoder.parameters().get("tenantId").get(0);
                if (tenantId != null) {
                    modelQuery.modelTenantId(tenantId);
                }
            }
            if (parameters.containsKey("tenantIdLike")) {
                String tenantIdLike = decoder.parameters().get("tenantIdLike").get(0);
                if (tenantIdLike != null) {
                    modelQuery.modelTenantIdLike(tenantIdLike);
                }
            }
            if (parameters.containsKey("withoutTenantId")) {
                String sWithoutTenantId = decoder.parameters().get("withoutTenantId").get(0);
                if (sWithoutTenantId != null) {
                    boolean withoutTenantId = Boolean.valueOf(sWithoutTenantId);
                    if (withoutTenantId) {
                        modelQuery.modelWithoutTenantId();
                    }
                }
            }
        }
        DataResponse response = new ModelsPaginateList(new RestResponseFactory(), request.getUri())
                .paginateList(allRequestParams, modelQuery, "id", allowedSortProperties);

        List<ModelResponse> modelResponseList = (List<ModelResponse>) response.getData();

        if (log.isDebugEnabled()) {
            log.debug("modelResponseList: " + modelResponseList.size());
        }

        return Response.ok().entity(response).build();
    }
}

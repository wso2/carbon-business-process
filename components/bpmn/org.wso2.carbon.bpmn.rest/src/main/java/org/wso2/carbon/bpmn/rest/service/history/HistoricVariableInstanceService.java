/**
 *  Copyright (c) 2015-2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.service.history;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricVariableInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.service.base.BaseHistoricVariableInstanceService;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Component(
		name = "org.wso2.carbon.bpmn.rest.service.history.BaseHistoricVariableInstanceService",
		service = Microservice.class,
		immediate = true)
@Path("/historic-variable-instances")
public class HistoricVariableInstanceService extends BaseHistoricVariableInstanceService
		implements Microservice {
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
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getHistoricActivityInstances(@Context HttpRequest request) {
		HistoricVariableInstanceQueryRequest query = new HistoricVariableInstanceQueryRequest();

		// Populate query based on request
		Map<String, String> allRequestParams = new HashMap<>();
		QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());

		for (String property : allPropertiesList) {
			String value = decoder.parameters().get(property).get(0);

			if (value != null) {
				allRequestParams.put(property, value);
			}
		}
		if (allRequestParams.get("excludeTaskVariables") != null) {
			query.setExcludeTaskVariables(
					Boolean.valueOf(allRequestParams.get("excludeTaskVariables")));
		}

		if (allRequestParams.get("taskId") != null) {
			query.setTaskId(allRequestParams.get("taskId"));
		}

		if (allRequestParams.get("executionId") != null) {
			query.setExecutionId(allRequestParams.get("executionId"));
		}

		if (allRequestParams.get("processInstanceId") != null) {
			query.setProcessInstanceId(allRequestParams.get("processInstanceId"));
		}

		if (allRequestParams.get("variableName") != null) {
			query.setVariableName(allRequestParams.get("variableName"));
		}

		if (allRequestParams.get("variableNameLike") != null) {
			query.setVariableNameLike(allRequestParams.get("variableNameLike"));
		}

		DataResponse dataResponse = getQueryResponse(query, allRequestParams);
		return Response.ok().entity(dataResponse).build();
	}

}

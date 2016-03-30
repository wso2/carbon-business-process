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

package org.wso2.carbon.bpmn.rest.service.form;

import org.activiti.engine.FormService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.form.EnumFormType;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.form.FormPropertyEnumDataHolder;
import org.wso2.carbon.bpmn.rest.model.form.FormPropertyResponse;
import org.wso2.carbon.bpmn.rest.model.form.FormPropertyResponseCollection;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(
		name = "org.wso2.carbon.bpmn.rest.service.form.ProcessDefinitionFormPropertyService",
		service = Microservice.class,
		immediate = true)
@Path("/")
public class ProcessDefinitionFormPropertyService implements Microservice{

	@Activate
	protected void activate(BundleContext bundleContext) {
		// Nothing to do
	}

	@Deactivate
	protected void deactivate(BundleContext bundleContext) {
		// Nothing to do
	}
    @GET
    @Path("/{process-definition-id}/properties")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getStartFormProperties(@PathParam("process-definition-id") String processDefinitionId) {

        FormService formService = BPMNOSGIService.getFormService();

        StartFormData startFormData = formService.getStartFormData(processDefinitionId);
        FormPropertyResponseCollection formPropertyResponseCollection = new FormPropertyResponseCollection();

        if(startFormData != null) {

            List<FormProperty> properties = startFormData.getFormProperties();
            List<FormPropertyResponse> formPropertyResponseList = new ArrayList<>();
            for (FormProperty property : properties) {
               // ObjectNode propertyJSON = objectMapper.createObjectNode();
                FormPropertyResponse formPropertyResponse = new FormPropertyResponse();
                formPropertyResponse.setId(property.getId());
                formPropertyResponse.setName(property.getName());


                if (property.getValue() != null) {
                    formPropertyResponse.setValue(property.getValue());
                } else {
                    formPropertyResponse.setValue(null);
                }


                if(property.getType() != null) {
                    formPropertyResponse.setType(property.getType().getName());

                    if (property.getType() instanceof EnumFormType) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> valuesMap = (Map<String, String>) property.getType().getInformation("values");
                        if (valuesMap != null) {
                            List<FormPropertyEnumDataHolder> formPropertyEnumDataHoldersList = new ArrayList<>();
                            for (String key : valuesMap.keySet()) {
                                FormPropertyEnumDataHolder formPropertyEnumDataHolder = new
                                        FormPropertyEnumDataHolder();
                                formPropertyEnumDataHolder.setId(key);
                                formPropertyEnumDataHolder.setName(valuesMap.get(key));
                                formPropertyEnumDataHoldersList.add(formPropertyEnumDataHolder);
                            }
                        }
                    }

                } else {
                    formPropertyResponse.setType("String");
                }
                formPropertyResponse.setRequired(property.isRequired());
                formPropertyResponse.setReadable(property.isReadable());
                formPropertyResponse.setWritable(property.isWritable());
                formPropertyResponseList.add(formPropertyResponse);
            }
            formPropertyResponseCollection.setData(formPropertyResponseList);
        }

        return Response.ok().entity(formPropertyResponseCollection).build();
    }
}

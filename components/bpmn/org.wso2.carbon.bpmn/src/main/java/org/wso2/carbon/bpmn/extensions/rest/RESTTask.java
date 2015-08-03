/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.bpmn.extensions.rest;

import com.jayway.jsonpath.JsonPath;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.el.JuelExpression;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.Resource;

import java.net.URI;

/**
 * Provides REST service invocation support within BPMN processes. It invokes the REST service given by "serviceURL" or "serviceRef" parameters using
 * the HTTP method given as "method" parameter. "serviceURL" parameter can be used to give a URL of a REST service endpoint, which cannot be changed after deployment.
 * "serviceRef" can point to a registry location which contains a URL (e.g. conf:/companyA/paymentService). URLs given in such registry resources can be changed after deployment
 * and the current value of the registry resource will be read before each service invocation.
 *
 * Optionally, input payload can be provided using the "input" parameter. Output received from the REST service will be assigned to a
 * process variable (as raw content) or parts of the output can be mapped to different process variables. Both these scenarios are illustrated in below examples.
 *
 * If a failure occurs in REST task, a BPMN error with error code "RestInvokeError" will be thrown. BPMN process can catch this error using an Error Boundary Event associated
 * with the REST service task.
 *
 * Example with text input and text output:
 *
 *  <serviceTask id="servicetask1" name="REST task1" activiti:class="org.wso2.carbon.bpmn.extensions.rest.RESTTask">
        <extensionElements>
             <activiti:field name="serviceURL">
                 <activiti:expression>http://10.0.3.1:9773/restSample1_1.0.0/services/rest_sample1/${method}</activiti:expression>
             </activiti:field>
             <activiti:field name="method">
                <activiti:string><![CDATA[POST]]></activiti:string>
             </activiti:field>
             <activiti:field name="input">
                <activiti:expression>Input for task1</activiti:expression>
             </activiti:field>
             <activiti:field name="outputVariable">
                <activiti:string><![CDATA[v1]]></activiti:string>
             </activiti:field>
         </extensionElements>
     </serviceTask>
 *
 * Example with JSON input and JSON output mapping and registry based URL:
 <serviceTask id="servicetask2" name="Rest task2" activiti:class="org.wso2.carbon.bpmn.extensions.rest.RESTTask">
     <extensionElements>
         <activiti:field name="serviceRef">
            <activiti:expression>conf:/test1/service2</activiti:expression>
         </activiti:field>
         <activiti:field name="method">
            <activiti:string><![CDATA[POST]]></activiti:string>
         </activiti:field>
         <activiti:field name="input">
             <activiti:expression>{
                 "companyName":"ibm",
                 "industry":"${industry}",
                 "address":{
                 "country":"USA",
                 "state":"${state}"}
                 }
             </activiti:expression>
         </activiti:field>
         <activiti:field name="outputMappings">
            <activiti:string><![CDATA[var2:customer.name,var3:item.price]]></activiti:string>
         </activiti:field>
     </extensionElements>
 </serviceTask>
 *
 */
public class RESTTask implements JavaDelegate {

    private static final Log log = LogFactory.getLog(RESTTask.class);

    private static final String GOVERNANCE_REGISTRY_PREFIX = "gov:/";
    private static final String CONFIGURATION_REGISTRY_PREFIX = "conf:/";
    private static final String REST_INVOKE_ERROR = "RestInvokeError";
    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    private RESTInvoker restInvoker;

    private JuelExpression serviceURL;
    private JuelExpression serviceRef;
    private FixedValue method;
    private JuelExpression input;
    private FixedValue outputVariable;
    private FixedValue outputMappings;

    public RESTTask() {
        int maxTotalConnections = 200; // TODO: make this configurable via config file
        int maxTotalConnectionsPerRoute = 200; // TODO: make this configurable via config file
        restInvoker = new RESTInvoker(maxTotalConnections, maxTotalConnectionsPerRoute);
    }

    @Override
    public void execute(DelegateExecution execution) {
        if (log.isDebugEnabled()) {
            log.debug("Executing RESTInvokeTask " + method + " - " + serviceURL.getValue(execution).toString());
        }

        String output = "";
        String url = null;
        try {
            if (serviceURL != null) {
                url = serviceURL.getValue(execution).toString();

            } else if (serviceRef != null) {
                String resourcePath = serviceRef.getValue(execution).toString();
                String registryPath;
                if (resourcePath.startsWith(GOVERNANCE_REGISTRY_PREFIX)) {
                    registryPath = resourcePath.substring(GOVERNANCE_REGISTRY_PREFIX.length());
                } else if (resourcePath.startsWith(CONFIGURATION_REGISTRY_PREFIX)) {
                    registryPath = resourcePath.substring(CONFIGURATION_REGISTRY_PREFIX.length());
                } else {
                    String msg = "Registry type is not specified for service reference in " + execution.getCurrentActivityId() + ":" +
                            execution.getCurrentActivityName() + ". serviceRef should begin with gov:/ or conf:/ to indicate the registry type.";
                    throw new BPMNRESTException(msg);
                }
                Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
                Resource urlResource = registry.get(registryPath);
                if (urlResource != null) {
                    url = new String((byte[]) urlResource.getContent());
                    url = url.trim();
                }

            } else {
                String urlNotFoundErrorMsg = "Service URL is not provided for " +
                        execution.getCurrentActivityId() + ":" + execution.getCurrentActivityName() + ". serviceURL or serviceRef must be provided.";
                throw new BPMNRESTException(urlNotFoundErrorMsg);
            }

            if (POST_METHOD.equals(method.getValue(execution).toString())) {
                String inputContent = input.getValue(execution).toString();
                output = restInvoker.invokePOST(new URI(url), inputContent);
            } else {
                output = restInvoker.invokeGET(new URI(url));
            }

            if (outputVariable != null) {
                String outVarName = outputVariable.getValue(execution).toString();
                execution.setVariable(outVarName, output);
            } else {
                String outMappings = outputMappings.getValue(execution).toString();
                outMappings = outMappings.trim();
                String[] mappings = outMappings.split(",");
                for (String mapping : mappings) {
                    String[] mappingParts = mapping.split(":");
                    String varName = mappingParts[0];
                    String jsonExpression = mappingParts[1];
                    String value = JsonPath.read(output, jsonExpression);
                    execution.setVariable(varName, value);
                }
            }
        } catch (Exception e) {
            String errorMessage = "Failed to execute " + method + " " + url;
            log.error(errorMessage, e);
            throw new BpmnError(REST_INVOKE_ERROR, errorMessage);
        }
    }

    public void setServiceURL(JuelExpression serviceURL) {
        this.serviceURL = serviceURL;
    }

    public void setServiceRef(JuelExpression serviceRef) {
        this.serviceRef = serviceRef;
    }

    public void setInput(JuelExpression input) {
        this.input = input;
    }

    public void setOutputVariable(FixedValue outputVariable) {
        this.outputVariable = outputVariable;
    }

    public void setMethod(FixedValue method) {
        this.method = method;
    }

    public FixedValue getOutputMappings() {
        return outputMappings;
    }

    public void setOutputMappings(FixedValue outputMappings) {
        this.outputMappings = outputMappings;
    }
}

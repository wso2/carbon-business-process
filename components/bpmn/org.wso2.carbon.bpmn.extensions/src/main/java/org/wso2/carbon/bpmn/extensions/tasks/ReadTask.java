/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpmn.extensions.tasks;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.json.JSONUtils;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.Utils;
import org.wso2.carbon.bpmn.extensions.registry.RegistryUtil;
import org.wso2.carbon.bpmn.extensions.registry.Resource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;
import javax.xml.parsers.ParserConfigurationException;

/**
 * ReadTask is a custom BPMN Activity that is capable of reading resources from a source and populate it to given
 * variable (by name)
 *
 * Supported sources:
 *      1. REGISTRY : WSO2 carbon registry
 *      2. ENVIRONMENT : Environment variables
 *      3. SYSTEM : Java system variables
 *
 * <serviceTask xmlns:activiti="http://activiti.org/bpmn" id="servicetask" name="Service Task" activiti:class="org.wso2.carbon.bpmn.extensions.tasks.ReadTask">
 *    <extensionElements>
 *       <activiti:field name="resource">
 *          <activiti:expression><![CDATA[conf:/custom/testJson]]></activiti:expression>
 *       </activiti:field>
 *       <activiti:field name="origin">
 *          <activiti:expression><![CDATA[REGISTRY]]></activiti:expression>
 *       </activiti:field>
 *       <activiti:field name="target">
 *          <activiti:expression><![CDATA[testRegJsonVar]]></activiti:expression>
 *       </activiti:field>
 *       <activiti:field name="type">
 *          <activiti:expression><![CDATA[json]]></activiti:expression>
 *       </activiti:field>
 *    </extensionElements>
 * </serviceTask>
 *
 * resource (mandatory) - resource name (incase if the resource is registry resource, this should be resource path)
 * origin (mandatory) - origin / source type. Supported sources : REGISTRY, ENVIRONMENT, SYSTEM
 * target (mandatory) - target variable name that should be populated with the content read by this task
 * type (optional) - type of the variable name. Content read by this task will be parsed / converted to given type
 *                      Supported types: string, integer, boolean, json, xml
 *
 */
public class ReadTask implements JavaDelegate {

    private static final Log LOG = LogFactory.getLog(ReadTask.class);

    private static final String ORIGIN_TYPE_REGISTRY = "REGISTRY";
    private static final String ORIGIN_TYPE_ENV = "ENVIRONMENT";
    private static final String ORIGIN_TYPE_SYS = "SYSTEM";

    private static final String DATA_TYPE_STRING = "string";
    private static final String DATA_TYPE_INTEGER = "integer";
    private static final String DATA_TYPE_BOOLEAN = "boolean";
    private static final String DATA_TYPE_JSON = "json";
    private static final String DATA_TYPE_XML = "xml";

    private Expression origin; // Source/Origin of the resource
    private Expression resource; // Resource name
    private Expression target; // Target variable name
    private Expression type; // data type of the resource. Default is string

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String originStr = getExpressionStrValue(origin, execution);
        if (originStr == null) {
            throw new WSO2TaskException("\"origin\" parameter is mandatory, but found null");
        }

        String resourceName = getExpressionStrValue(resource, execution);
        if (resourceName == null) {
            throw new WSO2TaskException("\"resource\" parameter is mandatory, but found null");
        }

        String targetVarName = getExpressionStrValue(target, execution);
        if (targetVarName == null) {
            throw new WSO2TaskException("\"target\" parameter is mandatory, but found null");
        }

        String targetType = getExpressionStrValue(type, execution);
        if (targetType == null) {
            // type is optional
            targetType = "string";
        }

        if (ORIGIN_TYPE_REGISTRY.equalsIgnoreCase(originStr)) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Registry resource Path : " + resourceName);
            }
            Resource resource = RegistryUtil.getRegistryResource(resourceName, Integer.parseInt(execution.getTenantId()));
            if (resource != null) {
                String regContent = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                execution.setVariable(targetVarName, typeConvert(regContent, targetType));
            }
        } else if (ORIGIN_TYPE_ENV.equalsIgnoreCase(getExpressionStrValue(origin, execution))) {
            execution.setVariable(targetVarName, typeConvert(System.getenv(resourceName), targetType));
        } else if (ORIGIN_TYPE_SYS.equalsIgnoreCase(getExpressionStrValue(origin, execution))) {
            execution.setVariable(targetVarName, typeConvert(System.getProperty(resourceName), targetType));
        } else {
            throw new WSO2TaskException("Unknown origin : " + originStr + ". Only support : REGISTRY, ENVIRONMENT, " +
                    "SYSTEM");
        }
    }

    public Expression getOrigin() {

        return origin;
    }

    public void setOrigin(Expression origin) {

        this.origin = origin;
    }

    public Expression getResource() {

        return resource;
    }

    public void setResource(Expression resource) {

        this.resource = resource;
    }

    public Expression getTarget() {

        return target;
    }

    public void setTarget(Expression target) {

        this.target = target;
    }

    public Expression getType() {

        return type;
    }

    public void setType(Expression type) {

        this.type = type;
    }

    /**
     * Function to retrieve value by evaluating the expression
     * @param expression
     * @param execution
     * @return return string value after evaluating the expression, return null if expression returns null or empty
     * string
     */
    private String getExpressionStrValue(Expression expression, DelegateExecution execution) {
        if (expression != null) {
            Object object = expression.getValue(execution);
            if (object != null && StringUtils.isNotEmpty(object.toString())) {
                return object.toString();
            }
        }
        return null;
    }

    /**
     * Function to convert given string to given data type
     *
     * @param str string containing the value
     * @param type data type name to convert
     * @return converted object
     * @throws WSO2TaskException
     */
    private Object typeConvert(String str, String type) throws WSO2TaskException {
        if (DATA_TYPE_STRING.equals(type)) {
            return str;
        } else if (DATA_TYPE_BOOLEAN.equals(type)) {
            return Boolean.valueOf(str);
        } else if (DATA_TYPE_INTEGER.equals(type)) {
            return new Integer(str);
        } else if (DATA_TYPE_JSON.equals(type)) {
            try {
                return JSONUtils.parse(str);
            } catch (IOException e) {
                throw new WSO2TaskException("Error occurred while parsing resource value to target data type : json", e);
            }
        } else if (DATA_TYPE_XML.equals(type)) {
            try {
                return Utils.parse(str);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new WSO2TaskException("Error occurred while parsing resource value to target data type : xml", e);
            }
        } else {
            throw new WSO2TaskException("Unknown target data type : " + type);
        }
    }
}

/**
 *  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.engine.impl.scripting.BeansResolverFactory;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.VariableScopeResolverFactory;
import org.activiti.engine.impl.variable.BooleanType;
import org.activiti.engine.impl.variable.ByteArrayType;
import org.activiti.engine.impl.variable.CustomObjectType;
import org.activiti.engine.impl.variable.DateType;
import org.activiti.engine.impl.variable.DefaultVariableTypes;
import org.activiti.engine.impl.variable.DoubleType;
import org.activiti.engine.impl.variable.IntegerType;
import org.activiti.engine.impl.variable.JsonType;
import org.activiti.engine.impl.variable.LongJsonType;
import org.activiti.engine.impl.variable.LongStringType;
import org.activiti.engine.impl.variable.LongType;
import org.activiti.engine.impl.variable.NullType;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.ShortType;
import org.activiti.engine.impl.variable.StringType;
import org.activiti.engine.impl.variable.UUIDType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.integration.BPSGroupManagerFactory;
import org.wso2.carbon.bpmn.core.integration.BPSUserManagerFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.json.ExtendedJsonType;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.XmlAPIResolverFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.XmlType;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for building and initiating the activiti engine
 *
 */
public class ActivitiEngineBuilder {

	private static final Log log = LogFactory.getLog(ActivitiEngineBuilder.class);

	private String dataSourceJndiName = null;
    private static ProcessEngine processEngine = null;
    protected ObjectMapper objectMapper = new ObjectMapper();//Object mapper for JsonType,ExtendedJsonType,LongJsonType

	 /* Instantiates the engine. Builds the state of the engine
	 *
	 * @return  ProcessEngineImpl object
	 * @throws BPSFault  Throws in the event of failure of ProcessEngine
	 */

    public ProcessEngine buildEngine() throws BPSFault {

        try {
            String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
            String activitiConfigPath = carbonConfigDirPath + File.separator +
                    BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
            File activitiConfigFile = new File(activitiConfigPath);
            ProcessEngineConfigurationImpl processEngineConfigurationImpl =
                    (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromInputStream(
                            new FileInputStream(
                                    activitiConfigFile));

            //Add script engine resolvers
            setResolverFactories(processEngineConfigurationImpl);
            //Add supported variable types
            setSupportedVariableTypes(processEngineConfigurationImpl);

            // we have to build the process engine first to initialize session factories.
            processEngine = processEngineConfigurationImpl.buildProcessEngine();
            processEngineConfigurationImpl.getSessionFactories().put(UserIdentityManager.class,
                    new BPSUserManagerFactory());
            processEngineConfigurationImpl.getSessionFactories().put(GroupIdentityManager.class,
                    new BPSGroupManagerFactory());

            dataSourceJndiName = processEngineConfigurationImpl.getProcessEngineConfiguration()
                    .getDataSourceJndiName();

        } catch (FileNotFoundException e) {
            String msg = "Failed to create an Activiti engine. Activiti configuration file not found";
            throw new BPSFault(msg, e);
        }
        return processEngine;
    }

    public String getDataSourceJndiName() {
        return dataSourceJndiName;
    }

    public static ProcessEngine getProcessEngine(){
        return ActivitiEngineBuilder.processEngine;
    }

    /**
     * Function to set supported variable types
     * @param processEngineConfiguration
     */
    private void setSupportedVariableTypes (ProcessEngineConfigurationImpl processEngineConfiguration) {
        VariableTypes variableTypes = new DefaultVariableTypes();

        List<VariableType> customPreVariableTypes = processEngineConfiguration.getCustomPreVariableTypes();
        if (customPreVariableTypes!=null) {
            for (VariableType customVariableType: customPreVariableTypes) {
                variableTypes.addType(customVariableType);
            }
        }

        //Default types in Activiti
        variableTypes.addType(new NullType());
        variableTypes.addType(new StringType(ProcessEngineConfigurationImpl.DEFAULT_ORACLE_MAX_LENGTH_STRING));
        variableTypes.addType(new LongStringType(ProcessEngineConfigurationImpl.DEFAULT_ORACLE_MAX_LENGTH_STRING + 1));
        variableTypes.addType(new BooleanType());
        variableTypes.addType(new ShortType());
        variableTypes.addType(new IntegerType());
        variableTypes.addType(new LongType());
        variableTypes.addType(new DateType());
        variableTypes.addType(new DoubleType());
        variableTypes.addType(new UUIDType());;
        variableTypes.addType(new JsonType(ProcessEngineConfigurationImpl.DEFAULT_ORACLE_MAX_LENGTH_STRING, objectMapper));
        variableTypes.addType(new LongJsonType(ProcessEngineConfigurationImpl.DEFAULT_ORACLE_MAX_LENGTH_STRING + 1, objectMapper));
        variableTypes.addType(new ByteArrayType());
        variableTypes.addType(new SerializableType());
        variableTypes.addType(new CustomObjectType("item", ItemInstance.class));
        variableTypes.addType(new CustomObjectType("message", MessageInstance.class));

        //types added for WSO2 BPS
        variableTypes.addType(new ExtendedJsonType(ProcessEngineConfigurationImpl.DEFAULT_ORACLE_MAX_LENGTH_STRING, objectMapper));
        variableTypes.addType(new XmlType());

        List<VariableType> customPostVariableTypes =processEngineConfiguration.getCustomPostVariableTypes();
        if (customPostVariableTypes != null) {
            for (VariableType customVariableType: customPostVariableTypes) {
                variableTypes.addType(customVariableType);
            }
        }

        processEngineConfiguration.setVariableTypes(variableTypes);
    }

    /**
     * Function to register resolver factories that used by script engines and JUEL
     * @param processEngineConfiguration
     */
    private void setResolverFactories (ProcessEngineConfigurationImpl processEngineConfiguration) {
        List <ResolverFactory> resolverFactories = new ArrayList<>();
        //Resolvers from Activiti
        resolverFactories.add(new VariableScopeResolverFactory());
        resolverFactories.add(new BeansResolverFactory());
        //Resolvers added for WSO2 BPS
        resolverFactories.add(new XmlAPIResolverFactory());
        processEngineConfiguration.setResolverFactories(resolverFactories);
    }
}

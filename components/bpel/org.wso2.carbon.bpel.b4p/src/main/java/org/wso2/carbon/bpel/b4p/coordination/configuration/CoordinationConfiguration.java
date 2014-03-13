/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.b4p.coordination.configuration;

import java.io.*;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.wso2.carbon.bpel.b4p.coordination.config.*;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

public class CoordinationConfiguration {

    private static Log log = LogFactory.getLog(CoordinationConfiguration.class);

    private static CoordinationConfiguration coordinationConfiguration = null;


    public static final String HUMAN_TASK_COORDINATION_CONFIG_FILE = "b4p-coordination-config.xml";
    public static final String PROTOCOL_HANDLER_USERNAME_ALIAS = "HumanTask.ProtocolHandler.Username";
    public static final String PROTOCOL_HANDLER_PASSWORD_ALIAS = "HumanTask.ProtocolHandler.Password";


    private boolean humantaskCoordinationEnabled = false;

    private boolean registrationServiceEnabled = false;

    // HumanTask engine's protocol handler related configs
    private String protocolHandlerAdminUser;
    private String protocolHandlerAdminPassword;

    //Persistence related configs
    private boolean showSQL = false;
    private boolean generateDdl = false;
    private String daoConnectionFactoryClass;

    private boolean clusteredTaskEngines = false;
    private String loadBalancerURL;

    private CoordinationConfiguration() {

        File htCoordinationConfigFile = getHumanTaskCoordinationConfigurationFile();

        HumanTaskCoordinationConfigurationDocument humanTaskCoordinationConfigurationDocument = readConfigFile(htCoordinationConfigFile);

        if (humanTaskCoordinationConfigurationDocument == null
                || humanTaskCoordinationConfigurationDocument.getHumanTaskCoordinationConfiguration() == null) {
            log.info("HumanTask Coordination disabled !");
            this.humantaskCoordinationEnabled = false;
            return;
        }

        // Reading values
        THtCoordinationConfig tHtCoordinationConfig = humanTaskCoordinationConfigurationDocument.getHumanTaskCoordinationConfiguration();

        this.humantaskCoordinationEnabled = tHtCoordinationConfig.getTaskCoordinationEnabled();
        this.registrationServiceEnabled = tHtCoordinationConfig.getRegistrationServiceEnabled();
        if (tHtCoordinationConfig.getTaskProtocolHandlerAuthentication() != null) {
            // Reading secured password
            getAuthenticationConfig(htCoordinationConfigFile, tHtCoordinationConfig.getTaskProtocolHandlerAuthentication());
        } else {
            log.warn("Error occurred while retrieving TaskEngineProtocolHandler configuration. ");
        }

        if (tHtCoordinationConfig.getPersistenceConfig() != null) {
            parsePersistenceConfig(tHtCoordinationConfig.getPersistenceConfig());
        }

        if (tHtCoordinationConfig.getClusteredTaskEngines() != null) {
            this.clusteredTaskEngines = true;
            parseClusterData(tHtCoordinationConfig.getClusteredTaskEngines());
        }
    }


    /**
     * Get protocol handler admin username and password from secure vault. If secure vault not set then
     * parse authentication configuration and extract protocol handler admin username and password
     *
     * @param file
     * @param authentication
     */
    private void getAuthenticationConfig(File file, TTaskAuthenticationConfig authentication) {
        //Since secretResolver only accept Element we have to build Element here.
        SecretResolver secretResolver = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            StAXOMBuilder builder = new StAXOMBuilder(in);
            secretResolver = SecretResolverFactory.create(builder.getDocumentElement(), true);
        } catch (Exception e) {
            log.warn("Error occurred while retrieving secured TaskEngineProtocolHandler configuration.", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        // Get Username
        if (secretResolver != null && secretResolver.isInitialized()
                && secretResolver.isTokenProtected(PROTOCOL_HANDLER_USERNAME_ALIAS)) {
            protocolHandlerAdminUser = secretResolver.resolve(PROTOCOL_HANDLER_USERNAME_ALIAS);
            if (log.isDebugEnabled()) {
                log.debug("Loaded TaskEngine's protocol handler username from secure vault");
            }
        } else {
            if (authentication.getUsername() != null) {
                this.protocolHandlerAdminUser = authentication.getUsername();
            }
        }
        // Get Password
        if (secretResolver != null && secretResolver.isInitialized()
                && secretResolver.isTokenProtected(PROTOCOL_HANDLER_PASSWORD_ALIAS)) {
            protocolHandlerAdminPassword = secretResolver.resolve(PROTOCOL_HANDLER_PASSWORD_ALIAS);
            if (log.isDebugEnabled()) {
                log.debug("Loaded TaskEngine's protocol handler password from secure vault");
            }
        } else {
            if (authentication.getPassword() != null) {
                this.protocolHandlerAdminPassword = authentication.getPassword();
            }
        }
    }

    private void parsePersistenceConfig(TPersistenceConfig persistenceConfig) {
        this.generateDdl = persistenceConfig.getGenerateDdl();
        this.showSQL = persistenceConfig.getShowSql();

        if (persistenceConfig.getDAOConnectionFactoryClass() != null) {
            this.daoConnectionFactoryClass = persistenceConfig.getDAOConnectionFactoryClass();
        }
    }

    private void parseClusterData(TClusterConfig clusterConfig) {
        if (clusterConfig.getLoadBalancerURL() != null) {
            this.loadBalancerURL = clusterConfig.getLoadBalancerURL();
        } else {
            this.clusteredTaskEngines = false;
        }
    }

    public static CoordinationConfiguration getInstance() {
        if (coordinationConfiguration == null) {
            coordinationConfiguration = new CoordinationConfiguration();
        }
        return coordinationConfiguration;
    }

    private HumanTaskCoordinationConfigurationDocument readConfigFile(File aFile) {
        try {
            return HumanTaskCoordinationConfigurationDocument.Factory.parse(new FileInputStream(aFile));
        } catch (XmlException e) {
            log.error("Error Parsing HumanTask Coordination configuration File.", e);
        } catch (FileNotFoundException e) {
            log.warn("Cannot find HumanTask Coordination configuration in location " + aFile.getPath() + ".");
        } catch (IOException e) {
            log.warn("Error while reading HumanTask coordination configuration file.");
        }

        return null;
    }

    /**
     * Calculate htCoordination-config.xml path and returns config file.
     *
     * @return HT-coordination config file
     */
    private File getHumanTaskCoordinationConfigurationFile() {
        return new File(CarbonUtils.getCarbonConfigDirPath() + File.separator + HUMAN_TASK_COORDINATION_CONFIG_FILE);
    }

    public boolean isHumantaskCoordinationEnabled() {
        return humantaskCoordinationEnabled;
    }

    public boolean isRegistrationServiceEnabled() {
        return registrationServiceEnabled;
    }

    public String getProtocolHandlerAdminUser() {
        return protocolHandlerAdminUser;
    }

    public String getProtocolHandlerAdminPassword() {
        return protocolHandlerAdminPassword;
    }

    public boolean isShowSQL() {
        return showSQL;
    }

    public boolean isGenerateDdl() {
        return generateDdl;
    }

    public String getDaoConnectionFactoryClass() {
        return daoConnectionFactoryClass;
    }

    public boolean isClusteredTaskEngines() {
        return clusteredTaskEngines;
    }

    public String getLoadBalancerURL() {
        return loadBalancerURL;
    }
}

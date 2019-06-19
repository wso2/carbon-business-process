/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.analytics.publisher.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisherConstants;
import org.wso2.carbon.bps.common.analytics.config.BPSAnalyticsDocument;
import org.wso2.carbon.bps.common.analytics.config.TAnalyticServer;
import org.wso2.carbon.bps.common.analytics.config.TBPMN;
import org.wso2.carbon.bps.common.analytics.config.TBPSAnalytics;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.namespace.QName;

/**
 * The memory model of the BPS analytics configuration - bps-analytics.xml.
 */
public class BPSAnalyticsConfiguration {

    private static final Log log = LogFactory.getLog(BPSAnalyticsConfiguration.class);
    private BPSAnalyticsDocument bpsAnalyticsDocument;

    /* Configurations related to analytics server */
    private String analyticsReceiverURLSet;
    private String analyticsAuthURLSet;
    private String analyticsServerUsername;
    private String analyticsServerPassword;

    /* Configurations related to bpmn analytics */
    private String bpmnAnalyticsPublisherType;
    private boolean bpmnDataPublishingEnabled;
    private boolean bpmnKPIDataPublishingEnabled;
    private boolean bpmnAsyncDataPublishingEnabled;

    /**
     * Create BPS Analytics Configuration from a configuration file. If error occurred while parsing configuration
     * file, default configuration will be created.
     *
     * @param BPSAnalyticsConfig XMLBeans object of BPS Analytics configuration file
     */
    public BPSAnalyticsConfiguration(File BPSAnalyticsConfig) {
        bpsAnalyticsDocument = readConfigurationFromFile(BPSAnalyticsConfig);

        if (bpsAnalyticsDocument == null) {
            return;
        }
        initConfigurationFromFile(BPSAnalyticsConfig);
    }

    // Getters retrieve BPS analytics configuration elements
    public static Log getLog() {
        return log;
    }

    /**
     * Parse BPS analytics configuration file - bps-analytics.xml and read configurations
     *
     * @param BPSAnalyticsConfigurationFile
     * @return
     */
    private BPSAnalyticsDocument readConfigurationFromFile(File BPSAnalyticsConfigurationFile) {
        try {
            return BPSAnalyticsDocument.Factory.parse(new FileInputStream(BPSAnalyticsConfigurationFile));
        } catch (XmlException e) {
            log.error("Error parsing BPS analytics configuration.", e);
        } catch (FileNotFoundException e) {
            log.info("Cannot find the BPS analytics configuration in specified location "
                    + BPSAnalyticsConfigurationFile.getPath() + " . Loads the default configuration.");
        } catch (IOException e) {
            log.error("Error reading BPS analytics configuration file" + BPSAnalyticsConfigurationFile.getPath());
        }
        return null;
    }

    /**
     * Initialize the configuration object from the properties in the BPS Analytics config xml file.
     */
    private void initConfigurationFromFile(File BPSAnalyticsConfigurationFile) {


        SecretResolver secretResolver = null;
        OMElement analyticPassword = null;
        try (InputStream in = new FileInputStream(BPSAnalyticsConfigurationFile);) {
            StAXOMBuilder builder = new StAXOMBuilder(in);
            OMElement rootElement = builder.getDocumentElement();
            secretResolver = SecretResolverFactory.create(rootElement, true);
            OMElement analyticServer = rootElement.getFirstChildWithName(new
                                        QName(AnalyticsPublisherConstants.BPS_ANALYTIC_SERVER_KEY));
            if (analyticServer != null) {
                analyticPassword = analyticServer.getFirstChildWithName(new
                                    QName(AnalyticsPublisherConstants.BPS_ANALYTIC_PASSWORD_KEY));
            }
        } catch (Exception e) {
            log.warn("Error occurred while retrieving secured BPS Analytics configuration.", e);
        }
        TBPSAnalytics tBPSAnalytics = bpsAnalyticsDocument.getBPSAnalytics();
        if (tBPSAnalytics == null) {
            return;
        }
        if (tBPSAnalytics.getAnalyticServer() != null) {
            initAnalytics(secretResolver, tBPSAnalytics.getAnalyticServer(), analyticPassword);
        }
        if (tBPSAnalytics.getBPMN() != null) {
            initBPMNAnalytics(tBPSAnalytics.getBPMN());
        }
    }


    /**
     * Initialize analytics common configurations
     *
     * @param tAnalyticServer
     * @param secretResolver
     */
    private void initAnalytics(SecretResolver secretResolver, TAnalyticServer tAnalyticServer,
                               OMElement analyticPassword) {

        // Get Auth URL Set
        this.analyticsAuthURLSet = tAnalyticServer.getAuthURLSet();

        // Get Receiver URL Set
        this.analyticsReceiverURLSet = tAnalyticServer.getReceiverURLSet();

        // Get Username
        this.analyticsServerUsername = tAnalyticServer.getUsername();
        // Get Password
        if (secretResolver != null && secretResolver.isInitialized() && analyticPassword != null) {
            this.analyticsServerPassword = MiscellaneousUtil.resolve(analyticPassword, secretResolver);
            if (log.isDebugEnabled()) {
                log.debug("Loaded analytics password from secure vault");
            }
        } else {
            if (tAnalyticServer.getPassword() != null) {
                this.analyticsServerPassword = tAnalyticServer.getPassword();
            }
        }

    }

    /**
     * Initialize bpmn analytics configurations
     *
     * @param tbpmn
     */
    private void initBPMNAnalytics(TBPMN tbpmn) {

        // Get Enabled configurations
        this.bpmnAnalyticsPublisherType = tbpmn.getPublisherType();
        this.bpmnDataPublishingEnabled = tbpmn.getDataPublishingEnabled();
        this.bpmnKPIDataPublishingEnabled = tbpmn.getKPIDataPublishingEnabled();
        this.bpmnAsyncDataPublishingEnabled = tbpmn.getAsyncDataPublishingEnabled();

    }

    public String getAnalyticsServerPassword() {
        return analyticsServerPassword;
    }

    public String getAnalyticsServerUsername() {
        return analyticsServerUsername;
    }

    public String getAnalyticsAuthURLSet() {
        return analyticsAuthURLSet;
    }

    public String getAnalyticsReceiverURLSet() {
        return analyticsReceiverURLSet;
    }

    public String getBpmnAnalyticsPublisherType() {
        return bpmnAnalyticsPublisherType;
    }

    public boolean isBpmnAsyncDataPublishingEnabled() {
        return bpmnAsyncDataPublishingEnabled;
    }

    public boolean isBpmnKPIDataPublishingEnabled() {
        return bpmnKPIDataPublishingEnabled;
    }

    public boolean isBpmnDataPublishingEnabled() {
        return bpmnDataPublishingEnabled;
    }
}

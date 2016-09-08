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
package org.wso2.carbon.bpmn.analytics.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.config.BPSAnalyticsConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * This Class represents the BPS Analytics and initialize configuration
 * and runtime environments
 * This will describe the OSGi service interface where others can used to interact with
 * BPS Analytics implementation.
 */
public class BPSAnalyticsServer {

    private static final Log log = LogFactory.getLog(BPSAnalyticsServer.class);

    /**
     * The BPS Analytics configurations
     */
    private BPSAnalyticsConfiguration bpsAnalyticsConfiguration;


    /**
     * Initialize BPS Analytics
     *
     * @throws BPMNDataPublisherException
     */
    public void init() throws BPMNDataPublisherException{
        loadBPSAnalyticsConfiguration();;
    }

    /**
     * Read the BPS Analytics configuration file and load it to memory. If configuration file is not there default
     * configuration will be created.
     */
    private void loadBPSAnalyticsConfiguration() {
        if (log.isDebugEnabled()) {
            log.debug("Loading BPS Analytics Configuration.");
        }
        if (isBPSAnalyticsConfigurationFileAvailable()) {
            File BPSAnalyticsConfigFile = new File(getBPSAnalyticsConfigurationFilePath());
            bpsAnalyticsConfiguration = new BPSAnalyticsConfiguration(BPSAnalyticsConfigFile);
        } else {
            log.warn("BPS Analytics configuration file: " +AnalyticsPublisherConstants.BPS_ANALYTICS_CONFIGURATION_FILE_NAME +
                    " not found. Loading default configurations.");
        }
    }


    /**
     * @return : true is the configuration file is in the file system false otherwise.
     */
    private boolean isBPSAnalyticsConfigurationFileAvailable() {
        File BPSAnalyticsConfigurationFile = new File(getBPSAnalyticsConfigurationFilePath());
        return BPSAnalyticsConfigurationFile.exists();
    }

    /**
     * @return BPS Analytics configuration path.
     */
    private String getBPSAnalyticsConfigurationFilePath() {
        return CarbonUtils.getCarbonConfigDirPath() + File.separator +
                AnalyticsPublisherConstants.BPS_ANALYTICS_CONFIGURATION_FILE_NAME;
    }

    /**
     * Get BPS Analytics configuration
     *
     * @return BPS Analytics configuration
     */
    public BPSAnalyticsConfiguration getBPSAnalyticsConfiguration() {
        return bpsAnalyticsConfiguration;
    }

}

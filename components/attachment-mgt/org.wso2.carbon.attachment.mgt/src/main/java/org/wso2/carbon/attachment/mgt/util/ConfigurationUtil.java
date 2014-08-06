/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class bridge the configurations related to Attachment-Mgt in file-system to run-time.
 */
public class ConfigurationUtil {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(ConfigurationUtil.class);

    /**
     * Returns the value for a given property key
     * @param propertyKey
     * @return propertyValue
     */
    public static String getConfigValue(File propertyFileLocation, String propertyKey) {
        String value = "Error:VALUE_IS_NOT_DEFINED";
        try {
            Properties prop = getPropertyMap(propertyFileLocation);

            value = prop.getProperty(propertyKey);
            if (value != null) {
                value = value.trim();
            }
        } catch (IOException ex) {
            log.error("Failed to load property value of " + propertyKey, ex);
        }
        return value;
    }

    /**
     * Returns property map for the given file location
     * @param propertyFileLocation
     * @return
     * @throws IOException
     */
    public static Properties getPropertyMap (File propertyFileLocation) throws IOException {
        File configFile = propertyFileLocation;
        try {

            Properties prop = new Properties();
            prop.load(new FileInputStream(configFile));
            return prop;
        } catch (IOException ex) {
            log.error("Failed to load property file:" + configFile.getAbsolutePath(), ex);
            throw ex;
        }
    }
}

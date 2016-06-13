/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/


package org.wso2.carbon.bpmn.tests.osgi.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DeploymentUtil {

    public static final String DEPLOYMET_HOME = "deployment";
    public static final String DEPLOYMET_BPMN_DIRECTORY = "deployment";

    public static void copyBPMNArtifact(File barFile) throws IOException {
        Path deploymentDirectory = BasicServerConfigurationUtil.getCarbonHome().resolve(DEPLOYMET_HOME).resolve
                (DEPLOYMET_BPMN_DIRECTORY);
        Files.copy(barFile.toPath(), deploymentDirectory, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void removeBPMNArtifacts(String barFile) throws IOException {
        Path deploymentArtifact = Paths.get(BasicServerConfigurationUtil.getCarbonHome().toString(), DEPLOYMET_HOME,
                DEPLOYMET_BPMN_DIRECTORY, barFile);
        Files.delete(deploymentArtifact);
    }
}

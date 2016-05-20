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

import org.ops4j.pax.exam.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Utility Class for generating basic BPMN server configuration.
 */
public class BasicServerConfigurationUtil {

    /**
     * Creates Basic Configuration required for BPMN Server.
     *
     * @return Basic Option List
     */
    public static List<Option> createBasicConfiguration() {

        List<Option> optionList = new ArrayList<>();
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.org.activiti")
                .artifactId("activiti-all")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.carbon.business-process")
                .artifactId("org.wso2.carbon.bpmn")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.carbon.datasources")
                .artifactId("org.wso2.carbon.datasource.core")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.carbon.jndi")
                .artifactId("org.wso2.carbon.jndi")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.carbon.messaging")
                .artifactId("org.wso2.carbon.messaging")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.carbon.security.caas")
                .artifactId("org.wso2.carbon.security.caas")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.carbon.security.userstore")
                .artifactId("org.wso2.carbon.security.userstore.jdbc")
                .versionAsInProject());
        // Activiti Dependencies.
        optionList.addAll(createActivitiDependenciesConfiguration());
        // DataSource dependencies.
        optionList.add(mavenBundle()
                .groupId("com.zaxxer")
                .artifactId("HikariCP")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("com.h2database")
                .artifactId("h2")
                .versionAsInProject());
        return optionList;
    }

    public static List<Option> createActivitiDependenciesConfiguration() {
        List<Option> optionList = new ArrayList<>();
        optionList.add(mavenBundle()
                .groupId("org.mybatis")
                .artifactId("mybatis")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.org.springframework.ws")
                .artifactId("spring.framework")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.apache.geronimo.specs")
                .artifactId("geronimo-jpa_2.0_spec")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.com.fasterxml.jackson.core")
                .artifactId("jackson-core")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.com.fasterxml.jackson.core")
                .artifactId("jackson-annotations")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.com.fasterxml.jackson.core")
                .artifactId("jackson-databind")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.com.fasterxml.jackson.module")
                .artifactId("jackson-module-jaxb-annotations")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.org.tinyjee.jgraphx")
                .artifactId("jgraphx")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("joda-time")
                .artifactId("joda-time")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("com.jayway.jsonpath.wso2")
                .artifactId("json-path")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("net.minidev")
                .artifactId("json-smart")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.eclipse.equinox")
                .artifactId("javax.servlet")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.wso2.orbit.org.yaml")
                .artifactId("snakeyaml")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("org.apache.commons")
                .artifactId("commons-lang3")
                .versionAsInProject());
        optionList.add(mavenBundle()
                .groupId("commons-io.wso2")
                .artifactId("commons-io")
                .versionAsInProject());
        return optionList;
    }

    /**
     * Return Carbon Home for Test Server.
     *
     * @return
     */
    public static Path getCarbonHome() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");
        return carbonHome;
    }

}

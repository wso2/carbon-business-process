/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.deployment;

import org.wso2.carbon.humantask.HumanInteractionsDocument;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.deployment.config.HTDeploymentConfigDocument;

import javax.wsdl.Definition;
import java.io.File;
import java.util.List;

/**
 * The builder class for HumanTaskDeploymentUnit.
 */
public abstract class HumanTaskDeploymentUnitBuilder {

    /** The humantask deployment unit object */
    private HumanTaskDeploymentUnit htDeploymentUnit;


    /**
     * Creates a new HumanTaskDeploymentUnit object.
     * @return : The created HumanTaskDeploymentUnit object.
     *
     * @throws HumanTaskDeploymentException :
     */
    public HumanTaskDeploymentUnit createNewHumanTaskDeploymentUnit()
            throws HumanTaskDeploymentException {
        if (isValidHumanTaskDeploymentUnit()) {
            throw new IllegalStateException("Please build the Human Task Deployment unit completely.");
        }
        htDeploymentUnit = new HumanTaskDeploymentUnit();
        htDeploymentUnit.setHumanInteractionsDefinition(getHumanInteractionsDocument());
        htDeploymentUnit.setDeploymentConfiguration(getHTDeploymentConfigDocument());
        htDeploymentUnit.setWSDLs(getWsdlDefinitions());
        htDeploymentUnit.setName(getArchiveName() + "-" + getVersion());
        htDeploymentUnit.setHumanTaskDefinitionFile(getHumanTaskDefinitionFile());
        htDeploymentUnit.setPackageName(getArchiveName());
        htDeploymentUnit.setVersion(getVersion());
        htDeploymentUnit.setMd5sum(getMd5sum());
        htDeploymentUnit.setTaskPackageStatus(TaskPackageStatus.ACTIVE);
        return htDeploymentUnit;
    }

    // validates the deployment unit against the artifacts in it.
    private boolean isValidHumanTaskDeploymentUnit() {
        return htDeploymentUnit != null &&
                htDeploymentUnit.getHumanInteractionsDefinition() != null &&
                htDeploymentUnit.getWSDLs() != null &&
                htDeploymentUnit.getName() != null &&
                htDeploymentUnit.getDeploymentConfiguration() != null;
    }

    public abstract void buildHumanInteractionDocuments() throws HumanTaskDeploymentException;

    /**
     * Builds the human task deployment configuration object in the unit.
     * @throws HumanTaskDeploymentException :
     */
    public abstract void buildDeploymentConfiguration() throws HumanTaskDeploymentException;

    /**
     * Reads the wsdl files in the deployment unit and put in to a map.
     *
     * @throws HumanTaskDeploymentException :
     */
    public abstract void buildWSDLs() throws HumanTaskDeploymentException;

    /**
     * Reads the xsd files in the deployment unit and put into a map.
     *
     * @throws HumanTaskDeploymentException :
     */
    public abstract void buildSchemas() throws HumanTaskDeploymentException;

    /**
     * @return : The human interaction document.
     *
     * @throws HumanTaskDeploymentException :
     */
    public abstract HumanInteractionsDocument getHumanInteractionsDocument()
            throws HumanTaskDeploymentException;

    /**
     * @return : The deployment configuration file.
     *
     * @throws HumanTaskDeploymentException :
     */
    public abstract HTDeploymentConfigDocument getHTDeploymentConfigDocument()
            throws HumanTaskDeploymentException;

    /**
     * @return : The archive name of the deployment unit.
     */
    public abstract String getArchiveName();

    /**
     * Get the human task definition file of the package.
     *
     * @return : The File object corresponding to the human task definition file in the archive.
     */
    public abstract File getHumanTaskDefinitionFile();

    /**
     * Get a list of Definition objects out of the wsdl files corresponding to the human task archive.
     *
     * @return : The list of Definition objects related to the wsdl files in the package.
     * @throws HumanTaskDeploymentException :
     */
    public abstract List<Definition> getWsdlDefinitions() throws HumanTaskDeploymentException;

    /**
     * @return the version of this deployment unit
     */
    public abstract long getVersion();

    /**
     * @return returns the md5sum for this archive
     */
    public abstract String getMd5sum();


}

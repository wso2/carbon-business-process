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
package org.wso2.carbon.bpmn.extensions.registry.impl;

import org.wso2.carbon.bpmn.extensions.registry.RegistryException;
import org.wso2.carbon.bpmn.extensions.registry.Resource;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This class contains resource implementation for carbon registry
 */
public class CarbonResource implements Resource {

    private org.wso2.carbon.registry.api.Resource resource;

    public CarbonResource(org.wso2.carbon.registry.api.Resource resource) {
        this.resource = resource;
    }

    public CarbonResource() {
    }

    public org.wso2.carbon.registry.api.Resource getResource() {
        return resource;
    }

    public void setResource(org.wso2.carbon.registry.api.Resource resource) {
        this.resource = resource;
    }


    @Override
    public String getId() {

        return resource.getId();
    }

    @Override
    public String getAuthorUserName() {

        return resource.getAuthorUserName();
    }

    @Override
    public Date getCreatedTime() {

        return resource.getCreatedTime();
    }

    @Override
    public Date getLastModified() {

        return resource.getLastModified();
    }

    @Override
    public String getDescription() {

        return resource.getDescription();
    }

    @Override
    public void setDescription(String description) {
        resource.setDescription(description);
    }

    @Override
    public String getPath() {

        return resource.getPath();
    }

    @Override
    public String getPermanentPath() {

        return resource.getPermanentPath();
    }

    @Override
    public String getMediaType() {

        return resource.getMediaType();
    }

    @Override
    public int getState() {

        return resource.getState();
    }

    @Override
    public void setMediaType(String type) {
        resource.setMediaType(type);
    }

    @Override
    public String getParentPath() {

        return resource.getParentPath();
    }

    @Override
    public String getProperty(String propertyName) {

        return resource.getProperty(propertyName);
    }

    @Override
    public List<String> getPropertyValues(String propertyName) {

        return resource.getPropertyValues(propertyName);
    }

    @Override
    public Properties getProperties() {

        return resource.getProperties();
    }

    @Override
    public void setProperty(String key, String value) {
        resource.setProperty(key, value);
    }

    @Override
    public void setProperty(String key, List<String> value) {
        resource.setProperty(key, value);
    }

    @Override
    public void addProperty(String key, String value) {
        resource.addProperty(key, value);
    }

    @Override
    public void setProperties(Properties properties) {
        resource.setProperties(properties);
    }

    @Override
    public void editPropertyValue(String key, String oldValue, String newValue) {
        resource.editPropertyValue(key, oldValue, newValue);
    }

    @Override
    public void removeProperty(String key) {
        resource.removeProperty(key);
    }

    @Override
    public void removePropertyValue(String key, String value) {
        resource.removePropertyValue(key, value);
    }

    @Override
    public Object getContent() throws RegistryException {

        try {
            return resource.getContent();
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new RegistryException(e);
        }
    }

    @Override
    public void setContent(Object content) throws RegistryException {

        try {
            resource.setContent(content);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new RegistryException(e);
        }
    }

    @Override
    public String getLastUpdaterUserName() {

        return resource.getLastUpdaterUserName();
    }

    @Override
    public InputStream getContentStream() throws RegistryException {

        try {
            return resource.getContentStream();
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new RegistryException(e);
        }
    }

    @Override
    public void setContentStream(InputStream contentStream) throws RegistryException {

        try {
            resource.setContentStream(contentStream);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new RegistryException(e);
        }
    }

    @Override
    public void discard() {

    }

}

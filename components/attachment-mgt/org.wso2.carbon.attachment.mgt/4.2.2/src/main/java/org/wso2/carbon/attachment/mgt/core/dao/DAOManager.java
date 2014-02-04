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

package org.wso2.carbon.attachment.mgt.core.dao;

import org.wso2.carbon.attachment.mgt.configuration.AttachmentServerConfiguration;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;

/**
 * Primary interface for the Data-Access-Object managers
 */
public interface DAOManager {

    /**
     * One time initialization if it's not already happens
     */
    public void init(AttachmentServerConfiguration serverConfig);

    /**
     * DAO Manager maintains a DAOConnectionFactory ({@link AttachmentMgtDAOConnectionFactory}
     * where the connections to the JPA based api is managed)
     * @return reference for the DAOConnectionFactory
     * @throws AttachmentMgtException if DAOConnectionFactory is not properly initialized at the moment
     */
    public AttachmentMgtDAOConnectionFactory getDAOConnectionFactory() throws
                                                                       AttachmentMgtException;

    /**
     * DAO-Manager needs to maintain a TransformerFactory which handles the transformation
     * between DAO objects at {@link org.wso2.carbon.attachment.mgt.core.dao} to API objects at
     * {@link org.wso2.carbon.attachment.mgt.api}
     *
     * @return TransformerFactory implementation class
     * @throws AttachmentMgtException
     */
    public AttachmentMgtDAOTransformerFactory getDAOTransformerFactory() throws
                                                                       AttachmentMgtException;

    /**
     * Shutdown logic.
     */
    public void shutdown();
}

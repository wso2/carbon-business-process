/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnection;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOFactory;

import javax.persistence.EntityManager;

public class AttachmentMgtDAOConnectionImpl implements AttachmentMgtDAOConnection {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtDAOConnectionImpl.class);

    private AttachmentMgtDAOFactory daoFactory;

    /**
     * Disabled empty constructor
     */
    private AttachmentMgtDAOConnectionImpl() {
    }

    public AttachmentMgtDAOConnectionImpl(EntityManager entityManager) {
        daoFactory = new AttachmentMgtDAOFactoryImpl(entityManager);
    }

    @Override
    public AttachmentMgtDAOFactory getAttachmentMgtDAOFactory() {
        return this.daoFactory;
    }
}

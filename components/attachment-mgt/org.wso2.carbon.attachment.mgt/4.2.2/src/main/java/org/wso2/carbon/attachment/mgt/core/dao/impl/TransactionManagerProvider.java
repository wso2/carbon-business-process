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

package org.wso2.carbon.attachment.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.util.GeneralException;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * Managed Runtime provider for Attachment-Mgt Transaction manager.
 */
//TODO: At the moment this is used by OpenJPA impl. Please check the correct package location for
// this class
public class TransactionManagerProvider implements ManagedRuntime {

    /**
     * Class Logger
     */
    private static final Log log = LogFactory.getLog(TransactionManagerProvider.class);

    private TransactionManager transactionManager;

    public TransactionManagerProvider(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TransactionManager getTransactionManager() throws Exception {
        if (transactionManager != null) {
            return transactionManager;
        } else {
            String errorMsg = "transactionManager is not initialized properly.";
            NullPointerException npe = new NullPointerException(errorMsg);
            log.error(errorMsg, npe);
            throw npe;
        }
    }

    public void setRollbackOnly(Throwable cause) throws Exception {
        // there is no generic support for setting the rollback cause
        getTransactionManager().getTransaction().setRollbackOnly();
    }

    public Throwable getRollbackCause() throws Exception {
        // there is no generic support for setting the rollback cause
        return null;
    }

    public Object getTransactionKey() throws Exception, SystemException {
        return transactionManager.getTransaction();
    }

    public void doNonTransactionalWork(java.lang.Runnable runnable) throws NotSupportedException {
        TransactionManager transactionManager = null;
        Transaction transaction = null;

        try {
            transactionManager = getTransactionManager();
            transaction = transactionManager.suspend();
        } catch (Exception e) {
            NotSupportedException nse =
                    new NotSupportedException(e.getMessage());
            nse.initCause(e);
            log.error(nse.getLocalizedMessage(), nse);
            throw nse;
        }

        runnable.run();

        try {
            transactionManager.resume(transaction);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            try {
                transaction.setRollbackOnly();
            } catch (SystemException se2) {
                throw new GeneralException(se2);
            }
            NotSupportedException nse =
                    new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        }
    }
}
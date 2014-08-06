/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.util.GeneralException;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * Managed Runtime provider for Humantask Transaction manager
 */
public class TransactionManagerProvider implements ManagedRuntime {

    private TransactionManager txMgr;

    public TransactionManagerProvider(TransactionManager txMgr) {
        this.txMgr = txMgr;
    }

    public TransactionManager getTransactionManager() throws Exception {
        return txMgr;
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
        return txMgr.getTransaction();
    }

    public void doNonTransactionalWork(java.lang.Runnable runnable) throws NotSupportedException {
        TransactionManager tm;
        Transaction transaction;

        try {
            tm = getTransactionManager();
            transaction = tm.suspend();
        } catch (Exception e) {
            NotSupportedException nse = new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        }

        runnable.run();

        try {
            tm.resume(transaction);
        } catch (Exception e) {
            try {
                transaction.setRollbackOnly();
            } catch (SystemException se2) {
                throw new GeneralException(se2);
            }
            NotSupportedException nse = new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        }
    }
}

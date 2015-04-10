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
package org.wso2.carbon.humantask.core.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.*;

public class LeanTaskConfiguration extends LeanTaskBaseConfiguration {
    private static Log log = LogFactory.getLog(LeanTaskConfiguration.class);
    private String targetNameSpace = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/leantask/api/200803";
    private TPresentationElements tPresentationElements;
    private TDelegation tDelegation;
    private TPeopleAssignments tPeopleAssignments;
    private TPriorityExpr tPriorityExpr;
    private TMessageSchema tMessageSchema;
    private TDeadlines tDeadlines;
    private TRenderings tRenderings;
    private TExpression tExpression;
    private TQuery tQuery;

    private org.wso2.carbon.humantask.TLeanTask tLeanTask;

    public LeanTaskConfiguration(org.wso2.carbon.humantask.TLeanTask tLeanTask,
                                 int tenantId,
                                 String task_versionName,
                                 String leanTaskDefName,
                                 long version) {
        super(tenantId, task_versionName, leanTaskDefName, version);
        this.tLeanTask = tLeanTask;
    }

    @Override
    public TPresentationElements getPresentationElements() {
        tPresentationElements = tLeanTask.getPresentationElements();
        return tPresentationElements;
    }

    @Override
    public TDelegation getDelegation() {
        tDelegation = tLeanTask.getDelegation();
        return tDelegation;
    }

    @Override
    public TPeopleAssignments getPeopleAssignments() {
        tPeopleAssignments = tLeanTask.getPeopleAssignments();
        return tPeopleAssignments;
    }

    /*@Override
    public QName getName() {
        return new QName(targetNameSpace + tLeanTask.getName());
    }*/

    @Override
    public TPriorityExpr getPriorityExpression() {
        tPriorityExpr = tLeanTask.getPriority();
        return tPriorityExpr;
    }

    @Override
    public TMessageSchema getMessageSchema() {
        tMessageSchema = tLeanTask.getMessageSchema();
        return tMessageSchema;
    }

    @Override
    public TDeadlines getDeadlines() {
        tDeadlines = tLeanTask.getDeadlines();
        return tDeadlines;
    }

    @Override
    public TRenderings getRenderings() {
        tRenderings = tLeanTask.getRenderings();
        return tRenderings;
    }

    @Override
    public TExpression getSearchBy() {
        tExpression = tLeanTask.getSearchBy();
        return tExpression;
    }

    @Override
    public TQuery getOutcome() {
        tQuery = tLeanTask.getOutcome();
        return tQuery;
    }


}

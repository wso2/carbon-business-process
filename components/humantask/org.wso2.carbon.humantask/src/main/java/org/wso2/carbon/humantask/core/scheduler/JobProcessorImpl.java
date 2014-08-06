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

package org.wso2.carbon.humantask.core.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.humantask.*;
import org.wso2.carbon.humantask.core.api.scheduler.Scheduler;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.runtime.ExpressionEvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Implements JobProcessor for - Deadlines
 */
public class JobProcessorImpl implements Scheduler.JobProcessor {

    private static Log log = LogFactory.getLog(JobProcessorImpl.class);

    /**
     * Implements execution of the job.
     *
     * @param jobInfo the job information
     * @throws org.wso2.carbon.humantask.core.api.scheduler.Scheduler.JobProcessorException
     *
     */
    public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        log.info("Executing Deadline: " + jobInfo.getName() + " :: " + jobInfo.getTaskId() +
                " :: " + jobInfo.getJobId() + " :: " + jobInfo.getType());

        try {
            switch (jobInfo.getType()) {
                case TIMER_DEADLINE:
                    executeDeadline(jobInfo.getTaskId(), jobInfo.getName());
                    break;
                case TIMER_SUSPEND:
                    executeSuspend(jobInfo.getTaskId());
                    break;
            }
        } catch (Exception ex) {
            throw new Scheduler.JobProcessorException(ex);
        }
    }

    private void executeSuspend(long taskId) {
        log.info("ON SUSPEND: task : " + taskId);
        //TODO
    }

    private void executeDeadline(long taskId, String name) throws HumanTaskException {
        //TODO what if two deadlines fired at the same time???
        //TODO do the needful for deadlines. i.e create notifications and re-assign
        log.info("ON DEADLINE: " + " : now: " + new Date());


        TaskDAO task = HumanTaskServiceComponent.getHumanTaskServer().getDaoConnectionFactory().
                getConnection().getTask(taskId);

        // Setting the tenant id and tenant domain
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(task.getTenantId());
        String tenantDomain = null;
        try {
            tenantDomain = HumanTaskServiceComponent.getRealmService().getTenantManager().getDomain(task.getTenantId());
        } catch (UserStoreException e) {
            log.error(" Cannot find the tenant domain " + e.toString());
        }

        if(tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);





        TaskConfiguration taskConf = (TaskConfiguration) HumanTaskServiceComponent.getHumanTaskServer().
                getTaskStoreManager().getHumanTaskStore(task.getTenantId()).
                getTaskConfiguration(QName.valueOf(task.getName()));
        TDeadline deadline = taskConf.getDeadline(name);
        EvaluationContext evalCtx = new ExpressionEvaluationContext(task, taskConf);

        List<TEscalation> validEscalations = new ArrayList<TEscalation>();
        boolean reassingnmentAdded = false;
        for (TEscalation escalation : deadline.getEscalationArray()) {
            if (!escalation.isSetCondition()) {
                //We only need the first Re-assignment and we ignore all other re-assignments
                if (escalation.isSetReassignment() && !reassingnmentAdded) {
                    reassingnmentAdded = true;
                } else if (escalation.isSetReassignment()) {
                    continue;
                }
                validEscalations.add(escalation);
                continue;
            }

            if (evaluateCondition(escalation.getCondition().newCursor().getTextValue(),
                    escalation.getCondition().getExpressionLanguage() == null ?
                            taskConf.getExpressionLanguage() :
                            escalation.getCondition().
                                    getExpressionLanguage(), evalCtx)) {
                if (escalation.isSetReassignment() && !reassingnmentAdded) {
                    reassingnmentAdded = true;
                } else if (escalation.isSetReassignment()) {
                    continue;
                }
                validEscalations.add(escalation);
            }
        }

        //We may do this in the above for loop as well
        for (TEscalation escalation : validEscalations) {
            if (log.isDebugEnabled()) {
                log.debug("Escalation: " + escalation.getName());
            }
            if (escalation.isSetLocalNotification() || escalation.isSetNotification()) {
                QName qName;
                if (escalation.isSetLocalNotification()) {
                    qName = escalation.getLocalNotification().getReference();
                } else {
                    qName = new QName(taskConf.getName().getNamespaceURI(),
                            escalation.getNotification().getName());
                }

                HumanTaskBaseConfiguration notificationConfiguration = HumanTaskServiceComponent.
                        getHumanTaskServer().getTaskStoreManager().
                        getHumanTaskStore(task.getTenantId()).getActiveTaskConfiguration(qName);
                if(notificationConfiguration == null) {
                    log.error("Fatal Error, notification definition not found for name " + qName.toString());
                    return;
                }

                TaskCreationContext taskContext = new TaskCreationContext();
                taskContext.setTaskConfiguration(notificationConfiguration);
                taskContext.setTenantId(task.getTenantId());
                taskContext.setPeopleQueryEvaluator(HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().
                        getPeopleQueryEvaluator());

                Map<String, Element> tempBodyParts = new HashMap<String, Element>();
                Map<String, Element> tempHeaderParts = new HashMap<String, Element>();
                QName tempName = null;


                TToParts toParts = escalation.getToParts();
                if (toParts == null) {
                    //get the input message of the task
                    MessageDAO msg = task.getInputMessage();
                    tempName = msg.getName();
                    for (Map.Entry<String, Element> partEntry : msg.getBodyParts().entrySet()) {
                        tempBodyParts.put(partEntry.getKey(), partEntry.getValue());
                    }
                    for (Map.Entry<String, Element> partEntry : msg.getHeaderParts().entrySet()) {
                        tempHeaderParts.put(partEntry.getKey(), partEntry.getValue());
                    }

                    taskContext.setMessageBodyParts(tempBodyParts);
                    taskContext.setMessageHeaderParts(tempHeaderParts);
                    taskContext.setMessageName(tempName);
                } else {
                    for (TToPart toPart : toParts.getToPartArray()) {
                        if (!notificationConfiguration.isValidPart(toPart.getName())) {
                            //This validation should be done at the deployment time
                            String errMsg = "The part: " + toPart.getName() + " is not available" +
                                    " in the corresponding WSDL message";
                            log.error(errMsg);
                            throw new RuntimeException(errMsg);
                        }
                        String expLang = toPart.getExpressionLanguage() == null ?
                                taskConf.getExpressionLanguage() : toPart.getExpressionLanguage();
                        Node nodePart = HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().
                                getExpressionLanguageRuntime(expLang).evaluateAsPart(
                                toPart.newCursor().getTextValue(),
                                toPart.getName(),
                                evalCtx);
                        tempBodyParts.put(toPart.getName(), (Element) nodePart);
                    }
                }

                taskContext.setMessageBodyParts(tempBodyParts);
                taskContext.setMessageHeaderParts(tempHeaderParts);
                taskContext.setMessageName(tempName);
                HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().
                        getDaoConnectionFactory().getConnection().createTask(taskContext);
            } else { //if re-assignment
                if (escalation.getReassignment().getPotentialOwners().isSetFrom()) {
                    escalation.getReassignment().getPotentialOwners().getFrom().getArgumentArray();


                    String roleName = null;
                    for (TArgument argument : escalation.getReassignment().getPotentialOwners().
                            getFrom().getArgumentArray()) {
                        if ("role".equals(argument.getName())) {
                            roleName = argument.newCursor().getTextValue().trim();
                        }
                    }

                    if (roleName == null) {
                        String errMsg = "Value for argument name 'role' is expected.";
                        log.error(errMsg);
                        throw new Scheduler.JobProcessorException(errMsg);
                    }

                    if (!isExistingRole(roleName, task.getTenantId())) {
                        log.warn("Role name " + roleName + " does not exist for tenant id" + task.getTenantId());
                    }


                    List<OrganizationalEntityDAO> orgEntities = new ArrayList<OrganizationalEntityDAO>();
                    OrganizationalEntityDAO orgEntity = HumanTaskServiceComponent.getHumanTaskServer().
                            getDaoConnectionFactory().getConnection().
                            createNewOrgEntityObject(roleName,
                                    OrganizationalEntityDAO.OrganizationalEntityType.GROUP);
                    orgEntities.add(orgEntity);
                    task.replaceOrgEntitiesForLogicalPeopleGroup(
                            GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS,
                            orgEntities);
                } else {
                    String errMsg = "From element is expected inside the assignment";
                    log.error(errMsg);
                    throw new Scheduler.JobProcessorException(errMsg);
                }
            }
        }
    }

    private boolean evaluateCondition(String exp, String expLang, EvaluationContext evalCtx) {
        return HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().
                getExpressionLanguageRuntime(expLang).evaluateAsBoolean(exp, evalCtx);
    }

    // Checks the particular role name exists.
    private boolean isExistingRole(String roleName, int tenantId) {
        RegistryService registryService = HumanTaskServiceComponent.getRegistryService();
        try {
            UserRealm userRealm = registryService.getUserRealm(tenantId);
            return userRealm.getUserStoreManager().isExistingRole(roleName);
        } catch (Exception e) {
            throw new HumanTaskRuntimeException("Cannot retrieve user realm for tenantId :" + tenantId, e);
        }
    }
}

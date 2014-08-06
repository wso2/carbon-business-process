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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa;

import org.wso2.carbon.humantask.*;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.provider.OrganizationalEntityProvider;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.provider.OrganizationalEntityProviderFactory;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.xpath.XPathEvaluatorUtil;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.NotificationConfiguration;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Task Utility methods containing new object creation.
 * NOTE : Add methods to this class only when you need to create a new object.
 * For any other utility methods
 *
 * @see CommonTaskUtil : The common utility method class.
 */
public final class JPATaskUtil {
    private JPATaskUtil() {
    }

    public static void processGenericHumanRoles(TaskDAO task,
                                                HumanTaskBaseConfiguration taskConfiguration,
                                                PeopleQueryEvaluator peopleQueryEvaluator,EvaluationContext evaluationContext)
            throws HumanTaskException {

        if (taskConfiguration.isTask()) { //Task
            TTask tTask = ((TaskConfiguration) taskConfiguration).getTask();

            //TODO move the reading of configuration file in to the TaskConfiguration class
            // Reading Excluded users
            TGenericHumanRoleAssignment[] tExcludedOwners =
                    tTask.getPeopleAssignments().getExcludedOwnersArray();
            if (tExcludedOwners != null && tExcludedOwners.length > 0) {
                assignHumanRoles(task, peopleQueryEvaluator, tExcludedOwners[0],
                        GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS,evaluationContext);
            }

            // Reading potential owners
            TPotentialOwnerAssignment[] tPotentialOwners =
                    tTask.getPeopleAssignments().getPotentialOwnersArray();
            if (tPotentialOwners != null && tPotentialOwners.length > 0) {
                TPotentialOwnerAssignment tPotentialOwner = tPotentialOwners[0];

                OrganizationalEntityProvider provider =
                        OrganizationalEntityProviderFactory.getOrganizationalEntityProvider(
                                tPotentialOwner.getFrom());
                List<OrganizationalEntityDAO> orgEntities =
                        provider.getOrganizationalEntities(peopleQueryEvaluator, tPotentialOwner.getFrom(), evaluationContext);
                if (tExcludedOwners != null && tExcludedOwners.length > 0) {
                    GenericHumanRoleDAO excludedOwners = task.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
                    for (OrganizationalEntityDAO excludedEntity : excludedOwners.getOrgEntities()) {
                        for (OrganizationalEntityDAO ownerEntity : orgEntities) {
                            if (excludedEntity.getOrgEntityType() == ownerEntity.getOrgEntityType() && excludedEntity.getName().equals(ownerEntity.getName())) {
                                orgEntities.remove(ownerEntity);
                                break;
                            }
                        }
                    }
                }
                GenericHumanRole potentialOwnersGHRole = new GenericHumanRole();
                potentialOwnersGHRole.setType(GenericHumanRole.GenericHumanRoleType.POTENTIAL_OWNERS);
                potentialOwnersGHRole.setOrgEntities(orgEntities);
                potentialOwnersGHRole.setTask(task);
                for (OrganizationalEntityDAO oe : orgEntities) {
                    oe.addGenericHumanRole(potentialOwnersGHRole);
                }
                task.addHumanRole(potentialOwnersGHRole);
            }

            // Reading Stake holders
            TGenericHumanRoleAssignment[] tStakeHolders =
                    tTask.getPeopleAssignments().getTaskStakeholdersArray();
            if (tStakeHolders != null && tStakeHolders.length > 0) {
                assignHumanRoles(task, peopleQueryEvaluator, tStakeHolders[0],
                        GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS,evaluationContext);
            }

            // Reading Business administrators
            TGenericHumanRoleAssignment[] tBusinessAdministrators =
                    tTask.getPeopleAssignments().getBusinessAdministratorsArray();
            if (tBusinessAdministrators != null && tBusinessAdministrators.length > 0) {
                assignHumanRoles(task, peopleQueryEvaluator, tBusinessAdministrators[0],
                        GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS,evaluationContext);
            }


        } else { //Notification
            TNotification tNotification = ((NotificationConfiguration) taskConfiguration).
                    getNotificationDefinition();
            // Reading Notification recipients
            TGenericHumanRoleAssignment[] tRecipients =
                    tNotification.getPeopleAssignments().getRecipientsArray();
            if (tRecipients != null && tRecipients.length > 0) {
                assignHumanRoles(task, peopleQueryEvaluator, tRecipients[0],
                        GenericHumanRoleDAO.GenericHumanRoleType.NOTIFICATION_RECIPIENTS,evaluationContext);
            }
        }

    }

    private static void assignHumanRoles(TaskDAO task, PeopleQueryEvaluator peopleQueryEvaluator,
                                         TGenericHumanRoleAssignment roleAssignment,
                                         GenericHumanRole.GenericHumanRoleType type,EvaluationContext evaluationContext)
            throws HumanTaskException {
        OrganizationalEntityProvider provider =
                OrganizationalEntityProviderFactory.getOrganizationalEntityProvider(
                        roleAssignment.getFrom());
        List<OrganizationalEntityDAO> orgEntities =
                provider.getOrganizationalEntities(peopleQueryEvaluator,
                        roleAssignment.getFrom(), evaluationContext);
        GenericHumanRole humanRole = new GenericHumanRole();
        humanRole.setType(type);
        humanRole.setOrgEntities(orgEntities);
        humanRole.setTask(task);
        for (OrganizationalEntityDAO oe : orgEntities) {
            oe.addGenericHumanRole(humanRole);
        }
        task.addHumanRole(humanRole);
    }

    public static void processPresentationElements(TaskDAO task,
                                                   HumanTaskBaseConfiguration taskConfiguration,
                                                   TaskCreationContext creationContext) {

        TPresentationElements presentationElements = taskConfiguration.getPresentationElements();

        TPresentationParameters presentationParameters = presentationElements.
                getPresentationParameters();

        if (presentationParameters != null) {
            String expressionLanguage = presentationParameters.getExpressionLanguage() == null ?
                    taskConfiguration.getExpressionLanguage() :
                    presentationParameters.getExpressionLanguage();

            TPresentationParameter[] params = presentationParameters.getPresentationParameterArray();
            for (TPresentationParameter param : params) {
                PresentationParameterDAO preParam = new PresentationParameter();
                preParam.setName(param.getName());
                preParam.setTask(task);
                preParam.setType(getTypeFromQName(param.getType()));
                XPathEvaluatorUtil.evaluatePresentationParamXPath(preParam, param.newCursor().getTextValue().trim(),
                        expressionLanguage, creationContext.getEvalContext());
                task.addPresentationParameter(preParam);
            }
        }

        TText[] names = presentationElements.getNameArray();
        for (TText name : names) {
            PresentationNameDAO preName = new PresentationName();
            preName.setValue(CommonTaskUtil.replaceUsingPresentationParams(task.getPresentationParameters(),
                    name.newCursor().getTextValue().trim()));
            preName.setXmlLang(name.getLang());
            preName.setTask(task);
            task.addPresentationName(preName);
        }

        //create the subject objects.
        TText[] subjects = presentationElements.getSubjectArray();
        for (TText subject : subjects) {
            PresentationSubject preSubject = new PresentationSubject();
            preSubject.setValue(CommonTaskUtil.replaceUsingPresentationParams(task.getPresentationParameters(),
                    subject.newCursor().getTextValue().trim()));
            preSubject.setXmlLang(subject.getLang());
            preSubject.setTask(task);
            task.addPresentationSubject(preSubject);
        }

        //create the description objects.
        TDescription[] descriptions = presentationElements.getDescriptionArray();
        if (descriptions != null && descriptions.length > 0) {
            for (TDescription description : descriptions) {
                PresentationDescriptionDAO preDesc = new PresentationDescription();
                preDesc.setValue(CommonTaskUtil.replaceUsingPresentationParams(task.getPresentationParameters(),
                        description.newCursor().getTextValue().trim()));
                preDesc.setXmlLang(description.getLang());
                preDesc.setTask(task);
                task.addPresentationDescription(preDesc);
            }
        }
    }


    private static PresentationParameter.Type getTypeFromQName(QName type) {
        if ("string".equalsIgnoreCase(type.getLocalPart())) {
            return PresentationParameter.Type.XSD_STRING;
        } else if ("int".equalsIgnoreCase(type.getLocalPart())) {
            return PresentationParameter.Type.XSD_INT;
        } else if ("bool".equalsIgnoreCase(type.getLocalPart())) {
            return PresentationParameter.Type.XSD_BOOL;
        } else if ("date".equalsIgnoreCase(type.getLocalPart())) {
            return PresentationParameter.Type.XSD_DATE;
        } else if ("decimal".equalsIgnoreCase(type.getLocalPart())) {
            return PresentationParameter.Type.XSD_DECIMALE;
        } else if ("double".equalsIgnoreCase(type.getLocalPart())) {
            return PresentationParameter.Type.XSD_DOUBLE;
        }

        return PresentationParameter.Type.XSD_ANYTYPE;
    }
}

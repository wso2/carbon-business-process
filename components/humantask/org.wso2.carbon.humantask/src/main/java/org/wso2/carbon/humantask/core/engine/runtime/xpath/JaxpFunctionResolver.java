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

package org.wso2.carbon.humantask.core.engine.runtime.xpath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.util.StringUtils;
import org.w3c.dom.*;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.humantask.core.utils.HTNameSpaces;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.*;

/**
 * The XPath function resolver for Human Task specific evaluation methods.
 */
public class JaxpFunctionResolver implements XPathFunctionResolver {

    private static final Log log = LogFactory.getLog(JaxpFunctionResolver.class);

    protected static final QName groupQname = HumanTaskConstants.groupQname;
    protected static final QName userQname = HumanTaskConstants.userQname;
    protected static final QName organizationalEntityQname = HumanTaskConstants.organizationalEntityQname;

    private EvaluationContext evalCtx;
    private PeopleQueryEvaluator peopleQueryEvaluator;

    public XPathFunction resolveFunction(QName functionName, int arity) {
        if (log.isDebugEnabled()) {
            log.debug("Resolving function: " + functionName);
        }

        if (functionName.getNamespaceURI() == null) {
            log.error(" Error in resolving xpath function name , undeclared namespace " + functionName.getLocalPart()
                    + " for function name " + functionName.getNamespaceURI());

            throw new NullPointerException("Undeclared namespace for " + functionName);
        } else if (HTNameSpaces.HTD_NS.equals(functionName.getNamespaceURI())) {
            String localPart = functionName.getLocalPart();
            String errMsg = "This operation is not currently supported in this version of WSO2 BPS.";

            if (XPath2Constants.FUNCTION_GET_INPUT.equals(localPart)) {
                return new GetInput();

            } else if (XPath2Constants.FUNCTION_GET_OUTPUT.equals(localPart)) {
                return new GetOutput();

            } else if (XPath2Constants.FUNCTION_GET_POTENTIAL_OWNERS.equals(localPart)) {
                return new GetPotentialOwners();

            } else if (XPath2Constants.FUNCTION_GET_ACTUAL_OWNER.equals(localPart)) {
                return new GetActualOwner();

            } else if (XPath2Constants.FUNCTION_GET_BUSINESS_ADMINISTRATORS.equals(localPart)) {
                return new GetBusinessAdministrators();

            } else if (XPath2Constants.FUNCTION_GET_EXCLUDED_OWNERS.equals(localPart)) {
                return new GetExcludedOwners();

            } else if (XPath2Constants.FUNCTION_GET_TASK_INITIATOR.equals(localPart)) {
                return new GetTaskInitiator();

            } else if (XPath2Constants.FUNCTION_GET_TASK_PRIORITY.equals(localPart)) {
                return new GetTaskPriority();

            } else if (XPath2Constants.FUNCTION_GET_TASK_STAKEHOLDERS.equals(localPart)) {
                return new GetTaskStakeholders();

            } else if (XPath2Constants.FUNCTION_GET_LOGICAL_PEOPLE_GROUP.equals(localPart)) {
                throw new UnsupportedOperationException(errMsg);

            } else if (XPath2Constants.FUNCTION_INTERSECT.equals(localPart)) {
                return new Intersect();

            } else if (XPath2Constants.FUNCTION_UNION.equals(localPart)) {
                return new Union();

            } else if (XPath2Constants.FUNCTION_EXCEPT.equals(localPart)) {
                return new Except();

            } else if (XPath2Constants.FUNCTION_GET_COUNT_OF_FINISHED_SUB_TASKS.equals(localPart)) {
                return new GetCountOfFinishedSubTasks();

            } else if (XPath2Constants.FUNCTION_GET_COUNT_OF_SUB_TASKS.equals(localPart)) {
                return new GetCountOfSubTasks();

            } else if (XPath2Constants.FUNCTION_GET_COUNT_OF_SUB_TASKS_IN_STATE.equals(localPart)) {
                return new GetCountOfSubTasksInState();

            } else if (XPath2Constants.FUNCTION_GET_OUTCOME.equals(localPart)) {
                throw new UnsupportedOperationException(errMsg);

            } else if (XPath2Constants.FUNCTION_GET_COUNT_OF_SUB_TASKS_WITH_OUTCOME.equals(localPart)) {
                throw new UnsupportedOperationException(errMsg);

            } else if (XPath2Constants.FUNCTION_GET_SUBTASK_OUTPUT.equals(localPart)) {
                throw new UnsupportedOperationException(errMsg);

            } else if (XPath2Constants.FUNCTION_GET_SUBTASK_OUTPUTS.equals(localPart)) {
                throw new UnsupportedOperationException(errMsg);

            } else if (XPath2Constants.FUNCTION_CONCAT.equals(localPart)) {
                return new Concat();

            } else if (XPath2Constants.FUNCTION_CONCAT_WITH_DELIMITER.equals(localPart)) {
                return new ConcatWithDelimiter();

            } else if (XPath2Constants.FUNCTION_LEAST_FREQUENT_OCCURENCE.equals(localPart)) {
                return new LeastFrequentOccurence();

            } else if (XPath2Constants.FUNCTION_MOST_FREQUENT_OCCURENCE.equals(localPart)) {
                return new MostFrequentOccurence();

            } else if (XPath2Constants.FUNCTION_VOTE_ON_STRING.equals(localPart)) {
                return new VoteOnString();

            } else if (XPath2Constants.FUNCTION_AND.equals(localPart)) {
                return new And();

            } else if (XPath2Constants.FUNCTION_OR.equals(localPart)) {
                return new Or();

            } else if (XPath2Constants.FUNCTION_VOTE.equals(localPart)) {
                return new Vote();

            } else if (XPath2Constants.FUNCTION_AVG.equals(localPart)) {
                return new Avg();

            } else if (XPath2Constants.FUNCTION_MAX.equals(localPart)) {
                return new Max();

            } else if (XPath2Constants.FUNCTION_MIN.equals(localPart)) {
                return new Min();

            } else if (XPath2Constants.FUNCTION_SUM.equals(localPart)) {
                return new Sum();
            } else {
                throw new IllegalArgumentException("Unknown Human Task Function: " + localPart);
            }

        }
        return null;
    }

    public JaxpFunctionResolver(EvaluationContext evalCtx) {
        this.evalCtx = evalCtx;
        peopleQueryEvaluator = HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine()
                .getPeopleQueryEvaluator();
    }

    /**
     * Returns the part of the task’s input message.
     * In : part name, task name (optional)
     * Out : input message part
     */
    public class GetInput implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            MessageDAO inputMsg = evalCtx.getInput();
            String partName = (String) args.get(0);
            Node matchingElement = null;
            if (StringUtils.isNullOrEmpty(partName)) {
                throw new HumanTaskRuntimeException("The getInput function should be provided with" + " the part name");
            }

            if (inputMsg.getBodyData().hasChildNodes()) {
                NodeList nodeList = inputMsg.getBodyData().getChildNodes();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (partName.trim().equals(nodeList.item(i).getNodeName())) {
                        matchingElement = nodeList.item(i);
                    }
                }
            }

            if (matchingElement == null || matchingElement.getFirstChild() == null) {
                throw new HumanTaskRuntimeException(
                        "Cannot find a matching Element for " + "expression evaluation: getInput");
            }

            return matchingElement.getFirstChild();
        }
    }

    /**
     * Returns the part of the task's output message.
     * Note: When User completes a task, taskoutput is saved with part name "message". But When user calls setOutput,
     * user can specify their own part name.
     * In : part name,task name (optional)
     * Out : output message part
     */
    public class GetOutput implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            MessageDAO outputMsg = evalCtx.getOutput();
            String partName = (String) args.get(0);
            Node matchingElement = null;
            if (StringUtils.isNullOrEmpty(partName)) {
                throw new HumanTaskRuntimeException(
                        "The getOutput function should be provided with" + " the part name");
            }

            if (outputMsg.getBodyData().hasChildNodes()) {
                NodeList nodeList = outputMsg.getBodyData().getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (partName.trim().equals(nodeList.item(i).getNodeName())) {
                        matchingElement = nodeList.item(i);
                    }
                }
            }

            if (matchingElement == null || matchingElement.getFirstChild() == null) {
                throw new HumanTaskRuntimeException(
                        "Cannot find a matching Element for " + "expression evaluation: getOutput");
            }

            return matchingElement.getFirstChild();
        }
    }

    /**
     * Returns the potential owners of the task. It MUST evaluate to an empty htt:organizationalEntity in case of an
     * error. If the task name is not present the current task MUST be considered.
     * In : task name (optional)
     * Out : potential owners (htt:organizationalEntity)
     */
    public class GetPotentialOwners implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO potentialOwners = null;
            TaskDAO taskDAO = evalCtx.getTask();
            if (args.size() == 0) {
                // Case 1: consider current Task.
                potentialOwners = getPotentialOwnersFromCtx();
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (taskName.equals(taskDAO.getName())) {
                    //Case 2: TaskName equals to current task, consider current task
                    potentialOwners = getPotentialOwnersFromCtx();
                } else if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getPotentialOwners(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getPotentialOwners() instead.");
                    // We can evaluate only role based and literal based people assignments only. expression based
                    // people eval will not work here.
                    // Also we can obtain only the HumanTaskBaseConfiguration, but without taskDAO we can't build
                    // eval context for that task configuration.
                }

            }
            // else is an Error case, so potentialOwners is null. createOrgEntity will generate an empty
            // htt:organizationalEntity element in such a scenario..

            return createOrgEntity(potentialOwners);
        }

        private GenericHumanRoleDAO getPotentialOwnersFromCtx() {
            GenericHumanRoleDAO potentialOwners;
            potentialOwners = evalCtx.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
            return potentialOwners;
        }
    }

    /**
     * Returns the actual owner of the task. It MUST evaluate to an empty htt:user in case there is no actual owner.
     * If the task name is not present the current task MUST be considered.
     * In : task name (optional)
     * Out : the actual owner (user id as htt:user)
     */
    public class GetActualOwner implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            String username = "";
            if (args.size() == 0) {
                // Case 1: consider current Task.
                username = getUserNameFromCtx();

            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().getName().equals(taskName)) {
                    //Case 2: TaskName equals to current task, consider current task
                    username = getUserNameFromCtx();
                } else if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getActualOwner(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getActualOwner() instead.");
                }

            }
            return createHttUser(username);
        }

        private String getUserNameFromCtx() {
            String username = "";
            GenericHumanRoleDAO actualOwners = null;
            actualOwners = evalCtx.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
            for (OrganizationalEntityDAO oe : actualOwners.getOrgEntities()) {
                if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(oe.getOrgEntityType())) {
                    username = oe.getName();
                    break;
                }
            }
            return username;
        }
    }

    /**
     * Returns the business administrators of the task. It MUST evaluate to an empty htt:organizationalEntity in case of
     * an error. If the task name is not present the current task MUST be considered.
     * In : task name (optional)
     * Out : business administrators (htt:organizationalEntity)
     */
    public class GetBusinessAdministrators implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO businessAdmins = null;
            if (args.size() == 0) {
                // Case 1: consider current Task.
                businessAdmins = getBussinessAdminsFromCtx();
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().getName().equals(taskName)) {
                    //Case 2 : TaskName equals to current task, consider current task
                    businessAdmins = getBussinessAdminsFromCtx();
                } else if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getBusinessAdministrators(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getBusinessAdministrators() instead.");
                }

            }
            return createOrgEntity(businessAdmins);
        }

        private GenericHumanRoleDAO getBussinessAdminsFromCtx() {
            return evalCtx.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
    }

    /**
     * Returns the excluded owners. It MUST evaluate to an empty htt:organizationalEntity in case of an error.
     * If the task name is not present the current task MUST be considered.
     * In : task name (optional)
     * Out : the actual owner (user id as htt:user)
     */
    public class GetExcludedOwners implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO excludedOwners = null;
            if (args.size() == 0) {
                // Case 1: consider current Task.
                excludedOwners = getExcludedOwnersFromCtx();
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().equals(taskName)) {
                    //Case 2 : TaskName equals to current task, consider current task
                    excludedOwners = getExcludedOwnersFromCtx();
                } else if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getExcludedOwners(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getExcludedOwners() instead.");
                }

            }
            return createOrgEntity(excludedOwners);
        }

        private GenericHumanRoleDAO getExcludedOwnersFromCtx() {
            return evalCtx.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
        }
    }

    /**
     * Returns the initiator of the task. It MUST evaluate to an empty htt:user in case there is no initiator.
     * <p/>
     * If the task name is not present the current task MUST be considered.
     * In : task name (optional)
     * Out :the task initiator (user id as htt:user)
     */
    public class GetTaskInitiator implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            String username = "";
            if (args.size() == 0)
                username = getUserNameFromCtx();
            else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().getName().equals(taskName)) {
                    //Case 2 : TaskName equals to current task, consider current task
                    username = getUserNameFromCtx();
                } else if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getTaskInitiator(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getTaskInitiator() instead.");
                }

            }
            return createHttUser(username);
        }

        private String getUserNameFromCtx() {
            String username = "";
            GenericHumanRoleDAO taskInitiator;
            taskInitiator = evalCtx.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
            for (OrganizationalEntityDAO oe : taskInitiator.getOrgEntities()) {
                if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(oe.getOrgEntityType())) {
                    username = oe.getName();
                    break;
                }
            }
            return username;
        }
    }

    /**
     * Returns the priority of the task. It MUST evaluate to “5” in case the priority is not explicitly set.
     * If the task name is not present the current task MUST be considered.
     * In : task name (optional)
     * Out : priority (htt:tPriority)
     */
    public class GetTaskPriority implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 0) {
                Integer priority = evalCtx.getTask().getPriority();
                return priority;
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().getName().equals(taskName)) {
                    //Case 2 : TaskName equals to current task, consider current task
                    Integer priority = evalCtx.getTask().getPriority();
                    return priority;
                } else if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getTaskPriority(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getTaskPriority() instead.");
                }
            }
            return 5;
        }
    }

    /**
     * Returns the stakeholders of the task. It MUST evaluate to an empty htt:organizationalEntity in case of an error.
     * If the task name is not present the current task MUST be considered.
     * In : task name (optional)
     * Out : task stakeholders (htt:organizationalEntity)
     */
    public class GetTaskStakeholders implements XPathFunction {

        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO stakeholders = null;
            if (args.size() == 0) {
                // Case 1: consider current Task.
                stakeholders = getStakeholdersFromCtx();
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().getName().equals(taskName)) {
                    //Case 2 : TaskName equals to current task, consider current task
                    stakeholders = getStakeholdersFromCtx();
                } else if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getTaskStakeholders(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getTaskStakeholders() instead.");
                }

            }
            return createOrgEntity(stakeholders);
        }

        private GenericHumanRoleDAO getStakeholdersFromCtx() {
            return evalCtx.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        }
    }

    /**
     * Constructs an organizationalEntity containing every user that occurs in both set1 and set2, eliminating duplicate
     * users.
     * In : set1 (htt:organizationalEntity|htt:user), set2 (htt:organizationalEntity|htt:user)
     * Out : result (htt:organizationalEntity)
     */
    public class Intersect implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 2) {
                throw new HumanTaskRuntimeException("Invalid number of arguments for expression: except");
            }
            if (!(args.get(0) instanceof Node && args.get(1) instanceof Node)) {
                throw new HumanTaskRuntimeException("Invalid arguments for expression: except");
            }
            Node node1 = (Node) args.get(0);
            Node node2 = (Node) args.get(1);
            Set<String> set1 = new HashSet<String>();
            Set<String> set2 = new HashSet<String>();
            parseOrgEntityTypeOrUser(node1, set1);
            parseOrgEntityTypeOrUser(node2, set2);
            set1.retainAll(set2);
            return createOrgEntity(set1);
        }

    }

    /**
     * Constructs an organizationalEntity containing every user that occurs in either set1 or set2, eliminating
     * duplicate users.
     * In : set1 (htt:organizationalEntity|htt:user),set2(htt:organizationalEntity|htt:user)
     * Out : result (htt:organizationalEntity)
     */
    public class Union implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 2) {
                throw new HumanTaskRuntimeException("Invalid number of arguments for expression: except");
            }
            if (!(args.get(0) instanceof Node && args.get(1) instanceof Node)) {
                throw new HumanTaskRuntimeException("Invalid arguments for expression: except");
            }
            Node node1 = (Node) args.get(0);
            Node node2 = (Node) args.get(1);
            Set<String> resoledUsers = new HashSet<String>();
            parseOrgEntityTypeOrUser(node1, resoledUsers);
            parseOrgEntityTypeOrUser(node2, resoledUsers);
            return createOrgEntity(resoledUsers);
        }

    }

    /**
     * Constructs an organizationalEntity containing every user that occurs in set1 but not in set2.
     * Note: TODO: This function is required to allow enforcing the separation of duties (“4-eyes principle”).
     * In : set1 (htt:organizationalEntity|htt:user), set2 (htt:organizationalEntity|htt:user)
     * Out : result (htt:organizationalEntity)
     */
    public class Except implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 2) {
                throw new HumanTaskRuntimeException(
                        "Invalid number of arguments :" + args.size() + ", for expression: except");
            }
            if (!(args.get(0) instanceof Node && args.get(1) instanceof Node)) {
                throw new HumanTaskRuntimeException(
                        "Invalid arguments :" + args.get(0) + " , " + args.get(1) + " ,  for expression: except");
            }
            Node node1 = (Node) args.get(0);
            Node node2 = (Node) args.get(1);
            Set<String> resoledUsers = new HashSet<String>();
            Set<String> excludedUsers = new HashSet<String>();

            parseOrgEntityTypeOrUser(node1, resoledUsers);
            parseOrgEntityTypeOrUser(node2, excludedUsers);
            resoledUsers.removeAll(excludedUsers);
            return createOrgEntity(resoledUsers);
        }

    }

    public void parseOrgEntityTypeOrUser(Node node1, Set<String> resoledUsers) {
        if (node1.getNodeType() == Node.ELEMENT_NODE) {
            if (organizationalEntityQname.getNamespaceURI().equals(node1.getNamespaceURI()) && organizationalEntityQname
                    .getLocalPart().equals(node1.getLocalName())) {
                //Parsing organizationalEntity element
                parseOrgEntity(node1, resoledUsers);
            } else if (userQname.getNamespaceURI().equals(node1.getNamespaceURI()) && userQname.getLocalPart()
                    .equals(node1.getLocalName())) {
                //Parsing user element
                String username = node1.getTextContent();
                if (username != null) {
                    username = username.trim();
                    if (username.length() > 0) {
                        resoledUsers.add(username);
                    }
                }
            } else if (node1.hasChildNodes()) {
                NodeList nodeList = node1.getChildNodes();
                Node childNode = null;
                for (int j = 0; j < nodeList.getLength(); j++) {
                    if (Node.ELEMENT_NODE == nodeList.item(j).getNodeType()) {
                        childNode = nodeList.item(j);
                        break;
                    }
                }
                //Parsing tOrganizationalEntity type also to have consistence the expression logic.
                if (childNode != null) {
                    if (childNode.getNodeType() == Node.ELEMENT_NODE && organizationalEntityQname.getNamespaceURI()
                            .equals(childNode.getNamespaceURI())) {
                        if (userQname.getLocalPart().equals(childNode.getLocalName()) || groupQname.getLocalPart()
                                .equals(childNode.getLocalName())) {
                            parseOrgEntity(node1, resoledUsers);
                        }
                    }
                } else { // No element found. this is text content.
                    String username = node1.getTextContent();
                    if (username != null) {
                        username = username.trim();
                        if (username.length() > 0) {
                            resoledUsers.add(username);
                        }
                    }
                }
            } else {
                throw new HumanTaskRuntimeException(
                        "this function should be provided with htt:organizationalEntity or htt:user element as an argument");
            }
        }
    }

    public void parseOrgEntity(Node node, Set<String> resoledUsers) {
        //Reading users
        NodeList userList = ((Element) node)
                .getElementsByTagNameNS(userQname.getNamespaceURI(), userQname.getLocalPart());
        for (int j = 0; j < userList.getLength(); j++) {
            Node item = userList.item(j);
            String username = item.getTextContent();
            if (username != null) {
                username = username.trim();
                if (username.length() > 0) {
                    resoledUsers.add(username);
                }
            }

        }
        //Reading groups
        NodeList groupList = ((Element) node)
                .getElementsByTagNameNS(groupQname.getNamespaceURI(), groupQname.getLocalPart());
        for (int j = 0; j < groupList.getLength(); j++) {
            Node item = groupList.item(j);
            String groupName = item.getTextContent();
            if (groupName != null) {
                groupName = groupName.trim();
                if (groupName.length() > 0) {
                    resoledUsers.addAll(peopleQueryEvaluator.getUserNameListForRole(groupName));
                }
            }

        }
    }

    /**
     * Create organizationalEntity Node from a given username list
     * <p/>
     * @param userList : User name set.
     * @return : organizationalEntity node
     */
    private Node createOrgEntity(Set<String> userList) {
        Document doc = DOMUtils.newDocument();
        Element orgEntity = doc
                .createElementNS(organizationalEntityQname.getNamespaceURI(), organizationalEntityQname.getLocalPart());
        if (userList != null) {
            Element user = null;
            for (String userName : userList) {
                user = doc.createElementNS(userQname.getNamespaceURI(), userQname.getLocalPart());
                user.setTextContent(userName);
                orgEntity.appendChild(user);
            }
        }
        return orgEntity;
    }

    /**
     * Create organizationalEntity Node from GenericHumanRoleDAO.
     * <p/>
     * @param ghr : GenericHumanRoleDAO
     * @return : organizationalEntity node
     */
    private Node createOrgEntity(GenericHumanRoleDAO ghr) {
        Document doc = DOMUtils.newDocument();
        Element orgEntity = doc
                .createElementNS(organizationalEntityQname.getNamespaceURI(), organizationalEntityQname.getLocalPart());
        if (ghr != null) {
            Element userOrGroup;
            List<OrganizationalEntityDAO> orgEntities = ghr.getOrgEntities();
            for (OrganizationalEntityDAO oe : orgEntities) {
                if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(oe.getOrgEntityType())) {
                    userOrGroup = doc.createElementNS(groupQname.getNamespaceURI(), groupQname.getLocalPart());
                    userOrGroup.setTextContent(oe.getName());
                    orgEntity.appendChild(userOrGroup);
                } else if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(oe.getOrgEntityType())) {
                    userOrGroup = doc.createElementNS(userQname.getNamespaceURI(), userQname.getLocalPart());
                    userOrGroup.setTextContent(oe.getName());
                    orgEntity.appendChild(userOrGroup);
                }
            }
        }
        return orgEntity;
    }

    /**
     * Creates htt:User node for given user name
     * @param name : String username
     * @return : htt:user node
     */
    private Node createHttUser(String name) {
        Document doc = DOMUtils.newDocument();
        Element httUser = doc.createElementNS(userQname.getNamespaceURI(), userQname.getLocalPart());
        httUser.setTextContent(name);
        return httUser;
    }

    /**
     * Returns the number of finished sub tasks of a task
     * If the task name is not present the current task MUST be considered
     * NOTE:finished status are Completed,failed,Error,Exited and Obsolete
     * In ; task name (optional)
     * Out : Number of the finished task sub-tasks. If the task doesn't have sub tasks then 0 is returned
     */
    public class GetCountOfFinishedSubTasks implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 0) {
                //Case 1 : Consider current task
                List<TaskDAO> subTasksList = evalCtx.getTask().getSubTasks(); //nullable
                return getCount(subTasksList);
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().getName().equals(taskName)) {
                    //Case 2 : TaskName equals to current task, consider current task
                    List<TaskDAO> subTasksList = evalCtx.getTask().getSubTasks();
                    return getCount(subTasksList);
                } else {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getCountOfFinishedSubTasks(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getCountOfFinishedSubTasks() instead.");
                }
            }

            return 0;
        }

        private Integer getCount(List<TaskDAO> subTasksList) {
            int count = 0;
            for (TaskDAO subTask : subTasksList) {
                TaskStatus status = subTask.getStatus();
                if (status.equals(TaskStatus.COMPLETED) || status.equals(TaskStatus.FAILED) || status
                        .equals(TaskStatus.ERROR) || status.equals(TaskStatus.EXITED) || status
                        .equals(TaskStatus.OBSOLETE)) {
                    count++;
                }
            }
            return count;
        }
    }

    /**
     * Returns the number of sub tasks of a task,
     * If the task name is not present the current task MUST be considered
     * In : task name (optional)
     * Out : Number of the task sub-tasks. If the task doesn't have sub tasks then 0 is returned
     */
    public class GetCountOfSubTasks implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 0) {
                //Case 1 : Consider current task
                return evalCtx.getTask().getSubTasks().size();
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (evalCtx.getTask().getName().equals(taskName)) {
                    //Case 2 : TaskName equals to current task, consider current task
                    return evalCtx.getTask().getSubTasks().size();
                } else {
                    // Case 3: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getCountOfSubTasks(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getCountOfSubTasks() instead.");
                }
            }
            return 0;
        }
    }

    /**
     * Returns the number of a task sub tasks that are in the specified state
     * If the task name is not present the current task MUST be considered
     * In : state task name (optional)
     * Out : Number of the task sub tasks in the specified state. If the task doesn't have sub tasks then 0 is returned
     */
    public class GetCountOfSubTasksInState implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            {
                if (args.size() == 1) {
                    //Case 1 : Consider current task
                    return countSubTasks(args.get(0));
                } else if ((args.size() == 2) && (args.get(1) instanceof String)) {
                    String taskName = (String) args.get(1);
                    if (evalCtx.getTask().getName().equals(taskName)) {
                        //Case 2 : TaskName equals to current task, consider current task
                        return countSubTasks(args.get(0));
                    } else {
                        // Case 3: Getting specific task. Unsupported.
                        log.warn("HumanTask Xpath: getCountOfSubTasks(\"" + taskName + "\")"
                                + " operation is not currently supported in this version of WSO2 BPS."
                                + " Use getCountOfSubTasks() instead.");
                    }
                } else {
                    throw new HumanTaskRuntimeException("Invalid number of arguments : " + args.size()
                            + ", for function getCountOfSubTasksInState()");
                }
                return 0;
            }
        }

        private Integer countSubTasks(Object arg) {
            int count = 0;
            if (arg instanceof String && isValidStatus((String) arg)) {
                //correct argument
                List<TaskDAO> subTaskList = evalCtx.getTask().getSubTasks();
                String state = (String) arg;
                for (TaskDAO subTask : subTaskList) {
                    if (state.equalsIgnoreCase(subTask.getStatus().name())) {
                        count++;
                    }
                }
            } else {
                //invalid argument
                throw new HumanTaskRuntimeException(
                        "Invalid argument : " + arg + ", for function getCountOfSubTasksInState()");
            }

            return count;
        }
    }

    //check the validity of a given task status
    private boolean isValidStatus(String status) {
        for (TaskStatus taskStatus : TaskStatus.values()) {
            if (status.equalsIgnoreCase(taskStatus.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the concatenation of all string nodes,
     * returns an empty string for an empty node-set
     * logic: goes through the list and concat textContent to result string,
     * return empty string for empty list.
     * In : node-set of string nodes
     */
    public class Concat implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            String result = "";
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                ArrayList nodeList = (ArrayList) args.get(0);
                for (int i = 0; i < nodeList.size(); i++) {
                    try {
                        String item = (((Element) nodeList.get(i))).getTextContent();
                        result = result.concat(item);
                    } catch (DOMException e) {
                        throw new HumanTaskRuntimeException(
                                "Invalid arguments:" + args.get(0) + ", for function concat()", e);
                    } catch (ClassCastException e) {
                        throw new HumanTaskRuntimeException(
                                "Invalid arguments:" + args.get(0) + " , for function concat()",e);
                    }
                }
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments:" + args + " , for function concat()");
            }
            return result;
        }
    }

    /**
     * Returns the concatenation of all string nodes, separated by the specified delimiter string,
     * returns an empty string for an empty node-set.
     * logic: go through the list and conact textContent and the delimiter to result,
     * if the last item of the list, delimiter not added.
     * In : node-set of string nodes,delimiter string
     */
    public class ConcatWithDelimiter implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            String result = "";
            if (args.size() == 2 && args.get(0) instanceof ArrayList && args.get(1) instanceof String) {
                ArrayList nodeList = (ArrayList) args.get(0);
                String delimiter = (String) args.get(1);
                int length = nodeList.size();
                for (int i = 0; i < length; i++) {
                    try {
                        String item = ((Element) nodeList.get(i)).getTextContent();
                        result = result.concat(item);
                        if (i != length - 1) {
                            result = result.concat(delimiter);
                        }
                    } catch (DOMException e) {
                        throw new HumanTaskRuntimeException(
                                "Invalid arguments:" + args.get(0) + ", for function concatWithDelimiter()", e);
                    } catch (ClassCastException e) {
                        throw new HumanTaskRuntimeException(
                                "Invalid arguments:" + args.get(0) + " , for function concatWithDelimiter()",e);
                    }
                }
            } else {
                throw new HumanTaskRuntimeException(
                        "Invalid arguments: " + args + ", for function concatWithDelimiter()");
            }
            return result;
        }
    }

    /**
     * Generates a HashMap containing string items and it's frequency in a given NodeList
     * logic : create a HashMap where key:textContent value:frequency of the textContent in the list
     * @param list : NodeList
     * @return  : Map<String,Integer> map of string occurrence frequencies
     */
    private Map<String, Integer> generateFrequencyMap(ArrayList list) throws HumanTaskRuntimeException {
        Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
        for (int i = 0; i < list.size(); i++) {
            try {
                String item = ((Element) list.get(i)).getTextContent();
                if (frequencyMap.containsKey(item)) {
                    int frequency = frequencyMap.get(item) + 1;
                    frequencyMap.put(item, frequency);
                } else {
                    frequencyMap.put(item, 1);
                }
            } catch (DOMException e) {
                throw new HumanTaskRuntimeException("Invalid arguments:" + list, e);
            } catch (ClassCastException e) {
                throw new HumanTaskRuntimeException("Invalid arguments:" + list, e);
            }
        }

        return frequencyMap;
    }

    /**
     * Returns the least frequently occurring string value within all string nodes,
     * or an empty string in case of a tie or for an empty node-set
     * logic: get the frequency map from generateFrequencyMap() and go through the map
     * recording the least value, corresponding result and if the least values are same
     * set tie = true. If tie=true returns empty string, else result string.
     * In : node-set of string nodes
     */
    public class LeastFrequentOccurence implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                try {
                    Map<String, Integer> frequencyMap = generateFrequencyMap((ArrayList) args.get(0));
                    boolean tie = false;
                    String result = "";
                    int least = Integer.MAX_VALUE;
                    for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
                        //check if the minimum
                        if (entry.getValue() < least) {
                            //new least occurrence
                            least = entry.getValue();
                            result = entry.getKey();
                            tie = false;
                        } else if (entry.getValue() == least) {
                            //a tie
                            tie = true;
                        }
                    }
                    if (tie) {
                        return "";
                    } else {
                        return result;
                    }
                } catch (HumanTaskRuntimeException e) {
                    throw new HumanTaskRuntimeException(
                            "Error in processing arguments" + args.get(0) + " for function leastFrequentOccurence()",
                            e);
                }

            } else {
                throw new HumanTaskRuntimeException(
                        "Invalid arguments:" + args + ", for function leastFrequentOccurence()");
            }
        }
    }

    /**
     * Returns the most frequently occurring string value within all string nodes,
     * or an empty string in case of a tie or for an empty node-set.
     * logic: get the frequency map from generateFrequencyMap() and go through the map
     * recording the maximum value, corresponding result and if the max values are same
     * set tie = true. If tie=true returns empty string, else result string.
     * In : node-set of string nodes
     */
    public class MostFrequentOccurence implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                try {
                    Map<String, Integer> frequencyMap = generateFrequencyMap((ArrayList) args.get(0));
                    boolean tie = false;
                    int max = Integer.MIN_VALUE;
                    String result = "";
                    for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
                        if (entry.getValue() > max) {
                            //new max
                            max = entry.getValue();
                            result = entry.getKey();
                            tie = false;
                        } else if (entry.getValue() == max) {
                            //a tie
                            tie = true;
                        }

                    }
                    if (tie) {
                        return "";
                    } else {
                        return result;
                    }

                } catch (HumanTaskRuntimeException e) {
                    throw new HumanTaskRuntimeException(
                            "Error in processing arguments for function mostFrequentOccurence()", e);
                }
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments for function mostFrequentOccurence()");
            }
        }
    }

    /**
     * Returns the most frequently occurring string value if its occurrence is above the specified percentage and there is no tie,
     * or an empty string otherwise (including an empty node-set)
     * logic: get the frequency map from generateFrequencyMap() and go through the map
     * recording the max value, corresponding result and if the max values are same
     * set tie = true. If tie=true returns empty string, if the frequency percentage
     * is higher than input percentage return result siring, else empty string.
     * Note: input percentage should be given as parts of 100.
     * In : node-set of string nodes, percentage
     */
    public class VoteOnString implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 2 && args.get(0) instanceof ArrayList && args.get(1) instanceof Number) {
                try {
                    ArrayList list = (ArrayList) args.get(0);
                    Number percentage = (Number) args.get(1);
                    Map<String, Integer> frequencyMap = generateFrequencyMap((ArrayList) args.get(0));
                    boolean tie = false;
                    int max = Integer.MIN_VALUE;
                    String result = "";
                    for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
                        if (entry.getValue() > max) {
                            //new max
                            max = entry.getValue();
                            result = entry.getKey();
                            tie = false;
                        } else if (entry.getValue() == max) {
                            //a tie
                            tie = true;
                        }

                    }
                    if (list.size() > 0 && !tie && (max * 100 / list.size()) > percentage.floatValue()) {
                        return result;
                    } else {
                        return "";
                    }

                } catch (HumanTaskRuntimeException e) {
                    throw new HumanTaskRuntimeException("Error in processing arguments for function voteOnString()", e);
                }
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments for function voteOnString()");
            }
        }
    }

    /**
     * Returns the conjunction of all boolean nodes - returns false for an empty node-set
     * logic: as long as a true is found keep result as true, when the first false is found
     * return false immediately, else return true.
     * Note: Assumed the nodes contains, "true","false","1" or "0"
     * In : node-set of boolean nodes
     */
    public class And implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {

            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                boolean result = false;
                ArrayList list = (ArrayList) args.get(0);
                for (int i = 0; i < list.size(); i++) {
                    try {
                        //iterate through node list
                        String nodeValue = ((Element) list.get(i)).getTextContent();
                        if (nodeValue.equalsIgnoreCase("true") || nodeValue.equalsIgnoreCase("1")) {
                            //true
                            result = true;
                        } else if (nodeValue.equalsIgnoreCase("false") || nodeValue.equalsIgnoreCase("0")) {
                            //false, no point of continuing
                            return false;
                        } else {
                            throw new HumanTaskRuntimeException(
                                    "Invalid argument :" + nodeValue + " for function and(), only booleans allowed");
                        }
                    } catch (DOMException e) {
                        throw new HumanTaskRuntimeException("Invalid arguments:" + args.get(0) + ", for function and()",
                                e);
                    } catch (ClassCastException e) {
                        throw new HumanTaskRuntimeException("Invalid arguments:" + args.get(0) + ", for function and()",
                                e);
                    }
                }
                return result;
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments: " + args + " for function and()");
            }

        }
    }

    /**
     * Returns the disjunction of all boolean nodes - returns false for an empty node-set
     * logic: go through the list, if a true found, returns true immediately else return false.
     * return false for empty list
     * In : node-set of boolean nodes
     */
    public class Or implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                boolean result = false;
                ArrayList list = (ArrayList) args.get(0);

                for (int i = 0; i < list.size(); i++) {
                    try {
                        //iterate through element list
                        String nodeValue = ((Element) list.get(i)).getTextContent();
                        if (nodeValue.equalsIgnoreCase("true") || nodeValue.equalsIgnoreCase("1")) {
                            //true, no point of continuing
                            return true;
                        } else if (nodeValue.equalsIgnoreCase("false") || nodeValue.equalsIgnoreCase("0")) {
                            //false,
                            result = false;
                        } else {
                            throw new HumanTaskRuntimeException(
                                    "Invalid argument:" + nodeValue + " for function or(), only boolean nodes allowed");
                        }
                    } catch (DOMException e) {
                        throw new HumanTaskRuntimeException("Invalid arguments:" + args.get(0) + ", for function or()",
                                e);
                    } catch (ClassCastException e) {
                        throw new HumanTaskRuntimeException("Invalid arguments:" + args.get(0) + ", for function or()",
                                e);
                    }
                }
                return result;
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments:" + args + ", for function or()");
            }

        }
    }

    /**
     * Returns the most frequently occurring boolean value if its occurrence is above the specified percentage,
     * or false otherwise (including an empty node-set)
     * logic: get the true and false count. if true is higher, check if above the percentage and return true.
     * return false for any other case. no need to check percentage for false.
     * In : node-set of boolean nodes, percentage
     */
    public class Vote implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 2 && args.get(0) instanceof ArrayList && args.get(1) instanceof Number) {
                ArrayList list = (ArrayList) args.get(0);
                Number percentage = (Number) args.get(1);
                int trueCount = 0;
                int falseCount = 0;
                for (int i = 0; i < list.size(); i++) {
                    try {
                        String nodeValue = ((Element) list.get(i)).getTextContent();
                        if (nodeValue.equalsIgnoreCase("true") || nodeValue.equalsIgnoreCase("1")) {
                            //true
                            trueCount++;
                        } else if (nodeValue.equalsIgnoreCase("false") || nodeValue.equalsIgnoreCase("0")) {
                            //false
                            falseCount++;
                        } else {
                            //invalid
                            throw new HumanTaskRuntimeException(
                                    "Invalid argument:" + nodeValue + ", only boolean nodes allowed");
                        }
                    } catch (DOMException e) {
                        throw new HumanTaskRuntimeException(
                                "Invalid arguments:" + args.get(0) + ", for function vote()", e);
                    } catch (ClassCastException e) {
                        throw new HumanTaskRuntimeException(
                                "Invalid arguments:" + args.get(0) + ", for function vote()", e);
                    }
                }

                float truePercentage = trueCount * 100 / (trueCount + falseCount);

                if (trueCount > falseCount && truePercentage > percentage.floatValue()) {
                    //returns true
                    return true;
                }
                //no point of evaluating for false, anyway false returned
                return false;
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments :" + args + ", for function vote()");
            }
        }
    }

    /**
     * Returns the average value of all number nodes - returns NaN for an empty node-set
     * logic: take the sum of all nodes, return dividing by list size. return NaN for empty list.
     * In : node-set of number nodes
     */
    public class Avg implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                ArrayList list = (ArrayList) args.get(0);
                if (list.size() > 0) {
                    try {
                        //at least one element exists
                        float sum = 0;
                        for (int i = 0; i < list.size(); i++) {
                            float nodeValue = Float.parseFloat(((Element) list.get(i)).getTextContent());
                            sum += nodeValue;
                        }
                        return sum / list.size();

                    } catch (DOMException e) {
                        throw new HumanTaskRuntimeException("Invalid arguments:" + args.get(0) + ", for function avg()",
                                e);
                    } catch (NumberFormatException e) {
                        throw new HumanTaskRuntimeException("Invalid arguments:" + args.get(0) + ", for function avg()",
                                e);
                    } catch (ClassCastException e) {
                        throw new HumanTaskRuntimeException("Invalid arguments:" + args.get(0) + ", for function avg()",
                                e);
                    }
                }
                return Double.NaN;
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments:" + args + " for function avg()");
            }
        }
    }

    /**
     * Returns the maximum value of all number nodes - returns NaN for an empty node-set
     * logic: go through the list and find the maximum value. return NaN for empty list.
     * In : node-set of number nodes
     */
    public class Max implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                ArrayList list = (ArrayList) args.get(0);
                if (list.size() > 0) {
                    //at least one element exists
                    float max = Float.MIN_VALUE;
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            float nodeValue = Float.parseFloat(((Element) list.get(i)).getTextContent());
                            if (nodeValue > max) {
                                //new max
                                max = nodeValue;
                            }
                        } catch (DOMException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function max()", e);
                        } catch (NumberFormatException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function max()", e);
                        } catch (ClassCastException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function max()", e);
                        }
                    }
                    return max;
                }
                //NaN for empty node sets
                return Double.NaN;
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments:" + args + ", for function max()");
            }
        }
    }

    /**
     * Returns the minimum value of all number nodes - returns NaN for an empty node-set
     * logic: go through the list and find the minimum value. return NaN for empty list.
     * In : node-set of number nodes
     */
    public class Min implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                ArrayList list = (ArrayList) args.get(0);
                if (list.size() > 0) {
                    float min = Float.MAX_VALUE;
                    //at least one element exists
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            float nodeValue = Float.parseFloat(((Element) list.get(i)).getTextContent());
                            if (nodeValue < min) {
                                //new min
                                min = nodeValue;
                            }
                        } catch (DOMException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function min()", e);
                        } catch (NumberFormatException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function min()", e);
                        } catch (ClassCastException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function min()", e);
                        }
                    }
                    return min;
                }
                //NaN for empty node sets
                return Double.NaN;
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments:" + args + ", for function min()");
            }
        }
    }

    /**
     * Returns the sum value of all number nodes - returns NaN for an empty node-set
     * logic: go through the list and add the values to result. return NaN for empty list.
     * In : node-set of number nodes
     */
    public class Sum implements XPathFunction {

        @Override public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 1 && args.get(0) instanceof ArrayList) {
                ArrayList list = (ArrayList) args.get(0);
                if (list.size() > 0) {
                    //at least one element exists
                    float sum = 0;
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            float nodeValue = Float.parseFloat(((Element) list.get(i)).getTextContent());
                            sum += nodeValue;
                        } catch (DOMException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function sum()", e);
                        } catch (NumberFormatException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function sum()", e);
                        } catch (ClassCastException e) {
                            throw new HumanTaskRuntimeException(
                                    "Invalid arguments:" + args.get(0) + ", for function sum()", e);
                        }
                    }
                    return sum;
                }
                return Double.NaN;
            } else {
                throw new HumanTaskRuntimeException("Invalid arguments:" + args + " for function sum()");
            }

        }
    }
}

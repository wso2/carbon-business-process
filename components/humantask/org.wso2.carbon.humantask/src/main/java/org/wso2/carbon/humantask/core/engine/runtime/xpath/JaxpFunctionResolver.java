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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.MessageDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.integration.CarbonUserManagerBasedPeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.humantask.core.utils.HTNameSpaces;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            } else {
                throw new IllegalArgumentException("Unknown Human Task Function: " + localPart);
            }

        }
        return null;
    }

    public JaxpFunctionResolver(EvaluationContext evalCtx) {
        this.evalCtx = evalCtx;
        peopleQueryEvaluator = HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getPeopleQueryEvaluator();
    }

    /**
     * Returns the part of the task’s input message.
     */
    public class GetInput implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            MessageDAO inputMsg = evalCtx.getInput();
            String partName = (String) args.get(0);
            Node matchingElement = null;
            if (StringUtils.isNullOrEmpty(partName)) {
                throw new HumanTaskRuntimeException("The getInput function should be provided with" +
                        " the part name");
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
                throw new HumanTaskRuntimeException("Cannot find a matching Element for " +
                        "expression evaluation: getInput");
            }

            return matchingElement.getFirstChild();
        }
    }

    /**
     * Returns the part of the task's output message.
     * Note: When User completes a task, taskoutput is saved with part name "message". But When user calls setOutput,
     * user can specify their own part name.
     */
    public class GetOutput implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            MessageDAO outputMsg = evalCtx.getOutput();
            String partName = (String) args.get(0);
            Node matchingElement = null;
            if (StringUtils.isNullOrEmpty(partName)) {
                throw new HumanTaskRuntimeException("The getOutput function should be provided with" +
                        " the part name");
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
                throw new HumanTaskRuntimeException("Cannot find a matching Element for " +
                        "expression evaluation: getOutput");
            }

            return matchingElement.getFirstChild();
        }
    }

    /**
     * Returns the potential owners of the task. It MUST evaluate to an empty htt:organizationalEntity in case of an
     * error. If the task name is not present the current task MUST be considered.
     */
    public class GetPotentialOwners implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO potentialOwners = null;
            if (args.size() == 0) {
                // Case 1: consider current Task.
                potentialOwners = evalCtx.getGenericHumanRole(
                        GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 2: Getting specific task. Unsupported.
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
    }

    /**
     * Returns the actual owner of the task. It MUST evaluate to an empty htt:user in case there is no actual owner.
     * If the task name is not present the current task MUST be considered.
     */
    public class GetActualOwner implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO actualOwners = null;
            String username = "";
            if (args.size() == 0) {
                // Case 1: consider current Task.
                actualOwners = evalCtx.getGenericHumanRole(
                        GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
                for (OrganizationalEntityDAO oe : actualOwners.getOrgEntities()) {
                    if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(oe.getOrgEntityType())) {
                        username = oe.getName();
                        break;
                    }
                }
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 2: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getActualOwner(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getActualOwner() instead.");
                }

            }
            return createHttUser(username);
        }
    }

    /**
     * Returns the business administrators of the task. It MUST evaluate to an empty htt:organizationalEntity in case of
     * an error. If the task name is not present the current task MUST be considered.
     */
    public class GetBusinessAdministrators implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO businessAdmins = null;
            if (args.size() == 0) {
                // Case 1: consider current Task.
                businessAdmins = evalCtx.getGenericHumanRole(
                        GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 2: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getBusinessAdministrators(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getBusinessAdministrators() instead.");
                }

            }
            return createOrgEntity(businessAdmins);
        }
    }

    /**
     * Returns the excluded owners. It MUST evaluate to an empty htt:organizationalEntity in case of an error.
     * If the task name is not present the current task MUST be considered.
     */
    public class GetExcludedOwners implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO excludedOwners = null;
            if (args.size() == 0) {
                // Case 1: consider current Task.
                excludedOwners = evalCtx.getGenericHumanRole(
                        GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 2: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getExcludedOwners(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getExcludedOwners() instead.");
                }

            }
            return createOrgEntity(excludedOwners);
        }
    }

    /**
     * Returns the initiator of the task. It MUST evaluate to an empty htt:user in case there is no initiator.
     * <p/>
     * If the task name is not present the current task MUST be considered.
     */
    public class GetTaskInitiator implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO taskInitiator = null;
            String username = "";
            if (args.size() == 0) {
                // Case 1: consider current Task.
                taskInitiator = evalCtx.getGenericHumanRole(
                        GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
                for (OrganizationalEntityDAO oe : taskInitiator.getOrgEntities()) {
                    if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(oe.getOrgEntityType())) {
                        username = oe.getName();
                        break;
                    }
                }
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 2: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getTaskInitiator(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getTaskInitiator() instead.");
                }

            }
            return createHttUser(username);
        }
    }

    /**
     * Returns the priority of the task. It MUST evaluate to “5” in case the priority is not explicitly set.
     * If the task name is not present the current task MUST be considered.
     */
    public class GetTaskPriority implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() == 0) {
                Integer priority = evalCtx.getTask().getPriority();
                return priority;
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 2: Getting specific task. Unsupported.
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
     */
    public class GetTaskStakeholders implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            GenericHumanRoleDAO stakeholders = null;
            if (args.size() == 0) {
                // Case 1: consider current Task.
                stakeholders = evalCtx.getGenericHumanRole(
                        GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
            } else if ((args.size() == 1) && (args.get(0) instanceof String)) {
                String taskName = (String) args.get(0);
                if (!StringUtils.isNullOrEmpty(taskName)) {
                    // Case 2: Getting specific task. Unsupported.
                    log.warn("HumanTask Xpath: getTaskStakeholders(\"" + taskName + "\")"
                            + " operation is not currently supported in this version of WSO2 BPS."
                            + " Use getTaskStakeholders() instead.");
                }

            }
            return createOrgEntity(stakeholders);
        }
    }

    /**
     * Constructs an organizationalEntity containing every user that occurs in both set1 and set2, eliminating duplicate
     * users.
     */
    public class Intersect implements XPathFunction {
        @Override
        public Object evaluate(List args) throws XPathFunctionException {
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
     */
    public class Union implements XPathFunction {
        @Override
        public Object evaluate(List args) throws XPathFunctionException {
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
     */
    public class Except implements XPathFunction {
        @Override
        public Object evaluate(List args) throws XPathFunctionException {
            if (args.size() != 2) {
                throw new HumanTaskRuntimeException("Invalid number of arguments for expression: except");
            }
            if (!(args.get(0) instanceof Node && args.get(1) instanceof Node)) {
                throw new HumanTaskRuntimeException("Invalid arguments for expression: except");
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
            if (organizationalEntityQname.getNamespaceURI().equals(node1.getNamespaceURI())
                    && organizationalEntityQname.getLocalPart().equals(node1.getLocalName())) {
                //Parsing organizationalEntity element
                parseOrgEntity(node1, resoledUsers);
            } else if (userQname.getNamespaceURI().equals(node1.getNamespaceURI()) && userQname.getLocalPart().equals(node1.getLocalName())) {
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
                    if (childNode.getNodeType() == Node.ELEMENT_NODE && organizationalEntityQname.getNamespaceURI().equals(childNode.getNamespaceURI())) {
                        if (userQname.getLocalPart().equals(childNode.getLocalName()) || groupQname.getLocalPart().equals(childNode.getLocalName())) {
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
                throw new HumanTaskRuntimeException("this function should be provided with htt:organizationalEntity or htt:user element as an argument");
            }
        }
    }


    public void parseOrgEntity(Node node, Set<String> resoledUsers) {
        //Reading users
        NodeList userList = ((Element) node).getElementsByTagNameNS(userQname.getNamespaceURI(), userQname.getLocalPart());
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
        NodeList groupList = ((Element) node).getElementsByTagNameNS(groupQname.getNamespaceURI(),
                groupQname.getLocalPart());
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
        Element orgEntity = doc.createElementNS(organizationalEntityQname.getNamespaceURI(),
                organizationalEntityQname.getLocalPart());
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
        Element orgEntity = doc.createElementNS(organizationalEntityQname.getNamespaceURI(),
                organizationalEntityQname.getLocalPart());
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
    private Node createHttUser(String name){
        Document doc = DOMUtils.newDocument();
        Element httUser = doc.createElementNS(userQname.getNamespaceURI(), userQname.getLocalPart());
        httUser.setTextContent(name);
        return httUser;
    }

}

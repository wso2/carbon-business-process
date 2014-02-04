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
import org.wso2.carbon.humantask.core.dao.MessageDAO;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.integration.CarbonUserManagerBasedPeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
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
            if (XPath2Constants.FUNCTION_GET_POTENTIAL_OWNERS.equals(localPart)) {
                return new GetPotentialOwners();
            } else {
                String errMsg = "This operation is not currently supported in this version of WSO2 BPS.";
                if (XPath2Constants.FUNCTION_GET_ACTUAL_OWNER.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_BUSINESS_ADMINISTRATORS.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_EXCLUDED_OWNERS.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_INPUT.equals(localPart)) {
                    return new GetInput();
                } else if (XPath2Constants.FUNCTION_GET_TASK_INITIATOR.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_TASK_PRIORITY.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

                } else if (XPath2Constants.FUNCTION_GET_TASK_STAKEHOLDERS.equals(localPart)) {
                    throw new UnsupportedOperationException(errMsg);

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

        }
        return null;
    }

    public JaxpFunctionResolver(EvaluationContext evalCtx) {
        this.evalCtx = evalCtx;
        peopleQueryEvaluator = HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getPeopleQueryEvaluator();
    }

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

    public static class GetPotentialOwners implements XPathFunction {
        public Object evaluate(List args) throws XPathFunctionException {
            return null;
        }
    }

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

    public Node createOrgEntity(Set<String> userList) {
        Document doc = DOMUtils.newDocument();
        Element orgEntity = doc.createElementNS(organizationalEntityQname.getNamespaceURI(), organizationalEntityQname.getLocalPart());
        Element user = null;
        for (String userName : userList) {
            user = doc.createElementNS(userQname.getNamespaceURI(), userQname.getLocalPart());
            user.setTextContent(userName);
            orgEntity.appendChild(user);
        }
        return orgEntity;
    }

}

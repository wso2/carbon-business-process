/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.humantask.TFrom;
import org.wso2.carbon.humantask.TLiteral;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class LiteralBasedOrgEntityProvider implements OrganizationalEntityProvider {

    private static final Log log = LogFactory.getLog(ExpressionBasedOrgEntityProvider.class);


    public List<OrganizationalEntityDAO> getOrganizationalEntities(
            PeopleQueryEvaluator peopleQueryEvaluator, TFrom tFrom,
            EvaluationContext evaluationContext) {

        TLiteral literal = tFrom.getLiteral();
        List<OrganizationalEntityDAO> orgEntityList = new ArrayList<OrganizationalEntityDAO>();

        Element domNode = (Element) literal.getDomNode();
        if (domNode != null) {
            NodeList orgEntityNodes = domNode.getElementsByTagNameNS(
                    HumanTaskConstants.organizationalEntityQname.getNamespaceURI(),
                    HumanTaskConstants.organizationalEntityQname.getLocalPart());
            // There should be only one organizational Entity
            if (orgEntityNodes.getLength() == 1) {
                Node orgEntityNode = orgEntityNodes.item(0);
                addOrgEntitiesForOrganizationEntityNode(orgEntityNode, peopleQueryEvaluator, orgEntityList);
            } else {
                NodeList elements = domNode.getElementsByTagNameNS(
                        HumanTaskConstants.userQname.getNamespaceURI(),
                        HumanTaskConstants.userQname.getLocalPart());
                if (elements.getLength() == 1) {
                    // There should only be one user element
                    CommonTaskUtil.addOrgEntityForUserNode(elements.item(0), peopleQueryEvaluator, orgEntityList);
                }
            }
        }
        return orgEntityList;
    }

    public static void addOrgEntityForUserNode(Node userNode, PeopleQueryEvaluator pqe,
                                               List<OrganizationalEntityDAO> orgEntityList) {
        NodeList childNodes = userNode.getChildNodes();
        if (childNodes.getLength() == 1) {
            Node textNode = childNodes.item(0);
            if (textNode != null && textNode.getNodeType() == Node.TEXT_NODE) {
                String username = textNode.getNodeValue();
                if (username != null) {
                    username = username.trim();
                    if (username.length() > 0) {
                        OrganizationalEntityDAO userOrgEntityForUser = pqe.createUserOrgEntityForName(username);
                        orgEntityList.add(userOrgEntityForUser);
                    }
                }
            }
        }
    }

    public static void addOrgEntitiesForOrganizationEntityNode(Node orgEntityNode, PeopleQueryEvaluator pqe,
                                                               List<OrganizationalEntityDAO> orgEntityList) {
        // Org entity node should contain either a user elements or group elements
        if (orgEntityNode.getNodeType() == Node.ELEMENT_NODE) {
            NodeList userList = ((Element) orgEntityNode).getElementsByTagNameNS(HumanTaskConstants.userQname.getNamespaceURI(),
                    HumanTaskConstants.userQname.getLocalPart());
            for (int j = 0; j < userList.getLength(); j++) {
                Node item = userList.item(j);
                NodeList childNodes = item.getChildNodes();
                if (childNodes.getLength() == 1) {
                    Node textNode = childNodes.item(0);
                    if (textNode != null && textNode.getNodeType() == Node.TEXT_NODE) {
                        String username = textNode.getNodeValue();
                        if (username != null) {
                            username = username.trim();
                            if (username.length() > 0) {
                                OrganizationalEntityDAO userOrgEntityForName = pqe.createUserOrgEntityForName(username);
                                orgEntityList.add(userOrgEntityForName);
                            }
                        }
                    }
                }
            }
            NodeList groupList = ((Element) orgEntityNode).getElementsByTagNameNS(HumanTaskConstants.groupQname.getNamespaceURI(),
                    HumanTaskConstants.groupQname.getLocalPart());
            for (int j = 0; j < groupList.getLength(); j++) {
                Node item = groupList.item(j);
                NodeList childNodes = item.getChildNodes();
                if (childNodes.getLength() == 1) {
                    Node textNode = childNodes.item(0);
                    if (textNode != null && textNode.getNodeType() == Node.TEXT_NODE) {
                        String groupName = textNode.getNodeValue();
                        if (groupName != null) {
                            groupName = groupName.trim();
                            if (groupName.length() > 0) {
                                OrganizationalEntityDAO groupOrgEntityForName = pqe.createGroupOrgEntityForRole(groupName);
                                orgEntityList.add(groupOrgEntityForName);
                            }
                        }
                    }
                }
            }
        }
    }
}

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
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.wso2.carbon.humantask.TFrom;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.ExpressionLanguageRuntime;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import java.util.ArrayList;
import java.util.List;

public class ExpressionBasedOrgEntityProvider implements OrganizationalEntityProvider {

    private static final Log log = LogFactory.getLog(ExpressionBasedOrgEntityProvider.class);

    public List<OrganizationalEntityDAO> getOrganizationalEntities(
            PeopleQueryEvaluator peopleQueryEvaluator, TFrom tFrom,
            EvaluationContext evaluationContext) {

        String expression = tFrom.newCursor().getTextValue().trim();

        log.debug("Evaluating expression " + expression + " for ExpressionBasedOrgEntityProvider");

        String expLang = (tFrom.getExpressionLanguage() == null) ?
                HumanTaskConstants.WSHT_EXP_LANG_XPATH20 : tFrom.getExpressionLanguage();
        ExpressionLanguageRuntime expLangRuntime = HumanTaskServiceComponent.getHumanTaskServer().
                getTaskEngine().getExpressionLanguageRuntime(expLang);
        List list = expLangRuntime.evaluate(expression, evaluationContext);

        List<OrganizationalEntityDAO> orgEntityList = new ArrayList<OrganizationalEntityDAO>();

        if (list.isEmpty() || list.size() > 1) {
            log.debug(" Organizational Entities evaluated to null or multiple list");
            return orgEntityList;
        }
        // Returned list should evaluate to an organizationalEntity or a user
        for (Object item : list) {
            if (item instanceof NodeList) {
                for(int i = 0; i < ((NodeList) item).getLength(); i++) {
                    Node node = ((NodeList) item).item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if (node.getLocalName().equals(HumanTaskConstants.userQname.getLocalPart()) &&
                                node.getNamespaceURI().equals(HumanTaskConstants.userQname.getNamespaceURI())) {
                            CommonTaskUtil.addOrgEntityForUserNode(node, peopleQueryEvaluator, orgEntityList);

                        } else  if(node.getLocalName().equals(HumanTaskConstants.groupQname.getLocalPart()) &&
                                node.getNamespaceURI().equals(HumanTaskConstants.groupQname.getNamespaceURI())) {
                            CommonTaskUtil.addOrgEntityForGroupNode(node, peopleQueryEvaluator, orgEntityList);
                        } else if(node.getLocalName().equals("wrapper")) {
                            // Expression evaluator wraps the string with wrapper element name
                            CommonTaskUtil.addOrgEntityForUserNode(node, peopleQueryEvaluator, orgEntityList);
                        } else if (node.getLocalName().equals(HumanTaskConstants.organizationalEntityQname.getLocalPart())
                                && node.getNamespaceURI().equals(HumanTaskConstants.organizationalEntityQname.getNamespaceURI())) {
                            // This is an organizational Entity node, hence parse it as org entity
                            // Most probably this logic wont be required
                            CommonTaskUtil.addOrgEntitiesForOrganizationEntityNode(node, peopleQueryEvaluator, orgEntityList);

                        }
                    } else if(node.getNodeType() == Node.TEXT_NODE) {
                        String nodeValue = node.getNodeValue().trim();
                        if(nodeValue.length() > 0) {
                            OrganizationalEntityDAO userOrgEntityForName = peopleQueryEvaluator.createUserOrgEntityForName(nodeValue);
                            if(userOrgEntityForName != null) {
                                orgEntityList.add(userOrgEntityForName);
                            }
                        }
                    }
                }
            }
        }
        return orgEntityList;
    }

}

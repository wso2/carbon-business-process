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

package org.wso2.carbon.bpel.b4p.extension;

import org.apache.ode.bpel.runtime.extension.AbstractExtensionCorreationFilter;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Correlation filter for the <code>&lt;peopleActivity&gt;</code> extension activity
 */
public class BPEL4PeopleCorrelationFilter extends AbstractExtensionCorreationFilter {
    public String getNamespaceURI() {
        return BPEL4PeopleConstants.B4P_NAMESPACE;
    }

    public void registerExtensionCorrelationFilter() {
        registerFilter(getName(),
                BPEL4PeopleCorrelationFilter.class);
    }

    public String[] computeCorrelationValues(Element body, Element header) {
        /*
        * <soapenv:Header>
        * <axis2ns1:correlation
        * xmlns:axis2ns1="http://docs.oasis-open.org/ns/bpel4people/bpel4people/200803" axis2ns1:taskid="500" />
        * </soapenv:Header>
        * */
        String taskID = null;
        NodeList nList = header.getElementsByTagNameNS(BPEL4PeopleConstants.B4P_NAMESPACE,
                BPEL4PeopleConstants.B4P_CORRELATION_HEADER);
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            NamedNodeMap namedNodeMap = nNode.getAttributes();
            Node attr = namedNodeMap.getNamedItemNS(BPEL4PeopleConstants.B4P_NAMESPACE,
                    BPEL4PeopleConstants.B4P_CORRELATION_HEADER_ATTRIBUTE);
            if (attr != null) {
                taskID = attr.getTextContent();
                break;
            }
        }
        return new String[]{taskID};
    }

    public String getName() {
        return BPEL4PeopleConstants.PEOPLE_ACTIVITY_FILTER_NAME;
    }
}

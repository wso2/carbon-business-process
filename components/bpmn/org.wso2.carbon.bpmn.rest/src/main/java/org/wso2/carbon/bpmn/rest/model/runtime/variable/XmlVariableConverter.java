/*
 *     Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.carbon.bpmn.rest.model.runtime.variable;


import org.activiti.engine.ActivitiIllegalArgumentException;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.Utils;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XMLDocument;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * WSO2 XML variable converter
 */
public class XmlVariableConverter implements RestVariableConverter {

    private static final String typeName = "xml";

    @Override
    public String getRestTypeName() {
        return typeName;
    }

    @Override
    public Class<?> getVariableType() {
        return XMLDocument.class;
    }

    @Override
    public Object getVariableValue(RestVariable result) {
        if (result.getValue() != null) {
            if (result.getType().equals(typeName) && result.getValue() instanceof String) {
                try {
                    return Utils.parse((String) result.getValue());
                } catch (ParserConfigurationException e) {
                    throw new ActivitiIllegalArgumentException("Converter cannot convert content to xml data type : " +
                                                        "Error occurred while parsing due to parser configuration issue", e);
                } catch (IOException e) {
                    throw new ActivitiIllegalArgumentException("Converter cannot convert content to xml data type : " +
                                                        "IO Error occurred while parsing", e);
                } catch (SAXException e) {
                    throw new ActivitiIllegalArgumentException("Converter cannot convert content to xml data type : " +
                                                        "Error occurred while parsing", e);
                }
            } else {
                throw new ActivitiIllegalArgumentException("Converter cannot convert " + result.getValue().getClass().getName()+
                                                        " type to XML");
            }
        }
        return null;
    }

    @Override
    public void convertVariableValue(Object variableValue, RestVariable result) {
        if (variableValue != null) {
            if (variableValue instanceof XMLDocument) {
                try {
                    result.setValue(Utils.stringify((XMLDocument) variableValue));
                } catch (TransformerException e) {
                    throw new ActivitiIllegalArgumentException("Error occurred while transforming XML Document to String");
                }
            } else {
                throw new ActivitiIllegalArgumentException("Converter can only convert XML variables");
            }
        } else {
            result.setValue(null);
        }
    }
}

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.wso2.carbon.bpmn.core.types.datatypes.xml;

import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * WSO2XML variable type class
 */
public class XmlType implements VariableType{

    private static final Log log = LogFactory.getLog(XmlType.class);

    @Override
    public String getTypeName() {
        return "WSO2Xml";
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public boolean isAbleToStore(Object obj) {

        if (obj == null || XMLDocument.class.isAssignableFrom(obj.getClass())) {
            return true;
        }

        return false;
    }

    @Override
    public void setValue(Object obj, ValueFields valueFields) {
        if (obj != null && obj instanceof XMLDocument) {
            try {

                valueFields.setTextValue(Utils.stringify((XMLDocument) obj));

            } catch (TransformerException e) {
                //since error occurred while transformation, set the variable value to null
                valueFields.setTextValue(null);
                log.error("Error occurred while converting XMLDocument to String", e);
            }
        } else {
            valueFields.setTextValue(null);
        }
    }

    @Override
    public Object getValue(ValueFields valueFields) {

        try {

            return Utils.parse(valueFields.getTextValue());

        } catch (ParserConfigurationException e) {
            log.error("Eror occurred due to DocumentBuilder cannot be created which satisfies the configuration requested.", e);
        } catch (IOException e) {
            log.error("IO Error occurred while parsing the variable value", e);
        } catch (SAXException e) {
            log.error("Error occurred while parsing the variable value", e);
        }

        return null;
    }
}

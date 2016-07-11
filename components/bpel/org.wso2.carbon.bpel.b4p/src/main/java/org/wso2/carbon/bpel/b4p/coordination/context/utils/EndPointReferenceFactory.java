/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.coordination.context.utils;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.wso2.carbon.bpel.b4p.coordination.context.WSConstants;

import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.QName;


/**
 * EndPoint Reference Factory, which is a utility class converts OM to EPR, EPR to OM.
 */
public class EndPointReferenceFactory {


    private static EndPointReferenceFactory endPointReferenceFactory = null;

    private EndPointReferenceFactory() {

    }

    public static EndPointReferenceFactory getInstance() {
        if (endPointReferenceFactory == null) {
            endPointReferenceFactory = new EndPointReferenceFactory();
        }
        return endPointReferenceFactory;
    }

    /**
     * Creates EndpointReference from a OMElement
     *
     * @param eprElement -OMElement
     * @return EndpointReference corresponding to OMElement
     */
    public EndpointReference eprFromOMElement(OMElement eprElement) {
        EndpointReference epr = null;
        epr = new EndpointReference(eprElement.getFirstChildWithName(
                new QName(WSConstants.WS_ADDRESSING_NAMESPACE, WSConstants.WS_ADDRESSING_ADDRESS)).getText());
        HashMap referenceParameters = new HashMap();
        OMElement referenceParametersElement = eprElement.getFirstChildWithName(
                new QName(WSConstants.WS_ADDRESSING_NAMESPACE, WSConstants.WS_ADDRESSING_REFERENCE_PARAMETERS));
        if (referenceParametersElement != null) {
            Iterator propertyIter = referenceParametersElement.getChildElements();
            while (propertyIter.hasNext()) {
                OMElement element = (OMElement) propertyIter.next();
                referenceParameters.put(element.getQName(), element.cloneOMElement());
            }
            epr.setReferenceParameters(referenceParameters);
        }
        return epr;
    }

    /**
     * Adds <wsa:Address /> and <wsa:ReferenceParameters/>  OMElements to parentElement.
     *
     * @param epr           EndpointReference
     * @param parentElement OMElement
     */
    public void omElementFromEPR(EndpointReference epr, OMElement parentElement) {
        parentElement.addChild(omAddressElementFromEPR(epr));
//       parentElement.addChild(omReferenceParameterFromEPR(epr));
    }

    /**
     * Creates WS-Addressing Address OMElement. i.e
     * <wsa:Address>http://example.com/registrationService</wsa:Address>
     *
     * @param epr EndpointReference
     * @return wsa-Address OMElement
     */
    public OMElement omAddressElementFromEPR(EndpointReference epr) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace wsAddressing = factory.createOMNamespace(
                WSConstants.WS_ADDRESSING_NAMESPACE,
                AddressingConstants.WSA_DEFAULT_PREFIX);
        OMElement addressElement = factory.createOMElement(WSConstants.WS_ADDRESSING_ADDRESS, wsAddressing);
        addressElement.setText(epr.getAddress());
        return addressElement;
    }

    //TODO implement
    public OMElement omReferenceParameterFromEPR(EndpointReference epr) {

        return null;
    }


}

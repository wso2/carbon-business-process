/**
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.humantask.core.integration.utils;

import org.apache.axiom.soap.*;
import org.wso2.carbon.humantask.core.HumanTaskConstants;

import javax.xml.namespace.QName;

/**
 * Used to implement all the SOAP message related utility methods for humantasks
 */
public class SOAPUtils {
    public static SOAPFault createSOAPFault(SOAPFactory soapFactory, String reason) {
//        OMElement detail = buildSoapDetail(bpelMessageContext, odeMessageContext);

        SOAPFault fault = soapFactory.createSOAPFault();
        SOAPFaultCode code = soapFactory.createSOAPFaultCode(fault);
        code.setText(new QName(HumanTaskConstants.SOAP_ENV_NS, "Server"));
        SOAPFaultReason faultReason = soapFactory.createSOAPFaultReason(fault);
        faultReason.setText(reason);
        SOAPFaultDetail soapDetail = soapFactory.createSOAPFaultDetail(fault);
//        if (detail != null) {
//            soapDetail.addDetailEntry(detail);
//        }
        return fault;
    }
}

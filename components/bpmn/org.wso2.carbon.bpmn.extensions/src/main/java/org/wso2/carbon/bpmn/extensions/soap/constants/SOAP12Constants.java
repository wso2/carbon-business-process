/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.soap.constants;

/**
 * constants specific to SOAP 1.2.
 */
public interface SOAP12Constants extends Constants {

    static final String SOAP_ENVELOPE_NAMESPACE_URI = "http://www.w3.org/2003/05/soap-envelope";

    static final String SOAP_ENCODING_NAMESPACE_URI = "http://www.w3.org/2003/05/soap-encoding";

    static final String SOAP12_CONTENT_TYPE = "application/soap+xml";

    static final String SOAP_ROLE = "role";

    //SOAP Fault Codes


    static final String SOAP_FAULT_CODE = "Code";
    static final String SOAP_FAULT_SUBCODE = "Subcode";
    static final String SOAP_FAULT_VALUE = "Value";

    // SOAP Fault Reason

    static final String SOAP_FAULT_REASON = "Reason";
    static final String SOAP_FAULT_TEXT = "Text";
    static final String SOAP_FAULT_TEXT_LANG = "lang";
    static final String SOAP_FAULT_TEXT_LANG_PREFIX = "xml";
    static final String SOAP_FAULT_TEXT_LANG_NAMESPACE = "en-US";

    // SOAP Fault Detail

    static final String SOAP_FAULT_DETAIL = "Detail";

    // SOAP Fault Role

    static final String SOAP_FAULT_ROLE = "Role";
}

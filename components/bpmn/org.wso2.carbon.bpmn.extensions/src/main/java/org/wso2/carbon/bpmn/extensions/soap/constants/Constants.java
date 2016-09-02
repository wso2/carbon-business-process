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
 * constants used by both SOAP 1.1 and SOAP 1.2 when creating the SOAP Envelope.
 */
public interface Constants {


    static final String SOAP_NAMESPACE_PREFIX = "soapenv";

    static final String SOAP_ENVELOPE = "Envelope";

    static final String HEADER = "Header";

    static final String BODY = "Body";

    static final String SOAP_FAULT = "Fault";

    static final String ENCODING_STYLE = "encodingStyle";

    static final String MUST_UNDERSTAND = "mustUnderstand";

    static final String SOAP11_VERSION = "soap11";

    static final String SOAP12_VERSION = "soap12";


}

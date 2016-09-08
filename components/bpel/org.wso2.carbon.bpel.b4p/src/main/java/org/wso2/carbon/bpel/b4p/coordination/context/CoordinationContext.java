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

package org.wso2.carbon.bpel.b4p.coordination.context;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.wso2.carbon.bpel.b4p.coordination.context.impl.SimpleCoordinationContext;

/**
 * This is a representation of the ws-c coordination context.
 */
public interface CoordinationContext {

    public abstract String getIdentifier();

    public abstract void setIdentifier(String identifier);

    public abstract String getCoordinationType();

    public abstract void setCoordinationType(String coordinationType);

    public abstract long getExpires();

    public abstract void setExpires(long expires);

    public abstract EndpointReference getRegistrationService();

    public abstract void getRegistrationService(EndpointReference endpointReference);

    public abstract OMElement toOM();

    /**
     * Factory method for creating Coordination Context.
     */
    public static final class Factory {
        public static CoordinationContext newContext(OMElement context) {
            return new SimpleCoordinationContext(context);
        }

        public static CoordinationContext newContext(String identifier, String coordinationType, EndpointReference
                endpointReference) {
            return new SimpleCoordinationContext(identifier, coordinationType, endpointReference);
        }

        public static CoordinationContext newContext(String identifier, String coordinationType, long expires,
                                                     EndpointReference endpointReference) {
            return new SimpleCoordinationContext(identifier, coordinationType, expires, endpointReference);
        }
    }

}

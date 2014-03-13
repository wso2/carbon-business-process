/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the ODE engine to make invocation of external
 * services using Axis2.
 */
public class BPELMessageExchangeContextImpl implements MessageExchangeContext {
    private static final Log log = LogFactory.getLog(BPELMessageExchangeContextImpl.class);

    public void invokePartner(PartnerRoleMessageExchange partnerRoleMessageExchange)
            throws ContextException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking a partner operation: " +
                      partnerRoleMessageExchange.getOperationName());
        }

        PartnerService partnerService = (PartnerService) partnerRoleMessageExchange.getChannel();
        partnerService.invoke(partnerRoleMessageExchange);
    }

    public void onAsyncReply(MyRoleMessageExchange myRoleMessageExchange)
            throws BpelEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Processing an async reply from service " +
                      myRoleMessageExchange.getServiceName());
        }

        // Nothing to do, no callback is necessary, the client just synchronizes itself with the
        // mex reply when invoking the engine.
    }
}
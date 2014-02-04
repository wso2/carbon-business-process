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

package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.wso2.carbon.bpel.ui.bpel2svg.TerminationHandlerInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.apache.axiom.om.OMElement;

/**
 * TerminationHandler tag UI impl
 */
public class TerminationHandlerImpl extends SequenceImpl implements TerminationHandlerInterface {

    public TerminationHandlerImpl(String token) {
        super(token);

        name = "TERMINATIONHANDLER" + System.currentTimeMillis();
        displayName = "Termination Handler";

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public TerminationHandlerImpl(OMElement omElement) {
        super(omElement);

        name = "TERMINATIONHANDLER" + System.currentTimeMillis();
        displayName = "Termination Handler";

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

     public TerminationHandlerImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        name = "TERMINATIONHANDLER" + System.currentTimeMillis();
        displayName = "Termination Handler";

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.TERMINATIONHANDLER_END_TAG;
    }

}

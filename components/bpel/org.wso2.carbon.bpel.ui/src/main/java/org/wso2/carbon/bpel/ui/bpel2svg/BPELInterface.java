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

package org.wso2.carbon.bpel.ui.bpel2svg;

import org.apache.axiom.om.OMElement;

public interface BPELInterface {
    public void processBpelString(OMElement om);

   // public OMElement load(String filename);
    public OMElement load(String bpelStr);

    public void setBpelElement(OMElement bpelElement);

    public OMElement getBpelElement();

    public void setIncludeAssign(boolean includeAssign);

    public boolean isIncludeAssign();

    public void setVertical(boolean vertical);

    public boolean isVertical();

    public ProcessInterface getRootActivity();

}

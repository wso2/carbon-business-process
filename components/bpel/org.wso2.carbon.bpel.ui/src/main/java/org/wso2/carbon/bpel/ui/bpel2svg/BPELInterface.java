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
    /**
     * Process the OmElement containing the bpel process definition
     * By passing the OmElement with the process definition a new Process Activity is created.
     * If there any links in the process,set the link properties i.e. the link name, source of the link and the target of the link.
     * Process the subactivites of the bpel process by iterating through the omElement
     * @param om omElement containing the bpel process definition
     */
    public void processBpelString(OMElement om);
    /**
     * Converts the bpel process definition to an omElement which is how the AXIS2 Object Model (AXIOM) represents an XML
     * element
     * @param bpelStr bpel process definition needed to create the SVG
     * @return omElement
     */
    public OMElement load(String bpelStr);
    /**
     * Sets the omElement containing the bpel process definition
     * @param bpelElement omElement containing the bpel process definition
     */
    public void setBpelElement(OMElement bpelElement);
    /**
     * Gets the omElement containing the bpel process definition
     * @return omElement containing the bpel process definition
     */
    public OMElement getBpelElement();
    /**
     * Sets the boolean value to include the assign activities
     * @param includeAssign boolean value to include the assign activities
     */
    public void setIncludeAssign(boolean includeAssign);
    /**
     * Gets the boolean value to include the assign activities
     * @return boolean value to include the assign activities->true/false
     */
    public boolean isIncludeAssign();
    /**
     * Sets the boolean value for the vertical layout
     * @param vertical boolean value -> true/false
     */
    public void setVertical(boolean vertical);
    /**
     * Gets the boolean value for the vertical layout
     * @return true/false
     */
    public boolean isVertical();
    /**
     * Gets the root activity i.e. the Process Activity
     * @return root activity i.e. Process Activity
     */
    public ProcessInterface getRootActivity();

}

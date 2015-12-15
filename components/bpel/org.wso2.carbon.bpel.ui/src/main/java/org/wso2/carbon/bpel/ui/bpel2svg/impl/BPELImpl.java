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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPELInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.Link;
import org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parse the BPEL process definition and recursively create the process object model
 */
public class BPELImpl implements BPELInterface {
    private Log log = LogFactory.getLog(BPELImpl.class);
    private ProcessInterface processActivity = null;
    private boolean vertical = true;
    private boolean includeAssign = true;

    //To handle links
    public Map<String, Link> links = new HashMap<String, Link>();
    public Set<ActivityInterface> sources = new HashSet<ActivityInterface>();
    public Set<ActivityInterface> targets = new HashSet<ActivityInterface>();

    /*To create an OmElement (OMElement is how the AXIS2 Object Model (AXIOM) represents an XML element)
     from the bpel process definition
    */
    private XMLStreamReader parser = null;
    private StAXOMBuilder builder = null;
    private OMElement bpelElement = null;

    /**
     * Process the OmElement containing the bpel process definition
     * By passing the OmElement with the process definition a new Process Activity is created.
     * If there any links in the process,set the link properties i.e. the link name, source of the link and the target of the link.
     * Process the subactivites of the bpel process by iterating through the omElement
     * @param om omElement containing the bpel process definition
     */
    public void processBpelString(OMElement om) {
        //Checks whether the omElement/XmlElement contains a value
        if (om != null) {
            //Creates a new instance of the Process activity by passing the omElement with process definition as the @param
            processActivity = new ProcessImpl(bpelElement);
            //Set the link properties i.e. the link name, source of the link and the target of the link of the bpel process
            processActivity.setLinkProperties(links, sources, targets);
            /**
             * Get the subactivites in the bpel process by passing the omElement with the process definition as the @param
             * Iterates through the omElement and processes the subActivities each one separately, if the activity name matches any of the element tags
             * then the constructor of that activity implementation is invoked
             */
            processActivity.processSubActivities(bpelElement);
        }
    }

    /**
     * Converts the bpel process definition to an omElement which is how the AXIS2 Object Model (AXIOM) represents an XML
     * element
     * @param bpelStr bpel process definition needed to create the SVG
     * @return omElement
     */
    public OMElement load(String bpelStr) {
        try {
            /*Creates a new instance of the XmlStreamReader class for the specified String input i.e. the bpel process definition
              using the StringReader class which enables you to turn an ordinary String into a Reader.
              This is useful if you have data as a String but need to pass that String to a component that only accepts a Reader.
            */
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(bpelStr));
            /*The instance of XmlStreamReader created is passed to the StAXOMBuilder which produces a pure XML infoset compliant object
             model which conatins the bpel process definition
             */
            builder = new StAXOMBuilder(parser);
            //The XML object created by the StAXOMBuilder is used to build an OMElement that is added to an existing OM tree
            bpelElement = builder.getDocumentElement();
            //OmElement containing the bpel process definition is returned
            return bpelElement;
        } catch (XMLStreamException e) {
            log.error("XMLStreamReader creation failed", e);
            throw new NullPointerException("Document Element is NULL");
        }
    }

    /**
     * Gets the root activity i.e. the Process Activity
     * @return root activity i.e. Process Activity
     */
    public ProcessInterface getRootActivity() {
        return processActivity;
    }

    /**
     * Gets the boolean value for the vertical layout
     * @return true/false
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * Sets the boolean value for the vertical layout
     * @param vertical boolean value -> true/false
     */
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }
    /**
     * Gets the boolean value to include the assign activities
     * @return boolean value to include the assign activities->true/false
     */
    public boolean isIncludeAssign() {
        return includeAssign;
    }
    /**
     * Sets the boolean value to include the assign activities
     * @param includeAssign boolean value to include the assign activities
     */
    public void setIncludeAssign(boolean includeAssign) {
        this.includeAssign = includeAssign;
    }

    /**
     * Gets the omElement containing the bpel process definition
     * @return omElement containing the bpel process definition
     */
    public OMElement getBpelElement() {
        return bpelElement;
    }

    /**
     * Sets the omElement containing the bpel process definition
     * @param bpelElement omElement containing the bpel process definition
     */
    public void setBpelElement(OMElement bpelElement) {
        this.bpelElement = bpelElement;
    }
}

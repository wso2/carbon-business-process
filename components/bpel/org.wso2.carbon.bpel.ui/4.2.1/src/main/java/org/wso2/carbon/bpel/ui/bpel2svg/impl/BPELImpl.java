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
    public Set<ActivityInterface> targets =
            new HashSet<ActivityInterface>();
    
    private XMLStreamReader parser = null;
    private StAXOMBuilder builder = null;
    private OMElement bpelElement = null;

    public void processBpelString(OMElement om) {

        if (om != null) {
            processActivity = new ProcessImpl(bpelElement);
            processActivity.setLinkProperties(links, sources,targets);
            processActivity.processSubActivities(bpelElement);

            //TODO do we need this kind of check. I think this is a problem that should be solved by a bpel compiler
//            OMElement startElement = bpelElement.getFirstChildWithName(new QName("http://docs.oasis-open.org/wsbpel/2.0/process/executable", BPEL2SVGFactory.SEQUENCE_START_TAG)); // namesapce should be changed, exceptions should be handled.
//            if (startElement != null) {/
//              ....
//            }else{
//                startElement = bpelElement.getFirstChildWithName(new QName("http://docs.oasis-open.org/wsbpel/2.0/process/executable", BPEL2SVGFactory.FLOW_START_TAG)); // namesapce should be changed, exceptions should be handled.
//                if(startElement != null){
//                   .....
//                }
//                else {
//                    startElement = bpelElement.getFirstChildWithName(new QName("http://docs.oasis-open.org/wsbpel/2.0/process/executable", BPEL2SVGFactory.SCOPE_START_TAG)); // namesapce should be changed, exceptions should be handled.
//                    if(startElement != null){
//                         ....
//                    } else {
//                         .....
//                    }
//                }
//            }
        }
    }

    public OMElement load(String bpelStr) {
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(bpelStr));

            builder = new StAXOMBuilder(parser);
            bpelElement = builder.getDocumentElement();
            //check whether the paser needed to be closed
            return bpelElement;
        } catch (XMLStreamException e) {
            log.error("XMLStreamReader creation failed", e);
            throw new NullPointerException("Document Element is NULL");
        }
    }

    public ProcessInterface getRootActivity() {
        return processActivity;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public boolean isIncludeAssign() {
        return includeAssign;
    }

    public void setIncludeAssign(boolean includeAssign) {
        this.includeAssign = includeAssign;
    }

    public OMElement getBpelElement() {
        return bpelElement;
    }

    public void setBpelElement(OMElement bpelElement) {
        this.bpelElement = bpelElement;
    }
}

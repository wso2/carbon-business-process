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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.wso2.carbon.bpel.ui.bpel2svg.*;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Process tag UI impl
 */
public class ProcessImpl extends ActivityImpl implements ProcessInterface {
    private Log log = LogFactory.getLog(ActivityImpl.class);
    
    public ProcessImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        endIconHeight = startIconHeight;
        endIconWidth = startIconWidth;
    }

    public ProcessImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        endIconHeight = startIconHeight;
        endIconWidth = startIconWidth;
    }

    public ProcessImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        endIconHeight = startIconHeight;
        endIconWidth = startIconWidth;
    }

    @Override
    public String getId() {
        return getName() + "-Process";
    }

    @Override
    public Element getSVGString(SVGDocument document) {

//        LayoutManager layoutManager = org.wso2.carbon.bpel.ui.bpel2svg.old.BPEL2SVGFactory.getInstance().getLayoutManager();
        // Build Specific Code
        //attention---------    getSVGFileHeader();
        doc = (SVGDocument) dom.createDocument(svgNS, "svg", null);
        root = doc.getDocumentElement();
        root.setAttributeNS(null, "height", "1000");
        root.appendChild(getDefs(doc));
        root.appendChild(getBoxDefinition(doc));
        root.appendChild(getImageDefinition(doc));
        // Process Sub Activities
        root.appendChild(getSubActivitiesSVGString(doc));
        root.appendChild(getEndImageDefinition(doc));
        // Add Arrow
        root.appendChild(getArrows(doc));
        // Add End Tag
        // getSCGFileFooter(); //this isn't used

        return root;
    }

        public SVGDocument getSVGDocument() {
//            LayoutManager layoutManager = org.wso2.carbon.bpel.ui.bpel2svg.old.BPEL2SVGFactory.getInstance().getLayoutManager();
            // Build Specific Code
            //attention---------    getSVGFileHeader();
            doc = (SVGDocument) dom.createDocument(svgNS, "svg", null);
            root = doc.getDocumentElement();

            int iHeight = getDimensions().getHeight();
            int iWidth = getDimensions().getWidth();

            root.setAttributeNS(null, "height", Integer.toString(iHeight));
            root.setAttributeNS(null, "width", Integer.toString(iWidth));
            root.appendChild(getDefs(doc));
            root.appendChild(getBoxDefinition(doc));
            root.appendChild(getImageDefinition(doc));
            // Process Sub Activities
            root.appendChild(getSubActivitiesSVGString(doc));
            root.appendChild(getEndImageDefinition(doc));
            // Add Arrow
            root.appendChild(getArrows(doc));
            if (getLinkArrows(doc) != null) {
                root.appendChild(getLinkArrows(doc));    //waruna
            }
        // Add End Tag
        // getSCGFileFooter(); //this isn't used

        return doc;  
    }

    //the following method should be removed.It's auto completed by Batik. But width and height should be changed.
    private String getSVGFileHeader() {
        StringBuffer svgSB = new StringBuffer();
        // Build Specific Code
        svgSB.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        svgSB.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1 Tiny//EN\"\n\t\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11-tiny.dtd\">\n");
        svgSB.append("<!-- Created with Oen ESB SVG Generator http://blogs.sun.com/toxophily/ -->\n");

        svgSB.append("<svg\n");
        svgSB.append("\txmlns=\"http://www.w3.org/2000/svg\"\n");
//        svgSB.append("\txmlns:svg=\"http://www.w3.org/2000/svg\"\n");
        svgSB.append("\txmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
//        svgSB.append("\txmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\n");
        svgSB.append("\tversion=\"1.1\"  baseProfile=\"tiny\"\n");
        // TODO : Fix Sizing
        svgSB.append("\twidth=\"" + dimensions.getWidth() * 2 + "\"\n");
        svgSB.append("\theight=\"" + dimensions.getHeight() * 2 + "\"\n");
        svgSB.append("\tid=\"" + getId() + "\">\n");

        return svgSB.toString();
    }

    private String getSCGFileFooter() {
        return "</svg>\n";
    }

    protected Element getDefs(SVGDocument doc) {
        Element defs = doc.createElementNS("http://www.w3.org/2000/svg", "defs");
        defs.setAttributeNS(null, "id", "defs4");

        Element marker1 = doc.createElementNS("http://www.w3.org/2000/svg", "marker");
        marker1.setAttributeNS(null, "refX", "0");
        marker1.setAttributeNS(null, "refY", "0");
        marker1.setAttributeNS(null, "orient", "auto");
        marker1.setAttributeNS(null, "id", "Arrow1Lend");
        marker1.setAttributeNS(null, "style", "overflow:visible");

        Element path1 = doc.createElementNS("http://www.w3.org/2000/svg", "path");
        path1.setAttributeNS(null, "d", "M 0,0 L 5,-5 L -12.5,0 L 5,5 L 0,0 z");
        path1.setAttributeNS(null, "transform", "matrix(-0.8,0,0,-0.8,-10,0)");
        path1.setAttributeNS(null, "id", "path3166");
        path1.setAttributeNS(null, "style", "fill-rule:evenodd;stroke:#000000;stroke-width:1pt;marker-start:none");

        Element marker2 = doc.createElementNS("http://www.w3.org/2000/svg", "marker");
        marker2.setAttributeNS(null, "refX", "0");
        marker2.setAttributeNS(null, "refY", "0");
        marker2.setAttributeNS(null, "orient", "auto");
        marker2.setAttributeNS(null, "id", "Arrow1Mend");
        marker2.setAttributeNS(null, "style", "overflow:visible");

        Element path2 = doc.createElementNS("http://www.w3.org/2000/svg", "path");
        path2.setAttributeNS(null, "d", "M 0,0 L 5,-5 L -12.5,0 L 5,5 L 0,0 z");
        path2.setAttributeNS(null, "transform", "matrix(-0.8,0,0,-0.8,-10,0)");
        path2.setAttributeNS(null, "id", "path3193");
        path2.setAttributeNS(null, "style", "fill-rule:evenodd;stroke:#000000;stroke-width:1pt;marker-start:none");

        Element linkMarker = doc.createElementNS("http://www.w3.org/2000/svg", "marker");
        linkMarker.setAttributeNS(null, "refX", "0");
        linkMarker.setAttributeNS(null, "refY", "0");
        linkMarker.setAttributeNS(null, "orient", "auto");
        linkMarker.setAttributeNS(null, "id", "LinkArrow");
        linkMarker.setAttributeNS(null, "style", "overflow:visible");

        Element linkPath = doc.createElementNS("http://www.w3.org/2000/svg", "path");
    //    linkPath.setAttributeNS(null, "d", "M 0,0 L 2.5,-7.5 L -1,0 L 2.5,7.5 L 0,0 z");
        linkPath.setAttributeNS(null, "d", "M -11.5,0 L -7,-7.5 L -12.5,0 L -7,7.5 L -11.5,0 z");
        linkPath.setAttributeNS(null, "transform", "matrix(-0.8,0,0,-0.8,-10,0)");
        linkPath.setAttributeNS(null, "id", "linkPath");
        linkPath.setAttributeNS(null, "style", "fill-rule:evenodd;stroke:#000000;stroke-width:1pt;marker-start:none");

        Element linearGradient = doc.createElementNS("http://www.w3.org/2000/svg", "linearGradient");
        linearGradient.setAttributeNS(null, "id", "orange_red");
        linearGradient.setAttributeNS(null, "x1" ,"0%");
        linearGradient.setAttributeNS(null, "y1", "0%");
        linearGradient.setAttributeNS(null, "x2" ,"0%");
        linearGradient.setAttributeNS(null, "y2", "100%");

        Element stop1 = doc.createElementNS("http://www.w3.org/2000/svg", "stop");
        stop1.setAttributeNS(null, "offset", "0%");
        stop1.setAttributeNS(null, "style", "stop-color:rgb(255,255,255);stop-opacity:1");

        Element stop2 = doc.createElementNS("http://www.w3.org/2000/svg", "stop");                   //these should be taken from the svg factory
        stop2.setAttributeNS(null, "offset", "100%");
        stop2.setAttributeNS(null, "style", "stop-color:rgb(0,0,255);stop-opacity:1");              //these should be taken from the svg factory
        
        marker1.appendChild(path1);
        marker2.appendChild(path2);
        linkMarker.appendChild(linkPath);
        defs.appendChild(marker1);
        defs.appendChild(marker2);
        defs.appendChild(linkMarker);
        linearGradient.appendChild(stop1);
        linearGradient.appendChild(stop2);
        defs.appendChild(linearGradient);

        return defs;

    }

    protected Element getArrows(SVGDocument doc) {
        Element group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        if (subActivities != null) {
            ActivityInterface startActivity = subActivities.get(0);
            ActivityInterface endActivity = subActivities.get(subActivities.size() - 1);
            SVGCoordinates exitCoords = getExitArrowCoords();
            group.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), startActivity.getEntryArrowCoords().getXLeft(), startActivity.getEntryArrowCoords().getYTop(), name));
            SVGCoordinates entryCoords = getEndEntryArrowCoords();
            group.appendChild(getArrowDefinition(doc, endActivity.getExitArrowCoords().getXLeft(), endActivity.getExitArrowCoords().getYTop(), entryCoords.getXLeft(), entryCoords.getYTop(), name));
        }
        return group;
    }

    private Element getLinkArrows(SVGDocument doc) {
        Element group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        if (links != null && !links.isEmpty()) {
            Set linksSet = links.entrySet();
            Iterator linksIterator = linksSet.iterator();
                while(linksIterator.hasNext()){
                    Map.Entry<String, Link> link = (Map.Entry<String, Link>)linksIterator.next();
                    ActivityInterface startActivity = link.getValue().getSource();
                    ActivityInterface endActivity = link.getValue().getTarget();
                    String linkName = link.getKey();
                    //Element pathGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
                    //group.setAttributeNS("xlink", "title", linkName);
                    if (endActivity != null && startActivity != null) {
                        group.appendChild(drawLink(doc, startActivity.getExitArrowCoords().getXLeft(),
                                startActivity.getExitArrowCoords().getYTop(), endActivity.getEntryArrowCoords().getXLeft(),
                                endActivity.getEntryArrowCoords().getYTop(), startActivity.getStartIconWidth(),
                                link.getKey(), linkName));
                        //group.appendChild(pathGroup);
                    }
                }
        }
        return group;
    }

    private Element drawLink(SVGDocument doc, int startX, int startY, int endX, int endY, int startIconWidth, String id, String linkName)
    {
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");

        int firstBend = 20;
        if(layoutManager.isVerticalLayout()){
            if (startY < endY) {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," + (startY + firstBend) +
                        " L " + startX + "," + (startY + firstBend) +  " L " + endX + "," + (startY + firstBend) +
                        " L " + endX + "," + endY);                            //use constants for these propotions
            }
            else {
                if (startX > endX) {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," + (startY + firstBend) +
                        " L " + (startX - (startIconWidth/2 + firstBend)) + "," + (startY + firstBend) +  " L " +
                        (startX - (startIconWidth/2 + firstBend)) + "," + (endY - firstBend) + " L " + endX + "," + (endY - firstBend)+
                        " L " + endX + "," + endY);                            //use constants for these propotions
                }
                else {
                    path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," + (startY + firstBend) +
                        " L " + (startX + (startIconWidth/2 + firstBend)) + "," + (startY + firstBend) +  " L " +
                        (startX + (startIconWidth/2 + firstBend)) + "," + (endY - firstBend) + " L " + endX + "," + (endY - firstBend)+
                        " L " + endX + "," + endY);
                }
            }

        }else{
            path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1* endX) / 2) + "," + startY + " L " + ((startX + 1* endX) / 2) + "," + endY + " L " + endX + "," + endY);                              //use constants for these propotions
        }
        path.setAttributeNS(null, "id", id);
        path.setAttributeNS(null, "style", getLinkArrowStyle());
        path.setAttributeNS("xlink", "title", linkName);
        //path.setAttributeNS(null, "style", "opacity:" + getIconOpacity());
        //path.setAttributeNS(null, "onmouseover", "this.style.opacity=1;this.filters.alpha.opacity=100");
        //path.setAttributeNS(null, "onmouseout", "this.style.opacity=" + getIconOpacity() + ";this.filters.alpha.opacity=25");


      //  path.setAttributeNS(null, "onmouseover", "this.style.opacity=1;this.filters.alpha.opacity=100");
       // path.setAttributeNS(null, "onmouseout", "this.style.opacity=" + getIconOpacity() + ";this.filters.alpha.opacity=100");

        //path.setAttributeNS(null, "onload", "this.style.opacity=" + getIconOpacity() + ";this.filters.alpha.opacity=100");
       // path.setAttributeNS(null, "onmousemove", "this.style.opacity=1;this.filters.alpha.opacity=100");

        return path;
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.PROCESS_END_TAG;
    }

    @Override
    public SVGDimension getDimensions() {
        if (dimensions == null) {
            int width = 0;
            int height = 0;
            dimensions = new SVGDimension(width, height);

            SVGDimension subActivityDim = null;
            ActivityInterface activity = null;
            Iterator<ActivityInterface> itr = getSubActivities().iterator();
            try {
                while (itr.hasNext()) {
                    activity = itr.next();
                    subActivityDim = activity.getDimensions();
                    if (subActivityDim.getWidth() > width) {
                        width += subActivityDim.getWidth();
                    }
                    height += subActivityDim.getHeight();
                }
            } catch (NoSuchElementException e) {
                log.error("Invalid Element access", e);
                //throw new Exception("Error in reading Dimensions", e);
            }

            height += ((getYSpacing() * 2) + getStartIconHeight() + getEndIconHeight());
            width += getXSpacing();

            dimensions.setWidth(width);
            dimensions.setHeight(height);
            
            layoutManager.setSvgHeight(height);
            layoutManager.setSvgWidth(width);

            if (!layoutManager.isVerticalLayout()) {
                switchDimensionsToHorizontal();
            }
        }
        return dimensions;
    }

    @Override
    public void layout(int startXLeft, int startYTop) {
        if (layoutManager.isVerticalLayout()) {
            layoutVertical(startXLeft, startYTop);
        } else {
            layoutHorizontal(startXLeft, startYTop);
        }
    }

    public void layoutVertical(int startXLeft, int startYTop) {
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop + (getYSpacing() / 2);
        int endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        int endYTop = startYTop + dimensions.getHeight() - getEndIconHeight() - (getYSpacing() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft = startXLeft + (getXSpacing() / 2);
        while (itr.hasNext()) {
            activity = itr.next();
            activity.layout(childXLeft, childYTop);
            childYTop += activity.getDimensions().getHeight();
        }

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        getDimensions().setXLeft(startXLeft);                                    //TODO why startXleft not Xleft?
        getDimensions().setYTop(startYTop);
    }

    private void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);
        int endXLeft = startXLeft + dimensions.getWidth() - getEndIconWidth() - (getYSpacing() / 2);
        int endYTop = centreOfMyLayout - (getEndIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);
        while (itr.hasNext()) {
            activity = itr.next();
            activity.layout(childXLeft, childYTop);
            childXLeft += activity.getDimensions().getWidth();
        }

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    @Override
    public SVGCoordinates getExitArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop() + getStartIconHeight();
        if (!layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + getStartIconWidth();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
        }
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);
        return coords;
    }

    public SVGCoordinates getEndEntryArrowCoords() {
        int xLeft = getEndIconXLeft() + (getEndIconWidth() / 2);
        int yTop = getEndIconYTop();
        if (!layoutManager.isVerticalLayout()) {
            xLeft = getEndIconXLeft();
            yTop = getEndIconYTop() + (getEndIconHeight() / 2);
        }
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);
        return coords;
    }

    @Override
    public boolean isAddOpacity() {
        return isAddCompositeActivityOpacity();
    }

    @Override
    public String getOpacity() {
        return getCompositeOpacity();
    }
}

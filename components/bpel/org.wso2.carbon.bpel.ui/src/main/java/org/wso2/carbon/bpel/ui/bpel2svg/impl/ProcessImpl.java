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
 * Process tag UI implementation
 */
public class ProcessImpl extends ActivityImpl implements ProcessInterface {
    private Log log = LogFactory.getLog(ActivityImpl.class);
    /**
     * Initializes a new instance of the ProcessImpl class using the specified string i.e. the token
     * @param token
     */
    public ProcessImpl(String token) {
        super(token);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        //Set the End icon height and width
        endIconHeight = startIconHeight;
        endIconWidth = startIconWidth;
    }
    /**
     * Initializes a new instance of the ProcessImpl class using the specified omElement
     * @param omElement which matches the Process tag
     */
    public ProcessImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        //Set the End icon height and width
        endIconHeight = startIconHeight;
        endIconWidth = startIconWidth;
    }
    /**
     * Initializes a new instance of the ProcessImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an Process Activity when processing the subActivities
     * of the process
     * @param omElement which matches the Process tag
     * @param parent
     */
    public ProcessImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        //Set the End icon height and width
        endIconHeight = startIconHeight;
        endIconWidth = startIconWidth;
    }
    /**
     *
     * @return String with name of the activity
     */
    @Override
    public String getId() {
        return getName() + "-Process";
    }

    /**
     *
     * @param document SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML or HTML document) which contains the components of the Process
     */
    @Override
    public Element getSVGString(SVGDocument document) {
        //Create the SVG document
        doc = (SVGDocument) dom.createDocument(svgNS, "svg", null);
        root = doc.getDocumentElement();
        //Set the height
        root.setAttributeNS(null, "height", "1000");
        //Get the container for referenced elements
        root.appendChild(getDefs(doc));
        //Get the scope/box of the process which contains the subActivities
        root.appendChild(getBoxDefinition(doc));
        //Get the icons of the activities i.e. create/define the activity icons
        root.appendChild(getImageDefinition(doc));
        // Process Sub Activities
        root.appendChild(getSubActivitiesSVGString(doc));
        root.appendChild(getEndImageDefinition(doc));
        //Get the arrow flows of the subActivities of the process
        root.appendChild(getArrows(doc));

        return root;
    }

    /**
     * Creates the SVG document which defines the components of the process
     * @return SVG document which defines the components including shapes, gradients etc. of the process
     */
    public SVGDocument getSVGDocument() {
        //Creates the SVG
        doc = (SVGDocument) dom.createDocument(svgNS, "svg", null);
        root = doc.getDocumentElement();
        //Get the height and the width of the process
        int iHeight = getDimensions().getHeight();
        int iWidth = getDimensions().getWidth();
        //Defining attributes of the SVG
        root.setAttributeNS(null, "height", Integer.toString(iHeight));
        root.setAttributeNS(null, "width", Integer.toString(iWidth));
        //Get the container for referenced elements
        root.appendChild(getDefs(doc));
        //Get the scope/box of the process which contains the subActivities
        root.appendChild(getBoxDefinition(doc));
        //Get the icons of the activities i.e. create/define the activity icons
        root.appendChild(getImageDefinition(doc));
        // Process Sub Activities
        root.appendChild(getSubActivitiesSVGString(doc));
        //Get the end image/icon definition
        root.appendChild(getEndImageDefinition(doc));
        //Get the arrow flows of the subActivities of the process
        root.appendChild(getArrows(doc));
        //Get the link arrows --> For FLOW activity
        if (getLinkArrows(doc) != null) {
            root.appendChild(getLinkArrows(doc));
        }

        return doc;
    }

    /**
     *
     * @return String with the SVG file header which defines the attributes like height, width, id
     */
    private String getSVGFileHeader() {
        StringBuffer svgSB = new StringBuffer();
        // Build Specific Code
        svgSB.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        svgSB.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1 Tiny//EN\"\n\t\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11-tiny.dtd\">\n");
        svgSB.append("<!-- Created with Oen ESB SVG Generator http://blogs.sun.com/toxophily/ -->\n");

        svgSB.append("<svg\n");
        svgSB.append("\txmlns=\"http://www.w3.org/2000/svg\"\n");
        svgSB.append("\txmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
        svgSB.append("\tversion=\"1.1\"  baseProfile=\"tiny\"\n");
        svgSB.append("\twidth=\"" + dimensions.getWidth() * 2 + "\"\n");
        svgSB.append("\theight=\"" + dimensions.getHeight() * 2 + "\"\n");
        svgSB.append("\tid=\"" + getId() + "\">\n");

        return svgSB.toString();
    }

    /**
     *
     * @return String with the </svg> closing tag/footer
     */
    private String getSCGFileFooter() {
        return "</svg>\n";
    }

    /**
     * All Elements are described inline
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML) which contains the components of the Process
     */
    protected Element getDefs(SVGDocument doc) {
        /* SVG <defs> element is used to embed definitions that can be reused inside an SVG image.
           For instance, you can group SVG shapes together and reuse them as a single shape.
        */
        Element defs = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "defs");
        defs.setAttributeNS(null, "id", "defs4");

        /*SVG markers are used to mark the start, mid and end of a line or path
            refx= Position where the marker connects with the vertex (default 0)
            refy= Position where the marker connects with the vertex (default 0)
            orient='auto' or an angle to always show the marker at. 'auto' will compute an angle that makes
                    the x-axis a tangent of the vertex (default 0)

         */
        Element marker1 = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "marker");
        //Defining the attributes
        marker1.setAttributeNS(null, "refX", "0");
        marker1.setAttributeNS(null, "refY", "0");
        marker1.setAttributeNS(null, "orient", "auto");
        marker1.setAttributeNS(null, "id", "Arrow1Lend");
        marker1.setAttributeNS(null, "style", "overflow:visible");

        /*Creating a SVG path and defining its attributes
            d=a set of commands which define the path
            pathLength=If present, the path will be scaled so that the computed path length of the points equals this value
            transform=a list of transformations
         */
        Element path1 = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "path");
        //Defining the attributes
        path1.setAttributeNS(null, "d", "M 0,0 L 5,-5 L -12.5,0 L 5,5 L 0,0 z");
        path1.setAttributeNS(null, "transform", "matrix(-0.8,0,0,-0.8,-10,0)");
        path1.setAttributeNS(null, "id", "path3166");
        path1.setAttributeNS(null, "style", "fill-rule:evenodd;stroke:#000000;stroke-width:1pt;marker-start:none");

        //Creating a SVG marker element and defining the attributes
        Element marker2 = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "marker");
        marker2.setAttributeNS(null, "refX", "0");
        marker2.setAttributeNS(null, "refY", "0");
        marker2.setAttributeNS(null, "orient", "auto");
        marker2.setAttributeNS(null, "id", "Arrow1Mend");
        marker2.setAttributeNS(null, "style", "overflow:visible");

        //Creating a SVG path element and defining the attributes
        Element path2 = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "path");
        path2.setAttributeNS(null, "d", "M 0,0 L 5,-5 L -12.5,0 L 5,5 L 0,0 z");
        path2.setAttributeNS(null, "transform", "matrix(-0.8,0,0,-0.8,-10,0)");
        path2.setAttributeNS(null, "id", "path3193");
        path2.setAttributeNS(null, "style", "fill-rule:evenodd;stroke:#000000;stroke-width:1pt;marker-start:none");

        //Creating a SVG marker element and defining the attributes
        Element linkMarker = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "marker");
        linkMarker.setAttributeNS(null, "refX", "0");
        linkMarker.setAttributeNS(null, "refY", "0");
        linkMarker.setAttributeNS(null, "orient", "auto");
        linkMarker.setAttributeNS(null, "id", "LinkArrow");
        linkMarker.setAttributeNS(null, "style", "overflow:visible");

        //Creating a SVG path element and defining the attributes
        Element linkPath = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "path");
        linkPath.setAttributeNS(null, "d", "M -11.5,0 L -7,-7.5 L -12.5,0 L -7,7.5 L -11.5,0 z");
        linkPath.setAttributeNS(null, "transform", "matrix(-0.8,0,0,-0.8,-10,0)");
        linkPath.setAttributeNS(null, "id", "linkPath");
        linkPath.setAttributeNS(null, "style", "fill-rule:evenodd;stroke:#000000;stroke-width:1pt;marker-start:none");

        /*
         Defines a linear gradient. Linear gradients fill the object by using a vector,
         and can be defined as horizontal, vertical or angular gradients
            x1- x start point of the gradient vector
            x2- x end point of the gradient vector
            y1- y start point of the gradient vector
            y2- y end point of the gradient vector
        */
        Element linearGradient = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "linearGradient");
        linearGradient.setAttributeNS(null, "id", "orange_red");
        linearGradient.setAttributeNS(null, "x1", "0%");
        linearGradient.setAttributeNS(null, "y1", "0%");
        linearGradient.setAttributeNS(null, "x2", "0%");
        linearGradient.setAttributeNS(null, "y2", "100%");

        /*
         The stops for a gradient
           offset= The offset for this stop (0 to 1/0% to 100%) -->  Required.
        */
        Element stop1 = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "stop");
        stop1.setAttributeNS(null, "offset", "0%");
        stop1.setAttributeNS(null, "style", "stop-color:rgb(255,255,255);stop-opacity:1");

        Element stop2 = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "stop");
        stop2.setAttributeNS(null, "offset", "100%");
        stop2.setAttributeNS(null, "style", "stop-color:rgb(0,0,255);stop-opacity:1");

        //Embeds the SVG components defined into the <def> container for referenced elements
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
    /**
     * Get the arrow coordinates of the activities
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the Process and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        Element group = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "g");
        if (subActivities != null) {
            //Gets the start activity of the process
            ActivityInterface startActivity = subActivities.get(0);
            //Gets the end activity of the process
            ActivityInterface endActivity = subActivities.get(subActivities.size() - 1);
            //Get the coordinates of the entry arrow of the start activity
            SVGCoordinates exitCoords = getExitArrowCoords();
            //Define the arrow flow according to the coordinates
            group.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), startActivity.getEntryArrowCoords().getXLeft(), startActivity.getEntryArrowCoords().getYTop(), name));
            //Get the coordinates of the entry arrows of the end activity
            SVGCoordinates entryCoords = getEndEntryArrowCoords();
            //Define the arrow flow according to the coordinates
            group.appendChild(getArrowDefinition(doc, endActivity.getExitArrowCoords().getXLeft(), endActivity.getExitArrowCoords().getYTop(), entryCoords.getXLeft(), entryCoords.getYTop(), name));
        }
        return group;
    }

    /**
     * Gets the Link arrow coordinates when there is a FLOW activity in the process
     * @param doc SVG document which defines the components including shapes, gradients etc. of the process
     * @return An element which contains the link arrow coordinates of the Process
     */
    private Element getLinkArrows(SVGDocument doc) {
        Element group = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "g");
        //Checks whether the any links exist
        if (links != null && !links.isEmpty()) {
            //Returns a collection-view of the map with the link names, sources(starting activity) and the target(ending activity)
            Set linksSet = links.entrySet();
            Iterator linksIterator = linksSet.iterator();
            //Iterates through the links
            while (linksIterator.hasNext()) {
                Map.Entry<String, Link> link = (Map.Entry<String, Link>) linksIterator.next();
                //Gets the source/start activity of the link
                ActivityInterface startActivity = link.getValue().getSource();
                //Gets the target/end activity of the link
                ActivityInterface endActivity = link.getValue().getTarget();
                //Get the link name
                String linkName = link.getKey();
                //Check if the source and the target of the link contains a value
                if (endActivity != null && startActivity != null) {
                    //Define the link flow/path by giving the coordinates of the start and end activity
                    group.appendChild(drawLink(doc, startActivity.getExitArrowCoords().getXLeft(),
                            startActivity.getExitArrowCoords().getYTop(), endActivity.getEntryArrowCoords().getXLeft(),
                            endActivity.getEntryArrowCoords().getYTop(), startActivity.getStartIconWidth(),
                            link.getKey(), linkName));
                }
            }
        }
        return group;
    }

    /**
     *
     * @param doc               SVG document which defines the components including shapes, gradients etc. of the process
     * @param startX            x-coordinate of the start point
     * @param startY            y-coordinate of the start point
     * @param endX              x-coordinate of the end point
     * @param endY              y-coordinate of the end point
     * @param id                previous activity id + current activity id
     * @param startIconWidth    width of the startIcon
     * @param linkName          name of the link
     * @return
     */
    private Element drawLink(SVGDocument doc, int startX, int startY, int endX, int endY, int startIconWidth, String id, String linkName) {
        Element path = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "path");
         /*Arrows are created using  <path> : An element in svg used to create smooth, flowing lines using relatively few
          control points.
          A path element is defined by attribute: d. This attribute contains a series of commands for path data :
          M = move to
          L = line to
          Arrow flows will be generated according to the coordinates given
         */
        int firstBend = 20;
        if (layoutManager.isVerticalLayout()) {
            if (startY < endY) {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," + (startY + firstBend) +
                        " L " + startX + "," + (startY + firstBend) + " L " + endX + "," + (startY + firstBend) +
                        " L " + endX + "," + endY);                            //use constants for these propotions
            } else {
                if (startX > endX) {
                    path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," + (startY + firstBend) +
                            " L " + (startX - (startIconWidth / 2 + firstBend)) + "," + (startY + firstBend) + " L " +
                            (startX - (startIconWidth / 2 + firstBend)) + "," + (endY - firstBend) + " L " + endX + "," + (endY - firstBend) +
                            " L " + endX + "," + endY);                            //use constants for these propotions
                } else {
                    path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," + (startY + firstBend) +
                            " L " + (startX + (startIconWidth / 2 + firstBend)) + "," + (startY + firstBend) + " L " +
                            (startX + (startIconWidth / 2 + firstBend)) + "," + (endY - firstBend) + " L " + endX + "," + (endY - firstBend) +
                            " L " + endX + "," + endY);
                }
            }

        } else {
            path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + endY + " L " + endX + "," + endY);                              //use constants for these propotions
        }
        //Set the id
        path.setAttributeNS(null, "id", id);
        //Set the arrow style
        path.setAttributeNS(null, "style", getLinkArrowStyle());
        //Set the link name
        path.setAttributeNS("xlink", "title", linkName);
        return path;
    }

    /**
     *
     * @return String with the end tag of Process
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.PROCESS_END_TAG;
    }
    /**
     * At the start: width=0, height=0
     * @return dimensions of the composite activity i.e. the final width and height after doing calculations by iterating
     *         through the dimensions of the subActivities
     */
    @Override
    public SVGDimension getDimensions() {
        if (dimensions == null) {
            int width = 0;
            int height = 0;
            //Set the dimensions at the start to (0,0)
            dimensions = new SVGDimension(width, height);
            //Dimensons of the subActivities
            SVGDimension subActivityDim = null;
            ActivityInterface activity = null;
            Iterator<ActivityInterface> itr = getSubActivities().iterator();
            try {
                //Iterate through the subActivities
                while (itr.hasNext()) {
                    activity = itr.next();
                    //Gets the dimensions of each subActivity separately
                    subActivityDim = activity.getDimensions();
                    //Checks whether the width of the subActivity is greater than zero
                    if (subActivityDim.getWidth() > width) {
                        width += subActivityDim.getWidth();
                    }
                     /*As the Process should increase in height when the number of subActivities increase, height of each
                      subActivity is added to the height of the main/composite activity
                      */
                    height += subActivityDim.getHeight();
                }
            } catch (NoSuchElementException e) {
                log.error("Invalid Element access", e);
            }
            /*After iterating through all the subActivities and altering the dimensions of the composite activity
              to get more spacing , Xspacing and Yspacing is added to the height and the width of the composite activity
            */
            height += ((getYSpacing() * 2) + getStartIconHeight() + getEndIconHeight());
            width += getXSpacing();
            //Set the calculated dimensions
            dimensions.setWidth(width);
            dimensions.setHeight(height);
            //Set the final SVG height and width
            layoutManager.setSvgHeight(height);
            layoutManager.setSvgWidth(width);
            //Check if the layout is vertical or not
            if (!layoutManager.isVerticalLayout()) {
                switchDimensionsToHorizontal();
            }
        }
        return dimensions;
    }
    /**
     * Sets the layout of the process drawn
     * @param startXLeft x-coordinate of the activity
     * @param startYTop  y-coordinate of the activity
     */
    @Override
    public void layout(int startXLeft, int startYTop) {
        if (layoutManager.isVerticalLayout()) {
            layoutVertical(startXLeft, startYTop);
        } else {
            layoutHorizontal(startXLeft, startYTop);
        }
    }
    /**
     * Sets the x and y positions of the activities
     * At the start: startXLeft=0, startYTop=0
     * centreOfMyLayout- center of the the SVG
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     *
     */
    public void layoutVertical(int startXLeft, int startYTop) {
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        //Positioning the startIcon
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop + (getYSpacing() / 2);
        //Positioning the endIcon
        int endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        int endYTop = startYTop + dimensions.getHeight() - getEndIconHeight() - (getYSpacing() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft = startXLeft + (getXSpacing() / 2);
        //Iterate through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Sets the xLeft and yTop position of the iterated activity
            activity.layout(childXLeft, childYTop);
            childYTop += activity.getDimensions().getHeight();
        }

        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        //Sets the xLeft and yTop positions of the end icon
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        //Sets the xLeft and yTop positions of the SVG  of the process after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }
    /**
     * Sets the x and y positions of the activities
     * At the start: startXLeft=0, startYTop=0
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     * centreOfMyLayout- center of the the SVG
     */
    private void layoutHorizontal(int startXLeft, int startYTop) {
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        //Positioning the startIcon
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);
        //Positioning the endIcon
        int endXLeft = startXLeft + dimensions.getWidth() - getEndIconWidth() - (getYSpacing() / 2);
        int endYTop = centreOfMyLayout - (getEndIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);
        //Iterates through all the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Sets the xLeft and yTop position of the iterated activity
            activity.layout(childXLeft, childYTop);
            childXLeft += activity.getDimensions().getWidth();
        }

        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        //Sets the xLeft and yTop positions of the end icon
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        //Sets the xLeft and yTop positions of the SVG  of the process after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }
    /**
     * At the start: xLeft= xLeft of startIcon + (width of startIcon)/2, yTop= yTop of startIcon + height of startIcon
     * Calculates the coordinates of the arrow which leaves an activity
     * @return coordinates/exit point of the exit arrow for the activities
     */
    @Override
    public SVGCoordinates getExitArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop() + getStartIconHeight();
        if (!layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + getStartIconWidth();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
        }
        //Returns the calculated coordinate points of the exit arrow
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);
        return coords;
    }
    /**
     * At the start: xLeft= xLeft of endIcon + (width of endIcon)/2, yTop= yTop of endIcon
     * Calculates the coordinates of the entry arrow of the end activity
     * @return coordinates/exit point of the entry arrow for the end activity
     */
    public SVGCoordinates getEndEntryArrowCoords() {
        int xLeft = getEndIconXLeft() + (getEndIconWidth() / 2);
        int yTop = getEndIconYTop();
        if (!layoutManager.isVerticalLayout()) {
            xLeft = getEndIconXLeft();
            yTop = getEndIconYTop() + (getEndIconHeight() / 2);
        }
        //Returns the calculated coordinate points of the entry arrow for end activity
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);
        return coords;
    }
    /**
     * Adds opacity to icons
     * @return true or false
     */
    @Override
    public boolean isAddOpacity() {
        return isAddCompositeActivityOpacity();
    }
    /**
     *
     * @return String with the opacity value
     */
    @Override
    public String getOpacity() {
        return getCompositeOpacity();
    }
}

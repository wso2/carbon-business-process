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
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.wso2.carbon.bpel.ui.bpel2svg.*;

import java.util.Iterator;

/**
 * Flow tag UI implementation
 */
public class FlowImpl extends ActivityImpl implements FlowInterface {

	 /**
     * Initializes a new instance of the FlowImpl class using the specified string i.e. the token
     * @param token
     */
    public FlowImpl(String token) {
        super(token);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }
	 /**
     * Initializes a new instance of the FlowImpl class using the specified omElement
     * @param omElement which matches the Flow tag
     */
    public FlowImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }
	/**
     * Initializes a new instance of the FlowImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an Flow Activity when processing the subActivities
     * of the process
     * @param omElement which matches the Flow tag
     * @param parent
     */
    public FlowImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());

        // setVerticalChildLayout(false);
    }

    /**
     *
     * @return String with name of the activity
     */
    @Override
    public String getId() {
        return getName(); // + "-Flow";
    }

    /**
     *
     * @return- String with the end tag of Flow Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.FLOW_END_TAG;
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

            //Iterates through the subActivites inside the composite activity
            Iterator<ActivityInterface> itr = getSubActivities().iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the dimensions of each subActivity separately
                subActivityDim = activity.getDimensions();
                //Checks whether the height of the subActivity is greater than zero
                if (subActivityDim.getHeight() > height) {
                    height += subActivityDim.getHeight();
                }
                //Width of each subActivity is added to the final width of the main/composite activity
                width += subActivityDim.getWidth();
            }
             /*After iterating through all the subActivities and altering the dimensions of the composite activity
              to get more spacing , Xspacing and Yspacing is added to the height and the width of the composite activity
            */
            height += (getYSpacing() * 2) + getStartIconHeight() + getEndIconHeight();
            width += getXSpacing();
            //Set the Calculated dimensions for the SVG height and width
            dimensions.setWidth(width);
            dimensions.setHeight(height);
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
        if (dimensions != null) {
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
            //Iterates through all the subActivities
            while (itr.hasNext()) {
                activity = itr.next();
                /* If the activity inside Flow activity is an instance of If activity, then setCheckIfinFlow becomes true.
                This If check is done to space the subActivities when an If activity is inside a Flow activity.
                This is a special case.
                 */
                if (activity instanceof IfImpl) {
                    ((IfImpl) activity).setCheckIfinFlow(true);
                }
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
			//Sets the xLeft and yTop positions of the start icon text
            setStartIconTextXLeft(startXLeft + BOX_MARGIN);
            setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
			//Sets the xLeft and yTop positions of the SVG  of the composite activity after setting the dimensions
            getDimensions().setXLeft(startXLeft);
            getDimensions().setYTop(startYTop);
        }
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
            childYTop += activity.getDimensions().getHeight();
        }

        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        //Sets the xLeft and yTop positions of the end icon
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        //Sets the xLeft and yTop positions of the start icon text
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        //Sets the xLeft and yTop positions of the SVG of the composite activity after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which enters an activity
     * @return coordinates/entry point of the entry arrow for the activities
     * After Calculations(Vertical Layout): xLeft=Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     */
    @Override
    public SVGCoordinates getEntryArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop();
        } else {
            xLeft = getStartIconXLeft();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);

        }
        //Returns the calculated coordinate points of the entry arrow
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which leaves an activity
     * @return coordinates/exit point of the exit arrow for the activities
     */
    @Override
    public SVGCoordinates getExitArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getEndIconXLeft() + (getEndIconWidth() / 2);
            yTop = getEndIconYTop() + getEndIconHeight();
        } else {
            xLeft = getEndIconXLeft() + getEndIconWidth();
            yTop = getEndIconYTop() + (getEndIconHeight() / 2);

        }
        //Returns the calculated coordinate points of the exit arrow
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }


    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which leaves the start Flow Icon
     * @return coordinates of the exit arrow for the start icon
     * After Calculations(Vertical Layout): xLeft= Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon + height of the icon
     */
    protected SVGCoordinates getStartIconExitArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop() + getStartIconHeight();
        } else {
            xLeft = getStartIconXLeft() + getStartIconWidth();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);

        }
        //Returns the calculated coordinate points of the exit arrow of the startIcon
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which enters the end icon
     * @return coordinates of the entry arrow for the end icon
     * After Calculations(Vertical Layout): xLeft= Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     */
    protected SVGCoordinates getEndIconEntryArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getEndIconXLeft() + (getEndIconWidth() / 2);
            yTop = getEndIconYTop();
        } else {
            xLeft = getEndIconXLeft();
            yTop = getEndIconYTop() + (getEndIconHeight() / 2);

        }
        //Returns the calculated coordinate points of the entry arrow of the endIcon
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }
	 /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Flow composite activity
     */
    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Get the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        // Check if Layer & Opacity required
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        group.appendChild(getBoxDefinition(doc));
        //Get the start icon of the activity
        group.appendChild(getImageDefinition(doc));
        //Get the start icon image text
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Get the end icon of the activity
        group.appendChild(getEndImageDefinition(doc));
        //Get the arrow flows of the Flow activity
        group.appendChild(getArrows(doc));

        return group;

    }
    /**
     * Get the arrow coordinates of the activities
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the Flow activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Gets the coordinates of the Flow start icon
        SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
        //Gets the coordinates of the Flow end icon
        SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
        //Arrow flow/path coordinates of the start and end icons of the flow activity
        //Arrow Flow Coordinates of the Start Flow Icon
        subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(),
                myStartCoords.getXLeft(), (myStartCoords.getYTop() + 30), "Flow_Top", true));
        subGroup.appendChild(getArrowDefinition(doc, (myStartCoords.getXLeft() - dimensions.getWidth() / 2 + getXSpacing()),
                (myStartCoords.getYTop() + 30), (myStartCoords.getXLeft() + dimensions.getWidth() / 2 - getXSpacing()),
                (myStartCoords.getYTop() + 30), "Flow_TopH", true));
        //Arrow Flow Coordinates of the End Flow Icon
        subGroup.appendChild(getArrowDefinition(doc, (myStartCoords.getXLeft() - dimensions.getWidth() / 2 + getXSpacing()),
                (myExitCoords.getYTop() - 20), (myStartCoords.getXLeft() + dimensions.getWidth() / 2 - getXSpacing()),
                (myExitCoords.getYTop() - 20), "Flow_DownH", true));
        subGroup.appendChild(getArrowDefinition(doc, myExitCoords.getXLeft(), myExitCoords.getYTop() - 20,
                myExitCoords.getXLeft(), myExitCoords.getYTop(), "Flow_Top", false));

        return subGroup;
    }
    /**
     * Get the arrow flows/paths from the coordinates given by getArrows()
     * @param doc
     * @param startX  x-coordinate of the start point
     * @param startY  y-coordinate of the start point
     * @param endX    x-coordinate of the end point
     * @param endY    y-coordinate of the end point
     * @param id      previous activity id + current activity id
     * @param to      true/false for the arrow style
     * @return An element which contains the arrow flows/paths of the Flow activity and its subActivities
     */
    public Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id, boolean to) {         //here we have to find whether
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");

         /*Arrows are created using  <path> : An element in svg used to create smooth, flowing lines using relatively few
          control points.
          A path element is defined by attribute: d. This attribute contains a series of commands for path data :
          M = move to
          L = line to
          Arrow flows will be generated according to the coordinates given
         */

        if ((startX == endX) || (startY == endY)) {
            path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + endX + "," + endY);
        } else {
            if (to) {
                if (layoutManager.isVerticalLayout()) {
                    path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," +
                            ((startY + 2 * endY) / 3) + " L " + endX + "," + ((startY + 2 * endY) / 3));                            //use constants for these propotions
                } else {
                    path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1 * endX) / 2) +
                            "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + endY);                              //use constants for these propotions
                }
            } else {
                if (layoutManager.isVerticalLayout()) {
                    path.setAttributeNS(null, "d", "M " + startX + "," + ((startY + 2 * endY) / 3) + " L " + endX + "," + ((startY + 2 * endY) / 3) + " L " + endX + "," + endY);                            //use constants for these propotions
                } else {
                    path.setAttributeNS(null, "d", "M " + ((startX + 1 * endX) / 2) + "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + endY + " L " + endX + "," + endY);                              //use constants for these propotions
                }

            }
        }
        //Set the id of the path
        path.setAttributeNS(null, "id", id);
        //Add styles to the arrows
        path.setAttributeNS(null, "style", getArrowStyle(to));
        return path;
    }

    /**
     *
     * @param to boolean variable true/false for the arrow style
     * @return String with the arrow styling attributes
     */
    private String getArrowStyle(boolean to) {
        if (to) {
            String largeArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:butt;stroke-linejoin:round;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1";
            String mediumArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:butt;stroke-linejoin:round;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1";

            if (isLargeArrow()) {
                return largeArrowStr;
            } else {
                return mediumArrowStr;
            }
        } else {
            String largeArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:butt;stroke-linejoin:round;marker-end:url(#Arrow1Lend);stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1";
            String mediumArrowStr = "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.0;stroke-linecap:butt;stroke-linejoin:round;marker-end:url(#Arrow1Mend);stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1";

            if (isLargeArrow()) {
                return largeArrowStr;
            } else {
                return mediumArrowStr;
            }
        }

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

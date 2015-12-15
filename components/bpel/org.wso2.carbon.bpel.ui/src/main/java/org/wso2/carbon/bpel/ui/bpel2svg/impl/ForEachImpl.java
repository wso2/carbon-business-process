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
 * ForEach tag UI implementation
 */
public class ForEachImpl extends ActivityImpl implements ForEachInterface {
    /**
     * Initializes a new instance of the ForEachImpl class using the specified string i.e. the token
     *
     * @param token
     */
    public ForEachImpl(String token) {
        super(token);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());

    }

    /**
     * Initializes a new instance of the ForEachImpl class using the specified omElement
     *
     * @param omElement which matches the ForEach tag
     */
    public ForEachImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());

    }

    /**
     * Initializes a new instance of the ForEachImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an ForEach Activity when processing the subActivities
     * of the process
     *
     * @param omElement which matches the ForEach tag
     * @param parent
     */
    public ForEachImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * @return String with name of the activity
     */
    @Override
    public String getId() {
        return getName() + "-ForEach";
    }

    /**
     * @return- String with the end tag of ForEach Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.FOREACH_END_TAG;
    }

    /**
     * At the start: width=0, height=0
     *
     * @return dimensions of the composite activity i.e. the final width and height after doing calculations by iterating
     * through the dimensions of the subActivities
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
                //Checks whether the width of the subActivity is greater than zero
                if (subActivityDim.getWidth() > width) {
                    width += subActivityDim.getWidth();
                }
                /*As ForEach should increase in height when the number of subActivities increase, height of each subActivity
                  is added to the height of the main/composite activity
                */
                height += subActivityDim.getHeight();
            }
            /*After iterating through all the subActivities and altering the dimensions of the composite activity
              to get more spacing , Xspacing and Yspacing is added to the height and the width of the composite activity
            */
            height += ((getYSpacing() * 2) + getStartIconHeight() + getEndIconHeight());
            width += getXSpacing();

            //Set the Calculated dimensions for the SVG height and width
            dimensions.setWidth(width);
            dimensions.setHeight(height);
        }
        return dimensions;
    }

    /**
     * Sets the layout of the process drawn
     *
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
     *
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     */
    public void layoutVertical(int startXLeft, int startYTop) {
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        //Positioning the startIcon
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop - (getYSpacing() / 4);
        //Positioning the endIcon
        int endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        int endYTop = startYTop + dimensions.getHeight() + 15 - getEndIconHeight();

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childYTop = yTop + getStartIconHeight() + getYSpacing();
        int childXLeft = startXLeft + (getXSpacing() / 2);
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
        //Sets the xLeft and yTop positions of the SVG  of the composite activity after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    /**
     * Sets the x and y positions of the activities
     * At the start: startXLeft=0, startYTop=0
     *
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     *                   centreOfMyLayout- center of the the SVG
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
     *
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
     *
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
     * Calculates the coordinates of the arrow which leaves the start ForEach Icon
     *
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
     *
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
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the ForEach composite activity
     */
    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Get the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        //Checks for the icon opacity
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        //Get the scope/box of ForEach which contains the subActivities
        group.appendChild(getBoxDefinition(doc));
        //Get the icons of the activities i.e. create/define the activity icons
        group.appendChild(getImageDefinition(doc));
        //Get the start icon/image text of the activity
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Get the end image icon for the activity
        group.appendChild(getEndImageDefinition(doc));
        //Get the arrow flows of the subActivities inside the ForEach composite activity
        group.appendChild(getArrows(doc));

        return group;
    }

    /**
     * Get the arrow coordinates of the activities
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the ForEach activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        //Creating an SVG Container "g"
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Checks for the subActivities
        if (subActivities != null) {
            ActivityInterface activity = null;
            String id = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            SVGCoordinates activityExitCoords = null;
            SVGCoordinates activityEntryCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            //Iterates through all the subActivities
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the entry and exit coordinates of the iterated activity
                activityExitCoords = activity.getExitArrowCoords();
                activityEntryCoords = activity.getEntryArrowCoords();
                //Gives the coordinates of the entry arrow to the first activity from the startIcon
                subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                //Gives the coordinates of the entry arrow to the endIcon of ForEach
                subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
            }
        }
        return subGroup;
    }

    /**
     * Adds opacity to icons
     *
     * @return true or false
     */
    @Override
    public boolean isAddOpacity() {
        return isAddCompositeActivityOpacity();
    }

    /**
     * @return String with the opacity value
     */
    @Override
    public String getOpacity() {
        return getCompositeOpacity();
    }

}

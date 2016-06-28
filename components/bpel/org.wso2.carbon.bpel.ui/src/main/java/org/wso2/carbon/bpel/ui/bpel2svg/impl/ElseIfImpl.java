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
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.ElseIfInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension;

import java.util.Iterator;
import java.util.List;

/**
 * ElseIf tag UI implementation
 */
public class ElseIfImpl extends ActivityImpl implements ElseIfInterface {
    private static final Log log = LogFactory.getLog(ElseIfImpl.class);

    //Variable to check whether a throw activity is inside ElseIf
    public boolean throwOrNot;

    /**
     * Initializes a new instance of the ElseIfImpl class using the specified string i.e. the token
     *
     * @param token
     */
    public ElseIfImpl(String token) {
        super(token);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * Initializes a new instance of the ElseIfImpl class using the specified omElement
     *
     * @param omElement which matches the ElseIf tag
     */
    public ElseIfImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * Initializes a new instance of the ElseIfImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an ElseIf Activity when processing the subActivities
     * of the process
     *
     * @param omElement which matches the ElseIf tag
     * @param parent
     */
    public ElseIfImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * @return String with the name of the activity
     */
    @Override
    public String getId() {
        return getName(); // + "-ElseIf";
    }

    /**
     * @return- String with the end tag of ElseIf Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.ELSEIF_END_TAG;
    }

    /**
     * At the start: width=0, height=0
     *
     * @return dimensions of the composite activity i.e. the final width and height after doing calculations by
     * iterating
     * through the dimensions of the subActivities
     */
    @Override
    public SVGDimension getDimensions() {
        if (dimensions == null) {
            int width = 0;
            int height = 0;
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
                /*As ElseIf should increase in height when the number of subActivities increase, height of each
                subActivity
                  is added to the height of the main/composite activity
                */
                height += subActivityDim.getHeight();
            }

            /*After iterating through all the subActivities and altering the dimensions of the composite activity
              to get more spacing Xspacing and Yspacing is added to the height and the width of the composite activity
            */
            height += getYSpacing() + getStartIconHeight();
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
     *
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     *                   centreOfMyLayout- center of the the SVG
     */
    public void layoutVertical(int startXLeft, int startYTop) {
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        //Positioning the startIcon
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop + (getYSpacing() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft;
        //Iterates through all the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Sets the xLeft position of the iterated activity : childXleft= center of the layout - (width of the
            // activity icon)/2
            childXLeft = centreOfMyLayout - activity.getDimensions().getWidth() / 2;
            //Sets the xLeft and yTop position of the iterated activity
            activity.layout(childXLeft, childYTop);
            childYTop += activity.getDimensions().getHeight();
        }

        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
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
    public void layoutHorizontal(int startXLeft, int startYTop) {
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        //Positioning the startIcon
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop;
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        //Iterates through all the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Sets the yTop position of the iterated activity : childYTop= center of layout -(height of the activity)/2
            childYTop = centreOfMyLayout - (activity.getDimensions().getHeight() / 2);
            //Sets the xLeft and yTop position of the iterated activity
            activity.layout(childXLeft, childYTop);
            childXLeft += activity.getDimensions().getWidth();
        }
        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
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
        xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        yTop = getStartIconYTop();
        // TODO : Review this code.
//        if (layoutManager.isVerticalLayout()) {
//            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
//            yTop = getStartIconYTop();
//        } else {
//            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
//            yTop = getStartIconYTop();
//
//        }
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
        //Exit arrow coordinates are calculated by invoking getStartIconExitArrowCoords()
        SVGCoordinates coords = getStartIconExitArrowCoords();
        //Checks for any subActivities
        if (subActivities != null && subActivities.size() > 0) {
            ActivityInterface activity = subActivities.get(subActivities.size() - 1);
            coords = activity.getExitArrowCoords();
        }
        //Returns the calculated coordinate points of the exit arrow
        return coords;
    }

    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which leaves the start ElseIf Icon
     *
     * @return coordinates of the exit arrow for the start icon
     * After Calculations(Vertical Layout): xLeft= Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon +
     * height of the icon
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
     * Calculates the coordinates of the exit arrow of Else Activity
     *
     * @return coordinates of the exit arrow for the Else Activity
     * After Calculations(Vertical Layout): xLeft= Xleft of Icon + width of icon , yTop= Ytop of the Icon + (height
     * of the icon)/2
     */
    public SVGCoordinates getNextElseExitArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + getStartIconWidth();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
        } else {
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop() + getStartIconHeight();

        }
        //Returns the calculated coordinate points of the exit arrow of the startIcon
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the ElseIf
     * composite activity
     */
    public Element getSVGString(SVGDocument doc) {

        Element group1 = null;
        group1 = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "g");
        //Get the id of the activity
        group1.setAttributeNS(null, "id", getLayerId());
        //Add opacity to the icons
        if (isAddOpacity()) {
            group1.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        //Get the icons of the activities i.e. create/define the activity icons
        group1.appendChild(getImageDefinition(doc));
        //Get sub activities
        group1.appendChild(getSubActivitiesSVGString(doc));
        //Get the arrow flows of the subActivities inside the ElseIf composite activity
        group1.appendChild(getArrows(doc));

        return group1;
    }

    /**
     * Get the arrow coordinates of the activities
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the ElseIf activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        //Checks for the subActivities
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            ActivityInterface seqActivity = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
//            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            SVGCoordinates exitCoords = null;
            SVGCoordinates activityEntryCoords = null;
//            SVGCoordinates activityExitCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            //Creating an SVG Container "g"
            Element subGroup = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "g");

            //Iterates through all the subActivities
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the entry and exit coordinates of the iterated activity
                activityEntryCoords = activity.getEntryArrowCoords();
//                activityExitCoords = activity.getExitArrowCoords();

                /*If the activity is a Sequence, then all the subActivities inside the Sequence is iterated and
                checked for
                any Throw activities inside it.
                If a Throw activity is present : throwOrNot =true ,
                Else : throwOrNot =false
                 */
                if (activity instanceof SequenceImpl) {

                    List<ActivityInterface> sub = activity.getSubActivities();
                    Iterator<ActivityInterface> as = sub.iterator();
                    while (as.hasNext()) {
                        seqActivity = as.next();
                        if (seqActivity instanceof ThrowImpl) {
                            throwOrNot = true;
                            break;
                        } else {
                            throwOrNot = false;
                        }
                    }
                }
                //Checks whether the activity is a Throw activity
                if (activity instanceof ThrowImpl) {
                    throwOrNot = true;
                }
                //Checks whether the previous activity is null
                if (prevActivity != null) {
                    //Get the exit arrow coordinates of the previous activity
                    exitCoords = prevActivity.getExitArrowCoords();
                    // id is assigned with the id of the previous activity + id of the current activity
                    id = prevActivity.getId() + "-" + activity.getId();
                     /*If the previous activity is not null, then arrow flow is from the previous activity to the
                     current activity
                      This gives the coordinates of the start point and the end point
                    */
                    subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(),
                            activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else {
                    /*If the previous activity is null, then arrow flow is directly from the startIcon to the activity
                      This gives the coordinates of the start point and the end point
                    */
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(),
                            activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                }
                //current activity is assigned to the previous activity
                prevActivity = activity;
            }
            return subGroup;
        }
        return null;
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

    /**
     * Get the arrow flows/paths from the coordinates given by getArrows()
     *
     * @param doc
     * @param startX x-coordinate of the start point
     * @param startY y-coordinate of the start point
     * @param endX   x-coordinate of the end point
     * @param endY   y-coordinate of the end point
     * @param id     previous activity id + current activity id
     * @return An element which contains the arrow flows/paths of the ElseIf activity and its subActivities
     */
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {
        Element path = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "path");
        /*Arrows are created using  <path> : An element in svg used to create smooth, flowing lines using relatively few
          control points.
          A path element is defined by attribute: d. This attribute contains a series of commands for path data :
          M = move to
          L = line to
          Arrow flows will be generated according to the coordinates given
         */
        if (startX == endX || startY == endY) {
            path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + endX + "," + endY);
        } else {
            if (layoutManager.isVerticalLayout()) {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," +
                        ((startY + 2 * endY) / 3) + " L " + endX + "," + ((startY + 2 * endY) / 3) + " L " + endX +
                        "," + endY);
            } else {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1 * endX) / 2) +
                        "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + endY + " L " + endX + "," + endY);
                //use constants for these propotions
            }
        }
        //Set the id of the path
        path.setAttributeNS(null, "id", id);
        //Add styles to the arrows
        path.setAttributeNS(null, "style", getArrowStyle());

        return path;
    }
}

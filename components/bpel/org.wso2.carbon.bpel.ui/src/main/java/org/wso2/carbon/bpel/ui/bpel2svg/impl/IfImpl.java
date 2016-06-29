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
import java.util.*;

/**
 * If tag UI implementation
 */
public class IfImpl extends ActivityImpl implements IfInterface {
    private static final Log log = LogFactory.getLog(IfImpl.class);
    private SVGDimension coreDimensions = null;
    private SVGDimension conditionalDimensions = null;

    /**
     * Initializes a new instance of the IfImpl class using the specified string i.e. the token
     *
     * @param token
     */
    public IfImpl(String token) {
        super(token);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    /**
     * Initializes a new instance of the IfImpl class using the specified omElement
     *
     * @param omElement which matches the If tag
     */
    public IfImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    /**
     * Initializes a new instance of the IfImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an If Activity when processing the subActivities
     * of the process
     *
     * @param omElement which matches the If tag
     * @param parent
     */
    public IfImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    /**
     * At start: adjustment=0
     *
     * @return int with the spacing/position of the ElseIf activity when it is inside a If condition
     * After Calculation (Vertical): adjustment= icon height+ YSpacing
     */
    protected int getElseIfAdjustment() {
        int adjustment = 0;
        if (layoutManager.isVerticalLayout()) {
            adjustment = getStartIconHeight() + getYSpacing();
        } else {
            adjustment = getStartIconWidth() + getYSpacing();
        }

        return adjustment;
    }

    /**
     * @return String with name of the activity
     */
    @Override
    public String getId() {
        return getName(); // + "-If";
    }

    /**
     * @return String with the end tag of If Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.IF_END_TAG;
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
            int coreWidth = 0;
            int coreHeight = 0;
            int conWidth = 0;
            int conHeight = 0;
            //Set the dimensions at the start to (0,0)
            dimensions = new SVGDimension(coreWidth, coreHeight);
            coreDimensions = new SVGDimension(coreWidth, coreHeight);
            conditionalDimensions = new SVGDimension(conWidth, conHeight);
            //Dimensons of the subActivities
            SVGDimension subActivityDim = null;
            ActivityInterface activity = null;
            //Iterates through the subActivites inside the composite activity
            Iterator<ActivityInterface> itr = getSubActivities().iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the dimensions of each subActivity separately
                subActivityDim = activity.getDimensions();
                //Checks whether the subActivity is a ElseIf or Else activity
                if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                    //Checks whether the icon height is greater than the conditional height
                    if (subActivityDim.getHeight() > conHeight) {
                        //height of the icon is added to the conditional height
                        conHeight += subActivityDim.getHeight();
                    }
                    //width of the subActivities added to the conditional width
                    conWidth += subActivityDim.getWidth();
                } else {
                    //If the subActivites are not instances of ElseIf and Else
                    if (subActivityDim.getWidth() > coreWidth) {
                        //width of the subActivities added to the core width
                        coreWidth += subActivityDim.getWidth();
                    }
                    //height of the subActivities added to the core height
                    coreHeight += subActivityDim.getHeight();
                }
            }
            //Spacing the core height by adding ySpacing + startIcon height + endIcon height
            coreHeight += getYSpacing() + getStartIconHeight() + getEndIconHeight();
            /* The ElseIf spacing or adjustment is added to the conditional height as the conditional dimensions are associated
               with ElseIf and Else activities
            */
            conHeight += getElseIfAdjustment();
            //Setting the core dimensions after calculations
            coreDimensions.setHeight(coreHeight);
            coreDimensions.setWidth(coreWidth);
            //Setting the conditional dimensions after calculations
            conditionalDimensions.setHeight(conHeight);
            conditionalDimensions.setWidth(conWidth);
            //Checks if the core height is greater than the conditional height
            if (coreHeight > conHeight) {
                height = coreHeight;
            } else {
                height = conHeight;
            }
            //core width and conditional width is added to the final width of the composite activity
            width = coreWidth + conWidth;
            height += getYSpacing();
            width += getXSpacing();
            //Set the Calculated dimensions for the SVG height and width
            dimensions.setWidth(width);
            dimensions.setHeight(height);
        }

        return dimensions;
    }

    /**
     * @return core dimensions of the activity
     */
    public SVGDimension getCoreDimensions() {
        return coreDimensions;
    }

    /**
     * @return conditional dimensions of the activity
     */
    public SVGDimension getConditionalDimensions() {
        return conditionalDimensions;
    }

    /**
     * Switch the dimensions of the activity to horizontal
     */
    @Override
    public void switchDimensionsToHorizontal() {
        super.switchDimensionsToHorizontal();
        int height = 0;
        int width = 0;
        // Switch Core Dimensions
        height = coreDimensions.getHeight();
        width = coreDimensions.getWidth();
        coreDimensions.setHeight(width);
        coreDimensions.setWidth(height);
        // Switch Conditional Dimensions
        height = conditionalDimensions.getHeight();
        width = conditionalDimensions.getWidth();
        conditionalDimensions.setHeight(width);
        conditionalDimensions.setWidth(height);
    }

    /**
     * @return false- if the subActivities are instances of ElseIf or Else
     * true - otherwise
     */
    private boolean isSimpleLayout() {
        boolean simple = true;

        ActivityInterface activity = null;
        //Iterates through the subActivities
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        while (itr.hasNext()) {
            activity = itr.next();
            //if the activity is an instance of ElseIf or Else, break the if condition
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                simple = false;
                break;
            }
        }

        return simple;
    }

    /**
     * Sets the layout of the activity
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
        int centerNHLayout = startXLeft + (coreDimensions.getWidth() / 2);
        /* Checks whether its a simple layout i.e. whether the subActivities are instances of ElseIf or Else
           if so --> true , else --> false
         */
        if (isSimpleLayout()) {
            xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
            endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        } else {
            xLeft = centerNHLayout - (getStartIconWidth() / 2) + (getXSpacing() / 2);
            endXLeft = centerNHLayout - (getEndIconWidth() / 2) + (getXSpacing() / 2);
        }

        ActivityInterface activity = null;
        //Iterates through the subActivities
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childYTop = yTop + getStartIconHeight() + getYSpacing();
        int childXLeft = startXLeft + (getXSpacing() / 2);

        // Process None Handlers First
        while (itr.hasNext()) {
            activity = itr.next();
            /*
            * This if check is a special case. It is done only when there is a If activity  inside a Flow activity.
            * When Flow activity iterates its subActivities, if a IF acivity is present it makes the isCheckIfinFlow(true).
            * This is done to increase the spacing of the subActivities inside IF when its parent is a FLOW activity
            * */
            if (this.isCheckIfinFlow() == true) {
                //Checks whether the subActivity is a Sequence activity
                if (activity instanceof SequenceImpl) {
                    childYTop = childYTop + getEndIconWidth() / 2;
                    //Sets the xLeft and yTop position of the iterated activity
                    activity.layout(childXLeft, childYTop);
                    childXLeft += activity.getDimensions().getWidth();
                } else {
                    //For all other activities except for Sequence
                    childYTop = childYTop + getEndIconWidth() / 2 + 20;
                    //Sets the xLeft and yTop position of the iterated activity
                    activity.layout(childXLeft, childYTop);
                    childXLeft += activity.getDimensions().getWidth();
                }
            } else {
                //Checks whether the iterated activity is an ElseIf or an Else
                if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                } else {
                    //Sets the xLeft and yTop position of the iterated activity
                    activity.layout(childXLeft, childYTop);
                    childXLeft += activity.getDimensions().getWidth();
                }
            }
        }
        // Process Handlers

        itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        childXLeft = startXLeft + coreDimensions.getWidth();
        childYTop = yTop + getElseIfAdjustment();
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the iterated activity is an ElseIf or an Else
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                //Sets the xLeft and yTop position of the iterated activity
                activity.layout(childXLeft, childYTop);
                childXLeft += activity.getDimensions().getWidth();
            }
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
        int centerNHLayout = startYTop + (coreDimensions.getHeight() / 2);
         /* Checks whether its a simple layout i.e. whether the subActivities are instances of ElseIf or Else
           if so --> true , else --> false
         */
        if (isSimpleLayout()) {
            yTop = centreOfMyLayout - (getStartIconHeight() / 2);
            endYTop = centreOfMyLayout - (getEndIconHeight() / 2);
        } else {
            yTop = centerNHLayout - (getStartIconHeight() / 2);
            endYTop = centerNHLayout - (getEndIconHeight() / 2);
        }

        ActivityInterface activity = null;
        //Iterates through the subActivities
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);

        // Process None Handlers First
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the iterated activity is an ElseIf or an Else
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
            } else {
                //Sets the xLeft and yTop position of the iterated activity
                activity.layout(childXLeft, childYTop);
                childYTop += activity.getDimensions().getHeight();
            }
        }
        // Process Handlers
        itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        childYTop = startYTop + coreDimensions.getHeight();
        childXLeft = xLeft + getElseIfAdjustment();
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the iterated activity is an ElseIf or an Else
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                //Sets the xLeft and yTop position of the iterated activity
                activity.layout(childXLeft, childYTop);
                childYTop += activity.getDimensions().getHeight();
            }
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
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop();

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
     * Calculates the coordinates of the arrow which leaves the start If Icon
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
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which enters the Else activity
     *
     * @return coordinates of the entry arrow for Else start icon
     * After Calculations(Vertical Layout): xLeft= Xleft of Icon + width of icon, yTop= Ytop of the Icon + (height of icon)/2
     */
    protected SVGCoordinates getStartIconElseArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + getStartIconWidth();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
        } else {
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop() + getStartIconHeight();

        }
        //Returns the calculated coordinate points of the entry arrow of the Else icon
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the If composite activity
     */
    public Element getSVGString(SVGDocument doc) {
        Element group1 = null;
        group1 = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "g");
        //Get the id of the activity
        group1.setAttributeNS(null, "id", getLayerId());
        //Checks for the icon opacity
        if (isAddOpacity()) {
            group1.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        //Get the If composite activity box/scope where the subActivities are placed
        group1.appendChild(getBoxDefinition(doc));
        //Get the icon definition of the activity
        group1.appendChild(getImageDefinition(doc));
        //Get the start icon/image text of the activity
        group1.appendChild(getStartImageText(doc));
        // Get Sub Activities
        group1.appendChild(getSubActivitiesSVGString(doc));
        //Get the end icon of the activity
        group1.appendChild(getEndImageDefinition(doc));
        //Get the arrow flows of the subActivities inside the Sequence composite activity
        group1.appendChild(getArrows(doc));

        return group1;
    }

    /**
     * Get the arrow coordinates of the activities
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the If activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface prevElseActivity = null;
            ActivityInterface activity = null;
            ActivityInterface seqActivity = null;
            boolean throwOrNot = true;
            String id = null;
            //Coordinates of the start icon exit arrow
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            //Coordinates of the end icon entry arrow
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            //Coordinates of the Else activity start icon entry arrow
            SVGCoordinates myStartElseCoords = getStartIconElseArrowCoords();
            SVGCoordinates exitCoords = null;
            SVGCoordinates activityEntryCoords = null;
            SVGCoordinates activityExitCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            Element subGroup = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "g");
            //Iterates through all the subActivities
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the entry and exit coordinates of the iterated activity
                activityEntryCoords = activity.getEntryArrowCoords();
                activityExitCoords = activity.getExitArrowCoords();
                //Checks if the iterated activity is an instance of ElseIf or Else activity
                if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                    //Checks whether there is a  previous activity and if so whether that activity is an ElseIf
                    if (prevActivity != null && prevActivity instanceof ElseIfImpl) {
                        //Get the exit arrow coordinates of the ElseIf activity
                        exitCoords = ((ElseIfInterface) prevActivity).getNextElseExitArrowCoords();
                        // id is assigned with the id of the previous activity + id of the current activity
                        id = prevActivity.getId() + "-" + activity.getId();
                        //Checks whether the activity is an instance of Else
                        if (activity instanceof ElseImpl) {
                            //Gets the boolean value assigned inside ElseImpl when a throw activity is in Else
                            boolean check = ((ElseImpl) activity).throwOrNot;
                            //Define the entry arrow flow coordinates for the activity
                            subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), activityEntryCoords.getXLeft() - getEndIconWidth() / 2, exitCoords.getYTop(), id));
                            //If there is a Throw activity inside Else, no exit arrow from Throw activity
                            if (check != true) {
                                subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                            }
                        }
                        //Checks whether the activity is an instance of ElseIf
                        else if (activity instanceof ElseIfImpl) {
                            //Gets the boolean value assigned inside ElseIfImpl when a throw activity is in ElseIf
                            boolean check = ((ElseIfImpl) activity).throwOrNot;
                            //Define the entry arrow flow coordinates for the activity
                            subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                            //If there is a Throw activity inside ElseIf, no exit arrow from Throw activity
                            if (check != true) {
                                subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                            }
                        }
                        //Entry and exit arrow flows defined for other activities except for instances of Elseif and Else
                        else {
                            subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                            subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                        }
                    }
                    //IF conditon fifnished --> ELSE IF ( previous activity is null and its is not an instance of ElseIf)
                    /*Checks if the previous activity is a Throw and if the current activity an Else, if so no exit arrow flows
                      only entry arrow flows.
                    */
                    else if (prevActivity instanceof ThrowImpl && activity instanceof ElseImpl) {
                        subGroup.appendChild(getArrowDefinition(doc, myStartElseCoords.getXLeft(), myStartElseCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                    }
                    //Checks whether the activity is an instance of ElseIf
                    else if (activity instanceof ElseIfImpl) {
                        //Gets the boolean value assigned inside ElseIfImpl when a throw activity is in ElseIf
                        boolean check = ((ElseIfImpl) activity).throwOrNot;
                        //Define the entry arrow flow coordinates for the activity
                        subGroup.appendChild(getArrowDefinition(doc, myStartElseCoords.getXLeft(), myStartElseCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                        //If there is a Throw activity inside ElseIf, no exit arrow from Throw activity
                        if (check != true) {
                            subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                        }
                    }
                    //Checks whether the activity is an instance of Else
                    else if (activity instanceof ElseImpl) {
                        //Gets the boolean value assigned inside ElseIfImpl when a throw activity is in Else
                        boolean check = ((ElseImpl) activity).throwOrNot;
                        //Define the entry arrow flow coordinates for the activity
                        subGroup.appendChild(getArrowDefinition(doc, myStartElseCoords.getXLeft(), myStartElseCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                        //If there is a Throw activity inside Else, no exit arrow from Throw activity
                        if (check != true) {
                            subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                        }
                    } else {
                        id = prevActivity.getId() + "-" + activity.getId();
                        //Checks if the current activity a Throw activity, if so no exit arrow flows only entry arrow flows
                        if (activity instanceof ThrowImpl) {
                            subGroup.appendChild(getArrowDefinition(doc, myStartElseCoords.getXLeft(), myStartElseCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                        } else {
                            // Define both the entry and the exit arrow flows to the activity
                            subGroup.appendChild(getArrowDefinition(doc, myStartElseCoords.getXLeft(), myStartElseCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                            subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                        }
                    }
                }
                //Checks if the current activity a Throw activity, if so no exit arrow flows only entry arrow flows
                else if (activity instanceof ThrowImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));

                }
                //Checks if the current activity a Source/s or Target/s activity, if so no exit or entry arrow flows as no icons are defined
                else if (activity instanceof SourceImpl || activity instanceof TargetImpl || activity instanceof SourcesImpl || activity instanceof TargetsImpl) {
                    //No arrow flows for Sources or Targets..
                } else {
                    if (prevActivity != null) {
                        //Gets the coordinates of the exit arrows of the previous activity
                        exitCoords = prevActivity.getExitArrowCoords();
                        id = prevActivity.getId() + "-" + activity.getId();
                        //Checks if the current activity a Source/s or Target/s activity, if so no exit or entry arrow flows as no icons are defined
                        if (prevActivity instanceof SourceImpl || prevActivity instanceof TargetImpl || prevActivity instanceof SourcesImpl || prevActivity instanceof TargetsImpl) {
                            //No arrow flows for Sources or Targets..
                        } else {
                            subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                        }
                    } else {
                        //Checks whether the activity is a Sequence
                        if (activity instanceof SequenceImpl) {
                            List<ActivityInterface> sub = activity.getSubActivities();
                            //Iterates through the subActivities
                            Iterator<ActivityInterface> as = sub.iterator();
                            while (as.hasNext()) {
                                seqActivity = as.next();
                                //Checks if the subActivity is a Throw activity
                                if (seqActivity instanceof ThrowImpl) {
                                    throwOrNot = true;
                                    //if condition breaks if the subActivity is a Throw activity
                                    break;
                                } else {
                                    throwOrNot = false;
                                }
                            }
                            //If its a Throw activity , no exit arrow flow only entry arrow flow to the activity
                            if (throwOrNot == true) {
                                subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                            } else {
                                //If not, define both the entry and the exit arrow flows to the activity
                                subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                                subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                            }
                        } else {
                            // Define both the entry and the exit arrow flows to the activity
                            subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                            subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                        }
                    }
                }
                //current activity is assigned to the previous activity
                prevActivity = activity;
            }
            return subGroup;
        }
        return null;
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
     * @param doc    SVG document which defines the components including shapes, gradients etc. of the activity
     * @param startX x-coordinate of the start point
     * @param startY y-coordinate of the start point
     * @param endX   x-coordinate of the end point
     * @param endY   y-coordinate of the end point
     * @param id     previous activity id + current activity id
     * @return An element which contains the arrow flows/paths of the If activity and its subActivities
     */
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {
        Element path = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "path");
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
                int middleX, middleY;
                if ((startX < endX)) {
                    middleY = startY;
                    middleX = endX;
                } else {
                    middleY = endY;
                    middleX = startX;
                }
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + middleX + "," + middleY + " L " + endX +
                        "," + endY);
            } else {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1 * endX) / 2) +
                        "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + endY + " L " + endX + "," + endY);                              //use constants for these propotions
            }
        }
        //Set the id of the path
        path.setAttributeNS(null, "id", id);
        //Add styles to the arrows
        path.setAttributeNS(null, "style", getArrowStyle());

        return path;
    }
}




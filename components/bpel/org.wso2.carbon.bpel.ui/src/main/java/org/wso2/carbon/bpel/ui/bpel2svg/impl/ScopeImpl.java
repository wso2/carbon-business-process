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
 * Scope tag UI implementation
 */
public class ScopeImpl extends ActivityImpl implements ScopeInterface {
    //Variables for Core and Conditional dimensions
    private SVGDimension coreDimensions = null;
    private SVGDimension conditionalDimensions = null;

    //Defining heights of the start and end icons
    protected int startIconHeight = 5;
    protected int endIconHeight = 5;

    //Getters and Setters for the start and end icon heights

    /**
     * Sets the height of the end icon of the activity
     *
     * @param iconHeightEnd height of the end icon of the activity
     */
    public void setEndIconHeight(int iconHeightEnd) {
        this.endIconHeight = iconHeightEnd;
    }

    /**
     * Sets the height of the start icon of the activity
     *
     * @param iconHeight height of the start icon of the activity
     */
    public void setStartIconHeight(int iconHeight) {
        this.startIconHeight = iconHeight;
    }

    /**
     * Gets the height of the end icon of the activity
     *
     * @return height of the end icon of the activity
     */
    public int getEndIconHeight() {
        return endIconHeight;
    }

    /**
     * Gets the height of the start icon of the activity
     *
     * @return height of the start icon of the activity
     */
    public int getStartIconHeight() {
        return startIconHeight;
    }

    //Constructors

    /**
     * Initializes a new instance of the ScopeImpl class using the specified string i.e. the token
     *
     * @param token
     */
    public ScopeImpl(String token) {
        super(token);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * Initializes a new instance of the ScopeImpl class using the specified omElement
     *
     * @param omElement which matches the Scope tag
     */
    public ScopeImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * Initializes a new instance of the ScopeImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an Scope Activity when processing the subActivities
     * of the process
     *
     * @param omElement which matches the Scope tag
     * @param parent
     */
    public ScopeImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    //Defining heights and the width for the handler icons
    private int handlerIconWidth = 70;
    private int handlerIconHeight = 50;

    //Getters and Setters for the handler icon width and height

    /**
     * Gets the height of the handler icon
     *
     * @return height of the handler icon
     */
    public int getHandlerIconHeight() {
        return handlerIconHeight;
    }

    /**
     * Gets the width of the handler icon
     *
     * @return width of the handler icon
     */
    public int getHandlerIconWidth() {
        return handlerIconWidth;
    }

    /**
     * Sets the height of the handler icon
     *
     * @param handlerIconHeight height of the handler icon
     */
    public void setHandlerIconHeight(int handlerIconHeight) {
        this.handlerIconHeight = handlerIconHeight;
    }

    /**
     * Sets the width of the handler icon
     *
     * @param handlerIconWidth width of the handler icon
     */
    public void setHandlerIconWidth(int handlerIconWidth) {
        this.handlerIconWidth = handlerIconWidth;
    }

    /**
     * @return spacing of the handler connector
     */
    protected int getHandlerConnectorSpacing() {
        int spacing = 10;
        return spacing;
    }

    /**
     * At start: adjustment=0
     *
     * @return spacing/adjustment of the handler icons
     */
    protected int getHandlerAdjustment() {
        int adjustment = 0;
        if (layoutManager.isVerticalLayout()) {
            adjustment = (getHandlerIconHeight() * 4) + (getHandlerConnectorSpacing() * 4);
        } else {
            adjustment = (getHandlerIconWidth() * 4) + (getHandlerConnectorSpacing() * 4);
        }
        return adjustment;
    }

    /**
     * @return String with name of the activity
     */
    @Override
    public String getId() {
        return getName();
    }

    /**
     * @return- String with the end tag of Scope Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.SCOPE_END_TAG;
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
                /*Checks whether the subActivity is any of the handlers i.e. FaultHandler, TerminationHandler,CompensateHandler
                  or EventHandler
                */
                if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                    if (subActivityDim.getHeight() > conHeight) {
                        //height of the icon is added to the conditional height
                        conHeight = subActivityDim.getHeight();
                    }
                    //width of the subActivities added to the conditional width
                    conWidth += subActivityDim.getWidth();
                }
                /*
                 If the activity is an instance of ForEach, Repeat Until, While or If activity, ySpacing = 70 is also
                  added to the core height of the composite activity as the start icons of those activities are placed on
                  the scope of the composite activity, so it requires more spacing.
                 */
                else if (activity instanceof RepeatUntilImpl || activity instanceof ForEachImpl || activity instanceof WhileImpl
                        || activity instanceof IfImpl) {
                    if (subActivityDim.getWidth() > coreWidth) {
                        //width of the subActivities added to the core width
                        coreWidth = subActivityDim.getWidth();
                    }
                    coreHeight += subActivityDim.getHeight() + getYSpacing();
                } else {
                    //If the subActivites are not instances of any handlers
                    if (subActivityDim.getWidth() > coreWidth) {
                        //width of the subActivities added to the core width
                        coreWidth = subActivityDim.getWidth();
                    }
                    //height of the subActivities added to the core height
                    coreHeight += subActivityDim.getHeight();
                }
            }
            //Spacing the core height by adding ySpacing + startIcon height + endIcon height
            coreHeight += getYSpacing() + getStartIconHeight() + getEndIconHeight();
            //Check if its a simple layout
            if (!isSimpleLayout()) {
                coreWidth += getXSpacing();
            }
            conHeight += getHandlerAdjustment();
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
            //Get the final height and width by adding Xspacing and Yspacing
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
     * @return false-->  if the subActivities are instances of handlers i.e. FaultHandler, TerminationHandler,CompensateHandler
     * or EventHandler
     * true -->  otherwise
     */
    private boolean isSimpleLayout() {
        boolean simple = true;

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the subActivities are instances of any handlers
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                simple = false;
                break;
            }
        }

        return simple;
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
        int centreOfMyLayout = startXLeft + (getDimensions().getWidth() / 2);
        int xLeft = 0;
        int yTop = 0;
        int endXLeft = 0;
        int endYTop = 0;
        int centerNHLayout = startXLeft + (getCoreDimensions().getWidth() / 2);
        //Set the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
        //Set the xLeft and yTop of the core dimensions
        getCoreDimensions().setXLeft(startXLeft + (getXSpacing() / 2));
        getCoreDimensions().setYTop(startYTop + (getYSpacing() / 2));
        /* Checks whether its a simple layout i.e. whether the subActivities are any handlers
           if so --> true , else --> false
         */
        if (isSimpleLayout()) {
            //Positioning the startIcon
            xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
            yTop = startYTop + (getYSpacing() / 2);
            //Positioning the endIcon
            endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
            endYTop = startYTop + getDimensions().getHeight() - getEndIconHeight() - (getYSpacing() / 2);
        } else {
            //Positioning the startIcon
            xLeft = centerNHLayout - (getStartIconWidth() / 2) + (getXSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + (getYSpacing() / 2);
            //Positioning the endIcon
            endXLeft = centerNHLayout - (getEndIconWidth() / 2) + (getXSpacing() / 2);
            endYTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight() - getEndIconHeight() - (getYSpacing() / 2);
        }

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();

        int childYTop = 0;
        int childXLeft = 0;
         /* Checks whether its a simple layout i.e. whether the subActivities are any handlers
           if so --> true , else --> false
         */
        if (isSimpleLayout()) {
            //Adjusting the childXLeft and childYTop positions
            childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
            childXLeft = startXLeft + (getXSpacing() / 2);
        } else {
            //Adjusting the childXLeft and childYTop positions
            childYTop = getCoreDimensions().getYTop() + getStartIconHeight() + (getYSpacing() / 2);
            childXLeft = getCoreDimensions().getXLeft() + (getXSpacing() / 2);
        }

        // Process None Handlers First

        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the activity is of any handler type
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
            }
            /* If the activity inside Scope activity is an instance of ForEach, Repeat Until, While or If activity,
               then increase the yTop position of start icon of those activities , as the start icon is placed
               on the scope/box which contains the subActivities.This requires more spacing, so the yTop of the
               activity following it i.e. the activity after it is also increased.
             */
            else if (activity instanceof RepeatUntilImpl || activity instanceof ForEachImpl || activity instanceof WhileImpl || activity instanceof IfImpl) {
                int x = childYTop + (getYSpacing() / 2);
                //Sets the xLeft and yTop position of the iterated activity
                activity.layout(childXLeft, x);
                //Calculate the yTop position of the next activity
                childXLeft += activity.getDimensions().getWidth();
            } else {
                //Sets the xLeft and yTop position of the iterated activity
                activity.layout(childXLeft, childYTop);
                //Calculate the yTop position of the next activity
                childXLeft += activity.getDimensions().getWidth();
            }
        }
        // Process Handlers
        itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        childXLeft = startXLeft + getCoreDimensions().getWidth();
        childYTop = yTop + getHandlerAdjustment();
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the activity is of any handler type
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
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
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = 0;
        int yTop = 0;
        int endXLeft = 0;
        int endYTop = 0;
        int centerNHLayout = startYTop + (coreDimensions.getHeight() / 2);
        //Set the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
        //Set the xLeft and yTop of the core dimensions
        getCoreDimensions().setXLeft(startXLeft + (getXSpacing() / 2));
        getCoreDimensions().setYTop(startYTop + (getYSpacing() / 2));
        /* Checks whether its a simple layout i.e. whether the subActivities are any handlers
           if so --> true , else --> false
         */
        if (isSimpleLayout()) {
            //Positioning the startIcon
            yTop = centreOfMyLayout - (getStartIconHeight() / 2);
            xLeft = startXLeft + (getYSpacing() / 2);
            //Positioning the endIcon
            endYTop = centreOfMyLayout - (getEndIconHeight() / 2);
            endXLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth() - getEndIconWidth() - (getXSpacing() / 2);
        } else {
            //Positioning the startIcon
            yTop = centerNHLayout - (getStartIconHeight() / 2) + (getYSpacing() / 2);
            xLeft = getCoreDimensions().getXLeft() + (getXSpacing() / 2);
            //Positioning the endIcon
            endYTop = centerNHLayout - (getEndIconHeight() / 2) + (getYSpacing() / 2);
            endXLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth() - getEndIconWidth() - (getXSpacing() / 2);
        }

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();

        int childXLeft = 0;
        int childYTop = 0;
        /* Checks whether its a simple layout i.e. whether the subActivities are any handlers
           if so --> true , else --> false
         */
        if (isSimpleLayout()) {
            //Adjusting the childXLeft and childYTop positions
            childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
            childYTop = startYTop + (getXSpacing() / 2);
        } else {
            //Adjusting the childXLeft and childYTop positions
            childXLeft = getCoreDimensions().getXLeft() + getStartIconWidth() + (getYSpacing() / 2);
            childYTop = getCoreDimensions().getYTop() + (getXSpacing() / 2);
        }

        // Process None Handlers First
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the activity is of any handler type
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
            } else {
                //Sets the xLeft and yTop position of the iterated activity
                activity.layout(childXLeft, childYTop);
                childYTop += activity.getDimensions().getHeight();
            }
        }
        // Process Handlers
        itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        childYTop = startYTop + getCoreDimensions().getHeight() + (getYSpacing() / 2);
        childXLeft = xLeft + getHandlerAdjustment();

        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Checks whether the activity is of any handler type
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
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
     * Calculates the coordinates of the arrow which leaves the start Scope Icon
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
     * Calculates the coordinates of the start arrow of the EventHandler
     *
     * @return coordinates of the start arrow of the EventHandler
     */
    protected SVGCoordinates getStartEventCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + getHandlerConnectorSpacing() + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + getHandlerConnectorSpacing() + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

        }
        //Returns the calculated coordinate points of the start arrow of the EventHandler
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the start arrow of the TerminationHandler
     *
     * @return coordinates of the start arrow of the TerminationHandler
     */
    protected SVGCoordinates getStartTerminationCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + getHandlerIconHeight() + (getHandlerConnectorSpacing() * 2) + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + getHandlerIconWidth() + (getHandlerConnectorSpacing() * 2) + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

        }
        //Returns the calculated coordinate points of the start arrow of the TerminationHandler
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the start arrow of the CompensationHandler
     *
     * @return coordinates of the start arrow of the CompensationHandler
     */
    protected SVGCoordinates getStartCompensationCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + (getHandlerIconHeight() * 2) + (getHandlerConnectorSpacing() * 3) + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + (getHandlerIconWidth() * 2) + (getHandlerConnectorSpacing() * 3) + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

        }
        //Returns the calculated coordinate points of the start arrow of the CompensationHandler
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the start arrow of the FaultHandler
     *
     * @return coordinates of the start arrow of the FaultHandler
     */
    protected SVGCoordinates getStartFaultCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + (getHandlerIconHeight() * 3) + (getHandlerConnectorSpacing() * 4) + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + (getHandlerIconWidth() * 3) + (getHandlerConnectorSpacing() * 4) + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

        }
        //Returns the calculated coordinate points of the start arrow of the FaultHandler
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Scope composite activity
     */
    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Gets the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        //Checks for the opacity of the icons
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        //Get the Scope composite activity box/scope where the subActivities are placed
        group.appendChild(getBoxDefinition(doc));
        //Get the start icon definition of the activity
        group.appendChild(getStartImageDefinition(doc));
        //Get the  icon definition of the activities
        group.appendChild(getImageDefinition(doc));
        //Check whether it is not a simple layout i.e. with handlers
        if (!isSimpleLayout()) {
            //Get the handler activity icons and their arrow flows
            group.appendChild(getEventHandlerIcon(doc));
            group.appendChild(getCompensationHandlerIcon(doc));
            group.appendChild(getFaultHandlerIcon(doc));
            group.appendChild(getTerminationHandlerIcon(doc));
        }
        // Get Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Get the end icon definition of the activity
        group.appendChild(getEndImageDefinition(doc));

        //Get the arrow flows of the subActivities inside the Scope composite activity
        group.appendChild(getArrows(doc));

        return group;
    }

    /**
     * Get the arrow coordinates of the activities
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the Scope activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        if (subActivities != null) {
            Element subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            //Coordinates of the start icon exit arrow
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            //Coordinates of the end icon entry arrow
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            //Coordinates of the start arrow of the EventHandler
            SVGCoordinates myStartEventCoords = getStartEventCoords();
            //Coordinates of the start arrow of the TerminationHandler
            SVGCoordinates myStartTerminationCoords = getStartTerminationCoords();
            //Coordinates of the start arrow of the CompensationHandler
            SVGCoordinates myStartCompensationCoords = getStartCompensationCoords();
            //Coordinates of the start arrow of the FaultHandler
            SVGCoordinates myStartFaultCoords = getStartFaultCoords();
            SVGCoordinates activityEntryCoords = null;
            SVGCoordinates activityExitCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            //Iterates through the subActivities
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the entry and exit coordinates of the iterated activity
                activityEntryCoords = activity.getEntryArrowCoords();
                activityExitCoords = activity.getExitArrowCoords();
                // id is assigned with the id of the previous activity + id of the current activity
                id = getId() + "-" + activity.getId();
                //Checks for the activity handler type and according to that the coordinates of the arrow flows are defined.
                if (activity instanceof FaultHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartFaultCoords.getXLeft(), myStartFaultCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else if (activity instanceof TerminationHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartTerminationCoords.getXLeft(), myStartTerminationCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else if (activity instanceof CompensationHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartCompensationCoords.getXLeft(), myStartCompensationCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else if (activity instanceof EventHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartEventCoords.getXLeft(), myStartEventCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else {
                    //If the activity is not a handler type, then the entry and the exit coordinates of the activity are defined
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                    subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                }
                prevActivity = activity;
            }
            return subGroup;
        }
        return null;
    }

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the box definition of the Scope activity
     */
    @Override

    protected Element getBoxDefinition(SVGDocument doc) {
        if (isSimpleLayout()) {
            return super.getBoxDefinition(doc);
        } else {
            return getBoxDefinition(doc, getCoreDimensions().getXLeft() + BOX_MARGIN, getCoreDimensions().getYTop() + BOX_MARGIN, getCoreDimensions().getWidth() - (BOX_MARGIN * 2), getCoreDimensions().getHeight() - (BOX_MARGIN * 2), getBoxId());
        }
    }

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the start image/icon text of the Scope activity
     */
    @Override
    protected Element getStartImageText(SVGDocument doc) {
        if (isSimpleLayout()) {
            return getImageText(doc, getDimensions().getXLeft(), getDimensions().getYTop(), getStartIconWidth(), getStartIconHeight(), getStartImageTextId(), getDisplayName());
        } else {
            return getImageText(doc, getCoreDimensions().getXLeft(), getCoreDimensions().getYTop(), getStartIconWidth(), getStartIconHeight(), getStartImageTextId(), getDisplayName());
        }
    }

    /**
     * At start: xLeft=0, yTop=0
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML) which contains the TerminationHandler icon and arrow flows
     */
    public Element getTerminationHandlerIcon(SVGDocument doc) {
        //Get the coordinates of the start arrow of the TerminationHandler
        SVGCoordinates coords = getStartTerminationCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        //Gets the icon path of the activity
        String iconPath = BPEL2SVGIcons.TERMINATIONHANDLER_ICON;

        //Get the image definition of the handler icon by passing the iconPath, x and y positions & icon height and width
        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
    }

    /**
     * At start: xLeft=0, yTop=0
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML) which contains the FaultHandler icon and arrow flows
     */
    public Element getFaultHandlerIcon(SVGDocument doc) {
        //Get the coordinates of the start arrow of the FaultHandler
        SVGCoordinates coords = getStartFaultCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        //Gets the icon path of the activity
        String iconPath = BPEL2SVGIcons.FAULTHANDLER_ICON;
        //Get the image definition of the handler icon by passing the iconPath, x and y positions & icon height and width
        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
    }

    /**
     * At start: xLeft=0, yTop=0
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML) which contains the CompensationHandler icon and arrow flows
     */
    public Element getCompensationHandlerIcon(SVGDocument doc) {
        //Get the coordinates of the start arrow of the CompensationHandler
        SVGCoordinates coords = getStartCompensationCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        //Gets the icon path of the activity
        String iconPath = BPEL2SVGIcons.COMPENSATIONHANDLER_ICON;
        //Get the image definition of the handler icon by passing the iconPath, x and y positions & icon height and width
        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
    }

    /**
     * At start: xLeft=0, yTop=0
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML) which contains the EventHandler icon and arrow flows
     */
    public Element getEventHandlerIcon(SVGDocument doc) {
        //Get the coordinates of the start arrow of the EventHandler
        SVGCoordinates coords = getStartEventCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        //Gets the icon path of the activity
        String iconPath = BPEL2SVGIcons.EVENTHANDLER_ICON;
        //Get the image definition of the handler icon by passing the iconPath, x and y positions & icon height and width
        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
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
     * Image Definitions for the different handlers
     *
     * @param doc       SVG document which defines the components including shapes, gradients etc. of the activity
     * @param imgPath   path of the activity icon
     * @param imgXLeft  xLeft position of the image
     * @param imgYTop   yTop position of the image
     * @param imgWidth  width of the image
     * @param imgHeight height of the image
     * @param id        id of the activity
     * @return
     */
    protected Element getImageDefinition(SVGDocument doc, String imgPath, int imgXLeft, int imgYTop,
                                         int imgWidth, int imgHeight, String id) {

        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());
        //Checks whether the start icon path is null
        if (getStartIconPath() != null && imgPath != getStartIconPath()) {

            //Rectangle to place the image
            Element x = null;
            x = doc.createElementNS("http://www.w3.org/2000/svg", "g");
            x.setAttributeNS(null, "id", id);
            //Attributes of the rectangle drawn
            Element rect = doc.createElementNS("http://www.w3.org/2000/svg", "rect");
            rect.setAttributeNS(null, "x", String.valueOf(imgXLeft + 10));
            rect.setAttributeNS(null, "y", String.valueOf(imgYTop));
            rect.setAttributeNS(null, "width", String.valueOf(imgWidth));
            rect.setAttributeNS(null, "height", String.valueOf(imgHeight));
            rect.setAttributeNS(null, "id", id);
            rect.setAttributeNS(null, "rx", "10");
            rect.setAttributeNS(null, "ry", "10");
            rect.setAttributeNS(null, "style", "fill:white;stroke:black;stroke-width:1.5;fill-opacity:0.1");

            //Image/Icon of the activity
            int embedImageX = imgXLeft + 25;
            int embedImageY = imgYTop + (5 / 2);
            int embedImageHeight = 45;
            int embedImageWidth = 45;
            //Attributes of the image embedded inside the rectangle
            Element embedImage = doc.createElementNS("http://www.w3.org/2000/svg", "image");
            embedImage.setAttributeNS(null, "xlink:href", imgPath);
            embedImage.setAttributeNS(null, "x", String.valueOf(embedImageX));
            embedImage.setAttributeNS(null, "y", String.valueOf(embedImageY));
            embedImage.setAttributeNS(null, "width", String.valueOf(embedImageWidth));
            embedImage.setAttributeNS(null, "height", String.valueOf(embedImageHeight));
            //Embed the rectangle/image holder into the container
            x.appendChild(rect);
            //Embed the image into the container
            x.appendChild(embedImage);

            return x;
        }

        return group;
    }

    //Different Implementations for start and end scope icons

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the end icon of the Scope activity
     */
    @Override
    protected Element getEndImageDefinition(SVGDocument doc) {
        return getStartEndImageDef(doc, getEndIconPath(), getEndIconXLeft(),
                getEndIconYTop(), getEndIconWidth(), getEndIconHeight(),
                getEndImageId());
    }

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the start icon of the Scope activity
     */
    protected Element getStartImageDefinition(SVGDocument doc) {
        return getStartEndImageDef(doc, getStartIconPath(), getStartIconXLeft(),
                getStartIconYTop(), getStartIconWidth(), getStartIconHeight(),
                getStartImageId());
    }

    /**
     * Image definitions for the start and end scope icons
     *
     * @param doc       SVG document which defines the components including shapes, gradients etc. of the activity
     * @param imgPath   path of the activity icon
     * @param imgXLeft  xLeft position of the image
     * @param imgYTop   yTop position of the image
     * @param imgWidth  width of the image
     * @param imgHeight height of the image
     * @param id        id of the activity
     * @return
     */
    protected Element getStartEndImageDef(SVGDocument doc, String imgPath, int imgXLeft, int imgYTop,
                                          int imgWidth, int imgHeight, String id) {

        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Get the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        //Checks whether the start icon path is null
        if (getStartIconPath() != null) {

            Element x = null;
            x = doc.createElementNS("http://www.w3.org/2000/svg", "g");
            //Get the id of the activity
            x.setAttributeNS(null, "id", id);
            int embedImageX = imgXLeft + imgWidth;
            int embedImageY = imgYTop + (5 / 2);

            /*Scope is depicted by a SVG line --> <line>
             (x1,y1) --> Start point coordinates
             (x2,y2) --> End point coordinates
             */
            Element embedImage = doc.createElementNS("http://www.w3.org/2000/svg", "line");
            embedImage.setAttributeNS(null, "x1", String.valueOf(imgXLeft + 10));
            embedImage.setAttributeNS(null, "y1", String.valueOf(embedImageY));
            embedImage.setAttributeNS(null, "x2", String.valueOf(embedImageX));
            embedImage.setAttributeNS(null, "y2", String.valueOf(embedImageY));
            //Styles the line object drawn
            embedImage.setAttributeNS(null, "style", "stroke:black;stroke-width:5");
            //embeds the image into the <g> container
            x.appendChild(embedImage);

            return x;
        }

        return group;
    }
}

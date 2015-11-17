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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension;
import org.wso2.carbon.bpel.ui.bpel2svg.SequenceInterface;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.apache.axiom.om.OMElement;

/**
 * Sequence tag UI implementation
 */
public class SequenceImpl extends ActivityImpl implements SequenceInterface {
    private static final Log log = LogFactory.getLog(SequenceImpl.class);
 	/**
     * Initializes a new instance of the SequenceImpl class using the specified string i.e. the token
     * @param token
     */
    public SequenceImpl(String token) {
        super(token);
		//Assigns the name of the activity to be displayed when drawing the process
        if (name == null) {
            name = "SEQUENCE" + System.currentTimeMillis();
            displayName = null;
        }
        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }
	/**
     * Initializes a new instance of the SequenceImpl class using the specified omElement
     * @param omElement which matches the Sequence tag
     */
    public SequenceImpl(OMElement omElement) {
        super(omElement);
		//Assigns the name of the activity to be displayed when drawing the process
        if (name == null) {
            name = "SEQUENCE" + System.currentTimeMillis();
            displayName = null;
        }
         // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }
	/**
     * Initializes a new instance of the SequenceImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an Sequence Activity when processing the subActivities
     * of the process
     * @param omElement which matches the Sequence tag
     * @param parent
     */
    public SequenceImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);
		//Assigns the name of the activity to be displayed when drawing the process
        if (name == null) {
            name = "SEQUENCE" + System.currentTimeMillis();
            displayName = name;
        }
         // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }
	//Default Constructor
    public SequenceImpl() {}
    
    /**
     *
     * @return String with name of the activity
     */
    @Override
    public String getId() {
        return getName(); // + "-Sequence";
    }
	/**
     *
     * @return String with the end tag of Sequence Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.SEQUENCE_END_TAG;
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
                //Checks whether the width of the subActivity is greater than zero
                if (subActivityDim.getWidth() > width) {
                    width += subActivityDim.getWidth();
                }
                /*As Sequence should increase in height when the number of subActivities increase, height of each subActivity
                  is added to the height of the main/composite activity
                */
                height += subActivityDim.getHeight();
            }
             /*After iterating through all the subActivities and altering the dimensions of the composite activity
              to get more spacing , Xspacing and Yspacing is added to the height and the width of the composite activity
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

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
		//Adjusting the childXLeft and childYTop positions
        int childYTop = yTop;
        int childXLeft = startXLeft;
        //Iterates through all the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Sets the xLeft position of the iterated activity : childXleft= center of the layout - (width of the activity icon)/2
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
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     * centreOfMyLayout- center of the the SVG
     */
    public void layoutHorizontal(int startXLeft, int startYTop) {
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
		//Positioning the startIcon
        int xLeft = startXLeft + (getXSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childYTop = yTop;
        int childXLeft = xLeft;
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
     * At the start: xLeft=Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     * Calculates the coordinates of the arrow which enters an activity
     * @return coordinates/entry point of the entry arrow for the activities
     * After Calculations(Vertical Layout): xLeft=Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     */
    @Override
    public SVGCoordinates getEntryArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop();
        SVGCoordinates coords = null;
        if (layoutManager.isVerticalLayout()) {
            //Sets the coordinates of the arrow
            coords = new SVGCoordinates(xLeft, yTop);
        } else {
            coords = new SVGCoordinates(yTop, xLeft);
        }
        // Check for Sub Activities
        if (subActivities != null && subActivities.size() > 0) {
            ActivityInterface activity = subActivities.get(0);
            //Get the entry arrow coordinate for each subActivity
            coords = activity.getEntryArrowCoords();
        }
        //Returns the calculated coordinate points of the entry arrow
        return coords;
    }
	/**
     * At the start: Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     * Calculates the coordinates of the arrow which leaves an activity
     * @return coordinates/exit point of the exit arrow for the activities
     */
    @Override
    public SVGCoordinates getExitArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop();
        SVGCoordinates coords = null;
        if (layoutManager.isVerticalLayout()) {
            //Sets the coordinates of the arrow
            coords = new SVGCoordinates(xLeft, yTop);
        } else {
            coords = new SVGCoordinates(yTop, xLeft);
        }
        // Check Sub Activities
        if (subActivities != null && subActivities.size() > 0) {
            ActivityInterface activity = subActivities.get(subActivities.size() - 1);
            //Get the exit arrow coordinate for each subActivity
            coords = activity.getExitArrowCoords();
        }
        //Returns the calculated coordinate points of the exit arrow
        return coords;
    }
    /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Sequence composite activity
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
        //Get the arrow flows of the subActivities inside the Sequence composite activity
        group.appendChild(getArrows(doc));
        //Get the Sequence Box where the subActivities are placed
        group.appendChild(getBoxDefinition(doc));
        //Get the start image/icon text
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));

        return group;
    }
    /**
     * Get the arrow coordinates of the activities
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the Sequence activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        //Creating an SVG Container "g" to place the activities
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Checks for the subActivities
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            SVGCoordinates exitCoords = null;
            SVGCoordinates entryCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            //Iterates through all the subActivities
            while (itr.hasNext()) {
                activity = itr.next();
                //Checks whether the previous activity is null
                if (prevActivity != null) {
                    //Get the exit arrow coordinates of the previous activity
                    exitCoords = prevActivity.getExitArrowCoords();
                    //Get the entry arrow coordinates of the current activity
                    entryCoords = activity.getEntryArrowCoords();
                    // id is assigned with the id of the previous activity + id of the current activity
                    id = prevActivity.getId() + "-" + activity.getId();
                    /*Check whether the activity is a Throw activity, if so setCheck()= true
                      This check is done to remove the exit arrow coming from the Throw activity,as when the process reaches
                       a Throw activity, the process terminates.
                    */
                    if (activity instanceof ThrowImpl) {
                        setCheck(true);
                    }
                    //Checks whether the previous activity is a Throw activity, if so no exit arrow
                    if (prevActivity instanceof ThrowImpl) {
                        //No exit arrow . Process terminates from there
                    } else if (prevActivity instanceof SourcesImpl || prevActivity instanceof SourceImpl || prevActivity instanceof TargetImpl
                            || prevActivity instanceof TargetsImpl || activity instanceof SourcesImpl || activity instanceof SourceImpl ||
                            activity instanceof TargetImpl || activity instanceof TargetsImpl) {
                        //No exit arrow for Source or Target as it doesn't have an icon specified.
                    } else {
                        subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), entryCoords.getXLeft(), entryCoords.getYTop(), id));
                    }
                }
                prevActivity = activity;
            }
        }
        return subGroup;
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

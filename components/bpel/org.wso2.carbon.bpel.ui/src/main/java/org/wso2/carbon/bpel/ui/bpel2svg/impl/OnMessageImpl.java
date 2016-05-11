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

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * OnMessage tag UI implementation
 */
public class OnMessageImpl extends ActivityImpl implements OnMessageInterface {
    /**
     * Initializes a new instance of the OnMessageImpl class using the specified string i.e. the token
     * @param token
     */
    public OnMessageImpl(String token) {
        //Variables to store the partnerLink & operation names
        String partnerLink = "";
        String operation = "";
        // Get the Partner Link Name
        int plIndex = token.indexOf("partnerLink");
        int firstQuoteIndex = 0;
        int lastQuoteIndex = 0;
        if (plIndex >= 0) {
            firstQuoteIndex = token.indexOf("\"", plIndex + 1);
            if (firstQuoteIndex >= 0) {
                lastQuoteIndex = token.indexOf("\"", firstQuoteIndex + 1);
                if (lastQuoteIndex > firstQuoteIndex) {
                    partnerLink = token.substring(firstQuoteIndex + 1, lastQuoteIndex);
                }
            }
        }
        // Get the Operation Name
        int opIndex = token.indexOf("operation");
        if (opIndex >= 0) {
            firstQuoteIndex = token.indexOf("\"", opIndex + 1);
            if (firstQuoteIndex >= 0) {
                lastQuoteIndex = token.indexOf("\"", firstQuoteIndex + 1);
                if (lastQuoteIndex > firstQuoteIndex) {
                    operation = token.substring(firstQuoteIndex + 1, lastQuoteIndex);
                    setDisplayName(operation);
                }
            }
        }
        //Set the name by combining the partnerLink and operation Names
        setName(partnerLink + "." + operation);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }
    /**
     * Initializes a new instance of the OnMessageImpl class using the specified omElement
     * @param omElement which matches the OnMessage tag
     */
    public OnMessageImpl(OMElement omElement) {
        super(omElement);
        //Variables to store the partnerLink & operation names
        String partnerLink = null;
        String operation = null;
        // Get the Partner Link Name
        if (omElement.getAttribute(new QName("partnerLink")) != null)
            partnerLink = omElement.getAttribute(new QName("partnerLink")).getAttributeValue();
        // Get the operation Name
        if (omElement.getAttribute(new QName("operation")) != null)
            operation = omElement.getAttribute(new QName("operation")).getAttributeValue();
        //Set the name by combining the partnerLink and operation Names
        setName(partnerLink + "." + operation);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }
    /**
     * Initializes a new instance of the OnMessageImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an OnMessage Activity when processing the subActivities
     * of the process
     * @param omElement which matches the OnMessage tag
     * @param parent
     */
    public OnMessageImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);
        //Variables to store the partnerLink & operation names
        String partnerLink = null;
        String operation = null;
        // Get the Partner Link Name
        if (omElement.getAttribute(new QName("partnerLink")) != null)
            partnerLink = omElement.getAttribute(new QName("partnerLink")).getAttributeValue();
        // Get the operation Name
        if (omElement.getAttribute(new QName("operation")) != null)
            operation = omElement.getAttribute(new QName("operation")).getAttributeValue();
        //Set the name by combining the partnerLink and operation Names
        setName(partnerLink + "." + operation);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     *
     * @return String with name of the activity
     */
    @Override
    public String getId() {
        return getName(); // + "-OnMessage";
    }

    /**
     *
     * @return- String with the end tag of OnMessage Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.ONMESSAGE_END_TAG;
    }
	
	 /**
     * At the start: width=0, height=0
     * @return dimensions of the activity i.e. the final width and height after doing calculations by iterating
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

            //Iterates through the subActivites inside the activity
            Iterator<ActivityInterface> itr = getSubActivities().iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the dimensions of each subActivity separately
                subActivityDim = activity.getDimensions();
                //Checks whether the width of the subActivity is greater than zero
                if (subActivityDim.getWidth() > width) {
                    width += subActivityDim.getWidth();
                }
                /*As OnMessage should increase in height when the number of subActivities increase, height of each subActivity
                  is added to the height of the main activity
                */
                height += subActivityDim.getHeight();
            }
			/*After iterating through all the subActivities and altering the dimensions of the  activity
              to get more spacing , Xspacing and Yspacing is added to the height and the width of the  activity
            */
            height += getYSpacing() + getStartIconHeight() + (getYSpacing() / 2);
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
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
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
        //Sets the xLeft and yTop positions of the SVG  of the activity after setting the dimensions
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
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childYTop = yTop;
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
        //Sets the xLeft and yTop positions of the SVG of the activity after setting the dimensions
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
     * Calculates the coordinates of the arrow which leaves the start OnMessage Icon
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
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the OnMessage  activity
     */
    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "g");
        //Get the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        //Checks for the opacity of the icons
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        group.appendChild(getBoxDefinition(doc));
        //Get the icons of the activity
        group.appendChild(getImageDefinition(doc));
        //Get the start image/icon text
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Get the arrow flows of the subActivities inside the OnMessage activity
        group.appendChild(getArrows(doc));

        return group;
    }

	 /**
     * Get the arrow coordinates of the activities
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the OnMessage activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS(SVG_Namespace.SVG_NAMESPACE, "g");
		//Checks for the subActivities
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            //Gets the exit coordinates of the start icon
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
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
                    /*If the previous activity is not null, then arrow flow is from the previous activity to the current activity
                      This gives the coordinates of the start point and the end point
                    */
                    subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), entryCoords.getXLeft(), entryCoords.getYTop(), id));
                } else {
                    //Get the entry arrow coordinates of the current activity
                    entryCoords = activity.getEntryArrowCoords();
                      /*If the previous activity is null, then arrow flow is directly from the startIcon to the activity
                      This gives the coordinates of the start point and the end point
                    */
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), entryCoords.getXLeft(), entryCoords.getYTop(), id));
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

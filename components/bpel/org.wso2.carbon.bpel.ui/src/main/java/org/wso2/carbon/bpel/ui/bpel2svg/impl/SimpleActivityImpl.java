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
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGCoordinates;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension;

/**
 * SimpleActivity tag UI implementation
 */
public class SimpleActivityImpl extends ActivityImpl {
    /**
     * Initializes a new instance of the SimpleActivityImpl class using the specified string i.e. the token
     *
     * @param token
     */
    public SimpleActivityImpl(String token) {
        super(token);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * Initializes a new instance of the SimpleActivityImpl class using the specified omElement
     *
     * @param omElement which matches the SimpleActivity tag
     */
    public SimpleActivityImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    /**
     * Initializes a new instance of the SimpleActivityImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an SimpleActivity Activity when processing the
     * subActivities
     * of the process
     *
     * @param omElement which matches the SimpleActivity tag
     * @param parent
     */
    public SimpleActivityImpl(OMElement omElement, ActivityInterface parent) {
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
        return getName();
    }

    /**
     * At the start: width=0, height=0
     *
     * @return dimensions of the  activity i.e. the final width and height after doing calculations
     * After Calculations: width= startIcon width + xSpacing , height= startIcon height + ySpacing
     */
    @Override
    public SVGDimension getDimensions() {
        if (dimensions == null) {
            int width = getStartIconWidth() + getXSpacing();
            int height = getStartIconHeight() + getYSpacing();
            //Sets the dimensions i.e. width and height
            dimensions = new SVGDimension(width, height);
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
     * Sets the x and y positions of the activity
     * At the start: startXLeft=0, startYTop=0
     *
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     */
    public void layoutVertical(int startXLeft, int startYTop) {
        int xLeft = startXLeft + (getXSpacing() / 2);
        int yTop = startYTop + (getYSpacing() / 2);

        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        //Sets the xLeft and yTop positions of the start icon text
        setStartIconTextXLeft(xLeft);
        setStartIconTextYTop(yTop + getStartIconHeight() + BPEL2SVGFactory.TEXT_ADJUST);
        //Sets the xLeft and yTop positions of the SVG  of the activity after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    /**
     * Sets the x and y positions of the activity
     * At the start: startXLeft=0, startYTop=0
     *
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     */
    public void layoutHorizontal(int startXLeft, int startYTop) {
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = startYTop + (getXSpacing() / 2);

        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        //Sets the xLeft and yTop positions of the start icon text
        setStartIconTextXLeft(xLeft);
        setStartIconTextYTop(yTop + getStartIconHeight() + BPEL2SVGFactory.TEXT_ADJUST);
        //Sets the xLeft and yTop positions of the SVG  of the activity after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    /**
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the SimpleActivity
     */
    @Override

    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "g");
        //Get the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        //Checks for the opacity of the icons
        if (isAddOpacity()) {
            group.setAttributeNS("xlink", "title", getActivityInfoString());
        }
        //group.appendChild(getBoxDefinition());
        //Get the icons of the activity
        group.appendChild(getImageDefinition(doc));
        //Get the start image/icon text
        group.appendChild(getStartImageText(doc));

        return group;
    }

    /**
     * At the start: xLeft=Xleft of Icon + (width of icon)/2, yTop=Ytop of the Icon
     * Calculates the coordinates of the arrow which enters an activity
     *
     * @return coordinates/entry point of the entry arrow for the activities
     * After Calculations(Vertical Layout): xLeft=Xleft of Icon , yTop= Ytop of the Icon + (height of startIcon)/2
     */
    @Override
    public SVGCoordinates getEntryArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop();
        if (!layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
        }
        //Returns the calculated coordinate points of the entry arrow
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);
        return coords;
    }

    /**
     * At the start: xLeft=Xleft of Icon + (width of icon)/2, yTop=Ytop of the Icon+ height of the icon
     * Calculates the coordinates of the arrow which leaves an activity
     *
     * @return coordinates/exit point of the exit arrow for the activities
     * After Calculations(Vertical Layout): xLeft=Xleft of Icon + width of startIcon , yTop= Ytop of the Icon +
     * (height of startIcon)/2
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
     * Adds opacity to icons
     *
     * @return true or false
     */
    @Override
    public boolean isAddOpacity() {
        return isAddSimpleActivityOpacity();
    }

    /**
     * @return String with the opacity value
     */
    @Override
    public String getOpacity() {
        return getSimpleActivityOpacity();
    }
}

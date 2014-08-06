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
 * EventHandler tag UI impl
 */
public class EventHandlerImpl extends ActivityImpl implements EventHandlerInterface {

    public EventHandlerImpl(String token) {
        super(token);
        if (name == null) {
            name = "EVENTHANDLER" + System.currentTimeMillis();
        }
        displayName = "Event Handlers";

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public EventHandlerImpl(OMElement omElement) {
        super(omElement);
        if (name == null) {
            name = "EVENTHANDLER" + System.currentTimeMillis();
        }
        displayName = "Event Handlers";

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public EventHandlerImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        if (name == null) {
            name = "EVENTHANDLER" + System.currentTimeMillis();
        }
        displayName = "Event Handlers";

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    @Override
    public String getId() {
        return getName(); // + "-EventHandler";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.EVENTHANDLER_END_TAG;
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
            while (itr.hasNext()) {
                activity = itr.next();
                subActivityDim = activity.getDimensions();
                if (subActivityDim.getHeight() > height) {
                    height += subActivityDim.getHeight();
                }
                width += subActivityDim.getWidth();
            }

            height += getYSpacing();
            width += getXSpacing();

            dimensions.setWidth(width);
            dimensions.setHeight(height);
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
        int childYTop = startYTop + (getYSpacing() / 2);
        int childXLeft = startXLeft + (getXSpacing() / 2);
        while (itr.hasNext()) {
            activity = itr.next();
//            childYTop += centreOfMyLayout - (activity.getDimensions().getHeight() / 2);
            activity.layout(childXLeft, childYTop);
            childXLeft += activity.getDimensions().getWidth();
        }

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
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
        int childXLeft = startXLeft + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);
        while (itr.hasNext()) {
            activity = itr.next();
//            childXLeft = centreOfMyLayout - activity.getDimensions().getWidth() / 2;
            activity.layout(childXLeft, childYTop);
            childYTop += activity.getDimensions().getHeight();
        }

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    @Override
    public SVGCoordinates getEntryArrowCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getDimensions().getXLeft() + (getDimensions().getWidth() / 2);
            yTop = getDimensions().getYTop() + BOX_MARGIN;
        } else {
            xLeft = getDimensions().getXLeft() + BOX_MARGIN;
            yTop = getDimensions().getYTop() + (getDimensions().getHeight() / 2);

        }

        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

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

        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());
        // Check if Layer & Opacity required
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        group.appendChild(getBoxDefinition(doc));
        group.appendChild(getImageDefinition(doc));
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        group.appendChild(getEndImageDefinition(doc));
        //Add Arrow
        //group.appendChild(getArrows());  attention - this has no implementaion
        return group;
    }

    protected String getArrows() {
        StringBuffer svgSB = new StringBuffer();
        return svgSB.toString();
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

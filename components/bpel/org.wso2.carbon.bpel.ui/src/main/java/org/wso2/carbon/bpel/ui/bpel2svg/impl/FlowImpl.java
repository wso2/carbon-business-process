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
 * Flow tag UI impl
 */
public class FlowImpl extends ActivityImpl implements FlowInterface {
    public FlowImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public FlowImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public FlowImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());

        // setVerticalChildLayout(false);
    }

    @Override
    public String getId() {
        return getName(); // + "-Flow";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.FLOW_END_TAG;
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

            height += (getYSpacing() * 2) + getStartIconHeight() + getEndIconHeight();
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
        if (dimensions != null) {
            int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
            int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
            int yTop = startYTop + (getYSpacing() / 2);
            int endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
            int endYTop = startYTop + dimensions.getHeight() - getEndIconHeight() - (getYSpacing() / 2);

            ActivityInterface activity = null;
            Iterator<ActivityInterface> itr = getSubActivities().iterator();
            int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
            int childXLeft = startXLeft + (getXSpacing() / 2);
            while (itr.hasNext()) {
                activity = itr.next();
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
    }

    private void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);
        int endXLeft = startXLeft + dimensions.getWidth() - getEndIconWidth() - (getYSpacing() / 2);
        int endYTop = centreOfMyLayout - (getEndIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);
        while (itr.hasNext()) {
            activity = itr.next();
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
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop();
        } else {
            xLeft = getStartIconXLeft();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);

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
        group.appendChild(getArrows(doc));

        return group;

    }

    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
        SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();

        subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(),
                myStartCoords.getXLeft(), (myStartCoords.getYTop() + 30), "Flow_Top", true));
        subGroup.appendChild(getArrowDefinition(doc, (myStartCoords.getXLeft() - dimensions.getWidth() / 2 + getXSpacing()),
                (myStartCoords.getYTop() + 30), (myStartCoords.getXLeft() + dimensions.getWidth() / 2 - getXSpacing()),
                (myStartCoords.getYTop() + 30), "Flow_TopH", true));
        subGroup.appendChild(getArrowDefinition(doc, (myStartCoords.getXLeft() - dimensions.getWidth() / 2 + getXSpacing()),
                (myExitCoords.getYTop() - 20), (myStartCoords.getXLeft() + dimensions.getWidth() / 2 - getXSpacing()),
                (myExitCoords.getYTop() - 20), "Flow_DownH", true));
        subGroup.appendChild(getArrowDefinition(doc, myExitCoords.getXLeft(), myExitCoords.getYTop() - 20,
                myExitCoords.getXLeft(), myExitCoords.getYTop(), "Flow_Top", false));

        return subGroup;
    }

    public Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id, boolean to) {         //here we have to find whether
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");

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
        path.setAttributeNS(null, "id", id);
        path.setAttributeNS(null, "style", getArrowStyle(to));
        return path;
    }

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

    @Override
    public boolean isAddOpacity() {
        return isAddCompositeActivityOpacity();
    }

    @Override
    public String getOpacity() {
        return getCompositeOpacity();
    }
}

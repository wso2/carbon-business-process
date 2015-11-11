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

import com.sun.xml.bind.annotation.OverrideAnnotationOf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.*;
import org.apache.axiom.om.OMElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.*;

/**
 * Else tag UI impl
 */
public class ElseImpl extends ActivityImpl implements ElseInterface {
    private static final Log log = LogFactory.getLog(ElseImpl.class);
    public boolean throwOrNot;

    public ElseImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public ElseImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public ElseImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getId() {
        return getName(); // + "-Else";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.ELSE_END_TAG;
    }


    public Element getSVGString(SVGDocument doc) {

        Element group1 = null;
        group1 = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group1.setAttributeNS(null, "id", getLayerId());
        group1.appendChild(getImageDefinition(doc));
        //Get sub activities
        group1.appendChild(getSubActivitiesSVGString(doc));
        if (getArrows(doc) != null) {
            group1.appendChild(getArrows(doc));
        }
        return group1;
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
                if (subActivityDim.getWidth() > width) {
                    width += subActivityDim.getWidth();
                }
                height += subActivityDim.getHeight();
            }

            height += getYSpacing() + getStartIconHeight();
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

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft = startXLeft;
        while (itr.hasNext()) {
            activity = itr.next();
            childXLeft = centreOfMyLayout - activity.getDimensions().getWidth() / 2;
            activity.layout(childXLeft, childYTop);
            childYTop += activity.getDimensions().getHeight();
        }

        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);

    }

    public void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop;
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        while (itr.hasNext()) {
            activity = itr.next();
            childYTop = centreOfMyLayout - (activity.getDimensions().getHeight() / 2);
            activity.layout(childXLeft, childYTop);
            childXLeft += activity.getDimensions().getWidth();
        }

        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
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
            xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
            yTop = getStartIconYTop();

        }

        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    @Override
    public SVGCoordinates getExitArrowCoords() {
        SVGCoordinates coords = getStartIconExitArrowCoords();

        if (subActivities != null && subActivities.size() > 0) {
            ActivityInterface activity = subActivities.get(subActivities.size() - 1);
            coords = activity.getExitArrowCoords();
        }
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


    protected Element getArrows(SVGDocument doc) {
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            ActivityInterface seqActivity = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            SVGCoordinates exitCoords = null;
            SVGCoordinates activityEntryCoords = null;
            SVGCoordinates activityExitCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            Element subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");

            while (itr.hasNext()) {
                activity = itr.next();
                activityEntryCoords = activity.getEntryArrowCoords();
                activityExitCoords = activity.getExitArrowCoords();

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
                if (activity instanceof ThrowImpl) {
                    throwOrNot = true;
                }
                if (prevActivity != null) {
                    exitCoords = prevActivity.getExitArrowCoords();
                    id = prevActivity.getId() + "-" + activity.getId();
                    subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else {
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                }

                prevActivity = activity;
            }
            return subGroup;
        }
        return null;
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
    public boolean isAddOpacity() {
        return isAddCompositeActivityOpacity();
    }

    @Override
    public String getOpacity() {
        return getCompositeOpacity();
    }

    //Get the arrow definitions/paths from the coordinates
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {         //here we have to find whether
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");

        if (startX == endX || startY == endY) {
            path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + endX + "," + endY);
        } else {
            if (layoutManager.isVerticalLayout()) {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," +
                        ((startY + 2 * endY) / 3) + " L " + endX + "," + ((startY + 2 * endY) / 3) + " L " + endX +
                        "," + endY);
            } else {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1 * endX) / 2) +
                        "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + endY + " L " + endX + "," + endY);                              //use constants for these propotions
            }
        }
        path.setAttributeNS(null, "id", id);
        path.setAttributeNS(null, "style", getArrowStyle());

        return path;
    }
}

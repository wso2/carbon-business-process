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
 * ElseIf tag UI impl
 */
public class ElseIfImpl extends ActivityImpl implements ElseIfInterface {
    public ElseIfImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
       /* setStartIconHeight(32);
        setStartIconWidth(32);*/
    }

    public ElseIfImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        /*setStartIconHeight(32);
        setStartIconWidth(32);*/
    }

    public ElseIfImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        /*setStartIconHeight(32);
        setStartIconWidth(32);*/
    }

    @Override
    public String getId() {
        return getName(); // + "-ElseIf";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.ELSEIF_END_TAG;
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
            xLeft = getStartIconXLeft();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
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

        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    public Element getSVGString(SVGDocument doc) {

        Element group1 = null;
        group1 = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group1.setAttributeNS(null, "id", getLayerId());
        if (isAddOpacity()) {
            group1.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        group1.appendChild(getBoxDefinition(doc));
        group1.appendChild(getImageDefinition(doc));
        //Get sub activities
        group1.appendChild(getSubActivitiesSVGString(doc));
        if (getArrows(doc) != null) {
            group1.appendChild(getArrows(doc));
        }
        return group1;
    }

    protected Element getArrows(SVGDocument doc) {
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity;
            String id = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
//            SVGCoordinates exitCoords;
            SVGCoordinates entryCoords;
            for (ActivityInterface subActivity : subActivities) {
                activity = subActivity;
//                if (prevActivity != null) {
//                    exitCoords = prevActivity.getExitArrowCoords();
//                    entryCoords = activity.getEntryArrowCoords();
//                    id = prevActivity.getId() + "-" + activity.getId();
//                    return getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(),
// entryCoords.getXLeft(), entryCoords.getYTop(), id);
//                } else {
                entryCoords = activity.getExitArrowCoords();
                return getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(),
                        entryCoords.getXLeft(), entryCoords.getYTop(), id);
            }

        }
        return null;
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

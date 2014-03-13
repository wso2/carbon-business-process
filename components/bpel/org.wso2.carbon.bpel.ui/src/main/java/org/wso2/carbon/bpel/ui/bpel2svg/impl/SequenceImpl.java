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
 * Sequence tag UI impl
 */
public class SequenceImpl extends ActivityImpl implements SequenceInterface {

    public SequenceImpl(String token) {
        super(token);
        if (name == null) {
            name = "SEQUENCE" + System.currentTimeMillis();
            displayName = null;
        }
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public SequenceImpl(OMElement omElement) {
        super(omElement);
        if (name == null) {
            name = "SEQUENCE" + System.currentTimeMillis();
            displayName = null;
        }
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public SequenceImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        if (name == null) {
            name = "SEQUENCE" + System.currentTimeMillis();
            displayName = name;                                       
        }
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getId() {
        return getName(); // + "-Sequence";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.SEQUENCE_END_TAG;
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

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop;
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
        getDimensions().setXLeft(startXLeft);                                          //TODO why startXleft not Xleft?
        getDimensions().setYTop(startYTop);

    }

    public void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = startXLeft + (getXSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop;
        int childXLeft = xLeft;
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
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop();
        SVGCoordinates coords = null;
        if (layoutManager.isVerticalLayout()) {
            coords = new SVGCoordinates(xLeft, yTop);
        } else {
            coords = new SVGCoordinates(yTop, xLeft);
        }
        // Check Sub Activities
        if (subActivities != null && subActivities.size() > 0) {
            ActivityInterface activity = subActivities.get(0);
            coords = activity.getEntryArrowCoords();
        }
        return coords;
    }

    @Override
    public SVGCoordinates getExitArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop();
        SVGCoordinates coords = null;
        if (layoutManager.isVerticalLayout()) {
            coords = new SVGCoordinates(xLeft, yTop);
        } else {
            coords = new SVGCoordinates(yTop, xLeft);
        }
        // Check Sub Activities
        if (subActivities != null && subActivities.size() > 0) {
            ActivityInterface activity = subActivities.get(subActivities.size() - 1);
            coords = activity.getExitArrowCoords();
        }
        return coords;
    }

    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;

        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());

        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        //Add Arrow
        group.appendChild(getArrows(doc));

        group.appendChild(getBoxDefinition(doc));
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Add Arrow
       // group.appendChild(getArrows(doc));

        return group;
    }

    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");

        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            SVGCoordinates exitCoords = null;
            SVGCoordinates entryCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                if (prevActivity != null) {
                    exitCoords = prevActivity.getExitArrowCoords();
                    entryCoords = activity.getEntryArrowCoords();
                    id = prevActivity.getId() + "-" + activity.getId();
                    subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), entryCoords.getXLeft(), entryCoords.getYTop(), id));
                }
                prevActivity = activity;
            }
        }
        return subGroup;
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

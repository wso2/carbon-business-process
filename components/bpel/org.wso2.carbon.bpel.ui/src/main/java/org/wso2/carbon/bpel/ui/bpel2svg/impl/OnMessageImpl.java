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
 * OnMessage tag UI impl
 */
public class OnMessageImpl extends ActivityImpl implements OnMessageInterface {

    public OnMessageImpl(String token) {
        String partnerLink = "";
        String operation = "";
        // Get Partner Link Name
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
        // Get Operation Name
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
        setName(partnerLink + "." + operation);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public OnMessageImpl(OMElement omElement) {
        super(omElement);

        String partnerLink = null;
        String operation = null;
        // Get Partner Link Name
        if (omElement.getAttribute(new QName("partnerLink")) != null)
            partnerLink = omElement.getAttribute(new QName("partnerLink")).getAttributeValue();      //attention-  consider about namespace
        if (omElement.getAttribute(new QName("operation")) != null)
            operation = omElement.getAttribute(new QName("operation")).getAttributeValue();      //attention-  consider about namespace

        setName(partnerLink + "." + operation);
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public OnMessageImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);

        String partnerLink = null;
        String operation = null;
        // Get Partner Link Name
        if (omElement.getAttribute(new QName("partnerLink")) != null)
            partnerLink = omElement.getAttribute(new QName("partnerLink")).getAttributeValue();      //attention-  consider about namespace
        if (omElement.getAttribute(new QName("operation")) != null)
            operation = omElement.getAttribute(new QName("operation")).getAttributeValue();      //attention-  consider about namespace

        setName(partnerLink + "." + operation);
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getId() {
        return getName(); // + "-OnMessage";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.ONMESSAGE_END_TAG;
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

            height += getYSpacing() + getStartIconHeight() + (getYSpacing() / 2);
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
            xLeft = getStartIconXLeft();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);

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

    @Override
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
        }
        group.appendChild(getBoxDefinition(doc));
        group.appendChild(getImageDefinition(doc));
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Add Arrow
        group.appendChild(getArrows(doc));

        return group;
    }

    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
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
                } else {
                    entryCoords = activity.getEntryArrowCoords();
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), entryCoords.getXLeft(), entryCoords.getYTop(), id));
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

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

/**
 * SimpleActivity tag UI impl
 */
public class SimpleActivityImpl extends ActivityImpl implements ReceiveInterface {

    public SimpleActivityImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public SimpleActivityImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public SimpleActivityImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public SVGDimension getDimensions() {
        if (dimensions == null) {
            int width = getStartIconWidth() + getXSpacing();
            int height = getStartIconHeight() + getYSpacing();
            dimensions = new SVGDimension(width, height);
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
        int xLeft = startXLeft + (getXSpacing() / 2);
        int yTop = startYTop + (getYSpacing() / 2);

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setStartIconTextXLeft(xLeft);
        setStartIconTextYTop(yTop + getStartIconHeight() + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);                          //TODO why startXleft not Xleft?
        getDimensions().setYTop(startYTop);
    }

    public void layoutHorizontal(int startXLeft, int startYTop) {
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = startYTop + (getXSpacing() / 2);

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setStartIconTextXLeft(xLeft);
        setStartIconTextYTop(yTop + getStartIconHeight() + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    @Override

    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());
        if (isAddOpacity()) {
            group.setAttributeNS(null, "style", "opacity:" + getOpacity());
            group.setAttributeNS(null, "onmouseover", "this.style.opacity=1;this.filters.alpha.opacity=100");
            group.setAttributeNS(null, "onmouseout", "this.style.opacity=" + getIconOpacity() + ";this.filters.alpha.opacity=40");
            group.setAttributeNS("xlink", "title", getActivityInfoString());
        }

//      group.appendChild(getBoxDefinition());        
        group.appendChild(getImageDefinition(doc));
        group.appendChild(getStartImageText(doc));

        return group;
    }

    @Override
    public SVGCoordinates getEntryArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop();
        if (!layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
        }
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);
        // Check Sub Activities
        return coords;
    }

    @Override
    public SVGCoordinates getExitArrowCoords() {
        int xLeft = getStartIconXLeft() + (getStartIconWidth() / 2);
        int yTop = getStartIconYTop() + getStartIconHeight();
        if (!layoutManager.isVerticalLayout()) {
            xLeft = getStartIconXLeft() + getStartIconWidth();
            yTop = getStartIconYTop() + (getStartIconHeight() / 2);
        }
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);
        // Check Sub Activities
        return coords;
    }

    @Override
    public boolean isAddOpacity() {
        return isAddSimpleActivityOpacity();
    }

    @Override
    public String getOpacity() {
        return getSimpleActivityOpacity();
    }
}

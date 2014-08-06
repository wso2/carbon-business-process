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
 * Scope tag UI impl
 */
public class ScopeImpl extends ActivityImpl implements ScopeInterface {

    private SVGDimension coreDimensions = null;
    private SVGDimension conditionalDimensions = null;

    public ScopeImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public ScopeImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    public ScopeImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    private int handlerIconWidth = 16;
    private int handlerIconHeight = 16;

    public int getHandlerIconHeight() {
        return handlerIconHeight;
    }

    public int getHandlerIconWidth() {
        return handlerIconWidth;
    }

    public void setHandlerIconHeight(int handlerIconHeight) {
        this.handlerIconHeight = handlerIconHeight;
    }

    public void setHandlerIconWidth(int handlerIconWidth) {
        this.handlerIconWidth = handlerIconWidth;
    }

    protected int getHandlerConnectorSpacing() {
        int spacing = 5;
        return spacing;
    }

    protected int getHandlerAdjustment() {
        int adjustment = 0;
        if (layoutManager.isVerticalLayout()) {
            adjustment = (getHandlerIconHeight() * 4) + (getHandlerConnectorSpacing() * 4);
        } else {
            adjustment = (getHandlerIconWidth() * 4) + (getHandlerConnectorSpacing() * 4);
        }
        return adjustment;
    }

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.SCOPE_END_TAG;
    }

    @Override
    public SVGDimension getDimensions() {
        if (dimensions == null) {
            int width = 0;
            int height = 0;
            int coreWidth = 0;
            int coreHeight = 0;
            int conWidth = 0;
            int conHeight = 0;
            dimensions = new SVGDimension(coreWidth, coreHeight);
            coreDimensions = new SVGDimension(coreWidth, coreHeight);
            conditionalDimensions = new SVGDimension(conWidth, conHeight);

            SVGDimension subActivityDim = null;
            ActivityInterface activity = null;
            Iterator<ActivityInterface> itr = getSubActivities().iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                subActivityDim = activity.getDimensions();
                if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                    if (subActivityDim.getHeight() > conHeight) {
                        conHeight = subActivityDim.getHeight();
                    }
                    conWidth += subActivityDim.getWidth();
                } else {
                    if (subActivityDim.getWidth() > coreWidth) {
                        coreWidth = subActivityDim.getWidth();
                    }
                    coreHeight += subActivityDim.getHeight();
                }
            }

            coreHeight += getYSpacing() + getStartIconHeight() + getEndIconHeight();
            if (!isSimpleLayout()) {
                coreWidth += getXSpacing();
            }
            conHeight += getHandlerAdjustment();

            coreDimensions.setHeight(coreHeight);
            coreDimensions.setWidth(coreWidth);

            conditionalDimensions.setHeight(conHeight);
            conditionalDimensions.setWidth(conWidth);

            if (coreHeight > conHeight) {
                height = coreHeight;
            } else {
                height = conHeight;
            }
            width = coreWidth + conWidth;

            height += getYSpacing();
            width += getXSpacing();

            dimensions.setWidth(width);
            dimensions.setHeight(height);
        }

        return dimensions;
    }

    public SVGDimension getCoreDimensions() {
        return coreDimensions;
    }

    public SVGDimension getConditionalDimensions() {
        return conditionalDimensions;
    }

    @Override
    public void switchDimensionsToHorizontal() {
        super.switchDimensionsToHorizontal();
        int height = 0;
        int width = 0;
        // Switch Core Dimensions
        height = coreDimensions.getHeight();
        width = coreDimensions.getWidth();
        coreDimensions.setHeight(width);
        coreDimensions.setWidth(height);
        // Switch Conditional Dimensions
        height = conditionalDimensions.getHeight();
        width = conditionalDimensions.getWidth();
        conditionalDimensions.setHeight(width);
        conditionalDimensions.setWidth(height);
    }

    private boolean isSimpleLayout() {
        boolean simple = true;

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                simple = false;
                break;
            }
        }

        return simple;
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
        int centreOfMyLayout = startXLeft + (getDimensions().getWidth() / 2);
        int xLeft = 0;
        int yTop = 0;
        int endXLeft = 0;
        int endYTop = 0;
        int centerNHLayout = startXLeft + (getCoreDimensions().getWidth() / 2);

        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
        getCoreDimensions().setXLeft(startXLeft + (getXSpacing() / 2));
        getCoreDimensions().setYTop(startYTop + (getYSpacing() / 2));

        if (isSimpleLayout()) {
            xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
            yTop = startYTop + (getYSpacing() / 2);
            endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
            endYTop = startYTop + getDimensions().getHeight() - getEndIconHeight() - (getYSpacing() / 2);
        } else {
            xLeft = centerNHLayout - (getStartIconWidth() / 2) + (getXSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + (getYSpacing() / 2);
            endXLeft = centerNHLayout - (getEndIconWidth() / 2) + (getXSpacing() / 2);
            endYTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight() - getEndIconHeight() - (getYSpacing() / 2);
        }

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();

        int childYTop = 0;
        int childXLeft = 0;

        if (isSimpleLayout()) {
            childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
            childXLeft = startXLeft + (getXSpacing() / 2);
        } else {
            childYTop = getCoreDimensions().getYTop() + getStartIconHeight() + (getYSpacing() / 2);
            childXLeft = getCoreDimensions().getXLeft() + (getXSpacing() / 2);
        }

        // Process None Handlers First
        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                // Ignore
                //throw new UnsupportedOperationException("This operation is not currently supported in this version of WSO2 BPS.");
            } else {
                activity.layout(childXLeft, childYTop);
                childXLeft += activity.getDimensions().getWidth();
            }
        }
        // Process Handlers
        itr = getSubActivities().iterator();
        childXLeft = startXLeft + getCoreDimensions().getWidth();
        childYTop = yTop + getHandlerAdjustment();

        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                activity.layout(childXLeft, childYTop);
                childXLeft += activity.getDimensions().getWidth();
            }
        }

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
    }

    private void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = 0;
        int yTop = 0;
        int endXLeft = 0;
        int endYTop = 0;
        int centerNHLayout = startYTop + (coreDimensions.getHeight() / 2);

        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
        getCoreDimensions().setXLeft(startXLeft + (getXSpacing() / 2));
        getCoreDimensions().setYTop(startYTop + (getYSpacing() / 2));

        if (isSimpleLayout()) {
            yTop = centreOfMyLayout - (getStartIconHeight() / 2);
            xLeft = startXLeft + (getYSpacing() / 2);
            endYTop = centreOfMyLayout - (getEndIconHeight() / 2);
            endXLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth() - getEndIconWidth() - (getXSpacing() / 2);
        } else {
            yTop = centerNHLayout - (getStartIconHeight() / 2) + (getYSpacing() / 2);
            xLeft = getCoreDimensions().getXLeft() + (getXSpacing() / 2);
            endYTop = centerNHLayout - (getEndIconHeight() / 2) + (getYSpacing() / 2);
            endXLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth() - getEndIconWidth() - (getXSpacing() / 2);
        }

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();

        int childXLeft = 0;
        int childYTop = 0;

        if (isSimpleLayout()) {
            childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
            childYTop = startYTop + (getXSpacing() / 2);
        } else {
            childXLeft = getCoreDimensions().getXLeft() + getStartIconWidth() + (getYSpacing() / 2);
            childYTop = getCoreDimensions().getYTop() + (getXSpacing() / 2);
        }

        // Process None Handlers First
        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                // Ignore
                //throw new UnsupportedOperationException("This operation is not currently supported in this version of WSO2 BPS.");
            } else {
                activity.layout(childXLeft, childYTop);
                childYTop += activity.getDimensions().getHeight();
            }
        }
        // Process Handlers
        itr = getSubActivities().iterator();
        childYTop = startYTop + getCoreDimensions().getHeight() + (getYSpacing() / 2);
        childXLeft = xLeft + getHandlerAdjustment();

        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof FaultHandlerImpl || activity instanceof TerminationHandlerImpl || activity instanceof CompensationHandlerImpl || activity instanceof EventHandlerImpl) {
                activity.layout(childXLeft, childYTop);
                childYTop += activity.getDimensions().getHeight();
            }
        }

        // Set the values
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
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

    protected SVGCoordinates getStartEventCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + getHandlerConnectorSpacing() + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + getHandlerConnectorSpacing() + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

        }

        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    protected SVGCoordinates getStartTerminationCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + getHandlerIconHeight() + (getHandlerConnectorSpacing() * 2) + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + getHandlerIconWidth() + (getHandlerConnectorSpacing() * 2) + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

        }

        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    protected SVGCoordinates getStartCompensationCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + (getHandlerIconHeight() * 2) + (getHandlerConnectorSpacing() * 3) + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + (getHandlerIconWidth() * 2) + (getHandlerConnectorSpacing() * 3) + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

        }

        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    protected SVGCoordinates getStartFaultCoords() {
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = getCoreDimensions().getXLeft() + getCoreDimensions().getWidth();
            yTop = getCoreDimensions().getYTop() + (getHandlerIconHeight() * 3) + (getHandlerConnectorSpacing() * 4) + (getYSpacing() / 2);
        } else {
            xLeft = getCoreDimensions().getXLeft() + (getHandlerIconWidth() * 3) + (getHandlerConnectorSpacing() * 4) + (getYSpacing() / 2);
            yTop = getCoreDimensions().getYTop() + getCoreDimensions().getHeight();

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
        if (!isSimpleLayout()) {
            group.appendChild(getEventHandlerIcon(doc));
            group.appendChild(getCompensationHandlerIcon(doc));
            group.appendChild(getFaultHandlerIcon(doc));
            group.appendChild(getTerminationHandlerIcon(doc));
        }
        // Get Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        group.appendChild(getEndImageDefinition(doc));
        //Add Arrow
        group.appendChild(getArrows(doc));
        //attention - here group1 contain the box definition+ImageDefinition+etc... in the original
        // but here group does not contain that
        return group;
    }

    protected Element getArrows(SVGDocument doc) {
        if (subActivities != null) {
            Element subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
            ActivityInterface prevActivity = null;
            ActivityInterface activity = null;
            String id = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            SVGCoordinates myStartEventCoords = getStartEventCoords();
            SVGCoordinates myStartTerminationCoords = getStartTerminationCoords();
            SVGCoordinates myStartCompensationCoords = getStartCompensationCoords();
            SVGCoordinates myStartFaultCoords = getStartFaultCoords();
            SVGCoordinates activityEntryCoords = null;
            SVGCoordinates activityExitCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                activityEntryCoords = activity.getEntryArrowCoords();
                activityExitCoords = activity.getExitArrowCoords();
                id = getId() + "-" + activity.getId();
                if (activity instanceof FaultHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartFaultCoords.getXLeft(), myStartFaultCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else if (activity instanceof TerminationHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartTerminationCoords.getXLeft(), myStartTerminationCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else if (activity instanceof CompensationHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartCompensationCoords.getXLeft(), myStartCompensationCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else if (activity instanceof EventHandlerImpl) {
                    subGroup.appendChild(getArrowDefinition(doc, myStartEventCoords.getXLeft(), myStartEventCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                } else {
                    subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                    subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));
                }
                prevActivity = activity;
            }
            return subGroup;
        }
        return null;
    }


    @Override

    protected Element getBoxDefinition(SVGDocument doc) {
        if (isSimpleLayout()) {
            return super.getBoxDefinition(doc);
        } else {
            return getBoxDefinition(doc, getCoreDimensions().getXLeft() + BOX_MARGIN, getCoreDimensions().getYTop() + BOX_MARGIN, getCoreDimensions().getWidth() - (BOX_MARGIN * 2), getCoreDimensions().getHeight() - (BOX_MARGIN * 2), getBoxId());
        }
    }

    @Override
    protected Element getStartImageText(SVGDocument doc) {
        if (isSimpleLayout()) {
            return getImageText(doc, getDimensions().getXLeft(), getDimensions().getYTop(), getStartIconWidth(), getStartIconHeight(), getStartImageTextId(), getDisplayName());
        } else {
            return getImageText(doc, getCoreDimensions().getXLeft(), getCoreDimensions().getYTop(), getStartIconWidth(), getStartIconHeight(), getStartImageTextId(), getDisplayName());
        }
    }

    public Element getTerminationHandlerIcon(SVGDocument doc) {
        SVGCoordinates coords = getStartTerminationCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        String iconPath = BPEL2SVGFactory.getInstance().getIconSource() + "/scopeterminationhandler" + BPEL2SVGFactory.getInstance().getIconExtension();

        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
    }

    public Element getFaultHandlerIcon(SVGDocument doc) {
        SVGCoordinates coords = getStartFaultCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        String iconPath = BPEL2SVGFactory.getInstance().getIconSource() + "/scopefaulthandler" + BPEL2SVGFactory.getInstance().getIconExtension();

        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
    }

    public Element getCompensationHandlerIcon(SVGDocument doc) {
        SVGCoordinates coords = getStartCompensationCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        String iconPath = BPEL2SVGFactory.getInstance().getIconSource() + "/scopecompensationhandler" + BPEL2SVGFactory.getInstance().getIconExtension();

        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
    }

    public Element getEventHandlerIcon(SVGDocument doc) {
        SVGCoordinates coords = getStartEventCoords();
        int xLeft = 0;
        int yTop = 0;
        if (layoutManager.isVerticalLayout()) {
            xLeft = coords.getXLeft() - getHandlerIconWidth();
            yTop = coords.getYTop() - (getHandlerIconHeight() / 2);
        } else {
            xLeft = coords.getXLeft() - (getHandlerIconWidth() / 2);
            yTop = coords.getYTop() - getHandlerIconHeight();
        }
        String iconPath = BPEL2SVGFactory.getInstance().getIconSource() + "/scopeeventhandler" + BPEL2SVGFactory.getInstance().getIconExtension();

        return getImageDefinition(doc, iconPath, xLeft, yTop, getHandlerIconWidth(), getHandlerIconHeight(), getId());
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

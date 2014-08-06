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
 * If tag UI impl
 */
public class IfImpl extends ActivityImpl implements IfInterface {

    private SVGDimension coreDimensions = null;
    private SVGDimension conditionalDimensions = null;

    public IfImpl(String token) {
        super(token);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public IfImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        // Set Layout
        setVerticalChildLayout(false);
    }

    public IfImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);
        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());

        setVerticalChildLayout(false);
    }

    protected int getElseIfAdjustment() {
        int adjustment = 0;
        if (layoutManager.isVerticalLayout()) {
            adjustment = getStartIconHeight() + getYSpacing();
        } else {
            adjustment = getStartIconWidth() + getYSpacing();
        }

        return adjustment;
    }

    @Override
    public String getId() {
        return getName(); // + "-If";
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.IF_END_TAG;
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
                if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                    if (subActivityDim.getHeight() > conHeight) {
                        conHeight += subActivityDim.getHeight();
                    }
                    conWidth += subActivityDim.getWidth();
                } else {
                    if (subActivityDim.getWidth() > coreWidth) {
                        coreWidth += subActivityDim.getWidth();
                    }
                    coreHeight += subActivityDim.getHeight();
                }
            }

            coreHeight += getYSpacing() + getStartIconHeight() + getEndIconHeight();
            conHeight += getElseIfAdjustment();

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

//            height += (getYSpacing() * 2) + getStartIconHeight() + getEndIconHeight();
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
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
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
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop + (getYSpacing() / 2);
        int endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        int endYTop = startYTop + dimensions.getHeight() - getEndIconHeight() - (getYSpacing() / 2);
        int centerNHLayout = startXLeft + (coreDimensions.getWidth() / 2);

        if (isSimpleLayout()) {
            xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
            endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        } else {
            xLeft = centerNHLayout - (getStartIconWidth() / 2) + (getXSpacing() / 2);
            endXLeft = centerNHLayout - (getEndIconWidth() / 2) + (getXSpacing() / 2);
        }

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();

        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft = startXLeft + (getXSpacing() / 2);

        // Process None Handlers First
        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                // Ignore
                //throw new UnsupportedOperationException("This operation is not currently supported in this version of WSO2 BPS.");
            } else {
                activity.layout(childXLeft, childYTop);
                childXLeft += activity.getDimensions().getWidth();
            }
        }
        // Process Handlers
        itr = getSubActivities().iterator();
        childXLeft = startXLeft + coreDimensions.getWidth();
        childYTop = yTop + getElseIfAdjustment();

        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
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
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    private void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);
        int endXLeft = startXLeft + dimensions.getWidth() - getEndIconWidth() - (getYSpacing() / 2);
        int endYTop = centreOfMyLayout - (getEndIconHeight() / 2);
        int centerNHLayout = startYTop + (coreDimensions.getHeight() / 2);

        if (isSimpleLayout()) {
            yTop = centreOfMyLayout - (getStartIconHeight() / 2);
            endYTop = centreOfMyLayout - (getEndIconHeight() / 2);
        } else {
            yTop = centerNHLayout - (getStartIconHeight() / 2);
            endYTop = centerNHLayout - (getEndIconHeight() / 2);
        }

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();

        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);

        // Process None Handlers First
        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                // Ignore
                //throw new UnsupportedOperationException("This operation is not currently supported in this version of WSO2 BPS.");
            } else {
                activity.layout(childXLeft, childYTop);
                childYTop += activity.getDimensions().getHeight();
            }
        }
        // Process Handlers
        itr = getSubActivities().iterator();
        childYTop = startYTop + coreDimensions.getHeight();
        childXLeft = xLeft + getElseIfAdjustment();

        while (itr.hasNext()) {
            activity = itr.next();
            if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
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

    protected SVGCoordinates getStartIconElseArrowCoords() {
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
        group1.appendChild(getStartImageText(doc));
        // Get Sub Activities
        group1.appendChild(getSubActivitiesSVGString(doc));
        group1.appendChild(getEndImageDefinition(doc));
        // Add Arrow
        group1.appendChild(getArrows(doc));

        //attention - here group1 contain the box definition+ImageDefinition+etc... in the original
        // but here group does not contain that

        return group1;
    }

    protected Element getArrows(SVGDocument doc) {
        if (subActivities != null) {
            ActivityInterface prevActivity = null;
            ActivityInterface prevElseActivity = null;
            ActivityInterface activity = null;
            String id = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            SVGCoordinates myStartElseCoords = getStartIconElseArrowCoords();
            SVGCoordinates exitCoords = null;
            SVGCoordinates activityEntryCoords = null;
            SVGCoordinates activityExitCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            Element subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
            while (itr.hasNext()) {
                activity = itr.next();
                activityEntryCoords = activity.getEntryArrowCoords();
                activityExitCoords = activity.getExitArrowCoords();

                if (activity instanceof ElseIfImpl || activity instanceof ElseImpl) {
                    if (prevActivity != null && prevActivity instanceof ElseIfImpl) {
                        exitCoords = ((ElseIfInterface) prevActivity).getNextElseExitArrowCoords();
//                        activityEntryCoords = activity.getEntryArrowCoords();
                        id = prevActivity.getId() + "-" + activity.getId();

                        subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                        subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));

                    } else {
//                        activityEntryCoords = activity.getEntryArrowCoords();
                        subGroup.appendChild(getArrowDefinition(doc, myStartElseCoords.getXLeft(), myStartElseCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                        subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));

                    }
                } else {
                    if (prevActivity != null) {
                        exitCoords = prevActivity.getExitArrowCoords();
//                        activityEntryCoords = activity.getEntryArrowCoords();
//                        activityExitCoords = activity.getExitArrowCoords();
                        id = prevActivity.getId() + "-" + activity.getId();
                        subGroup.appendChild(getArrowDefinition(doc, exitCoords.getXLeft(), exitCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                    } else {
//                        activityEntryCoords = activity.getEntryArrowCoords();
                        subGroup.appendChild(getArrowDefinition(doc, myStartCoords.getXLeft(), myStartCoords.getYTop(), activityEntryCoords.getXLeft(), activityEntryCoords.getYTop(), id));
                        subGroup.appendChild(getArrowDefinition(doc, activityExitCoords.getXLeft(), activityExitCoords.getYTop(), myExitCoords.getXLeft(), myExitCoords.getYTop(), id));

                    }
                }

                prevActivity = activity;
            }
            return subGroup;
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

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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.bom.Sources;
import org.h2.java.lang.System;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.wso2.carbon.bpel.ui.bpel2svg.*;

import java.util.*;

/**
 * Activity tag UI impl
 */
public abstract class ActivityImpl implements ActivityInterface {
    private static final Log log = LogFactory.getLog(ActivityImpl.class);
    // Local Variables
    protected LayoutManager layoutManager = BPEL2SVGFactory.getInstance()
            .getLayoutManager();
    protected String name = null;
    protected String displayName = null;
    protected List<ActivityInterface> subActivities = new ArrayList<ActivityInterface>();
    protected List<BPELAttributeValuePair> attributes = new ArrayList<BPELAttributeValuePair>();

    public List<BPELAttributeValuePair> getAttributes() {
        return attributes;
    }

    public Map<String, Link> links;
    protected Set<ActivityInterface> sources;
    protected Set<ActivityInterface> targets;

    public int getCorrectionY() {
        return correctionY;
    }

    public void setCorrectionY(int correctionY) {
        this.correctionY += correctionY;
    }

    protected int correctionY = 0;

    public ActivityInterface getParent() {
        return parent;
    }

    public void setParent(ActivityInterface parent) {
        this.parent = parent;
    }

    protected ActivityInterface parent = null;

    // Start Icon
    protected String startIconPath = null;
    protected int startIconHeight = layoutManager.getStartIconDim();
    protected int startIconWidth = layoutManager.getIconWidth();
    protected int startIconXLeft = 0;
    protected int startIconYTop = 0;
    protected int startIconTextXLeft = 0;
    protected int startIconTextYTop = 0;
    // End Icon
    protected String endIconPath = null;
    protected int endIconHeight = layoutManager.getEndIconDim();
    protected int endIconWidth = layoutManager.getIconWidth();
    protected int endIconXLeft = 0;
    protected int endIconYTop = 0;
    protected int endIconTextXLeft = 0;
    protected int endIconTextYTop = 0;
    // Layout
    protected boolean verticalChildLayout = true;
    // SVG Specific
    protected SVGDimension dimensions = null;
    protected boolean exitIcon = false;

    //SVG Batik Specific - I modify
    protected /*static*/ SVGGraphics2D generator = null;

    protected /*static*/ DOMImplementation dom = SVGDOMImplementation
            .getDOMImplementation();
    protected /*static*/ String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    protected /*static*/ SVGDocument doc = (SVGDocument) dom.createDocument(svgNS, "svg", null);
    protected /*static*/ Element root = doc.getDocumentElement();

    // Box
    public final static int BOX_MARGIN = 10;
    protected int boxXLeft = 0;
    protected int boxYTop = 0;
    protected int boxHeight = 0;
    protected int boxWidth = 0;
    protected String boxStyle = "fill-opacity:0.04;fill-rule:evenodd;stroke:#0000FF;stroke-width:1.99999988;"
            +
            "stroke-linecap:square;stroke-linejoin:bevel;stroke-miterlimit:1;stroke-dasharray:none;"
            +
            "bbbbbbbstroke-opacity:1;fill:url(#orange_red);stroke-opacity:0.2";

    // Constructor
    public ActivityImpl() {
        super();
    }

    public ActivityImpl(String token) {
        int nameIndex = token.indexOf("name");
        if (nameIndex >= 0) {
            int firstQuoteIndex = token.indexOf("\"", nameIndex + 1);
            if (firstQuoteIndex >= 0) {
                int lastQuoteIndex = token.indexOf("\"", firstQuoteIndex + 1);
                if (lastQuoteIndex > firstQuoteIndex) {
                    setName(token
                            .substring(firstQuoteIndex + 1, lastQuoteIndex));
                    setDisplayName(getName());
                }
            }
        }
    }

    public ActivityImpl(OMElement omElement) {
        Iterator tmpIterator = omElement.getAllAttributes();

        while (tmpIterator.hasNext()) {
            OMAttribute omAttribute = (OMAttribute) tmpIterator.next();
            String tmpAttribute = omAttribute.getLocalName();
            String tmpValue = omAttribute.getAttributeValue();

            if (tmpAttribute != null && tmpValue != null) {
                attributes.add(new BPELAttributeValuePair(tmpAttribute,
                        tmpValue));

                if (tmpAttribute.equals("name")) {
                    setName(tmpValue);
                    setDisplayName(getName());
                }
            }
        }
    }

    private boolean check = false;

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean isCheckIfinFlow() {
        return checkIfinFlow;
    }

    public void setCheckIfinFlow(boolean checkIfinFlow) {
        this.checkIfinFlow = checkIfinFlow;
    }

    private boolean checkIfinFlow;

    // Properties
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return getName();
    }

    public String getLayerId() {
        return getLayerId(getId());
    }

    public String getLayerId(String id) {
        return id; //+"-Layer";
    }

    public boolean isAddOpacity() {
        return layoutManager.isAddIconOpacity();
    }

    public boolean isAddCompositeActivityOpacity() {
        return layoutManager.isAddCompositeActivityOpacity();
    }

    public boolean isAddIconOpacity() {
        return layoutManager.isAddIconOpacity();
    }

    public boolean isAddSimpleActivityOpacity() {
        return layoutManager.isAddSimpleActivityOpacity();
    }

    public String getOpacity() {
        return layoutManager.getOpacity();
    }

    public String getSimpleActivityOpacity() {
        return layoutManager.getSimpleActivityOpacity();
    }

    public String getCompositeOpacity() {
        return layoutManager.getCompositeActivityOpacity();
    }

    public String getIconOpacity() {
        return layoutManager.getIconOpacity();
    }

    public String getBoxId() {
        return getId(); // + "-Box";
    }

    public String getStartImageId() {
        return getId(); // + "-StartImage";
    }

    public String getEndImageId() {
        return getId(); // + "-EndImage";
    }

    public String getArrowId(String startId, String endId) {
        return startId + "-" + endId + "-Arrow";
    }

    public String getStartImageTextId() {
        return getStartImageId(); // + "-Text";
    }

    public String getEndImageTextId() {
        return getEndImageId(); // + "-Text";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartIconHeight() {
        return startIconHeight;
    }

    public String getStartIconPath() {
        return startIconPath;
    }

    public void setStartIconPath(String iconPath) {
        this.startIconPath = iconPath;
    }

    public String getEndIconPath() {
        return endIconPath;
    }

    public int getStartIconWidth() {
        return startIconWidth;
    }

    public int getEndIconHeight() {
        return endIconHeight;
    }

    public int getEndIconWidth() {
        return endIconWidth;
    }

    public void setStartIconHeight(int iconHeight) {
        this.startIconHeight = iconHeight;
    }

    public void setStartIconWidth(int iconWidth) {
        this.startIconWidth = iconWidth;
    }

    public int getStartIconXLeft() {
        return startIconXLeft;
    }

    public void setStartIconXLeft(int xLeft) {
        this.startIconXLeft = xLeft;
    }

    public int getStartIconYTop() {
        return startIconYTop + correctionY;
    }

    public void setStartIconYTop(int yTop) {
        this.startIconYTop = yTop;
    }

    public int getStartIconTextXLeft() {
        return startIconTextXLeft;
    }

    public void setStartIconTextXLeft(int startIconTextXLeft) {
        this.startIconTextXLeft = startIconTextXLeft;
    }

    public int getStartIconTextYTop() {
        return startIconTextYTop + correctionY;
    }

    public void setStartIconTextYTop(int startIconTextYTop) {
        this.startIconTextYTop = startIconTextYTop;
    }

    public int getEndIconXLeft() {
        return endIconXLeft;
    }

    public void setEndIconXLeft(int xLeftEnd) {
        this.endIconXLeft = xLeftEnd;
    }

    public int getEndIconYTop() {
        return endIconYTop + correctionY;
    }

    public void setEndIconYTop(int yTopEnd) {
        this.endIconYTop = yTopEnd;
    }

    public int getEndIconTextXLeft() {
        return endIconTextXLeft;
    }

    public void setEndIconTextXLeft(int endIconTextXLeft) {
        this.endIconTextXLeft = endIconTextXLeft;
    }

    public int getEndIconTextYTop() {
        return endIconTextYTop;
    }

    public void setEndIconTextYTop(int endIconTextYTop) {
        this.endIconTextYTop = endIconTextYTop;
    }

    public int getXSpacing() {
        return layoutManager.getXSpacing();
    }

    public int getYSpacing() {
        return layoutManager.getYSpacing();
    }

    public int getBoxHeight() {
        return boxHeight;
    }

    public void setBoxHeight(int boxHeight) {
        this.boxHeight = boxHeight;
    }

    public String getBoxStyle() {
        return boxStyle;
    }

    public void setBoxStyle(String boxStyle) {
        this.boxStyle = boxStyle;
    }

    public int getBoxWidth() {
        return boxWidth;
    }

    public void setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
    }

    public int getBoxXLeft() {
        return boxXLeft;
    }

    public void setBoxXLeft(int boxXLeft) {
        this.boxXLeft = boxXLeft;
    }

    public int getBoxYTop() {
        return boxYTop;
    }

    public void setBoxYTop(int boxYTop) {
        this.boxYTop = boxYTop;
    }

    public boolean isExitIcon() {
        return exitIcon;
    }

    public void setExitIcon(boolean exitIcon) {
        this.exitIcon = exitIcon;
    }

    public void setEndIconHeight(int iconHeightEnd) {
        this.endIconHeight = iconHeightEnd;
    }

    public void setEndIconWidth(int iconWidthEnd) {
        this.endIconWidth = iconWidthEnd;
    }

    public boolean isIncludeAssigns() {
        return layoutManager.isIncludeAssigns();
    }

    public List<ActivityInterface> getSubActivities() {
        return subActivities;
    }

    public boolean isVerticalChildLayout() {
        return verticalChildLayout;
    }

    public void setVerticalChildLayout(boolean verticalChildLayout) {
        this.verticalChildLayout = verticalChildLayout;
    }

    public boolean isHorizontalChildLayout() {
        return !isVerticalChildLayout();
    }

    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());
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

    //Get the arrow coordinates of the activities
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        if (subActivities != null) {
            ActivityInterface activity = null;
            String id = null;
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            SVGCoordinates activityExitCoords = null;
            SVGCoordinates activityEntryCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            while (itr.hasNext()) {
                activity = itr.next();
                activityExitCoords = activity.getExitArrowCoords();
                activityEntryCoords = activity.getEntryArrowCoords();
                subGroup.appendChild(
                        getArrowDefinition(doc, myStartCoords.getXLeft(),
                                myStartCoords.getYTop(),
                                activityEntryCoords.getXLeft(),
                                activityEntryCoords.getYTop(), id));
                subGroup.appendChild(
                        getArrowDefinition(doc, activityExitCoords.getXLeft(),
                                activityExitCoords.getYTop(),
                                myExitCoords.getXLeft(), myExitCoords.getYTop(),
                                id));

            }
        }

        return subGroup;
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

    public Element getSubActivitiesSVGString(SVGDocument doc) {
        Iterator<ActivityInterface> itr = subActivities.iterator();
        ActivityInterface activity = null;
        Element subElement = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        while (itr.hasNext()) {
            activity = itr.next();
            subElement.appendChild(activity.getSVGString(doc));
            name = activity.getId();
        }
        return subElement;
    }

    //Get the images of the activities
    protected Element getImageDefinition(SVGDocument doc, String imgPath,
                                         int imgXLeft, int imgYTop, int imgWidth, int imgHeight, String id) {

        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());

        if (getStartIconPath() != null) {

            Element x = null;
            x = doc.createElementNS("http://www.w3.org/2000/svg", "g");
            x.setAttributeNS(null, "id", id);

            Element rect = doc.createElementNS("http://www.w3.org/2000/svg", "rect");
            rect.setAttributeNS(null, "x", String.valueOf(imgXLeft));
            rect.setAttributeNS(null, "y", String.valueOf(imgYTop));
            rect.setAttributeNS(null, "width", String.valueOf(imgWidth));
            rect.setAttributeNS(null, "height", String.valueOf(imgHeight));
            rect.setAttributeNS(null, "id", id);
            rect.setAttributeNS(null, "rx", "10");
            rect.setAttributeNS(null, "ry", "10");
            rect.setAttributeNS(null, "style", "fill:white;stroke:black;stroke-width:1.5;fill-opacity:0.1");

            int embedImageX = imgXLeft + 25;
            int embedImageY = (imgYTop + (5 / 2));
            int embedImageHeight = 45;
            int embedImageWidth = 50;

            Element embedImage = doc.createElementNS("http://www.w3.org/2000/svg", "image");
            embedImage.setAttributeNS(null, "xlink:href", imgPath);
            embedImage.setAttributeNS(null, "x", String.valueOf(embedImageX));
            embedImage.setAttributeNS(null, "y", String.valueOf(embedImageY));
            embedImage.setAttributeNS(null, "width", String.valueOf(embedImageWidth));
            embedImage.setAttributeNS(null, "height", String.valueOf(embedImageHeight));

            x.appendChild(rect);
            x.appendChild(embedImage);

            return x;
        }
        return group;
    }

    protected Element getImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getStartIconPath(), getStartIconXLeft(),
                getStartIconYTop(), getStartIconWidth(), getStartIconHeight(),
                getStartImageId());
    }

    protected Element getEndImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getEndIconPath(), getEndIconXLeft(),
                getEndIconYTop(), getEndIconWidth(), getEndIconHeight(),
                getEndImageId());
    }

    protected Element getStartImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getStartIconPath(), getStartIconXLeft(),
                getStartIconYTop(), getStartIconWidth(), getStartIconHeight(),
                getStartImageId());
    }

    protected Element getImageText(SVGDocument doc, int imgXLeft, int imgYTop, int imgWidth,
                                   int imgHeight, String imgName, String imgDisplayName) {
        int txtXLeft = imgXLeft;
        int txtYTop = imgYTop;

        Element a = doc.createElementNS("http://www.w3.org/2000/svg", "a");
        if (imgDisplayName != null) {
            a.setAttributeNS(null, "id", imgName);

            Element text1 = doc
                    .createElementNS("http://www.w3.org/2000/svg", "text");
            text1.setAttributeNS(null, "x", String.valueOf(txtXLeft));
            text1.setAttributeNS(null, "y", String.valueOf(txtYTop));
            text1.setAttributeNS(null, "id", imgName + ".Text");
            text1.setAttributeNS(null, "xml:space", "preserve");
            text1.setAttributeNS(null, "style",
                    "font-size:12px;font-style:normal;font-variant:normal;font-weight:"
                            +
                            "normal;font-stretch:normal;text-align:start;line-height:125%;writing-mode:lr-tb;text-anchor:"
                            +
                            "start;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;"
                            +
                            "stroke-linejoin:bevel;stroke-opacity:1;font-family:Arial Narrow;"
                            +
                            "-inkscape-font-specification:Arial Narrow");

            Element tspan = doc
                    .createElementNS("http://www.w3.org/2000/svg", "tspan");
            tspan.setAttributeNS(null, "x", String.valueOf(txtXLeft + 5));
            tspan.setAttributeNS(null, "y", String.valueOf(txtYTop + 5));
            tspan.setAttributeNS(null, "id", "tspan-" + imgName);

            Text text2 = doc.createTextNode(imgDisplayName);
            tspan.appendChild(text2);

            text1.appendChild(tspan);
            a.appendChild(text1);
        }
        return a;
    }

    protected Element getStartImageText(SVGDocument doc) {
        return getImageText(doc, getStartIconTextXLeft(),
                getStartIconTextYTop(), getStartIconWidth(),
                getStartIconHeight(), getStartImageTextId(), getDisplayName());
    }

    protected void getEndImageText(SVGDocument doc) {
        getImageText(doc, getEndIconTextXLeft(), getEndIconTextYTop(),
                getStartIconWidth(), getStartIconHeight(), getEndImageTextId(),
                getDisplayName());
    }

    protected boolean isLargeArrow() {
        return largeArrow;
    }

    protected void setLargeArrow(boolean largeArrow) {
        this.largeArrow = largeArrow;
    }

    protected boolean largeArrow = false;

    protected String getArrowStyle() {
        String largeArrowStr =
                "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.5;stroke-linecap:"
                        +
                        "butt;stroke-linejoin:bevel;marker-end:url(#Arrow1Lend);stroke-dasharray:"
                        +
                        "none;stroke-opacity:1";
        String mediumArrowStr =
                "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.5;stroke-linecap:"
                        +
                        "butt;stroke-linejoin:bevel;marker-end:url(#Arrow1Mend);stroke-dasharray:"
                        +
                        "none;stroke-opacity:1";

        if (largeArrow) {
            return largeArrowStr;
        } else {
            return mediumArrowStr;
        }
    }

    protected String getLinkArrowStyle() {
        String largeArrowStr =
                "fill:none;fill-rule:evenodd;stroke:#FF0000;stroke-width:3;stroke-linecap:"
                        +
                        "butt;stroke-linejoin:bevel;marker-end:url(#LinkArrow);stroke-dasharray:"
                        +
                        "none;stroke-opacity:1;opacity: 0.25;";
        String mediumArrowStr =
                "fill:none;fill-rule:evenodd;stroke:#FF0000;stroke-width:3;stroke-linecap:"
                        +
                        "butt;stroke-linejoin:bevel;marker-end:url(#LinkArrow);stroke-dasharray:"
                        +
                        "none;stroke-opacity:1;opacity: 0.25;";

        if (largeArrow) {
            return largeArrowStr;
        } else {
            return mediumArrowStr;
        }
    }

    //Get the arrow definitions/paths from the coordinates
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");

        if (startX == endX || startY == endY) {
            path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + endX + "," + endY);
        } else {
            if (layoutManager.isVerticalLayout()) {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + startX + "," + ((startY + 2 * endY) / 3) + " L " + endX + ","
                        + ((startY + 2 * endY) / 3) + " L " + endX + "," + endY);
            } else {
                path.setAttributeNS(null, "d", "M " + startX + "," + startY + " L " + ((startX + 1 * endX) / 2) +
                        "," + startY + " L " + ((startX + 1 * endX) / 2) + "," + endY + " L " + endX + ","
                        + endY);
            }
        }
        path.setAttributeNS(null, "id", id);
        path.setAttributeNS(null, "style", getArrowStyle());

        return path;
    }

    protected Element getArrowDefinition(SVGDocument doc, int startX,
                                         int startY, int midX, int midY, int endX, int endY, String id) {
        Element path = doc
                .createElementNS("http://www.w3.org/2000/svg", "path");
        path.setAttributeNS(null, "d",
                "M " + startX + "," + startY + " L " + midX + "," + midY + "L "
                        + endX +
                        "," + endY);
        path.setAttributeNS(null, "id", id);
        path.setAttributeNS(null, "style", getArrowStyle());

        return path;
    }

    protected Element getBoxDefinition(SVGDocument doc) {
        return getBoxDefinition(doc, getDimensions().getXLeft() + BOX_MARGIN,
                getDimensions().getYTop() + BOX_MARGIN,
                getDimensions().getWidth() - (BOX_MARGIN * 2),
                getDimensions().getHeight() - (BOX_MARGIN * 2), getBoxId());
    }

    protected Element getBoxDefinition(SVGDocument doc, int boxXLeft, int boxYTop, int boxWidth, int boxHeight, String id) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", "Layer-" + id);

        if (layoutManager.isShowSequenceBoxes()) {

            Element rect = doc.createElementNS("http://www.w3.org/2000/svg", "rect");
            rect.setAttributeNS(null, "width", String.valueOf(boxWidth));
            rect.setAttributeNS(null, "height", String.valueOf(boxHeight));
            rect.setAttributeNS(null, "x", String.valueOf(boxXLeft));
            rect.setAttributeNS(null, "y", String.valueOf(boxYTop));
            rect.setAttributeNS(null, "id", "Rect" + id);
            rect.setAttributeNS(null, "rx", "10");
            rect.setAttributeNS(null, "ry", "10");
            rect.setAttributeNS(null, "style", boxStyle);

            group.appendChild(rect);
        }
        return group;
    }

    public SVGDimension getDimensions() {
        SVGDimension obj = new SVGDimension();
        obj.setHeight(layoutManager.getSvgHeight());
        obj.setWidth(layoutManager.getSvgWidth());
        return obj;
    }

    public void switchDimensionsToHorizontal() {
        int width = 0;
        int height = 0;

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        while (itr.hasNext()) {
            activity = itr.next();
            activity.switchDimensionsToHorizontal();
        }

        width = getDimensions().getWidth();
        height = getDimensions().getHeight();
        // Switch
        getDimensions().setHeight(width);
        getDimensions().setWidth(height);
    }

    public void layout(int startXLeft, int startYTop) {
        if (layoutManager.isVerticalLayout()) {
            layoutVertical(startXLeft, startYTop);
        } else {
            layoutHorizontal(startXLeft, startYTop);
        }
    }

    public void layoutVertical(int startXLeft, int startYTop) {
        dimensions = getDimensions();
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop + (getYSpacing() / 2);
        int endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        int endYTop =
                startYTop + dimensions.getHeight() - getEndIconHeight() - (
                        getYSpacing() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft = startXLeft + (getXSpacing() / 2);
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
        setStartIconTextYTop(
                startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    private void layoutHorizontal(int startXLeft, int startYTop) {
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);
        int endXLeft =
                startXLeft + dimensions.getWidth() - getEndIconWidth() - (
                        getYSpacing() / 2);
        int endYTop = centreOfMyLayout - (getEndIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);
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
        setStartIconTextYTop(
                startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }

    public String getEndTag() {
        return BPEL2SVGFactory.CATCH_END_TAG;
    }

    @Override
    public String toString() {
        return getId();
    }

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

    public void passContent() {
        root = doc.getDocumentElement();
        generator.getRoot(root);
    }

    // Methods

    public Set<ActivityInterface> getLinkRoots() {
        sources.removeAll(targets);
        return sources;
    }

    //Get the subactivites in the bpel process
    public ActivityInterface processSubActivities(OMElement omElement) {
        ActivityInterface endActivity = null;
        if (omElement != null) {
            ActivityInterface activity = null;
            Iterator iterator = omElement.getChildElements();
            while (iterator.hasNext()) {
                OMElement tmpElement = (OMElement) iterator.next();
                if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.ASSIGN_START_TAG)
                        && isIncludeAssigns()) {
                    activity = new AssignImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.EMPTY_START_TAG)) {
                    activity = new EmptyImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.CATCHALL_START_TAG)) {
                    activity = new CatchAllImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.CATCH_START_TAG)) {
                    activity = new CatchImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.COMPENSATESCOPE_START_TAG)) {
                    activity = new CompensateScopeImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.COMPENSATE_START_TAG)) {
                    activity = new CompensateImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.COMPENSATIONHANDLER_START_TAG)) {
                    activity = new CompensationHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.ELSEIF_START_TAG)) {
                    activity = new ElseIfImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.ELSE_START_TAG)) {
                    activity = new ElseImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.EVENTHANDLER_START_TAG)) {
                    activity = new EventHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.EXIT_START_TAG)) {
                    activity = new ExitImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.FAULTHANDLER_START_TAG)) {
                    activity = new FaultHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.FLOW_START_TAG)) {
                    activity = new FlowImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.FOREACH_START_TAG)) {
                    activity = new ForEachImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.IF_START_TAG)) {
                    activity = new IfImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.INVOKE_START_TAG)) {
                    activity = new InvokeImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.ONALARM_START_TAG)) {
                    activity = new OnAlarmImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.ONEVENT_START_TAG)) {
                    activity = new OnEventImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.ONMESSAGE_START_TAG)) {
                    activity = new OnMessageImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.PICK_START_TAG)) {
                    activity = new PickImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.PROCESS_START_TAG)) {
                    activity = new ProcessImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.RECEIVE_START_TAG)) {
                    activity = new ReceiveImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.REPEATUNTIL_START_TAG)) {
                    activity = new RepeatUntilImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.REPLY_START_TAG)) {
                    activity = new ReplyImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.RETHROW_START_TAG)) {
                    activity = new ReThrowImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.SCOPE_START_TAG)) {
                    activity = new ScopeImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.SEQUENCE_START_TAG)) {
                    activity = new SequenceImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.SOURCE_START_TAG)) {
                    activity = new SourceImpl(tmpElement, this);//source;
                    if (activity.getAttributes().get(0).getAttribute()
                            .equals("linkName")) {
                        if (links.containsKey(activity.getAttributes().get(0)
                                .getValue())) {    //if a entry for the particular link name already exists
                            links.get(
                                    activity.getAttributes().get(0).getValue())
                                    .setSource(this.parent);
                        } else {
                            Link link = new Link();
                            link.setSource(this.parent);
                            links.put(
                                    activity.getAttributes().get(0).getValue(),
                                    link);
                        }
                        sources.add(this.parent);
                    }
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.SOURCES_START_TAG)) {
                    activity = new SourcesImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.TARGET_START_TAG)) {
                    activity = new TargetImpl(tmpElement, this);//target;
                    if (activity.getAttributes().get(0).getAttribute()
                            .equals("linkName")) {
                        if (links.containsKey(
                                activity.getAttributes().get(0).getValue())) {
                            links.get(
                                    activity.getAttributes().get(0).getValue())
                                    .setTarget(this.parent);
                        } else {
                            Link link = new Link();
                            link.setTarget(this.parent);
                            links.put(
                                    activity.getAttributes().get(0).getValue(),
                                    link);
                        }
                        targets.add(this.parent);
                    }
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.TARGETS_START_TAG)) {
                    activity = new TargetsImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.TERMINATIONHANDLER_START_TAG)) {
                    activity = new TerminationHandlerImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.THROW_START_TAG)) {
                    activity = new ThrowImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.WAIT_START_TAG)) {
                    activity = new WaitImpl(tmpElement, this);
                } else if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.WHILE_START_TAG)) {
                    activity = new WhileImpl(tmpElement, this);
                } else if (tmpElement.getLocalName().equals(getEndTag())) {
                    break;
                } else {
                    continue;
                }

                activity.setLinkProperties(links, sources, targets);
               /* if(activity instanceof SourceImpl || activity instanceof TargetImpl || activity instanceof SourcesImpl || activity instanceof TargetImpl)
                {
                    log.info("Only a Source");
                }
                else {
                    log.info("Not a source or target");
                    subActivities.add(activity);
                }*/
                subActivities.add(activity);

                if (tmpElement.getChildElements().hasNext()) {
                    ActivityInterface replyActivity = activity
                            .processSubActivities(tmpElement);
                    if (replyActivity != null) {
                        subActivities.add(replyActivity);
                    }
                }
                if (tmpElement.getLocalName()
                        .equals(BPEL2SVGFactory.PROCESS_START_TAG)) {
                    break;
                }
            }
        }
        return endActivity;
    }

    public Element getRoot() {
        return root;
    }

    public String getActivityInfoString() {
        String infoString = null;
        for (BPELAttributeValuePair x : attributes) {
            String attrib = x.getAttribute();
            String val = x.getValue();
            if (infoString == null)
                infoString = "<" + attrib + "=" + val + "> ";
            else
                infoString += "<" + attrib + "=" + val + "> ";
        }

        if (infoString != null)
            return infoString;
        else
            return "No Attributes defined";
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinkProperties(Map<String, Link> links,
                                  Set<ActivityInterface> sources, Set<ActivityInterface> targets) {
        this.links = links;
        this.sources = sources;
        this.targets = targets;
    }
}

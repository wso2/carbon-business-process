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
 * Implements the Activity element UI implementation
 */
public abstract class ActivityImpl implements ActivityInterface {
    private static final Log log = LogFactory.getLog(ActivityImpl.class);

    // Local Variables
    protected LayoutManager layoutManager = BPEL2SVGFactory.getInstance()
            .getLayoutManager();
    protected String name = null;
    protected String displayName = null;

    //List defined to keep the subactivities of a bpel process
    protected List<ActivityInterface> subActivities = new ArrayList<ActivityInterface>();

    protected List<BPELAttributeValuePair> attributes = new ArrayList<BPELAttributeValuePair>();

    /**
     * Gets the attributes of the activities
     * @return list with the attributes of the activities
     */
    public List<BPELAttributeValuePair> getAttributes() {
        return attributes;
    }

    /**
     * Properties related to the Flow Activity
     * links: Give a level of dependency indicating that the activity that is the target of the link
     * is only executed if the activity that is the source of the link has completed.
     * sources:Known as the starting activity of the link
     * targets: Known as the ending activity/destination of the link
     */
    public Map<String, Link> links;
    protected Set<ActivityInterface> sources;
    protected Set<ActivityInterface> targets;

    /**
     * Gets the value of correctionY i.e. corrected value of the yTop position
     * @return correctionY
     */
    public int getCorrectionY() {
        return correctionY;
    }

    /**
     * Sets the value of correctionY i.e. corrected value of the yTop position
     * @param correctionY
     */
    public void setCorrectionY(int correctionY) {
        this.correctionY += correctionY;
    }

    protected int correctionY = 0;

    /**
     * When considering a composite activity which can have many subactivities inside it like a Sequence,
     * the parent of those subactivities will always be the composite activity which holds them.
     * @return- parent of the activity that invokes the method from its constructor
     */
    protected ActivityInterface parent = null;

    /**
     * Gets the parent activity of any given activity
     * @return parent
     */
    public ActivityInterface getParent() {
        return parent;
    }

    /**
     * Sets the parent activity of any given activity
     * @param parent
     */
    public void setParent(ActivityInterface parent) {
        this.parent = parent;
    }


    // Attributes of the Start Icon
    protected String startIconPath = null;
    protected int startIconHeight = layoutManager.getStartIconDim();
    protected int startIconWidth = layoutManager.getIconWidth();
    protected int startIconXLeft = 0;
    protected int startIconYTop = 0;
    protected int startIconTextXLeft = 0;
    protected int startIconTextYTop = 0;
    // Attributes of the End Icon
    protected String endIconPath = null;
    protected int endIconHeight = layoutManager.getEndIconDim();
    protected int endIconWidth = layoutManager.getIconWidth();
    protected int endIconXLeft = 0;
    protected int endIconYTop = 0;
    protected int endIconTextXLeft = 0;
    protected int endIconTextYTop = 0;
    // Attributes of the Layout
    protected boolean verticalChildLayout = true;
    // SVG Specific
    protected SVGDimension dimensions = null;
    protected boolean exitIcon = false;

    //SVG Batik Specific
    protected /*static*/ SVGGraphics2D generator = null;

    protected /*static*/ DOMImplementation dom = SVGDOMImplementation
            .getDOMImplementation();
    protected /*static*/ String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    protected /*static*/ SVGDocument doc = (SVGDocument) dom.createDocument(svgNS, "svg", null);
    protected /*static*/ Element root = doc.getDocumentElement();

    // Attributes of the Box/Scope which holds the subActivities
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
    /**
     * Initializes a new instance of the ActivityImpl class using the specified string i.e. the token
     * @param token
     */
    public ActivityImpl(String token) {
        int nameIndex = token.indexOf("name");
        if (nameIndex >= 0) {
            int firstQuoteIndex = token.indexOf("\"", nameIndex + 1);
            if (firstQuoteIndex >= 0) {
                int lastQuoteIndex = token.indexOf("\"", firstQuoteIndex + 1);
                if (lastQuoteIndex > firstQuoteIndex) {
                    setName(token
                            .substring(firstQuoteIndex + 1, lastQuoteIndex));
                    //Set the name of the activity
                    setDisplayName(getName());
                }
            }
        }
    }

    /**
     * When processing for subActivities in a process, the process is iterated and each activity is taken into a temp omElement.
     * If the name of the temp omElement matches the tag name of an activity , the constructor of that activity implementation is invoked.
     * The constructor of the activity implementation invokes this method which is the constructor of the base class by passing
     * the omElement as a @param
     * Gets the name and the value of the omElement that is taken as the @param
     * The attribute name and the attribute value is added to a list as a key-value pair
     *
     * @param omElement -an activity of the bpel process (obtained by iterating the omElement which contains the process definition)
     *
     */
    public ActivityImpl(OMElement omElement) {
        Iterator tmpIterator = omElement.getAllAttributes();
        //Iterates through the attributes of the omElement
        while (tmpIterator.hasNext()) {
            OMAttribute omAttribute = (OMAttribute) tmpIterator.next();
            //Gets the type of the activity
            String tmpAttribute = omAttribute.getLocalName();
            //Gets the name of the activity
            String tmpValue = omAttribute.getAttributeValue();

            if (tmpAttribute != null && tmpValue != null) {
                //type and the name of the attribute is added to a list
                attributes.add(new BPELAttributeValuePair(tmpAttribute,
                        tmpValue));

                if (tmpAttribute.equals("name")) {
                    //Set the name of the activity
                    setName(tmpValue);
                    setDisplayName(getName());
                }
            }
        }
    }
    /*
        Property related to the SEQUENCE Activity
        If a Throw activity is in the Sequence, check --> true
        Else check --> false
        This check is done to remove the exit arrow from the Throw activity, as once a process reaches a Throw activity
        the process terminates from that place without continuing.
    */
    private boolean check = false;

    /**
     * Gets the value of check
     * @return check--> true/false
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * Sets the value of check to true or false which is determined by whether a Throw activity is in a Sequence
     * @param check
     */
    public void setCheck(boolean check) {
        this.check = check;
    }
    /*
       Property related to the FLOW Activity
       If a Throw activity is in the Flow activity, check --> true
       Else check --> false
       This check is done to remove the exit arrow from the Throw activity, as once a process reaches a Throw activity
       the process terminates from that place without continuing.
   */
    private boolean checkIfinFlow;

    /**
     * Gets the value of check
     * @return checkIfinFlow--> true/false
     */
    public boolean isCheckIfinFlow() {
        return checkIfinFlow;
    }

    /**
     * Sets the value of checkIfinFlow to true or false which is determined by whether a Throw activity is in a Flow
     * @param checkIfinFlow
     */
    public void setCheckIfinFlow(boolean checkIfinFlow) {
        this.checkIfinFlow = checkIfinFlow;
    }

    // Properties

    /**
     * Get the name of the activity to be displayed
     * @return name of the activity
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the name of the activity to be displayed
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the id/name of the activity
     * @return name of the activity
     */
    public String getId() {
        return getName();
    }

    /**
     * Get the layer id/ activiy name
     * @return name of the activity
     */
    public String getLayerId() {
        return getLayerId(getId());
    }

    /**
     * Get the layer id by passing the id/name of the activity as a @param
     * @param id
     * @return id --> layer id/name of the activity
     */
    public String getLayerId(String id) {
        return id; //+"-Layer";
    }

    /**
     * Gets a true/false to add opacity
     * @return true/false
     */
    public boolean isAddOpacity() {
        return layoutManager.isAddIconOpacity();
    }
    /**
     * Gets a true/false to add  opacity to composite activity icons e.g:like IF, ELSE IF activities
     * @return true/false
     */
    public boolean isAddCompositeActivityOpacity() {
        return layoutManager.isAddCompositeActivityOpacity();
    }

    /**
     * Gets a true/false to add  opacity to activity icons
     * @return true/false
     */
    public boolean isAddIconOpacity() {
        return layoutManager.isAddIconOpacity();
    }
    /**
     * Gets a true/false to add  opacity to simple activity icons e.g:like ASSIGN, THROW activities
     * @return true/false
     */
    public boolean isAddSimpleActivityOpacity() {
        return layoutManager.isAddSimpleActivityOpacity();
    }

    /**
     * Gets the icon opacity amount
     * @return String with the opacity "0.5"
     */
    public String getOpacity() {
        return layoutManager.getOpacity();
    }
    /**
     * Gets the icon opacity amount for simple activities
     * @return String with the opacity "0.251"
     */
    public String getSimpleActivityOpacity() {
        return layoutManager.getSimpleActivityOpacity();
    }
    /**
     * Gets the icon opacity amount for composite activities
     * @return String with the opacity "0.10"
     */
    public String getCompositeOpacity() {
        return layoutManager.getCompositeActivityOpacity();
    }
    /**
     * Gets the icon opacity amount for activities
     * @return String with the opacity "0.25"
     */
    public String getIconOpacity() {
        return layoutManager.getIconOpacity();
    }

    /**
     * Gets the id of the box to be displayed
     * @return String with the id of the box
     */
    public String getBoxId() {
        return getId(); // + "-Box";
    }

    /**
     * Gets the id of the start image to be displayed
     * @return String with the id of the start image
     */
    public String getStartImageId() {
        return getId(); // + "-StartImage";
    }

    /**
     * Gets the id of the end image to be displayed
     * @return String with the id of the end image
     */
    public String getEndImageId() {
        return getId(); // + "-EndImage";
    }

    /**
     * Gets the id of the arrow flow i.e. id of the start activity + id of the end activity to be displayed
     * @return String with the id of the start activity + id of the end activity
     */
    public String getArrowId(String startId, String endId) {
        return startId + "-" + endId + "-Arrow";
    }

    /**
     * Gets the id of the start image text to be displayed
     * @return String with the id of the start image text
     */
    public String getStartImageTextId() {
        return getStartImageId(); // + "-Text";
    }

    /**
     * Gets the id of the end image text to be displayed
     * @return String with the id of the end image text
     */
    public String getEndImageTextId() {
        return getEndImageId(); // + "-Text";
    }

    /**
     * Gets the name of the activity to be displayed
     * @return String with the name of the activity
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the activity name
     * @param name name of the activity to be displayed
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the height of the start icon of the activity
     * @return height of the start icon of the activity
     */
    public int getStartIconHeight() {
        return startIconHeight;
    }

    /**
     * Gets the icon path of the start icon of the activity
     * @return String with the icon path of the start icon of the activity
     */
    public String getStartIconPath() {
        return startIconPath;
    }

    /**
     * Sets the icon path of the start icon of the activity
     * @param iconPath icon path of the start icon of the activity
     */
    public void setStartIconPath(String iconPath) {
        this.startIconPath = iconPath;
    }
    /**
     * Gets the icon path of the end icon of the activity
     * @return String with the icon path of the end icon of the activity
     */
    public String getEndIconPath() {
        return endIconPath;
    }
    /**
     * Gets the width of the start icon of the activity
     * @return width of the start icon of the activity
     */
    public int getStartIconWidth() {
        return startIconWidth;
    }
    /**
     * Gets the height of the end icon of the activity
     * @return height of the end icon of the activity
     */
    public int getEndIconHeight() {
        return endIconHeight;
    }
    /**
     * Gets the width of the end icon of the activity
     * @return width of the end icon of the activity
     */
    public int getEndIconWidth() {
        return endIconWidth;
    }

    /**
     * Sets the height of the start icon of the activity
     * @param iconHeight height of the start icon of the activity
     */
    public void setStartIconHeight(int iconHeight) {
        this.startIconHeight = iconHeight;
    }
    /**
     * Sets the width of the start icon of the activity
     * @param iconWidth width of the start icon of the activity
     */
    public void setStartIconWidth(int iconWidth) {
        this.startIconWidth = iconWidth;
    }
    /**
     * Gets the xLeft position of the start icon of the activity
     * @return xLeft position of the start icon of the activity
     */
    public int getStartIconXLeft() {
        return startIconXLeft;
    }

    /**
     * Sets the xLeft position of the start icon of the activity
     * @param xLeft xLeft position of the start icon of the activity
     */
    public void setStartIconXLeft(int xLeft) {
        this.startIconXLeft = xLeft;
    }
    /**
     * Gets the yTop position of the start icon of the activity
     * @return yTop position of the start icon of the activity
     */
    public int getStartIconYTop() {
        return startIconYTop + correctionY;
    }
    /**
     * Sets the yTop position of the start icon of the activity
     * @param yTop yTop position of the start icon of the activity
     */
    public void setStartIconYTop(int yTop) {
        this.startIconYTop = yTop;
    }
    /**
     * Gets the xLeft position of the start image text of the activity
     * @return xLeft position of the start image text of the activity
     */
    public int getStartIconTextXLeft() {
        return startIconTextXLeft;
    }

    /**
     * Sets the xLeft position of the start image text of the activity
     * @param startIconTextXLeft xLeft position of the start image text of the activity
     */
    public void setStartIconTextXLeft(int startIconTextXLeft) {
        this.startIconTextXLeft = startIconTextXLeft;
    }
    /**
     * Gets the yTop position of the start image text of the activity
     * @return yTop position of the start image text of the activity
     */
    public int getStartIconTextYTop() {
        return startIconTextYTop + correctionY;
    }
    /**
     * Sets the yTop position of the start image text of the activity
     * @param startIconTextYTop yTop position of the start image text of the activity
     */
    public void setStartIconTextYTop(int startIconTextYTop) {
        this.startIconTextYTop = startIconTextYTop;
    }
    /**
     * Gets the xLeft position of the end icon of the activity
     * @return xLeft position of the end icon of the activity
     */
    public int getEndIconXLeft() {
        return endIconXLeft;
    }
    /**
     * Sets the xLeft position of the end icon of the activity
     * @param xLeftEnd xLeft position of the end icon of the activity
     */
    public void setEndIconXLeft(int xLeftEnd) {
        this.endIconXLeft = xLeftEnd;
    }
    /**
     * Gets the yTop position of the end icon of the activity
     * @return yTop position of the end icon of the activity
     */
    public int getEndIconYTop() {
        return endIconYTop + correctionY;
    }
    /**
     * Sets the yTop position of the end icon of the activity
     * @param yTopEnd yTop position of the end icon of the activity
     */
    public void setEndIconYTop(int yTopEnd) {
        this.endIconYTop = yTopEnd;
    }
    /**
     * Gets the xLeft position of the end image text of the activity
     * @return xLeft position of the end image text of the activity
     */
    public int getEndIconTextXLeft() {
        return endIconTextXLeft;
    }
    /**
     * Sets the xLeft position of the end image text of the activity
     * @param endIconTextXLeft xLeft position of the end image text of the activity
     */
    public void setEndIconTextXLeft(int endIconTextXLeft) {
        this.endIconTextXLeft = endIconTextXLeft;
    }
    /**
     * Gets the yTop position of the end image text of the activity
     * @return yTop position of the end image text of the activity
     */
    public int getEndIconTextYTop() {
        return endIconTextYTop;
    }
    /**
     * Sets the yTop position of the end image text of the activity
     * @param endIconTextYTop yTop position of the end image text of the activity
     */
    public void setEndIconTextYTop(int endIconTextYTop) {
        this.endIconTextYTop = endIconTextYTop;
    }

    /**
     * Gets the xSpacing which is added to the width of the activities when setting the dimensions
     * @return xSpacing "50"
     */
    public int getXSpacing() {
        return layoutManager.getXSpacing();
    }
    /**
     * Gets the ySpacing which is added to the height of the activities when setting the dimensions
     * @return ySpacing "70"
     */
    public int getYSpacing() {
        return layoutManager.getYSpacing();
    }
    /**
     * Gets the box height after calculating the dimensions
     * @return box height after calculating the dimensions
     */
    public int getBoxHeight() {
        return boxHeight;
    }

    /**
     * Sets the height of the box after calculating the dimensions
     * @param boxHeight height of the box
     */
    public void setBoxHeight(int boxHeight) {
        this.boxHeight = boxHeight;
    }

    /**
     * Gets the box styling attributes
     * @return String with the styling attributes of the box
     */
    public String getBoxStyle() {
        return boxStyle;
    }

    /**
     * Sets the box styling attributes
     * @param boxStyle styling attributes of the box
     */
    public void setBoxStyle(String boxStyle) {
        this.boxStyle = boxStyle;
    }

    /**
     * Gets the box width after calculating the dimensions
     * @return width of the box after calculating the dimensions
     */
    public int getBoxWidth() {
        return boxWidth;
    }
    /**
     * Sets the width of the box after calculating the dimensions
     * @param boxWidth width of the box
     */
    public void setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
    }
    /**
     * Gets the xLeft position of the box
     * @return xLeft position of the box
     */
    public int getBoxXLeft() {
        return boxXLeft;
    }

    /**
     * Sets the xLeft position of the box
     * @param boxXLeft xLeft position of the box
     */
    public void setBoxXLeft(int boxXLeft) {
        this.boxXLeft = boxXLeft;
    }
    /**
     * Gets the yTop position of the box
     * @return yTop position of the box
     */
    public int getBoxYTop() {
        return boxYTop;
    }
    /**
     * Sets the yTop position of the box
     * @param boxYTop yTop position of the box
     */
    public void setBoxYTop(int boxYTop) {
        this.boxYTop = boxYTop;
    }

    /**
     *
     * @return true/false
     */
    public boolean isExitIcon() {
        return exitIcon;
    }

    /**
     * Sets true/false to add the exit icon
     * @param exitIcon true/false
     */
    public void setExitIcon(boolean exitIcon) {
        this.exitIcon = exitIcon;
    }
    /**
     * Sets the height of the end icon of the activity
     * @param iconHeightEnd height of the end icon of the activity
     */
    public void setEndIconHeight(int iconHeightEnd) {
        this.endIconHeight = iconHeightEnd;
    }
    /**
     * Sets the width of the end icon of the activity
     * @param iconWidthEnd width of the end icon of the activity
     */
    public void setEndIconWidth(int iconWidthEnd) {
        this.endIconWidth = iconWidthEnd;
    }

    /**
     * Gets the boolean value to include the assign activities
     * @return boolean value to include the assign activities->true/false
     */
    public boolean isIncludeAssigns() {
        return layoutManager.isIncludeAssigns();
    }

    /**
     * Gets the list of subActivities of a process
     * @return list of subActivities of a process
     */
    public List<ActivityInterface> getSubActivities() {
        return subActivities;
    }

    /**
     * Gets true/false for the vertical layout of the child activities
     * @return true/false
     */
    public boolean isVerticalChildLayout() {
        return verticalChildLayout;
    }

    /**
     * Sets true/false for the vertical layout of the child activities
     * @param verticalChildLayout true/false
     */
    public void setVerticalChildLayout(boolean verticalChildLayout) {
        this.verticalChildLayout = verticalChildLayout;
    }
    /**
     * Gets true/false for the horizontal layout of the child subActivities
     * @return true/false
     */
    public boolean isHorizontalChildLayout() {
        return !isVerticalChildLayout();
    }
    /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the  activity
     */
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Get the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        //Get the box/scope where the subActivities are placed
        group.appendChild(getBoxDefinition(doc));
        //Get the icon definition of the activity
        group.appendChild(getImageDefinition(doc));
        //Get the start icon/image text of the activity
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Get the end icon of the activity
        group.appendChild(getEndImageDefinition(doc));
        //Get the arrow flows of the activity
        group.appendChild(getArrows(doc));

        return group;
    }

    /**
     * Get the arrow coordinates of the activities
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return An element which contains the arrow coordinates of the If activity and its subActivities
     */
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        if (subActivities != null) {
            ActivityInterface activity = null;
            String id = null;
            //Coordinates of the start icon exit arrow
            SVGCoordinates myStartCoords = getStartIconExitArrowCoords();
            //Coordinates of the end icon entry arrow
            SVGCoordinates myExitCoords = getEndIconEntryArrowCoords();
            SVGCoordinates activityExitCoords = null;
            SVGCoordinates activityEntryCoords = null;
            Iterator<ActivityInterface> itr = subActivities.iterator();
            //Iterate through the subActivities
            while (itr.hasNext()) {
                activity = itr.next();
                //Gets the entry and exit coordinates of the iterated activity
                activityExitCoords = activity.getExitArrowCoords();
                activityEntryCoords = activity.getEntryArrowCoords();
                //Define the entry arrow flow coordinates for the activity
                subGroup.appendChild(
                        getArrowDefinition(doc, myStartCoords.getXLeft(),
                                myStartCoords.getYTop(),
                                activityEntryCoords.getXLeft(),
                                activityEntryCoords.getYTop(), id));
                //Define the exit arrow flow coordinates for the activity
                subGroup.appendChild(
                        getArrowDefinition(doc, activityExitCoords.getXLeft(),
                                activityExitCoords.getYTop(),
                                myExitCoords.getXLeft(), myExitCoords.getYTop(),
                                id));

            }
        }

        return subGroup;
    }
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which leaves the activity
     * @return coordinates of the exit arrow for the activity
     * After Calculations(Vertical Layout): xLeft= Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon + height of the icon
     */
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
        //Returns the calculated coordinate points of the exit arrow of the activity
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which enters the activity
     * @return coordinates of the entry arrow for the activity
     * After Calculations(Vertical Layout): xLeft= Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     */
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
        //Returns the calculated coordinate points of the entry arrow of the endIcon
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }

    /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the subActivities
     * @return Element(represents an element in a XML/HTML document) which contains the components of the subActivities
     */
    public Element getSubActivitiesSVGString(SVGDocument doc) {
        Iterator<ActivityInterface> itr = subActivities.iterator();
        ActivityInterface activity = null;
        Element subElement = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Embeds the Element returned by each subActivity(which contains the components of the subActivities) into the SVG container <g>
            subElement.appendChild(activity.getSVGString(doc));
            //Get the name of the activity
            name = activity.getId();
        }
        return subElement;
    }

    /**
     *Image Definitions or attributes for the activity icons
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @param imgPath path of the activity icon
     * @param imgXLeft xLeft position of the image
     * @param imgYTop  yTop position of the image
     * @param imgWidth width of the image
     * @param imgHeight height of the image
     * @param id id of the activity
     * @return
     */
    protected Element getImageDefinition(SVGDocument doc, String imgPath,
                                         int imgXLeft, int imgYTop, int imgWidth, int imgHeight, String id) {

        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        group.setAttributeNS(null, "id", getLayerId());
        //Checks whether the start icon path is null
        if (getStartIconPath() != null) {

            Element x = null;
            x = doc.createElementNS("http://www.w3.org/2000/svg", "g");
            x.setAttributeNS(null, "id", id);
            //Rectangle/Image holder to place the image
            Element rect = doc.createElementNS("http://www.w3.org/2000/svg", "rect");
            //Attributes of the rectangle drawn
            rect.setAttributeNS(null, "x", String.valueOf(imgXLeft));
            rect.setAttributeNS(null, "y", String.valueOf(imgYTop));
            rect.setAttributeNS(null, "width", String.valueOf(imgWidth));
            rect.setAttributeNS(null, "height", String.valueOf(imgHeight));
            rect.setAttributeNS(null, "id", id);
            rect.setAttributeNS(null, "rx", "10");
            rect.setAttributeNS(null, "ry", "10");
            rect.setAttributeNS(null, "style", "fill:white;stroke:black;stroke-width:1.5;fill-opacity:0.1");

            //Image/Icon of the activity
            int embedImageX = imgXLeft + 25;
            int embedImageY = (imgYTop + (5 / 2));
            int embedImageHeight = 45;
            int embedImageWidth = 50;
            //Attributes of the image embedded inside the rectangle
            Element embedImage = doc.createElementNS("http://www.w3.org/2000/svg", "image");
            embedImage.setAttributeNS(null, "xlink:href", imgPath);
            embedImage.setAttributeNS(null, "x", String.valueOf(embedImageX));
            embedImage.setAttributeNS(null, "y", String.valueOf(embedImageY));
            embedImage.setAttributeNS(null, "width", String.valueOf(embedImageWidth));
            embedImage.setAttributeNS(null, "height", String.valueOf(embedImageHeight));
            //Embed the rectangle/image holder into the container
            x.appendChild(rect);
            //Embed the image into the container
            x.appendChild(embedImage);

            return x;
        }
        return group;
    }

    /**
     * Image Definitions or attributes for the activity icons
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the end icon of the activity
     */
    protected Element getImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getStartIconPath(), getStartIconXLeft(),
                getStartIconYTop(), getStartIconWidth(), getStartIconHeight(),
                getStartImageId());
    }
    /**
     * Image Definitions or attributes for the end activity icon
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the end icon of the  activity
     */
    protected Element getEndImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getEndIconPath(), getEndIconXLeft(),
                getEndIconYTop(), getEndIconWidth(), getEndIconHeight(),
                getEndImageId());
    }
    /**
     * Image Definitions or attributes for the start activity icon
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the end icon of the  activity
     */
    protected Element getStartImageDefinition(SVGDocument doc) {
        return getImageDefinition(doc, getStartIconPath(), getStartIconXLeft(),
                getStartIconYTop(), getStartIconWidth(), getStartIconHeight(),
                getStartImageId());
    }
    /**
     * Get the image/icon text i.e. the name of the activity to be displayed
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the  image/icon text of the activity
     */
    protected Element getImageText(SVGDocument doc, int imgXLeft, int imgYTop, int imgWidth,
                                   int imgHeight, String imgName, String imgDisplayName) {
        int txtXLeft = imgXLeft;
        int txtYTop = imgYTop;
        // SVG <a> element is used to create links in SVG images
        Element a = doc.createElementNS("http://www.w3.org/2000/svg", "a");
        if (imgDisplayName != null) {
            //Set the image/activity name
            a.setAttributeNS(null, "id", imgName);
            //Attributes of the <text> which is used to define a text
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
            //Creating an SVG <tspan> element which is used to draw multiple lines of text in SVG
            Element tspan = doc
                    .createElementNS("http://www.w3.org/2000/svg", "tspan");
            //Attributes of the tspan element i.e. xLeft and yTop position and the name of the activity
            tspan.setAttributeNS(null, "x", String.valueOf(txtXLeft + 5));
            tspan.setAttributeNS(null, "y", String.valueOf(txtYTop + 5));
            tspan.setAttributeNS(null, "id", "tspan-" + imgName);
            //Creating a Text object and creating a text node/element with the display name of the activity
            Text text2 = doc.createTextNode(imgDisplayName);
            //Embed the text object containing the activity name in tspan
            tspan.appendChild(text2);
            //Embed the tspan with the image text in <text> element which contains the text styling attributes
            text1.appendChild(tspan);
            //Embed the <text> element as a link with <a> element
            a.appendChild(text1);
        }
        return a;
    }

    /**
     * Defines the start icon/image text
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the start image/icon text of the  activity
     */
    protected Element getStartImageText(SVGDocument doc) {
        return getImageText(doc, getStartIconTextXLeft(),
                getStartIconTextYTop(), getStartIconWidth(),
                getStartIconHeight(), getStartImageTextId(), getDisplayName());
    }

    /**
     * Defines the end icon/image text
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the end image/icon text of the  activity
     */
    protected void getEndImageText(SVGDocument doc) {
        getImageText(doc, getEndIconTextXLeft(), getEndIconTextYTop(),
                getStartIconWidth(), getStartIconHeight(), getEndImageTextId(),
                getDisplayName());
    }

    /**
     * Gets the boolean value of largeArrow(Arrow Styles)
     * @return largeArrow --> true/false
     */
    protected boolean isLargeArrow() {
        return largeArrow;
    }

    /**
     * Sets the boolean value for the largeArrow(Arrow Styles)
     * @param largeArrow
     */
    protected void setLargeArrow(boolean largeArrow) {
        this.largeArrow = largeArrow;
    }

    protected boolean largeArrow = false;

    /**
     * Gets the arrow flow styles
     * @return String with the arrow styling attributes
     */
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
        //Checks whether the arrow needed is a largeArrow
        if (largeArrow) {
            return largeArrowStr;
        } else {
            return mediumArrowStr;
        }
    }
    /**
     * Gets the link arrow styles i.e. link joining the source and the target
     * @return String with the link arrow styling attributes
     */
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
        //Checks whether the link arrow needed is a largeArrow
        if (largeArrow) {
            return largeArrowStr;
        } else {
            return mediumArrowStr;
        }
    }

    /**
     * Get the arrow flows/paths from the coordinates given by getArrows()
     * @param doc     SVG document which defines the components including shapes, gradients etc. of the activity
     * @param startX  x-coordinate of the start point
     * @param startY  y-coordinate of the start point
     * @param endX    x-coordinate of the end point
     * @param endY    y-coordinate of the end point
     * @param id      previous activity id + current activity id
     * @return An element which contains the arrow flows/paths of the activity
     */
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");
        /*Arrows are created using  <path> : An element in svg used to create smooth, flowing lines using relatively few
          control points.
          A path element is defined by attribute: d. This attribute contains a series of commands for path data :
          M = move to
          L = line to
          Arrow flows will be generated according to the coordinates given
         */
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
        //Set the id of the path
        path.setAttributeNS(null, "id", id);
        //Add styles to the arrows
        path.setAttributeNS(null, "style", getArrowStyle());

        return path;
    }
    /**
     * Get the arrow flows/paths from the coordinates given by getArrows()
     * @param doc     SVG document which defines the components including shapes, gradients etc. of the activity
     * @param startX  x-coordinate of the start point
     * @param startY  y-coordinate of the start point
     * @param midX    x-coordinate of the mid point
     * @param midY    y-coordinate of the mid point
     * @param endX    x-coordinate of the end point
     * @param endY    y-coordinate of the end point
     * @param id      previous activity id + current activity id
     * @return An element which contains the arrow flows/paths of the activity
     */
    protected Element getArrowDefinition(SVGDocument doc, int startX,
                                         int startY, int midX, int midY, int endX, int endY, String id) {
        Element path = doc
                .createElementNS("http://www.w3.org/2000/svg", "path");
        path.setAttributeNS(null, "d",
                "M " + startX + "," + startY + " L " + midX + "," + midY + "L "
                        + endX +
                        "," + endY);
        //Set the id of the path
        path.setAttributeNS(null, "id", id);
        //Add styles to the arrows
        path.setAttributeNS(null, "style", getArrowStyle());

        return path;
    }
    /**
     * Defines the box i.e . the scope of a composite activity represented as a box
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the attributes i.e. x and y position &
     *         width and height of the box
     */
    protected Element getBoxDefinition(SVGDocument doc) {
        return getBoxDefinition(doc, getDimensions().getXLeft() + BOX_MARGIN,
                getDimensions().getYTop() + BOX_MARGIN,
                getDimensions().getWidth() - (BOX_MARGIN * 2),
                getDimensions().getHeight() - (BOX_MARGIN * 2), getBoxId());
    }

    /**
     *Defines the box i.e . the scope of a composite activity represented as a box
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @param boxXLeft x-coordinate of the box
     * @param boxYTop  y-coordinate of the box
     * @param boxWidth width of the box
     * @param boxHeight height of the box
     * @param id id of the box/activity
     * @return
     */
    protected Element getBoxDefinition(SVGDocument doc, int boxXLeft, int boxYTop, int boxWidth, int boxHeight, String id) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Set the id of the box
        group.setAttributeNS(null, "id", "Layer-" + id);
        //Check whether Sequence boxes can be shown/ is true
        if (layoutManager.isShowSequenceBoxes()) {
            //Rectangle/Box to hold the subActivities inside the Sequence
            Element rect = doc.createElementNS("http://www.w3.org/2000/svg", "rect");
            //Attributes of the box is defined
            rect.setAttributeNS(null, "width", String.valueOf(boxWidth));
            rect.setAttributeNS(null, "height", String.valueOf(boxHeight));
            rect.setAttributeNS(null, "x", String.valueOf(boxXLeft));
            rect.setAttributeNS(null, "y", String.valueOf(boxYTop));
            rect.setAttributeNS(null, "id", "Rect" + id);
            rect.setAttributeNS(null, "rx", "10");
            rect.setAttributeNS(null, "ry", "10");
            rect.setAttributeNS(null, "style", boxStyle);
            //Embed the box to the container
            group.appendChild(rect);
        }
        return group;
    }
    /**
     * Get the dimensions of the SVG
     * @return dimensions of the SVG i.e. height and width of the SVG
     */
    public SVGDimension getDimensions() {
        SVGDimension obj = new SVGDimension();
        obj.setHeight(layoutManager.getSvgHeight());
        obj.setWidth(layoutManager.getSvgWidth());
        return obj;
    }
    /**
     * At start: width=0, height=0
     * Switch the dimensions of the activity to horizontal
     */
    public void switchDimensionsToHorizontal() {
        int width = 0;
        int height = 0;

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Iterates through all the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Switch the dimension of each subActivity to the horizontal
            activity.switchDimensionsToHorizontal();
        }
        //Get the width and the height
        width = getDimensions().getWidth();
        height = getDimensions().getHeight();
        // Set the dimensions by switching the width and the height
        getDimensions().setHeight(width);
        getDimensions().setWidth(height);
    }
    /**
     * Sets the layout of the activity
     * @param startXLeft x-coordinate of the activity
     * @param startYTop  y-coordinate of the activity
     */
    public void layout(int startXLeft, int startYTop) {
        if (layoutManager.isVerticalLayout()) {
            layoutVertical(startXLeft, startYTop);
        } else {
            layoutHorizontal(startXLeft, startYTop);
        }
    }
    /**
     * Sets the x and y positions of the activities
     * At the start: startXLeft=0, startYTop=0
     * centreOfMyLayout- center of the the SVG
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     *
     */
    public void layoutVertical(int startXLeft, int startYTop) {
        //Get the dimensions of the SVG i.e. width and the height
        dimensions = getDimensions();
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startXLeft + (dimensions.getWidth() / 2);
        //Positioning the startIcon
        int xLeft = centreOfMyLayout - (getStartIconWidth() / 2);
        int yTop = startYTop + (getYSpacing() / 2);
        //Positioning the endIcon
        int endXLeft = centreOfMyLayout - (getEndIconWidth() / 2);
        int endYTop =
                startYTop + dimensions.getHeight() - getEndIconHeight() - (
                        getYSpacing() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childYTop = yTop + getStartIconHeight() + (getYSpacing() / 2);
        int childXLeft = startXLeft + (getXSpacing() / 2);
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Sets the xLeft and yTop position of the iterated activity
            activity.layout(childXLeft, childYTop);
            childYTop += activity.getDimensions().getHeight();
        }
        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        //Sets the xLeft and yTop positions of the end icon
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        //Sets the xLeft and yTop positions of the start icon text
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(
                startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        //Sets the xLeft and yTop positions of the SVG  of the composite activity after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }
    /**
     * Sets the x and y positions of the activities
     * At the start: startXLeft=0, startYTop=0
     * @param startXLeft x-coordinate
     * @param startYTop  y-coordinate
     * centreOfMyLayout- center of the the SVG
     */
    private void layoutHorizontal(int startXLeft, int startYTop) {
        //Aligns the activities to the center of the layout
        int centreOfMyLayout = startYTop + (dimensions.getHeight() / 2);
        //Positioning the startIcon
        int xLeft = startXLeft + (getYSpacing() / 2);
        int yTop = centreOfMyLayout - (getStartIconHeight() / 2);
        //Positioning the endIcon
        int endXLeft =
                startXLeft + dimensions.getWidth() - getEndIconWidth() - (
                        getYSpacing() / 2);
        int endYTop = centreOfMyLayout - (getEndIconHeight() / 2);

        ActivityInterface activity = null;
        Iterator<ActivityInterface> itr = getSubActivities().iterator();
        //Adjusting the childXLeft and childYTop positions
        int childXLeft = xLeft + getStartIconWidth() + (getYSpacing() / 2);
        int childYTop = startYTop + (getXSpacing() / 2);
        //Iterates through the subActivities
        while (itr.hasNext()) {
            activity = itr.next();
            //Sets the xLeft and yTop position of the iterated activity
            activity.layout(childXLeft, childYTop);
            childXLeft += activity.getDimensions().getWidth();
        }
        //Sets the xLeft and yTop positions of the start icon
        setStartIconXLeft(xLeft);
        setStartIconYTop(yTop);
        //Sets the xLeft and yTop positions of the end icon
        setEndIconXLeft(endXLeft);
        setEndIconYTop(endYTop);
        //Sets the xLeft and yTop positions of the start icon text
        setStartIconTextXLeft(startXLeft + BOX_MARGIN);
        setStartIconTextYTop(
                startYTop + BOX_MARGIN + BPEL2SVGFactory.TEXT_ADJUST);
        //Sets the xLeft and yTop positions of the SVG  of the composite activity after setting the dimensions
        getDimensions().setXLeft(startXLeft);
        getDimensions().setYTop(startYTop);
    }
    /**
     *
     * @return String with the end tag of activity
     */
    public String getEndTag() {
        return BPEL2SVGFactory.CATCH_END_TAG;
    }

    /**
     *
     * @return name of the activity
     */
    @Override
    public String toString() {
        return getId();
    }
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which enters an activity
     * @return coordinates/entry point of the entry arrow for the activities
     * After Calculations(Vertical Layout): xLeft=Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     */
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
        //Returns the calculated coordinate points of the entry arrow
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which leaves an activity
     * @return coordinates/exit point of the exit arrow for the activities
     */
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
        //Returns the calculated coordinate points of the exit arrow
        SVGCoordinates coords = new SVGCoordinates(xLeft, yTop);

        return coords;
    }


    public void passContent() {
        root = doc.getDocumentElement();
        generator.getRoot(root);
    }


    /**
     * Returns a list of activities which are only SOURCE activities and not TARGET activities
     * @return list of the sources
     */
    public Set<ActivityInterface> getLinkRoots() {
        //Removes all the target activities inside the sources list
        sources.removeAll(targets);
        return sources;
    }


    /**Get the subactivites in the bpel process
     * Processes the subActivities each one separately, if the activity name matches any of the element tags
     * then the constructor of that activity implementation is invoked
     * @param omElement process definition of the bpel process
     * @return activity
     */
    public ActivityInterface processSubActivities(OMElement omElement) {
        ActivityInterface endActivity = null;
        //Checks whether omElement contains a value
        if (omElement != null) {
            ActivityInterface activity = null;
            Iterator iterator = omElement.getChildElements();
            //Iterates through the subActivities
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
                    activity = new SourceImpl(tmpElement, this);//source
                    if (activity.getAttributes().get(0).getAttribute()
                            .equals("linkName")) {
                        if (links.containsKey(activity.getAttributes().get(0)
                                .getValue())) {
                            //if a entry for the particular link name already exists
                            links.get(
                                    activity.getAttributes().get(0).getValue())
                                    .setSource(this.parent);
                        } else {
                            //if the link name doesnot exist i.e. if the link is a new link
                            //Create a new Link object
                            Link link = new Link();
                            //Set the source(Start activity) as the parent activity
                            link.setSource(this.parent);
                            links.put(
                                    activity.getAttributes().get(0).getValue(),
                                    link);
                        }
                        //Add the parent activity of the activity to the source-list
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
                            //if a entry for the particular link name already exists
                            links.get(
                                    activity.getAttributes().get(0).getValue())
                                    .setTarget(this.parent);
                        } else {
                            //if the link name doesnot exist i.e. if the link is a new link
                            //Create a new Link object
                            Link link = new Link();
                            //Set the target(End activity) as the parent activity
                            link.setTarget(this.parent);
                            links.put(
                                    activity.getAttributes().get(0).getValue(),
                                    link);
                        }
                        //Add the parent activity of the activity to the target-list
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
                //Set the link properties i.e. the link name, source of the link and the target of the link
                activity.setLinkProperties(links, sources, targets);
                //Add the activity to the subActivities list
                subActivities.add(activity);
                //Checks if the activity has any child activities
                if (tmpElement.getChildElements().hasNext()) {
                    //The child activities are processed again by invoking the same method recursively
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

    /**
     * Gets the root i.e. documentElement from SVGDocument
     * @return Element(represents an element in a XML/HTML document) which contains the components of the activity
     */
    public Element getRoot() {
        return root;
    }

    /**
     * Gets the information of each activity i.e. the activity type and name from the list
     * @return String with the type and the name of the activity stored as key-value pairs
     */
    public String getActivityInfoString() {
        String infoString = null;
        //Iterates through the list to get attributes stored as key-value pairs
        for (BPELAttributeValuePair x : attributes) {
            //Get the activity type
            String attrib = x.getAttribute();
            //Get the name of the activity
            String val = x.getValue();
            //Make the activity infoString by combining the activity type and name
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

    /**
     * Gets the link name and the Link object which contains the source and the target
     * @return Map with the link name and the Link object which contains the source and the target
     */
    public Map<String, Link> getLinks() {
        return links;
    }

    /**
     * Set the link properties i.e. the link name, source activity and the target activity
     * @param links contains the link name  and a link object which contains the source and the target of the link specified
     * @param sources source activities(Starting point/activity of a link)
     * @param targets target activities(Ending point/activity of a link)
     */
    public void setLinkProperties(Map<String, Link> links,
                                  Set<ActivityInterface> sources, Set<ActivityInterface> targets) {
        this.links = links;
        this.sources = sources;
        this.targets = targets;
    }
}

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

package org.wso2.carbon.bpel.ui.bpel2svg;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.apache.axiom.om.OMElement;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Activity tag UI impl
 */
public interface ActivityInterface {

    /**Get the subactivites in the bpel process
     * Processes the subActivities each one separately, if the activity name matches any of the element tags
     * then the constructor of that activity implementation is invoked
     * @param om process definition of the bpel process
     * @return activity
     */
    public ActivityInterface processSubActivities(OMElement om);
    /**
     * Sets the layout of the activity
     * @param startXLeft x-coordinate of the activity
     * @param startYTop  y-coordinate of the activity
     */
    public void layout(int startXLeft, int startYTop);
    /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the  activity
     */
    public Element getSVGString(SVGDocument doc);
    /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the subActivities
     * @return Element(represents an element in a XML/HTML document) which contains the components of the subActivities
     */
    public Element getSubActivitiesSVGString(SVGDocument doc);
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which enters an activity
     * @return coordinates/entry point of the entry arrow for the activities
     * After Calculations(Vertical Layout): xLeft=Xleft of Icon + (width of icon)/2 , yTop= Ytop of the Icon
     */
    public SVGCoordinates getEntryArrowCoords();
    /**
     * At the start: xLeft=0, yTop=0
     * Calculates the coordinates of the arrow which leaves an activity
     * @return coordinates/exit point of the exit arrow for the activities
     */
    public SVGCoordinates getExitArrowCoords();
    /**
     * Gets the list of subActivities of a process
     * @return list of subActivities of a process
     */
    public List<ActivityInterface> getSubActivities();
    /**
     * Get the dimensions of the SVG
     * @return dimensions of the SVG i.e. height and width of the SVG
     */
    public SVGDimension getDimensions();
    /**
     * At start: width=0, height=0
     * Switch the dimensions of the activity to horizontal
     */
    public void switchDimensionsToHorizontal();
    /**
     *
     * @return name of the activity
     */
    public String getId();
    /**
     * Gets the name of the activity to be displayed
     * @return String with the name of the activity
     */
    public String getName();
    /**
     * Sets the activity name
     * @param name name of the activity to be displayed
     */
    public void setName(String name);
    /**
     * Get the name of the activity to be displayed
     * @return name of the activity
     */
    public String getDisplayName();
    /**
     * Set the name of the activity to be displayed
     * @param displayName
     */
    public void setDisplayName(String displayName);

    // Start Icon Methods

    /**
     * Gets the xLeft position of the start icon of the activity
     * @return xLeft position of the start icon of the activity
     */
    public int getStartIconXLeft();
    /**
     * Sets the xLeft position of the start icon of the activity
     * @param xLeft xLeft position of the start icon of the activity
     */
    public void setStartIconXLeft(int xLeft);
    /**
     * Gets the yTop position of the start icon of the activity
     * @return yTop position of the start icon of the activity
     */
    public int getStartIconYTop();
    /**
     * Sets the yTop position of the start icon of the activity
     * @param yTop yTop position of the start icon of the activity
     */
    public void setStartIconYTop(int yTop);
    /**
     * Gets the width of the start icon of the activity
     * @return width of the start icon of the activity
     */
    public int getStartIconWidth();
    /**
     * Gets the height of the start icon of the activity
     * @return height of the start icon of the activity
     */
    public int getStartIconHeight();
    /**
     * Sets the height of the start icon of the activity
     * @param iconHeight height of the start icon of the activity
     */
    public void setStartIconHeight(int iconHeight);
    /**
     * Sets the width of the start icon of the activity
     * @param iconWidth width of the start icon of the activity
     */
    public void setStartIconWidth(int iconWidth);
    /**
     * Gets the icon path of the start icon of the activity
     * @return String with the icon path of the start icon of the activity
     */
    public String getStartIconPath();
    /**
     * Sets the icon path of the start icon of the activity
     * @param iconPath icon path of the start icon of the activity
     */
    public void setStartIconPath(String iconPath);

    // End Icon methods

    /**
     * Gets the xLeft position of the end icon of the activity
     * @return xLeft position of the end icon of the activity
     */
    public int getEndIconXLeft();
    /**
     * Sets the xLeft position of the end icon of the activity
     * @param xLeft xLeft position of the end icon of the activity
     */
    public void setEndIconXLeft(int xLeft);
    /**
     * Gets the yTop position of the end icon of the activity
     * @return yTop position of the end icon of the activity
     */
    public int getEndIconYTop();
    /**
     * Sets the yTop position of the end icon of the activity
     * @param yTop yTop position of the end icon of the activity
     */
    public void setEndIconYTop(int yTop);
    /**
     * Gets the width of the end icon of the activity
     * @return width of the end icon of the activity
     */
    public int getEndIconWidth();
    /**
     * Gets the height of the end icon of the activity
     * @return height of the end icon of the activity
     */
    public int getEndIconHeight();
    /**
     * Gets the icon path of the end icon of the activity
     * @return String with the icon path of the end icon of the activity
     */
    public String getEndIconPath();
    /**
     * Gets the boolean value to include the assign activities
     * @return boolean value to include the assign activities->true/false
     */
    public boolean isIncludeAssigns();
    /**
     * Gets true/false for the vertical layout of the child activities
     * @return true/false
     */
    public boolean isVerticalChildLayout();
    /**
     * Sets true/false for the vertical layout of the child activities
     * @param verticalChildLayout true/false
     */
    public void setVerticalChildLayout(boolean verticalChildLayout);
    /**
     * Gets true/false for the horizontal layout of the child subActivities
     * @return true/false
     */
    public boolean isHorizontalChildLayout();
    /**
     *
     * @return String with the end tag of activity
     */
    public String getEndTag();
    /**
     * Gets the root i.e. documentElement from SVGDocument
     * @return Element(represents an element in a XML/HTML document) which contains the components of the activity
     */
    public Element getRoot();
    /**
     * Gets the information of each activity i.e. the activity type and name from the list
     * @return String with the type and the name of the activity stored as key-value pairs
     */
    public String getActivityInfoString();
    /**
     * Gets the attributes of the activities
     * @return list with the attributes of the activities
     */
    public List<BPELAttributeValuePair> getAttributes();

    /**
     * Returns a list of activities which are only SOURCE activities and not TARGET activities
     * @return list of the sources
     */
    public Set<ActivityInterface> getLinkRoots();

    /**
     * Gets the parent activity of any given activity
     * @return parent
     */
    public ActivityInterface getParent();
    /**
     * Gets the value of correctionY i.e. corrected value of the yTop position
     * @return correctionY
     */
    public int getCorrectionY();
    /**
     * Sets the value of correctionY i.e. corrected value of the yTop position
     * @param correctionY
     */
    public void setCorrectionY(int correctionY);
    /**
     * Set the link properties i.e. the link name, source activity and the target activity
     * @param links contains the link name  and a link object which contains the source and the target of the link specified
     * @param sources source activities(Starting point/activity of a link)
     * @param targets target activities(Ending point/activity of a link)
     */
    public void setLinkProperties(Map<String, Link> links, Set<ActivityInterface> sources,
                                  Set<ActivityInterface> targets);
    /**
     * Gets the link name and the Link object which contains the source and the target
     * @return Map with the link name and the Link object which contains the source and the target
     */
    public Map<String, Link> getLinks();
}

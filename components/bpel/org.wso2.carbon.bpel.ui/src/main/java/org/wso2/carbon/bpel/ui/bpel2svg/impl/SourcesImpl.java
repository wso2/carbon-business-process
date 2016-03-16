/**
 *  Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.wso2.carbon.bpel.ui.bpel2svg.*;
import org.apache.axiom.om.OMElement;

/**
 * Sources tag UI implementation
 */
public class SourcesImpl extends ActivityImpl implements SourcesInterface {

    //Making the height and the width of the start and end icons to zero
    protected int startIconHeight = 0;
    protected int endIconHeight = 0;
    protected int startIconWidth = 0;
    protected int endIconWidth = 0;

    //Getters and Setters of height and width of the Start and End Icons
    /**
     * Sets the height of the end icon of the activity
     * @param iconHeightEnd height of the end icon of the activity
     */
    public void setEndIconHeight(int iconHeightEnd) {
        this.endIconHeight = iconHeightEnd;
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
     * Sets the width of the end icon of the activity
     * @param iconWidthEnd width of the end icon of the activity
     */
    public void setEndIconWidth(int iconWidthEnd) {
        this.endIconWidth = iconWidthEnd;
    }
    /**
     * Gets the height of the end icon of the activity
     * @return height of the end icon of the activity
     */
    public int getEndIconHeight() {
        return endIconHeight;
    }
    /**
     * Gets the height of the start icon of the activity
     * @return height of the start icon of the activity
     */
    public int getStartIconHeight() {
        return startIconHeight;
    }

    /**
     * Gets the width of the start icon of the activity
     * @return width of the start icon of the activity
     */
    public int getStartIconWidth() {
        return startIconWidth;
    }

    /**
     * Gets the width of the end icon of the activity
     * @return width of the end icon of the activity
     */
    public int getEndIconWidth() {
        return endIconWidth;
    }

 	/**
     * Initializes a new instance of the SourcesImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an Sources Activity when processing the subActivities
     * of the process
     * @param omElement which matches the Sources tag
     * @param parent
     */
    public SourcesImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

		//Set the parent of the activity
        setParent(parent);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

	 /**
     * Initializes a new instance of the SourcesImpl class using the specified omElement
     * @param omElement which matches the Sources tag
     */
    public SourcesImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }
    /**
     *
     * @return String with the end tag of Sources Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.SOURCES_END_TAG;
    }

    /**
     *
     * @return String with the name of the activity
     */
    public String getId() {
        return getName(); // + "-Sequence";
    }


    //Different Implementations for start and end scope icons
	/**
     * Gets the End Image Definition
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Sources activity
     */
    @Override
    protected Element getEndImageDefinition(SVGDocument doc) {
        return getStartEndImageDef(doc, getEndIconPath(), getEndIconXLeft(),
                getEndIconYTop(), getEndIconWidth(), getEndIconHeight(),
                getEndImageId());
    }
    /**
     * Gets the Start Image Definition
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Sources activity
     */
    protected Element getStartImageDefinition(SVGDocument doc) {
        return getStartEndImageDef(doc, getStartIconPath(), getStartIconXLeft(),
                getStartIconYTop(), getStartIconWidth(), getStartIconHeight(),
                getStartImageId());
    }

    /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @param imgPath = null
     * @param imgXLeft = null
     * @param imgYTop = null
     * @param imgWidth = null
     * @param imgHeight = null
     * @param id = null
     * @return Empty element which contains the components of the Sources activity
     *         In this case the Sources activity doesn't contain any components
     */
    protected Element getStartEndImageDef(SVGDocument doc, String imgPath, int imgXLeft, int imgYTop,
                                          int imgWidth, int imgHeight, String id) {

        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        return group;
    }

    /**
     * Get the arrow coordinates of the activities
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Empty element which contains the components of the Sources activity.
     *         In this case the Sources activity doesn't contain any components, so no arrow coordinates
     */
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        return subGroup;
    }

    /**
     * Get the arrow definitions/paths from the coordinates
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @param startX = null
     * @param startY = null
     * @param endX = null
     * @param endY = null
     * @param id = null
     * @return Empty element which contains the components of the Sources activity.
     *         In this case the Sources activity doesn't contain any components, so no arrow definitions/paths from activities
     */
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");
        return path;
    }

    /**
     * SVGDimensions (width and height) are set to zero as no icon is specified
     * @return dimensions of the SVG i.e. width and the height
     */
    public SVGDimension getDimensions() {
        SVGDimension obj = new SVGDimension();
        obj.setHeight(0);
        obj.setWidth(0);
        return obj;
    }

    /**
     *
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Sources activity
     */
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        //Get the id of the activity
        group.setAttributeNS(null, "id", getLayerId());
        group.appendChild(getBoxDefinition(doc));
        //Get the icon image definitions
        group.appendChild(getImageDefinition(doc));
        //Gets the start icon text
        group.appendChild(getStartImageText(doc));
        // Process Sub Activities
        group.appendChild(getSubActivitiesSVGString(doc));
        //Gets the end icon image definition
        group.appendChild(getEndImageDefinition(doc));
        //Get the arrow flows of the Sources activity
        group.appendChild(getArrows(doc));

        return group;
    }

}

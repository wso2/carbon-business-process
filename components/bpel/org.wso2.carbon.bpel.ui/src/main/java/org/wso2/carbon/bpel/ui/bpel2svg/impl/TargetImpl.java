/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension;
import org.wso2.carbon.bpel.ui.bpel2svg.TargetInterface;

/**
 * Target tag UI implementation
 */
public class TargetImpl extends TargetsImpl implements TargetInterface {


    //Getters and Setters of height and width of the Start and End Icons

    /**
     * Initializes a new instance of the TargetImpl class using the specified omElement
     * @param omElement which matches the Target tag
     */
    public TargetImpl(OMElement omElement) {
        super(omElement);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        //Making the height and the width of the start and end icons to zero
        startIconHeight = 0;
        endIconHeight = 0;
        startIconWidth = 0;
        endIconWidth = 0;
    }

    /**
     * Initializes a new instance of the TargetImpl class using the specified omElement
     * Constructor that is invoked when the omElement type matches an Target Activity when processing the subActivities
     * of the process
     * @param omElement which matches the Target tag
     * @param parent
     */
    public TargetImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);

        //Set the parent of the activity
        setParent(parent);

        // Set Start and End Icons and their Sizes
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
        //Making the height and the width of the start and end icons to zero
        startIconHeight = 0;
        endIconHeight = 0;
        startIconWidth = 0;
        endIconWidth = 0;
    }

    /**
     * Gets the height of the end icon of the activity
     * @return height of the end icon of the activity
     */
    public int getEndIconHeight() {
        return endIconHeight;
    }

    /**
     * Sets the height of the end icon of the activity
     * @param iconHeightEnd height of the end icon of the activity
     */
    public void setEndIconHeight(int iconHeightEnd) {
        this.endIconHeight = iconHeightEnd;
    }

    /**
     * Gets the height of the start icon of the activity
     * @return height of the start icon of the activity
     */
    public int getStartIconHeight() {
        return startIconHeight;
    }

    /**
     * Sets the height of the start icon of the activity
     * @param iconHeight height of the start icon of the activity
     */
    public void setStartIconHeight(int iconHeight) {
        this.startIconHeight = iconHeight;
    }

    /**
     * Gets the width of the start icon of the activity
     * @return width of the start icon of the activity
     */
    public int getStartIconWidth() {
        return startIconWidth;
    }

    /**
     * Sets the width of the start icon of the activity
     * @param iconWidth width of the start icon of the activity
     */
    public void setStartIconWidth(int iconWidth) {
        this.startIconWidth = iconWidth;
    }

    /**
     * Gets the width of the end icon of the activity
     * @return width of the end icon of the activity
     */
    public int getEndIconWidth() {
        return endIconWidth;
    }

    /**
     * Sets the width of the end icon of the activity
     * @param iconWidthEnd width of the end icon of the activity
     */
    public void setEndIconWidth(int iconWidthEnd) {
        this.endIconWidth = iconWidthEnd;
    }

    /**
     *
     * @return String with the end tag of Target Activity
     */
    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.TARGET_END_TAG;
    }

    //Different Implementations for start and end scope icons as no icon is specified

    /**
     * Gets the End Image Definition
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Target
     * activity
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
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Target
     * activity
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
     * @return Empty element which contains the components of the Target activity
     *         In this case the Target activity doesn't contain any components
     */
    protected Element getStartEndImageDef(SVGDocument doc, String imgPath, int imgXLeft, int imgYTop,
                                          int imgWidth, int imgHeight, String id) {

        Element group = null;
        group = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "g");
        return group;
    }

    /**
     * Get the arrow coordinates of the activities
     * @param doc SVG document which defines the components including shapes, gradients etc. of the activity
     * @return Empty element which contains the components of the Target activity.
     *         In this case the Target activity doesn't contain any components, so no arrow coordinates
     */
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "g");
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
     * @return Empty element which contains the components of the Target activity.
     *         In this case the Target activity doesn't contain any components, so no arrow definitions/paths from
     *         activities
     */
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {
        Element path = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "path");
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
     * @return Element(represents an element in a XML/HTML document) which contains the components of the Target
     * activity
     */
    public Element getSVGString(SVGDocument doc) {
        Element group = null;
        group = doc.createElementNS(SVGNamespace.SVG_NAMESPACE, "g");
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
        //Get the arrow flows of the Source activity
        group.appendChild(getArrows(doc));

        return group;
    }

}

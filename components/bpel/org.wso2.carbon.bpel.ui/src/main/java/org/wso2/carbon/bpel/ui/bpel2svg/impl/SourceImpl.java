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
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGDimension;
import org.wso2.carbon.bpel.ui.bpel2svg.SourceInterface;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bpel.ui.bpel2svg.BPEL2SVGFactory;

/**
 * Source tag UI impl
 */
public class SourceImpl extends SourcesImpl implements SourceInterface {

    //Making the height and the width of the start and end icons to zero
    protected int startIconHeight = 0;
    protected int endIconHeight = 0;
    protected int startIconWidth = 0;
    protected int endIconWidth = 0;

    public void setEndIconHeight(int iconHeightEnd) {
        this.endIconHeight = iconHeightEnd;
    }

    public void setStartIconHeight(int iconHeight) {
        this.startIconHeight = iconHeight;
    }

    public void setStartIconWidth(int iconWidth) {
        this.startIconWidth = iconWidth;
    }

    public void setEndIconWidth(int iconWidthEnd) {
        this.endIconWidth = iconWidthEnd;
    }

    public int getEndIconHeight() {
        return endIconHeight;
    }

    public int getStartIconHeight() {
        return startIconHeight;
    }

    public int getStartIconWidth() {
        return startIconWidth;
    }

    public int getEndIconWidth() {
        return endIconWidth;
    }

    public SourceImpl(OMElement omElement) {
        super(omElement);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());

    }

    public SourceImpl(OMElement omElement, ActivityInterface parent) {
        super(omElement);
        setParent(parent);

        // Set Icon and Size
        startIconPath = BPEL2SVGFactory.getInstance().getIconPath(this.getClass().getName());
        endIconPath = BPEL2SVGFactory.getInstance().getEndIconPath(this.getClass().getName());
    }

    @Override
    public String getEndTag() {
        return BPEL2SVGFactory.SOURCE_END_TAG;
    }

    //Different Implementations for start and end scope icons
    @Override
    protected Element getEndImageDefinition(SVGDocument doc) {
        return getStartEndImageDef(doc, getEndIconPath(), getEndIconXLeft(),
                getEndIconYTop(), getEndIconWidth(), getEndIconHeight(),
                getEndImageId());
    }

    protected Element getStartImageDefinition(SVGDocument doc) {
        return getStartEndImageDef(doc, getStartIconPath(), getStartIconXLeft(),
                getStartIconYTop(), getStartIconWidth(), getStartIconHeight(),
                getStartImageId());
    }

    protected Element getStartEndImageDef(SVGDocument doc, String imgPath, int imgXLeft, int imgYTop,
                                          int imgWidth, int imgHeight, String id) {

        Element group = null;
        group = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        return group;
    }

    //Get the arrow coordinates of the activities
    protected Element getArrows(SVGDocument doc) {
        Element subGroup = null;
        subGroup = doc.createElementNS("http://www.w3.org/2000/svg", "g");
        return subGroup;
    }

    //Get the arrow definitions/paths from the coordinates
    protected Element getArrowDefinition(SVGDocument doc, int startX, int startY, int endX, int endY, String id) {
        Element path = doc.createElementNS("http://www.w3.org/2000/svg", "path");
        return path;
    }

    public SVGDimension getDimensions() {
        SVGDimension obj = new SVGDimension();
        obj.setHeight(0);
        obj.setWidth(0);
        return obj;
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
}

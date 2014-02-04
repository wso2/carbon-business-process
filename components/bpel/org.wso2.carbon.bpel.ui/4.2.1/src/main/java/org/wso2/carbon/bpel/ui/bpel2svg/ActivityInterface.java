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
    //public ActivityInterface processSubActivities(StringTokenizer bpelST);
    public ActivityInterface processSubActivities(OMElement om);

    public void layout(int startXLeft, int startYTop);

    // public String getSVGString();
    public Element getSVGString(SVGDocument doc);

    //public String getSubActivitiesSVGString();
    public Element getSubActivitiesSVGString(SVGDocument doc);

    public SVGCoordinates getEntryArrowCoords();

    public SVGCoordinates getExitArrowCoords();

    public List<ActivityInterface> getSubActivities();

    public SVGDimension getDimensions() ;

    public void switchDimensionsToHorizontal();

    public String getId();

    public String getName();

    public void setName(String name);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    // Start Icon Methods
    public int getStartIconXLeft();

    public void setStartIconXLeft(int xLeft);

    public int getStartIconYTop();

    public void setStartIconYTop(int yTop);

    public int getStartIconWidth();

    public int getStartIconHeight();

    public void setStartIconHeight(int iconHeight);

    public void setStartIconWidth(int iconWidth);

    public String getStartIconPath();

    public void setStartIconPath(String iconPath);

    // End Icon methods
    public int getEndIconXLeft();

    public void setEndIconXLeft(int xLeft);

    public int getEndIconYTop();

    public void setEndIconYTop(int yTop);

    public int getEndIconWidth();

    public int getEndIconHeight();

    public String getEndIconPath();

    public boolean isIncludeAssigns();

    public boolean isVerticalChildLayout();

    public void setVerticalChildLayout(boolean verticalChildLayout);

    public boolean isHorizontalChildLayout();

    public String getEndTag();

    public Element getRoot();

    public String getActivityInfoString();

    public List<BPELAttributeValuePair> getAttributes();

    public Set<ActivityInterface> getLinkRoots();

    public ActivityInterface getParent();

    public int getCorrectionY();

    public void setCorrectionY(int correctionY);

    public void setLinkProperties(Map<String, Link> links, Set<ActivityInterface> sources,
                                  Set<ActivityInterface> targets);

    public Map<String, Link> getLinks();
}

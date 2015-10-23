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

import java.util.*;

/**
 * Manage the whole layout of the SVG graph
 */
public class LayoutManager {
    // Variables
    // Properties
    private int svgWidth = 2800;

    public int getSvgWidth() {
        return svgWidth;
    }

    public void setSvgWidth(int svgWidth) {
        this.svgWidth = svgWidth;
    }

    private int svgHeight = 3000;

    public int getSvgHeight() {
        return svgHeight;
    }

    public void setSvgHeight(int svgHeight) {
        this.svgHeight = svgHeight;
    }

    private int xSpacing = 50;

    public int getXSpacing() {
        return xSpacing;
    }

    public void setXSpacing(int xSpacing) {
        this.xSpacing = xSpacing;
    }

    private int ySpacing = 70;

    public int getYSpacing() {
        return ySpacing;
    }

    public void setYSpacing(int ySpacing) {
        this.ySpacing = ySpacing;
    }

    private boolean includeAssigns = true;

    public boolean isIncludeAssigns() {
        return includeAssigns;
    }

    public void setIncludeAssigns(boolean includeAssigns) {
        this.includeAssigns = includeAssigns;
    }

    private boolean showSequenceBoxes = true;

    public boolean isShowSequenceBoxes() {
        return showSequenceBoxes;
    }

    public void setShowSequenceBoxes(boolean showSequenceBoxes) {
        this.showSequenceBoxes = showSequenceBoxes;
    }

    private boolean verticalLayout = false;

    public boolean isVerticalLayout() {
        return verticalLayout;
    }

    public void setVerticalLayout(boolean verticalLayout) {
        this.verticalLayout = verticalLayout;
    }

    private boolean addCompositeActivityOpacity = false;

    public boolean isAddCompositeActivityOpacity() {
        return addCompositeActivityOpacity;
    }

    public void setAddCompositeActivityOpacity(boolean addCompositeActivityOpacity) {
        this.addCompositeActivityOpacity = addCompositeActivityOpacity;
    }

    private boolean addIconOpacity = false;

    public boolean isAddIconOpacity() {
        return addIconOpacity;
    }

    public void setAddIconOpacity(boolean addIconOpacity) {
        this.addIconOpacity = addIconOpacity;
    }

    private boolean addSimpleActivityOpacity = true;

    public boolean isAddSimpleActivityOpacity() {
        return addSimpleActivityOpacity;
    }

    public void setAddSimpleActivityOpacity(boolean addSimpleActivityOpacity) {
        this.addSimpleActivityOpacity = addSimpleActivityOpacity;
    }

    private String iconOpacity = "0.25";

    public String getCompositeActivityOpacity() {
        return compositeActivityOpacity;
    }

    public void setCompositeActivityOpacity(String compositeActivityOpacity) {
        this.compositeActivityOpacity = compositeActivityOpacity;
    }

    private String opacity = "0.50";

    public String getIconOpacity() {
        return iconOpacity;
    }

    public void setIconOpacity(String iconOpacity) {
        this.iconOpacity = iconOpacity;
    }

    private String simpleActivityOpacity = "0.251";

    public String getOpacity() {
        return opacity;
    }

    public void setOpacity(String opacity) {
        this.opacity = opacity;
    }

    private String compositeActivityOpacity = "0.10";

    public String getSimpleActivityOpacity() {
        return simpleActivityOpacity;
    }

    public void setSimpleActivityOpacity(String simpleActivityOpacity) {
        this.simpleActivityOpacity = simpleActivityOpacity;
    }

    private int startIconDim = 50;

    public int getStartIconDim() {
        return startIconDim;
    }

    public void setStartIconDim(int startIconDim) {
        this.startIconDim = startIconDim;
    }

    private int endIconDim = 50;

    public int getEndIconDim() {
        return endIconDim;
    }

    public void setEndIconDim(int endIconDim) {
        this.endIconDim = endIconDim;
    }

    //Get Icon Width 
    private int iconWidth = 100;

    public int getIconWidth() {
        return iconWidth;
    }

    public void setIconWidth(int iconWidth) {
        this.iconWidth = endIconDim;
    }

    // Methods
    public void layoutSVG(ActivityInterface rootActivity) {
        rootActivity.getDimensions();
        layoutLinks(rootActivity);
        rootActivity.layout(0, 0);
    }

    private Map<ActivityInterface, ArrayList<ActivityInterface>> getLinkAdjacencyList(Map<String, Link> links) {
        Map<ActivityInterface, ArrayList<ActivityInterface>> linkAdjacencyList =
                new HashMap<ActivityInterface, ArrayList<ActivityInterface>>();
        if (links != null && !links.isEmpty()) {
            Set linksSet = links.entrySet();
            Iterator linksIterator = linksSet.iterator();
            while (linksIterator.hasNext()) {
                Map.Entry<String, Link> link = (Map.Entry<String, Link>) linksIterator.next();
                ActivityInterface startActivity = link.getValue().getSource();
                ActivityInterface endActivity = link.getValue().getTarget();
                if (linkAdjacencyList.containsKey(startActivity)) {
                    linkAdjacencyList.get(startActivity).add(endActivity);
                } else {
                    ArrayList<ActivityInterface> tmpArrayList = new ArrayList<ActivityInterface>();
                    tmpArrayList.add(endActivity);
                    linkAdjacencyList.put(startActivity, tmpArrayList);
                }
            }
        }
        return linkAdjacencyList;
    }

    private Map<ActivityInterface, ArrayList<ActivityInterface>> linkAdjacencyList;
    private Set<ActivityInterface> rootLinks;
    private int correctionCumulation = 0;
    private int hieghestCorrectionCumulation = 0;

    private void setCorrectionY(ActivityInterface source, ArrayList<ActivityInterface> children) {
        if (children != null && !children.isEmpty()) {
            for (ActivityInterface target : children) {
                int whereTargetShouldBe = (source.getStartIconYTop() + source.getStartIconHeight() + 40);
                if (target != null) {
                    int whereTargetIs = target.getStartIconYTop();
                    int correction = whereTargetShouldBe - whereTargetIs;
                    if (whereTargetIs < whereTargetShouldBe) {
                        target.setCorrectionY(correction);
                        int relativeCorrection = whereTargetShouldBe - source.getStartIconYTop();
                        correctionCumulation += relativeCorrection;
                        setCorrectionY(target, linkAdjacencyList.get(target));
                        correctionCumulation -= relativeCorrection;
                    }
                }
            }
        } else {
            if (hieghestCorrectionCumulation < correctionCumulation) {
                hieghestCorrectionCumulation = correctionCumulation;
            }
        }
    }

    private void layoutLinks(ActivityInterface rootActivity) {
        Map<String, Link> links = rootActivity.getLinks();
        if (links != null && !links.isEmpty()) {
            linkAdjacencyList = getLinkAdjacencyList(links);
            rootLinks = rootActivity.getLinkRoots();

            for (ActivityInterface root : rootLinks) {
                correctionCumulation = 0;
                setCorrectionY(root, linkAdjacencyList.get(root));
            }

            ActivityInterface tempParent = rootLinks.iterator().next().getParent();
            while (tempParent != null) {
                int tempHeight = tempParent.getDimensions().getHeight();
                tempParent.getDimensions().setHeight(tempHeight + hieghestCorrectionCumulation);
                tempParent = tempParent.getParent();
            }

            rootLinks.clear();
        }
    }
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Manage the whole layout of the SVG graph
 */
public class LayoutManager {
    // Variables
    // Properties
    private int svgWidth = 2800;
    private int svgHeight = 3000;
    private int xSpacing = 50;
    private int ySpacing = 70;
    private boolean includeAssigns = true;
    private boolean showSequenceBoxes = true;
    private boolean verticalLayout = false;
    private boolean addCompositeActivityOpacity = false;
    private boolean addIconOpacity = false;
    private boolean addSimpleActivityOpacity = true;
    private String iconOpacity = "0.25";
    private String opacity = "0.50";
    private String simpleActivityOpacity = "0.251";
    private String compositeActivityOpacity = "0.10";
    private int startIconDim = 50;
    private int endIconDim = 50;
    //Get Icon Width
    private int iconWidth = 100;
    private Map<ActivityInterface, ArrayList<ActivityInterface>> linkAdjacencyList;
    private Set<ActivityInterface> rootLinks;
    private int correctionCumulation = 0;
    private int hieghestCorrectionCumulation = 0;

    /**
     * Gets the width of the SVG graph
     *
     * @return width of the SVG graph
     */
    public int getSvgWidth() {
        return svgWidth;
    }

    /**
     * Sets the width of the SVG graph
     *
     * @param svgWidth width of the SVG graph
     */
    public void setSvgWidth(int svgWidth) {
        this.svgWidth = svgWidth;
    }

    /**
     * Gets the height of the SVG graph
     *
     * @return height of the SVG graph
     */
    public int getSvgHeight() {
        return svgHeight;
    }

    /**
     * Sets the height of the SVG graph
     *
     * @param svgHeight height of the SVG graph
     */
    public void setSvgHeight(int svgHeight) {
        this.svgHeight = svgHeight;
    }

    /**
     * Gets the xSpacing which is added to the width of the activities when setting the dimensions
     *
     * @return xSpacing "50"
     */
    public int getXSpacing() {
        return xSpacing;
    }

    /**
     * Sets the xSpacing which is added to the width of the activities when setting the dimensions
     *
     * @param xSpacing "50"
     */
    public void setXSpacing(int xSpacing) {
        this.xSpacing = xSpacing;
    }

    /**
     * Gets the ySpacing which is added to the height of the activities when setting the dimensions
     *
     * @return ySpacing "70"
     */
    public int getYSpacing() {
        return ySpacing;
    }

    /**
     * Sets the ySpacing which is added to the height of the activities when setting the dimensions
     *
     * @param ySpacing "70"
     */
    public void setYSpacing(int ySpacing) {
        this.ySpacing = ySpacing;
    }

    /**
     * Gets the boolean value to include the assign activities
     *
     * @return boolean value to include the assign activities->true/false
     */
    public boolean isIncludeAssigns() {
        return includeAssigns;
    }

    /**
     * Sets the boolean value to include the assign activities
     *
     * @param includeAssigns boolean value to include the assign activities
     */
    public void setIncludeAssigns(boolean includeAssigns) {
        this.includeAssigns = includeAssigns;
    }

    /**
     * Gets the boolean value to show the Sequence box
     *
     * @return boolean value to show the the Sequence box->true/false
     */
    public boolean isShowSequenceBoxes() {
        return showSequenceBoxes;
    }

    /**
     * Sets the boolean value to show the Sequence box
     *
     * @param showSequenceBoxes boolean value to show the the Sequence box->true/false
     */
    public void setShowSequenceBoxes(boolean showSequenceBoxes) {
        this.showSequenceBoxes = showSequenceBoxes;
    }

    /**
     * Gets the boolean value to select the vertical layout
     *
     * @return boolean value to select the vertical layout -> true/false
     */
    public boolean isVerticalLayout() {
        return verticalLayout;
    }

    /**
     * Sets the boolean value to select the vertical layout
     *
     * @param verticalLayout boolean value to select the vertical layout
     */
    public void setVerticalLayout(boolean verticalLayout) {
        this.verticalLayout = verticalLayout;
    }

    /**
     * Gets true/false to add  opacity to composite activity icons e.g:like IF, ELSE IF activities
     *
     * @return true/false
     */
    public boolean isAddCompositeActivityOpacity() {
        return addCompositeActivityOpacity;
    }

    /**
     * Sets true/false to add  opacity to composite activity icons
     *
     * @param addCompositeActivityOpacity boolean value to add  opacity to composite activity icons
     */
    public void setAddCompositeActivityOpacity(boolean addCompositeActivityOpacity) {
        this.addCompositeActivityOpacity = addCompositeActivityOpacity;
    }

    /**
     * Gets true/false to add  opacity to activity icons
     *
     * @return true/false
     */
    public boolean isAddIconOpacity() {
        return addIconOpacity;
    }

    /**
     * Sets true/false to add  opacity to activity icons
     *
     * @param addIconOpacity boolean value to add opacity to activity icons
     */
    public void setAddIconOpacity(boolean addIconOpacity) {
        this.addIconOpacity = addIconOpacity;
    }

    /**
     * Gets true/false to add opacity to simple activity icons e.g:like ASSIGN, THROW activities
     *
     * @return true/false
     */
    public boolean isAddSimpleActivityOpacity() {
        return addSimpleActivityOpacity;
    }

    /**
     * Sets true/false to add opacity to simple activity icons
     *
     * @param addSimpleActivityOpacity boolean value to add opacity to simple activity icons
     */
    public void setAddSimpleActivityOpacity(boolean addSimpleActivityOpacity) {
        this.addSimpleActivityOpacity = addSimpleActivityOpacity;
    }

    /**
     * Gets the icon opacity amount for composite activities
     *
     * @return String with the opacity "0.10"
     */
    public String getCompositeActivityOpacity() {
        return compositeActivityOpacity;
    }

    /**
     * Sets the icon opacity amount for composite activities
     *
     * @param compositeActivityOpacity icon opacity amount for composite activities
     */
    public void setCompositeActivityOpacity(String compositeActivityOpacity) {
        this.compositeActivityOpacity = compositeActivityOpacity;
    }

    /**
     * Gets the icon opacity amount for activities
     *
     * @return String with the opacity "0.25"
     */
    public String getIconOpacity() {
        return iconOpacity;
    }

    /**
     * Sets the icon opacity amount for activities
     *
     * @param iconOpacity icon opacity amount for activities
     */
    public void setIconOpacity(String iconOpacity) {
        this.iconOpacity = iconOpacity;
    }

    /**
     * Gets the icon opacity amount
     *
     * @return String with the opacity "0.5"
     */
    public String getOpacity() {
        return opacity;
    }

    /**
     * Sets the icon opacity amount
     *
     * @param opacity icon opacity
     */
    public void setOpacity(String opacity) {
        this.opacity = opacity;
    }

    /**
     * Gets the icon opacity amount for simple activities
     *
     * @return String with the opacity "0.251"
     */
    public String getSimpleActivityOpacity() {
        return simpleActivityOpacity;
    }

    /**
     * Sets the icon opacity amount for simple activities
     *
     * @param simpleActivityOpacity icon opacity amount for simple activities
     */
    public void setSimpleActivityOpacity(String simpleActivityOpacity) {
        this.simpleActivityOpacity = simpleActivityOpacity;
    }

    /**
     * Gets the dimensions of the start icon of the activity
     *
     * @return dimensions of the start icon of the activity
     */
    public int getStartIconDim() {
        return startIconDim;
    }

    /**
     * Sets the dimensions of the start icon of the activity
     *
     * @param startIconDim dimensions of the start icon of the activity
     */
    public void setStartIconDim(int startIconDim) {
        this.startIconDim = startIconDim;
    }

    // Methods

    /**
     * Gets the dimensions of the end icon of the activity
     *
     * @return dimensions of the end icon of the activity
     */
    public int getEndIconDim() {
        return endIconDim;
    }

    /**
     * Sets the dimensions of the end icon of the activity
     *
     * @param endIconDim dimensions of the end icon of the activity
     */
    public void setEndIconDim(int endIconDim) {
        this.endIconDim = endIconDim;
    }

    /**
     * Gets the width of the rectangle/image holder which holds the activity icon
     *
     * @return icon width i.e. width of the rectangle/image holder which holds the activity icon
     */
    public int getIconWidth() {
        return iconWidth;
    }

    /**
     * Sets the width of the rectangle/image holder which holds the activity icon
     *
     * @param iconWidth width of the rectangle/image holder which holds the activity icon
     */
    public void setIconWidth(int iconWidth) {
        this.iconWidth = endIconDim;
    }

    /**
     * Layout the SVG
     * Get the dimensions i.e. width and height of the root activity, set the links of the process if there are any
     *
     * @param rootActivity
     */
    public void layoutSVG(ActivityInterface rootActivity) {
        rootActivity.getDimensions();
        layoutLinks(rootActivity);
        rootActivity.layout(0, 0);
    }

    /**
     * Get the Link Adjacency List for a process (Map which contains the Source and Target activities for links )
     *
     * @param links
     * @return
     */
    private Map<ActivityInterface, ArrayList<ActivityInterface>> getLinkAdjacencyList(Map<String, Link> links) {
        Map<ActivityInterface, ArrayList<ActivityInterface>> linkAdjacencyList =
                new HashMap<ActivityInterface, ArrayList<ActivityInterface>>();
        //Checks whether they are any links in the bpel process
        if (links != null && !links.isEmpty()) {
            //Returns a set view of the mappings contained in the links map
            Set linksSet = links.entrySet();
            //Iterates through each element in the set
            Iterator linksIterator = linksSet.iterator();
            while (linksIterator.hasNext()) {
                Map.Entry<String, Link> link = (Map.Entry<String, Link>) linksIterator.next();
                //Gets the Source/Start activity of the link
                ActivityInterface startActivity = link.getValue().getSource();
                //Gets the Target/End activity of the link
                ActivityInterface endActivity = link.getValue().getTarget();
                //Checks whether the AdjacencyList contains the Source/Start activity of the link(key)
                if (linkAdjacencyList.containsKey(startActivity)) {
                    //Get the Source/Start activity(key) and append the Target/End activity(value)
                    linkAdjacencyList.get(startActivity).add(endActivity);
                } else {
                    //If the AdjacencyList doesn't contain the Source/Start activity of the link
                    ArrayList<ActivityInterface> tmpArrayList = new ArrayList<ActivityInterface>();
                    //Add the Target/End activity to the temp arraylist
                    tmpArrayList.add(endActivity);
                    //Include the Source/Start activity and the temp arraylist which contains the Target/End activity
                    linkAdjacencyList.put(startActivity, tmpArrayList);
                }
            }
        }
        return linkAdjacencyList;
    }

    /**
     * Set the yTop position of the Target activities
     *
     * @param source
     * @param children
     */
    private void setCorrectionY(ActivityInterface source, ArrayList<ActivityInterface> children) {
        //Checks whether the source contains any target activities
        if (children != null && !children.isEmpty()) {
            //Iterate through the target activities of the Source
            for (ActivityInterface target : children) {
                int whereTargetShouldBe = source.getStartIconHeight() + 40;
                //Checks whether the target is null
                if (target != null) {
                    //Gets the yTop position of the Target activity
                    int whereTargetIs = target.getStartIconYTop();
                    //Calculate the actual yTop position of the Target activity
//                    int correction = whereTargetShouldBe - whereTargetIs;
                    if (whereTargetIs < whereTargetShouldBe) {
                        //Set the correct yTop position of the target activity
                        target.setCorrectionY(target.getStartIconYTop());
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

    /**
     * Sets the layout of the links in the process
     *
     * @param rootActivity root Activity of the process
     */
    private void layoutLinks(ActivityInterface rootActivity) {
        //Get the links in a process
        Map<String, Link> links = rootActivity.getLinks();
        //Checks whether the list with the links is empty or null i.e. whether the  process has any links
        if (links != null && !links.isEmpty()) {
            //Get details of the links  from the linkAdjacencyList
            linkAdjacencyList = getLinkAdjacencyList(links);
            //Get a list of activities which are only SOURCE activities and not TARGET activities
            rootLinks = rootActivity.getLinkRoots();
            //Iterate through the activities which are only SOURCE activities
            for (ActivityInterface root : rootLinks) {
                correctionCumulation = 0;
                /*
                * root- iterated activity which is only a SOURCE activity
                * get(root) ---> the value to which the activity is mapped, or
     *                          {@code null} if this map contains no mapping for the activity
                * */
                setCorrectionY(root, linkAdjacencyList.get(root));
            }

            ActivityInterface tempParent = rootLinks.iterator().next().getParent();
            //Iterate through the parent activities of the SOURCE activities
            while (tempParent != null) {
                //Get the height of the parent activity
                int tempHeight = tempParent.getDimensions().getHeight();
                //Set the height of the parent activity by adding the hieghestCorrectionCumulation to the activity
                // height
                tempParent.getDimensions().setHeight(tempHeight + hieghestCorrectionCumulation);
                tempParent = tempParent.getParent();
            }
            //Removes all of the activities from this set. The set will be empty
            rootLinks.clear();
        }
    }
}

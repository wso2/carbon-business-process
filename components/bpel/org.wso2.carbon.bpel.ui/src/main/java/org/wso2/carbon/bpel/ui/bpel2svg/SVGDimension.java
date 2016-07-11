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

/**
 * Manage the dimensions of the SVG graph
 */
public class SVGDimension {

    //protected LayoutManager layoutManager = BPEL2SVGFactory.getInstance().getLayoutManager();
    private int xLeft = 0;
    private int yTop = 0;
    private int width = 0;
    private int height = 0;

    //Constructors
    public SVGDimension() {
    }

    /**
     * @param width  width of the SVG
     * @param height height of the SVG
     */
    public SVGDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Gets the height of the SVG
     *
     * @return height of the SVG
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the SVG
     *
     * @param height height of the SVG
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Gets the width of the SVG
     *
     * @return width of the SVG
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the SVG
     *
     * @param width height of the SVG
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Gets the xLeft position of the SVG
     *
     * @return xLeft position of the SVG
     */
    public int getXLeft() {
        return xLeft;
    }

    /**
     * Sets the xLeft position of the SVG
     *
     * @param xLeft height of the SVG
     */
    public void setXLeft(int xLeft) {
        this.xLeft = xLeft;
    }

    /**
     * Gets the yTop position of the SVG
     *
     * @return yTop position of the SVG
     */
    public int getYTop() {
        return yTop;
    }

    /**
     * Sets the yTop position of the SVG
     *
     * @param yTop position of the SVG
     */
    public void setYTop(int yTop) {
        this.yTop = yTop;
    }
}

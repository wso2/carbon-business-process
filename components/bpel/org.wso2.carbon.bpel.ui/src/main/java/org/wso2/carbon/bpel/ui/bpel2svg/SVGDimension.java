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

public class SVGDimension {

    protected LayoutManager layoutManager = BPEL2SVGFactory.getInstance().getLayoutManager();
    private int xLeft = 0;
    private int yTop = 0;
    private int width = 0;
    private int height = 0;

    public SVGDimension() {
    }

    public SVGDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getXLeft() {
        return xLeft;
    }

    public void setXLeft(int xLeft) {
        this.xLeft = xLeft;
    }

    public int getYTop() {
        return yTop;
    }

    public void setYTop(int yTop) {
        this.yTop = yTop;
    }
}

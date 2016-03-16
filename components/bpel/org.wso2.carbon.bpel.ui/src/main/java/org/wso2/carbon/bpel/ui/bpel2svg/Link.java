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

package org.wso2.carbon.bpel.ui.bpel2svg;

/**
 * Attributes of a link i.e. the source and the target activity
 */
public class Link {
    /**
     * Gets the Start/Source activity of the link
     * @return Start/Source Activity of the link
     */
    public ActivityInterface getSource() {
        return source;
    }

    /**
     * Sets the Start/Source activity of the link
     * @param source Start/Source activity of the link
     */
    public void setSource(ActivityInterface source) {
        this.source = source;
    }
    /**
     * Gets the End/Target activity of the link
     * @return End/Target Activity of the link
     */
    public ActivityInterface getTarget() {
        return target;
    }
    /**
     * Sets the End/Target activity of the link
     * @param target End/Target activity of the link
     */
    public void setTarget(ActivityInterface target) {
        this.target = target;
    }

    private ActivityInterface source;
    private ActivityInterface target;
}

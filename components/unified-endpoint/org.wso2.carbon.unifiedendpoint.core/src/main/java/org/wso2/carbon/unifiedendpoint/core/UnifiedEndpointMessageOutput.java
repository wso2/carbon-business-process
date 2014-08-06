package org.wso2.carbon.unifiedendpoint.core;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * MessageOutput
 */
public class UnifiedEndpointMessageOutput {
    private String format;
    private String optimize;
    private String charSet;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOptimize() {
        return optimize;
    }

    public void setOptimize(String optimize) {
        this.optimize = optimize;
    }

    public String getCharSetEncoding() {
        return charSet;
    }

    public void setCharSetEncoding(String charSet) {
        this.charSet = charSet;
    }

}

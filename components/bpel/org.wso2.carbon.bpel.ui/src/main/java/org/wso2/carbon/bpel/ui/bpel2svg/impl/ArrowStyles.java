/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpel.ui.bpel2svg.impl;

/**
 * Arrow Style constant.
 */
public class ArrowStyles {


    public static String LARGE_ARROW_STYLE =
            "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.5;stroke-linecap:" +
                    "butt;stroke-linejoin:bevel;marker-end:url(#Arrow1Lend);stroke-dasharray:" +
                    "none;stroke-opacity:1";

    public static String MEDIUM_ARROW_STYLE =
            "fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1.5;stroke-linecap:" +
                    "butt;stroke-linejoin:bevel;marker-end:url(#Arrow1Mend);stroke-dasharray:" +
                    "none;stroke-opacity:1";

    public static String LARGE_LINK_ARROW_STYLE =
            "fill:none;fill-rule:evenodd;stroke:#FF0000;stroke-width:3;stroke-linecap:" +
                    "butt;stroke-linejoin:bevel;marker-end:url(#LinkArrow);stroke-dasharray:" +
                    "none;stroke-opacity:1;opacity: 0.25;";

    public static String MEDIUM_LINK_ARROW_STYLE =
            "fill:none;fill-rule:evenodd;stroke:#FF0000;stroke-width:3;stroke-linecap:" +
                    "butt;stroke-linejoin:bevel;marker-end:url(#LinkArrow);stroke-dasharray:" +
                    "none;stroke-opacity:1;opacity: 0.25;";


}

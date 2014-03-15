<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointTransport" %>

<%
    Object obj = request.getAttribute("uepTransport");
    if (obj != null) {

        UnifiedEndpointTransport uepTransport = (UnifiedEndpointTransport) obj;
        
        String sizeStr = request.getParameter("trp_props_size");
        int propsSize = 0;
        if (sizeStr != null) {
            propsSize = Integer.parseInt(sizeStr);
        }

        String action = request.getParameter("action");
        String mode = request.getParameter("mode");


        if (action != null && mode != null) {
            if (action.equals("none"))
                if (mode.equals("edit")) {
                    uepTransport.getTransportProperties().clear();
                }
        }

        for (int i = 0; i < propsSize; i++) {
            if (request.getParameter("trp_prop_name" + i) != null
                    && request.getParameter("trap_prop_val" + i) != null) {
                uepTransport.addTransportProperty(request.getParameter("trp_prop_name" + i), request.getParameter("trap_prop_val" + i));
            }

        }
    }

%>
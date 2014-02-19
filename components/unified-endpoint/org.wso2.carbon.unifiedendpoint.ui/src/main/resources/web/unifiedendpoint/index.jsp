<%--
 ~ Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.ui.UnifiedEndpointAdminClient" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointSerializer" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointFactory" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint" %>
<%@ page import="java.util.HashMap" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>

<!-- Connection handling lib -->
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<!-- Source File -->

<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<link type="text/css" rel="stylesheet" href="css/style.css"/>
<%--<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>--%>


<fmt:bundle basename="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="unifiedendpoint"
        resourceBundle="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>


<script type="text/javascript">
    var allowTabChange = true;
    var emtpyEntries = false;

    $(function() {
        var $myTabs = $("#tabs");

        $myTabs.tabs({
            select: function(event, ui) {
                if (!allowTabChange) {
                    alert("Tab selection is disabled, while you are in the middle of a workflow");
                }
                return allowTabChange;
            },

            show: function(event, ui) {
                var selectedTab = $myTabs.tabs('option', 'selected');
                allowTabChange = true;
            }
        });

        $myTabs.tabs('select', 1);
        if (emtpyEntries) {
            $myTabs.tabs('select', 0);
        }
    });

    function deleteUep(name) {
        window.location.href = "unified_ep_design.jsp?name=" + name;
    }

    function editUep(name) {
        window.location.href = "unified_ep_design.jsp?namex=" + name + "&mode=edit&action=none";
    }


</script>

<%

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    UnifiedEndpointAdminClient client = new UnifiedEndpointAdminClient(cookie, url, configContext);


    Map<String, UnifiedEndpoint> uepMap = new HashMap<String, UnifiedEndpoint>();
    UnifiedEndpointFactory uepFactory = new UnifiedEndpointFactory();

    /*ToDo : parameters should be used during pagination. Currently parameters have no use.*/
    String[] uepArray = client.getAllEndpoints(1, 5);
    if (uepArray != null) {
        for (String uepStr : uepArray) {
            System.out.println("UEP name - " + uepStr );
            if (uepStr != null) {
                UnifiedEndpoint uep = uepFactory.createEndpoint(uepStr);
                if (uep != null) {
                    uepMap.put(uep.getUepId(), uep);
                }
            }
        }
    }

%>


<div id="middle">
    <h2><fmt:message key="uep.menu.text"/></h2>

    <div id="workArea">
        <div id="tabs">
            <%
                String res = "init";
                if (uepMap.isEmpty()) {
            %>
            <script type="text/javascript"> emtpyEntries=true</script>
            <%
                }
            %>
            <ul>
                <li><a href="#tabs-1"><fmt:message key="uep.add.ep.tab.header"/></a></li>
                <li><a href="#tabs-2"><fmt:message key="uep.available.ep.tab.header"/></a></li>
            </ul>


            <div id="tabs-1">
                <form id="uep_form" method="POST" action="">
                    <input type="hidden" name="formSubmitted" value="true"/>
                    <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                        <tr>
                            <td>
                                <a class="icon-link"
                                   href="unified_ep_design.jsp"
                                   style="background-image: url(../admin/images/add.gif);">
                                    <fmt:message key="uep.add.new.ep"/>
                                </a>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>

            <div id="tabs-2">
                <table class="styledLeft" cellspacing="1" id="sequencesTable">
                    <thead>
                    <tr>
                        <th>
                            <fmt:message key="uep.name"/>
                        </th>
                        <th>
                            <fmt:message key="uep.wsa.address"/>
                        </th>
                        <th colspan="2">
                            <fmt:message key="uep.action"/>
                        </th>
                    </tr>
                    </thead>

                    <tbody>
                    <%
                        for (Map.Entry<String, UnifiedEndpoint> entry : uepMap.entrySet()) {
                    %>
                    <tr>
                        <td>
                            <%= entry.getKey() %>
                        </td>
                        <td>
                            <%= entry.getValue().getAddress() %>
                        </td>

                        <td style="border-left:none;border-right:none;width:100px">
                            <div class="inlineDiv">
                                <a href="#" onclick="editUep('<%= entry.getKey() %>')" class="icon-link"
                                   style="background-image:url(../admin/images/edit.gif);">
                                    <fmt:message key="edit"/></a>
                            </div>
                        </td>
                        <td style="border-left:none;width:100px">
                            <div class="inlineDiv">
                                <a href="#" onclick="deleteUep('<%= entry.getKey() %>')" class="icon-link"
                                   style="background-image:url(../admin/images/delete.gif);">
                                    <fmt:message key="delete"/></a>
                            </div>
                        </td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>

                <form id="uep_form" method="POST" action="">
                    <input type="hidden" name="formSubmitted" value="true"/>
                    <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                        <tr>
                            <td>
                                <a class="icon-link"
                                   href="unified_ep_design.jsp"
                                   style="background-image: url(../admin/images/add.gif);">
                                    <fmt:message key="uep.add.new.ep"/>
                                </a>
                            </td>
                        </tr>
                    </table>

                </form>


            </div>
        </div>
    </div>
</div>

</fmt:bundle>
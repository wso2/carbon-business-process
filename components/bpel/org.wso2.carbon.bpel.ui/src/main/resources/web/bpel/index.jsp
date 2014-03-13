<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<!--
~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    response.setHeader("Cache-Control",
            "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    boolean isAuthenticatedToViewSummery =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel") &&
            (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/instances") ||
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes"));

    boolean isAuthenticatedViewProcess =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes") ||
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");

    boolean isAuthenticatedToViewSystemSummary =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor");

%>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
    <carbon:breadcrumb
            label="bpel.packages"
            resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <script type="text/javascript" src="js/bpel-main.js"></script>

        <!--[if IE]>
        <script type="text/javascript" src="js/flot/excanvas.min.js"></script><![endif]-->
        <script type="text/javascript" src="js/flot/jquery.flot.js"></script>
        <script type="text/javascript" src="js/flot/jquery.flot.stack.js"></script>
        <script type="text/javascript" src="js/bpel-dashboard.js"></script>
        <script type="text/javascript">
            serverDataNotAvailable = "<fmt:message key="server.info.not.available"/>"
        </script>
        <style type="text/css">
            .summery {
                width: 400px;
                height: 250px;
                margin-top: 10px;
            }

            #mem-graph{
                width: 450px;
                height: 250px;
                margin-top: 10px;
            }

            table#bps-summary td{
                padding:3px;
            }
        </style>
<%
    if (isAuthenticatedToViewSummery) {
%>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                // Load global instance summary. We can make this reload every 10 or 20 seconds.
                BPEL.summary.drawOverallInstanceSummary("process-instance-summary");
                BPEL.summary.drawLongRunningInstanceSummary("long-running-instance-summary");
                BPEL.summary.drawInstanceSummaryAgainstProcess("process-instance-summary-vs-process", <%=isAuthenticatedViewProcess%>);




<%
        if (isAuthenticatedToViewSystemSummary) { // Here both isAuthenticatedToViewSummery and
                                                  // isAuthenticatedToViewSystemSummary should be satisfied
%>
                function updateServerInfo(){BPEL.summary.drawServerInformation("server-info")}

                updateServerInfo();
                serverInfoIntervalObject = setInterval(updateServerInfo, 15000);
                
                BPEL.summary.drawServerMemoryConsumptionGraph();
                memInfoInterlObject = setInterval(BPEL.summary.drawServerMemoryConsumptionGraph, 500);
<%
        }
%>
            });
        </script>
<%
    }
%>
        <div id="bps-home-main">
            <h2><fmt:message key="bps.home"/></h2>

            <div id="workArea">
                <div id="bps-home">
                    <table width="100%" id="bps-summary">
                        <tbody>
                        <tr>
                            <td width="50%" id="td00">
                                <table class="styledLeft" id="instance_summary">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="overall.instance.summary.graph"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td style="text-align: center;">
<%
    if (isAuthenticatedToViewSummery) {
%>
                                            <div id="process-instance-summary" class="summery"></div>
                                            <div id="process-instance-summary-legend" align="center"></div>
<%
    } else {
%>
                                            <div class="summery"><fmt:message key="do.not.have.permission.to.view.instance.summery"/></div>
<%
    }
%>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td width="50%" id="td01">
                                <table class="styledLeft" id="instance_summary_vs_process">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="overall.instance.summary.vs.process.graph"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td style="text-align: center;">
<%
    if (isAuthenticatedToViewSummery) {
%>
                                            <div id="process-instance-summary-vs-process" class="summery"></div>
                                            <div id="process-instance-summary-vs-process-legend" align="center"></div>
<%
    } else {
%>
                                            <div class="summery"><fmt:message key="do.not.have.permission.to.view.instance.summery"/></div>
<%
    }
%>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                            </td>
                        </tr>
                        <tr>
                            <td width="50%" id="td10">
                                <table class="styledLeft" id="long-running">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="long.running.instance.summary.graph"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td style="text-align: center;">
<%
    if (isAuthenticatedToViewSummery) {
%>
                                            <div id="long-running-instance-summary" class="summery"></div>
                                            <div id="long-running-instance-summary-legend" align="center"></div>
<%
    } else {
%>
                                            <div class="summery"><fmt:message key="do.not.have.permission.to.view.long.running.instance.summery"/></div>
<%
    }
%>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td width="50%" id="td11">
                                <%--<table class="styledLeft" id="long-running">--%>
                                    <%--<thead>--%>
                                    <%--<tr>--%>
                                        <%--<th><fmt:message key="long.running.instance.summary.graph"/></th>--%>
                                    <%--</tr>--%>
                                    <%--</thead>--%>
                                    <%--<tbody>--%>
                                    <%--<tr>--%>
                                        <%--<td style="text-align: center;">--%>
                                            <%--<div id="long-running-instance-summary" class="summery"></div>--%>
                                            <%--<div id="long-running-instance-summary-legend" align="center"></div>--%>
                                        <%--</td>--%>
                                    <%--</tr>--%>
                                    <%--</tbody>--%>
                                <%--</table>--%>

                            </td>
                        </tr>
                        <tr>
                            <td width="50%" id="td12">
                                <div id="server-info"></div>
                            </td>
                            <td width="50%" id="td13">
<%
    if (isAuthenticatedToViewSystemSummary) {
%>
                                <table class="styledLeft" id="server-memory">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="server.memory.graph"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td style="text-align: center;" id="server-mem-graph">
                                            <div id="mem-graph"></div>
                                            <div id="mem-legend"></div>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
<%
    }
%>
                            </td>
                        </tr>

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</fmt:bundle>
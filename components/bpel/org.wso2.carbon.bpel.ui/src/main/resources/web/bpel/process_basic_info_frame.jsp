<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>
<!--
 ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    String packageName = Encode.forXml(request.getParameter("packageName"));
    String processID = Encode.forXml(request.getParameter("processID"));
    String processVersion = Encode.forXml(request.getParameter("processVersion"));
    String processOlderVersion = Encode.forXml(request.getParameter("processOlderVersion"));
    String processStatus = Encode.forXml(request.getParameter("processStatus"));
    String processDeployedDate = Encode.forXml(request.getParameter("processDeployedDate"));
    String noOfInstances = Encode.forXml(request.getParameter("noOfInstances"));

    boolean isAuthorizedToManageProcesses =
                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");
    boolean isAuthorizedToMonitor =
                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");
%>
<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
<link rel="stylesheet" type="text/css" href="css/bpel_icon_link.css" />
<table class="styledLeft" id="processInfoTable" style="margin-left:0px;" width="100%">
    <thead>
        <tr>
            <th colspan="2" align="left"><fmt:message key="processdetails"/></th>
        </tr>
    </thead>
<%
    if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
%>
    <tr>
        <td width="30%"><fmt:message key="processid"/></td>
        <td><%= processID%>
        </td>
    </tr>
    <tr>
        <td width="30%"><fmt:message key="version"/></td>
        <td><%= processVersion%>
        </td>
    </tr>
    <tr>
        <td width="30%"><fmt:message key="status"/></td>
        <%--<td><%= processStatus%></td>--%>
        <td><%= processStatus%>&nbsp;&nbsp;
<%
        if (isAuthorizedToManageProcesses && processOlderVersion.equals("0")) {
%>
            [
<%
            if (processStatus.toUpperCase().equals("ACTIVE")) {
%>
            <a href="<%=BpelUIUtil.generateRetireLinkForProcessInfoPage(processID)%>" id="process_det_ret"
               class="bpel-icon-link registryWriteOperation"
               style="background-image:url(images/deactivate.gif);"><fmt:message key="retire"/></a>
            <a href="<%=BpelUIUtil.generateRetireLinkForProcessInfoPage(processID)%>" id="process_det_ret"
               class="bpel-icon-link registryNonWriteOperation"
               style="background-image:url(images/deactivate.gif);color:#777;cursor:default;"
               onclick="return false"><fmt:message key="retire"/></a>

            <script type="text/javascript">
                jQuery('#process_det_ret').click(function(){
                    function handleYes(){
                        window.location = jQuery('#process_det_ret').attr('href');
                    }
                    sessionAwareFunction(function() {
                    CARBON.showConfirmationDialog(
                            "Do you want to retire process <%=processID%>?",
                            handleYes,
                            null);
                    }, "<fmt:message key="session.timed.out"/>");
                    return false;
                });
            </script>
<%
            } else if (processOlderVersion.equals("0")){
%>
            <a href="<%=BpelUIUtil.generateActivateLinkForProcessInfoPage(processID)%>" id="process_det_act"
               class="bpel-icon-link registryWriteOperation"
               style="background-image:url(images/activate.gif);"><fmt:message key="activate"/></a>
            <a href="<%=BpelUIUtil.generateActivateLinkForProcessInfoPage(processID)%>" id="process_det_act"
               class="bpel-icon-link registryNonWriteOperation"
               style="background-image:url(images/activate.gif);color:#777;cursor:default;"
               onclick="return false"><fmt:message key="activate"/></a>

            <script type="text/javascript">
                jQuery('#process_det_act').click(function(){
                    function handleYes(){
                        window.location = jQuery('#process_det_act').attr('href');
                    }
                    sessionAwareFunction(function() {
                    CARBON.showConfirmationDialog(
                            "Do you want to activate process <%=processID%>?",
                            handleYes,
                            null);
                    }, "<fmt:message key="session.timed.out"/>");
                    return false;
                });
            </script>
    <%
            }
    %>
            ]
<%
        }
%>
                                </td>
    </tr>
    <tr>
        <td width="30%"><fmt:message key="deployed.date"/></td>
        <td><%= processDeployedDate%>
        </td>
    </tr>
    <tr>
        <td width="30%"><fmt:message key="total.instances"/></td>
         <td><a href="<%=BpelUIUtil.getInstanceFilterURL(processID)%>"><%= noOfInstances%></a>
        </td>
    </tr>
    <tr>
        <td width="30%"><fmt:message key="package.name"/></td>
        <td><%= packageName%>
        </td>
    </tr>
    <tr>
        <td width="30%"><fmt:message key="deployment.information"/></td>
        <% if (processStatus.toUpperCase().equals("ACTIVE")) { %>
            <td><a href="./deployment_descriptor_editor.jsp?Pid=<%=processID%>">View Deployment Descriptor</a></td>
        <% } else { %>
            <td>View Deployment Descriptor</td>
        <% } %>
    </tr>
<%
    } else {
%>
    <tr>
        <td colspan="2" align="left"><fmt:message key="do.not.have.permission.to.view.process.details"/></td>
    </tr>
<%
    }
%>
</table>
<script type="text/javascript">
    alternateTableRows('processInfoTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>
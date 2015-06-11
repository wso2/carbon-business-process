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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.InstanceManagementServiceClient" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
<%@ page import="org.wso2.carbon.bpel.ui.InstanceFilterUtil" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.wso2.carbon.bpel.ui.InstanceFilter" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.net.URLEncoder" %>

<jsp:useBean id="instanceFilter" scope="session" class="org.wso2.carbon.bpel.ui.InstanceFilter"/>
<jsp:setProperty name="instanceFilter" property="*" />
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    response.setHeader("Cache-Control",
            "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    InstanceManagementServiceClient client;
    ProcessManagementServiceClient processManagementClient = null;
    PaginatedInstanceList instanceList = null;
    int numberOfPages = 0;
    String processIds[] = null;
    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String deleteMex = CharacterEncoder.getSafeText(request.getParameter("deleteMex"));
    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    int pageNumberInt = 0;
    String instanceListFilter = CharacterEncoder.getSafeText(request.getParameter("filter"));
    String instanceListOrderBy = CharacterEncoder.getSafeText(request.getParameter("order"));

    String parameters = null;
    final String ENCODING_SCHEME = "UTF-8";

    String resetFilterLink = "list_instances.jsp?pageNumber=0&operation=reset";
    String activeInstancesFilterLink = "list_instances.jsp?pageNumber=0&filter=status%3Dactive&order=%2dlast-active";
    String completedInstanceFilterLink = "list_instances.jsp?pageNumber=0&filter=status%3Dcompleted&order=%2dlast-active";
    String suspendedInstanceFilterLink = "list_instances.jsp?pageNumber=0&filter=status%3Dsuspended&order=%2dlast-active";
    String terminatedInstanceFilterLink = "list_instances.jsp?pageNumber=0&filter=status%3Dterminated&order=%2dlast-active";
    String failedInstanceFilterLink = "list_instances.jsp?pageNumber=0&filter=status%3Dfailed&order=%2dlast-active";

    String deleteInstancesInFilterLink = "list_instances.jsp?pageNumber=0&operation=deleteInstances";

    boolean isAuthenticatedForProcessManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");
    boolean isAuthenticatedForInstanceManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/instances");
    boolean isAuthenticatedForInstanceMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");

    if (isAuthenticatedForInstanceManagement || isAuthenticatedForInstanceMonitor) {
        if(operation != null && operation.equals("reset")){
            instanceFilter = null;
            session.removeAttribute("instanceFilter");
        }

        if(pageNumber == null){
            pageNumber = "0";
        }
        try{
            pageNumberInt = Integer.parseInt(pageNumber);
        } catch (NumberFormatException ignored){

        }


        if (instanceFilter == null) {
            instanceFilter = new InstanceFilter();
            instanceFilter.setAsdec("Ascending");
            instanceFilter.setOrderby("last-active");
        }
        // When handling instance filter, we give the priority to filter URL parameter. If the filter URL
        // parameter is null we use the advance parameter. Also for the order-by filed we give the priority to
        // URL parameter.
        if (instanceListFilter != null && instanceListFilter.length() > 0 && !(" ").equals(instanceListFilter)) {
            String[] stateArray = new String[1];
//            stateArray[0] = instanceListFilter.substring(instanceListFilter.indexOf('=') + 1);
            String[] filterParams = instanceListFilter.split(" ");
            for (String filterParam : filterParams) {
                String[] param = filterParam.split("=");
                if (param[0].equals("pid")) {
                    instanceFilter.setPid(param[1]);
                } else if (param[0].equals("status")) {
                    stateArray[0] = param[1];
                    instanceFilter.setStatus(stateArray);
                }
            }
//            instanceFilter.setStatus(stateArray);
        } //else use the advanced filter

        // TODO: Add instance filter saving support(only for the session)
        if(instanceListOrderBy != null && instanceListOrderBy.length() > 0) {
            if (instanceListOrderBy.startsWith("-")) {
                instanceFilter.setAsdec("Ascending");
            } else {
                instanceFilter.setAsdec("Descending");
            }

            // assumption instanceListOrderBy starts with either "-" or "+"
            instanceFilter.setOrderby(instanceListOrderBy.substring(1));
        }

        instanceListFilter = InstanceFilterUtil.createInstanceFilterStringFromFormData(instanceFilter);
        instanceListOrderBy = InstanceFilterUtil.getOrderByFromFormData(instanceFilter);

        parameters = URLEncoder.encode("filter=" + instanceListFilter + "&order=" + instanceListOrderBy,ENCODING_SCHEME);

        try {
            client = new InstanceManagementServiceClient(cookie, backendServerURL, configContext);
            processManagementClient = new ProcessManagementServiceClient(cookie, backendServerURL,
                                                                         configContext,
                                                                         request.getLocale());
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }

        if(operation != null && isAuthenticatedForInstanceManagement) {
            String iid = CharacterEncoder.getSafeText(request.getParameter("iid"));
            if(iid != null && !operation.equals("reset")) {
                Long instanceId = Long.parseLong(iid.trim());
                if(operation.trim().equals("suspend")) {
                    try {
                        client.suspendInstance(instanceId);
                    } catch(Exception e) {
                        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                        return;
                    }
                } else if(operation.trim().equals("resume")) {
                    try {
                        client.resumeInstance(instanceId);
                    } catch(Exception e) {
                        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                        return;
                    }
                } else if(operation.trim().equals("delete")) {
                    try {
                        if(deleteMex == null) {
                            client.deleteInstance(instanceId, false);
                        } else {
                            client.deleteInstance(instanceId, true);
                        }
                    } catch(Exception e) {
                        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                        return;
                    }
                } else if(operation.trim().equals("terminate")) {
                    try {
                        client.terminateInstance(instanceId);
                    } catch(Exception e) {
                        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                        return;
                    }
                }
            } else if (operation.equals("deleteInstances")) {
                try {
                    int deletedCount = 0;
                    if(deleteMex == null) {
                        deletedCount = client.deleteInstances(instanceListFilter, false);
                    } else {
                        deletedCount = client.deleteInstances(instanceListFilter, true);
                    }
%>
                    <fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
                        <script type="application/javascript">
                            CARBON.showInfoDialog(<% out.print(deletedCount); %> + ' ' + '<fmt:message key="bpel.instance.delete.done" />');
                        </script>
                    </fmt:bundle>
<%
                } catch(Exception e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                    return;
                }
            } else {
                if(!operation.equals("reset")){
                    BpelUIUtil.logWarn("Instance Id is null for operation " + operation + ".", null);
                }
            }
        }

        try{
            processIds = processManagementClient.getAllProcesses();
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }

        try{
            instanceList = client.getPaginatedInstanceList(instanceListFilter, instanceListOrderBy, 200, pageNumberInt);
            numberOfPages = instanceList.getPages();
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
    <carbon:breadcrumb
            label="bpel.instances"
            resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <div id="instance-list-main">
<%
    if (isAuthenticatedForInstanceManagement || isAuthenticatedForInstanceMonitor) {
%>
            <link rel="stylesheet" type="text/css" href="css/bpel_icon_link.css" />
            <script type="text/javascript" src="js/dyndatetime/jquery.dynDateTime.min.js"></script>
            <script type="text/javascript"  src="js/dyndatetime/lang/calendar-en.min.js"></script>
            <script type="text/javascript" src="js/bpel-main.js"></script>
            <style type="text/css">
                table#toolbar tbody tr td{
                    line-height:15px;
                }
                div#workArea table.normal tbody tr td{
                    height:20px;
                }
            </style>
            <script type="text/javascript">
                BPEL.instance.confirm = function(url) {
                    function handleYes() {
                        window.location.href = url;
                    }
                    sessionAwareFunction(function() {
                        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.instances"/>', handleYes);
                    }, "<fmt:message key="session.timed.out"/>");
                    return false;
                };

                BPEL.instance.colourLink = function(filter) {
                    //only the selected link is coloured and others are not re-set, because the request loads the whole new page
                    //so that previously coloured links are no longer exist.
                    switch (filter) {
                        case " ":
                            jQuery("#linkreset").css("display", "none");
                            break;
                        case " status=completed ":
                            jQuery("#linkcompleted").css("color","black");
                            break;
                        case " status=active ":
                            jQuery("#linkactive").css("color","black");
                            break;
                        case " status=terminated ":
                            jQuery("#linkterminated").css("color","black");
                            break;
                        case " status=failed ":
                            jQuery("#linkfailed").css("color","black");
                            break;
                        case " status=suspended ":
                            jQuery("#linksuspended").css("color","black");
                            break;
//                    case " status=error ":
//                        jQuery("#linkerror").css("color","black");
//                        break;
                        default:
                            jQuery("#advance_instance_filter_toggle").css("color","black");

                    }
                };

                jQuery(document).ready(function() {
                    BPEL.instance.colourLink('<%=instanceListFilter%>');
                });
            </script>

            <link rel="stylesheet" type="text/css" href="js/dyndatetime/css/calendar-blue.css" />
           <a id="linkactive"  class="icon-link-nofloat" style="background-image:url(images/bpel-ins-active.gif);" href="retry_instances.jsp"><fmt:message key="retry.page"/></a>
            <h2><fmt:message key="instaces.created"/></h2>
            <div id="workArea">
                <div id="instance-list">
                    <table class="styledLeft" width="100%">
                        <form method="POST" action="list_instances.jsp">
                            <thead>
                                <tr>
                                    <th>
                                        <table id="toolbar" class="normal" style="padding: 0px; width: 100%">
                                            <tbody>
                                                <tr>
                                                    <td style="border-right: 1px solid #CCCCCC; width: 90px;">
                                                        <a id="advance_instance_filter_toggle" href="#" onclick="BPEL.instance.toggleFilter()"><fmt:message key="advanced.filter"/></a>
                                                    </td>
                                                    <td style="border-right: 1px solid #CCCCCC; width: 40px;">
                                                        <a id="linkactive"  class="icon-link-nofloat" style="background-image:url(images/bpel-ins-active.gif);" href="<%=activeInstancesFilterLink%>"><fmt:message key="active"/></a>
                                                    </td>
                                                    <td style="border-right: 1px solid #CCCCCC; width: 70px;">
                                                        <a id="linksuspended" class="icon-link-nofloat" style="background-image:url(images/bpel-ins-suspended.gif);" href="<%=suspendedInstanceFilterLink%>"><fmt:message key="suspended"/></a>
                                                    </td>
                                                    <td style="border-right: 1px solid #CCCCCC; width: 70px;">
                                                        <a id="linkcompleted" class="icon-link-nofloat" style="background-image:url(images/bpel-ins-completed.gif);" href="<%=completedInstanceFilterLink%>"><fmt:message key="completed"/></a>
                                                    </td>
                                                    <td style="border-right: 1px solid #CCCCCC; width: 70px;">
                                                        <a id="linkterminated" class="icon-link-nofloat" style="background-image:url(images/bpel-ins-terminated.gif);" href="<%=terminatedInstanceFilterLink%>"><fmt:message key="terminated"/></a>
                                                    </td>
                                                     <%--<td style="border-right: 1px solid #CCCCCC; width: 35px;">--%>
                                                        <%--<a id="linkerror" href="<%=errorInstanceFilterLink%>"><fmt:message key="error"/></a>--%>
                                                    <%--</td>--%>
                                                    <td style="border-right: 1px solid #CCCCCC; width: 35px;">
                                                        <a id="linkfailed" class="icon-link-nofloat" style="background-image:url(images/bpel-ins-failed.gif);" href="<%=failedInstanceFilterLink%>"><fmt:message key="failed"/></a>
                                                    </td>
                                                    <td id="linkreset" style="border-right: 1px solid #CCCCCC; width: 70px;">
                                                        <a href="<%=resetFilterLink%>"><fmt:message key="reset.filter"/></a>
                                                    </td>
                                                    <td></td>
<%
        if(isAuthenticatedForInstanceManagement && instanceList.getPages() > 0) {
%>
                                                    <td style="border-left: 1px solid #CCCCCC; text-align:right; width: 100px;">
                                                        <a id="linkDeleteInstances" href="<%=deleteInstancesInFilterLink%>" onClick="BPEL.instance.onDeleteInstances();return false;"><fmt:message key="delete.instances"/></a>
                                                    </td>
<%
        }
%>
                                                    <script type="text/javascript">
                                                        BPEL.instance.toggleFilter = function () {
                                                            jQuery("#advanced_instance_filter").slideToggle("10", function() {
                                                                var pattern=/display: block/gi;
                                                                if (jQuery("#advanced_instance_filter").attr("style") != undefined &&
                                                                        jQuery("#advanced_instance_filter").attr("style").match(pattern) != null) {
                                                                    jQuery("#advanced_instance_filter").removeAttr("style");
                                                                }
                                                            });
                                                        };
                                                        jQuery(document).ready(function() {
                                                            jQuery("#starteddate").dynDateTime({
                                                                showsTime: true,
                                                                ifFormat: "%Y-%m-%dT%H:%M"
                                                            });
                                                        });
                                                    </script>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </th>
                                </tr>
                            </thead>
                            <tbody id="advanced_instance_filter" style="display: none;">
                                <tr>
                                    <td>
                                        <table class="normal">
                                            <tbody>
                                                <tr>
                                                    <td>Process</td>
                                                    <td>
                                                        <select name="pid">
                                                            <option value="all" selected>All</option>
<%
            if(processIds != null && processIds.length > 0) {
                Map<String, String> sortedPIDs = new TreeMap<String, String>();
                for(String pid : processIds) {
                    int i = pid.lastIndexOf("}");
                    sortedPIDs.put(pid.substring(i + 1) + "\t" + "|" + "\t" + pid.substring(0, i + 1), pid);
                }
                for(String modifiedPId : sortedPIDs.keySet()) {
                    String pid = sortedPIDs.get(modifiedPId);
                    if(instanceFilter != null && instanceFilter.getPid() != null && instanceFilter.getPid().equals(pid)) {
%>
                                                            <option value="<%=pid%>" selected><%=modifiedPId%></option>
<%
                    } else {
%>
                                                            <option value="<%=pid%>"><%=modifiedPId%></option>
<%
                    }
                }
            } else {
%>
                                                            <option value="noprocesses"><fmt:message key="no.processes"/></option>
<%
            }
%>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><fmt:message key="status"/></td>
<%
            if(instanceFilter != null) {
%>
                                                    <td>
                                                        <input type="checkbox" name="status" value="active" <%=instanceFilter.isActiveStatusSelected()%>>
                                                        <fmt:message key="active"/>
                                                        <input type="checkbox" name="status" value="completed" <%=instanceFilter.isComlpetedSelected()%>>
                                                        <fmt:message key="completed"/>
                                                        <input type="checkbox" name="status" value="suspended" <%=instanceFilter.isSuspendedSelected()%>>
                                                        <fmt:message key="suspended"/>
                                                        <input type="checkbox" name="status" value="terminated" <%=instanceFilter.isTerminatedSelected()%>>
                                                        <fmt:message key="terminated"/>
                                                        <%--<input type="checkbox" name="status" value="error" <%=instanceFilter.isErrorSelected()%>>--%>
                                                        <%--<fmt:message key="error"/>--%>
                                                        <input type="checkbox" name="status" value="failed" <%=instanceFilter.isFailedSelected()%>>
                                                        <fmt:message key="failed"/>
                                                    </td>
<%
            } else {
%>
                                                    <td>
                                                        <input type="checkbox" name="status" value="active">
                                                        <fmt:message key="active"/>
                                                        <input type="checkbox" name="status" value="completed">
                                                        <fmt:message key="completed"/>
                                                        <input type="checkbox" name="status" value="suspended">
                                                        <fmt:message key="suspended"/>
                                                        <input type="checkbox" name="status" value="terminated">
                                                        <fmt:message key="terminated"/>
                                                        <%--<input type="checkbox" name="status" value="error">--%>
                                                        <%--<fmt:message key="error"/>--%>
                                                        <input type="checkbox" name="status" value="failed">
                                                        <fmt:message key="failed"/>
                                                    </td>
<%
            }
%>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <fmt:message key="started"/>
                                                    </td>
                                                    <td>
<%
            if(instanceFilter != null) {
%>
                                                        <input type="radio" name="startedopt" value="onb" <%=instanceFilter.isStartedOnOrBeforeSelected()%>>
                                                        <fmt:message key="on.or.before"/>
                                                        <input type="radio" name="startedopt" value="ona" <%=instanceFilter.isStartedOnOrAfterSelected()%>>
                                                        <fmt:message key="on.or.after"/>
                                                        <input type="text" id="starteddate" size="20" name="starteddate" value="<%=instanceFilter.getStratedDate()%>">
                                                        <script type="text/javascript">
                                                            jQuery(document).ready(function() {
                                                                jQuery("#starteddate").dynDateTime({
                                                                    showsTime: true,
                                                                    ifFormat: "%Y-%m-%dT%H:%M"
                                                                });
                                                            });
                                                        </script>
<%
            } else {
%>
                                                        <input type="radio" name="startedopt" value="onb">
                                                        <fmt:message key="on.or.before"/>
                                                        <input type="radio" name="startedopt" value="ona">
                                                        <fmt:message key="on.or.after"/>
                                                        <input type="text" id="starteddate" size="20" name="starteddate">
                                                        <script type="text/javascript">
                                                            jQuery(document).ready(function() {
                                                                jQuery("#starteddate").dynDateTime({
                                                                    showsTime: true,
                                                                    ifFormat: "%Y-%m-%dT%H:%M"
                                                                });
                                                            });
                                                        </script>
<%
            }
%>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <fmt:message key="last.active1"/>
                                                    </td>
                                                    <td>
<%
            if(instanceFilter != null) {
%>
                                                        <input type="radio" name="ladateopt" value="onb" <%=instanceFilter.isLastActiveOnOrBeforeSelected()%>>
                                                        <fmt:message key="on.or.before"/>
                                                        <input type="radio" name="ladateopt" value="ona" <%=instanceFilter.isLastActiveOnOrAfterSelected()%>>
                                                        <fmt:message key="on.or.after"/>
                                                        <input type="text" id="ladate" size="20" name="ladate" value="<%=instanceFilter.getLastActiveDate()%>">
                                                        <script type="text/javascript">
                                                            jQuery(document).ready(function() {
                                                                jQuery("#ladate").dynDateTime({
                                                                    showsTime: true,
                                                                    ifFormat: "%Y-%m-%dT%H:%M"
                                                                });
                                                            });
                                                        </script>
<%
            } else {
%>
                                                        <input type="radio" name="ladateopt" value="onb">
                                                        <fmt:message key="on.or.before"/>
                                                        <input type="radio" name="ladateopt" value="ona">
                                                        <fmt:message key="on.or.after"/>
                                                        <input type="text" id="ladate" size="20" name="ladate">
                                                        <script type="text/javascript">
                                                            jQuery(document).ready(function() {
                                                                jQuery("#ladate").dynDateTime({
                                                                    showsTime: true,
                                                                    ifFormat: "%Y-%m-%dT%H:%M"
                                                                });
                                                            });
                                                        </script>
<%
            }
%>
                                                    </td>
                                                </tr>
                                            <tr>
                                                <td><fmt:message key="order.by"/></td>
                                                <td>
<%
            if(instanceFilter != null) {
%>
                                                    <input type="radio" name="asdec" value="Descending" <%=instanceFilter.isOrderByDescendingSelected()%>>
                                                    <fmt:message key="ascending"/>
                                                    <input type="radio" name="asdec" value="Ascending" <%=instanceFilter.isOrderByAscendingSelected()%>>
                                                    <fmt:message key="descending"/>
                                                    <select name="orderby">
                                                         <option value="pid" <%=instanceFilter.isOrderByPidSelected()%>>
                                                             <fmt:message key="pid"/>
                                                         </option>
                                                        <option value="status" <%=instanceFilter.isOrderByStatusSelected()%>>
                                                            <fmt:message key="status"/>
                                                        </option>
                                                        <option value="started" <%=instanceFilter.isOrderByStartedDateSelected()%>>
                                                            <fmt:message key="started.date"/>
                                                        </option>
                                                        <option value="last-active" <%=instanceFilter.isOrderByLastActiveDateSelected()%>>
                                                            <fmt:message key="last.active"/>
                                                        </option>
                                                    </select>
<%
            } else {
%>
                                                    <input type="radio" name="asdec" value="Ascending" checked>
                                                    <fmt:message key="ascending"/>
                                                    <input type="radio" name="asdec" value="Descending">
                                                    <fmt:message key="descending"/>
                                                    <select name="orderby">
                                                         <option value="pid">
                                                             <fmt:message key="pid"/>
                                                         </option>
                                                        <option value="status">
                                                            <fmt:message key="status"/>
                                                        </option>
                                                        <option value="started">
                                                            <fmt:message key="started.date"/>
                                                        </option>
                                                        <option value="last-active" selected>
                                                            <fmt:message key="last.active"/>
                                                        </option>
                                                    </select>
<%
            }
%>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td></td>
                                                <td>
                                                    <input type="submit" name="filtersubmit" value="Filter">
                                                    <input type="reset" name="cancelsubmit" value="Cancel">
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </td>
                                </tr>
                            </tbody>
                        </form>
                    </table>
                    <br/>
<%
        if(instanceList != null && instanceList.getInstance() != null) {
%>


                    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                      page="list_instances.jsp" pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%= parameters%>"/>
                    <table id="instanceListTable" class="styledLeft" width="100%">
                        <thead>
                            <tr>
                                <th><nobr><fmt:message key="instance.id"/></nobr></th>
                                <th><nobr><fmt:message key="process.id"/></nobr></th>
                                <th><nobr><fmt:message key="status"/></nobr></th>
                                <th><nobr><fmt:message key="date.started"/></nobr></th>
                                <th><nobr><fmt:message key="last.active"/></nobr></th>
<%
        if(isAuthenticatedForInstanceManagement) {
%>
                                <th colspan="3"><fmt:message key="actions"/></th>
<%
        }
%>
                            </tr>
                        </thead>
                        <tbody>
<%
        for(LimitedInstanceInfoType instanceInfo : instanceList.getInstance()) {
%>
                            <tr>
                                <td><a href="instance_view.jsp?iid=<%=instanceInfo.getIid()%>"><%=instanceInfo.getIid()%></a></td>
<%
        if (isAuthenticatedForProcessManagement || isAuthenticatedForInstanceMonitor) {
%>
                                <td><a href="process_info.jsp?Pid=<%=instanceInfo.getPid()%>"><%=instanceInfo.getPid()%></a></td>
<%
        } else {
%>
                                <td><%=instanceInfo.getPid()%></td>
<%
        }
%>
                                <td><%=instanceInfo.getStatus()%></td>
                                <td><%=instanceInfo.getDateStarted().getTime().toString()%></td>
<%
    if (instanceInfo.getDateLastActive() != null && instanceInfo.getDateLastActive().getTime() != null) {
%>
                                <td><%=instanceInfo.getDateLastActive().getTime().toString()%></td>
<%
    } else {
%>
                                <td></td>
<%
    }

                                QName pid = QName.valueOf(instanceInfo.getPid());

            if(isAuthenticatedForInstanceManagement) {
%>
                <%=BpelUIUtil.getInstanceOperations(instanceInfo.getStatus(), instanceInfo.getIid())%>
<%
        } else {
%>
                                    &nbsp;
<%
        }
%>
                            </tr>
<%
        }
%>
                        </tbody>
                    </table>
                    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                      page="list_instances.jsp" pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%= parameters%>"/>
<%
        } else {
%>
                    <p><fmt:message key="no.process.instances.found" /> </p>
<%
        }
%>
            </div>
          </div>
<%
    } else {
%>
                    <p><fmt:message key="do.not.have.permission.to.view.instance.details" /> </p>
<%
    }
%>

        </div>
    </div>
</fmt:bundle>
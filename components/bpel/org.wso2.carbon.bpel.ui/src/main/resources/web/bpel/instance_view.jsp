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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.InstanceManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.*" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    response.setHeader("Cache-Control",
            "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    InstanceManagementServiceClient client;
    InstanceInfoType instanceInfo = null;
    ScopeInfoType scopeInfo;
    EventInfo[] infoArray = null;
    
    String instanceId = CharacterEncoder.getSafeText(request.getParameter("iid"));
    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String activityId = CharacterEncoder.getSafeText(request.getParameter("aid"));

    Comparator<EventInfo> activityInfoComparator = new Comparator<EventInfo>() {

        public int compare(EventInfo e1, EventInfo e2) {
            return (int) (e1.getActivityId() - e2.getActivityId());
        }
    };

    boolean isAuthenticatedForInstanceManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/instances");
    boolean isAuthenticatedForProcessManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");
    boolean isAuthenticatedForInstanceMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");

    if (isAuthenticatedForInstanceManagement || isAuthenticatedForInstanceMonitor) {
        if (instanceId == null) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg =
                    new CarbonUIMessage(CarbonUIMessage.ERROR, "Instance ID is not available.");
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
    }

    client = new InstanceManagementServiceClient(cookie, backendServerURL, configContext);

    if (isAuthenticatedForInstanceManagement) {
        if(operation != null) {
            Long iid = Long.parseLong(instanceId.trim());
            Long aid = null;
            if (activityId != null) {
                aid = Long.parseLong(activityId.trim());
            }
            //BpelUIUtil.logInfo("Instance ID: " + iid, null);
            if(iid != null) {
                if(operation.trim().equals("suspend")) {
                    try{
                        client.suspendInstance(iid);
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
                    try{
                        client.resumeInstance(iid);
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
                    try{
                        client.deleteInstance(iid, false);
%>
<script type="text/javascript">window.location = "list_instances.jsp"</script>
<%
                        return;
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
                    try{
                        client.terminateInstance(iid);
                    } catch(Exception e) {
                        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                        return;
                    }
                } else if(operation.trim().equals("retry")) {
                    try{
                        if (aid != null) {
                            client.recoverActivity(iid, aid, Action_type1.retry);
%>
<script type="text/javascript">window.location = "instance_view.jsp?iid=<%=iid%>"</script>
<%
                        } else {
                            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, "Activity id is not " +
                                                                        "available to retry the activity");
                            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                            return;
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
                } else if(operation.trim().equals("cancel")) {
                    try{
                        if (aid != null) {
                            client.recoverActivity(iid, aid, Action_type1.cancel);
%>
<script type="text/javascript">window.location = "instance_view.jsp?iid=<%=iid%>"</script>
<%
                        } else {
                            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, "Activity id is not " +
                                                                        "available to cancel activity retry");
                            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                            return;
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
                }
            } else {
                BpelUIUtil.logWarn("Instance Id is null for operation " + operation + ".", null);
            }
        }
    }

    if (isAuthenticatedForInstanceManagement || isAuthenticatedForInstanceMonitor) {
        try {
            instanceInfo = client.getInstanceInfo(Long.parseLong(instanceId));
            //client.getInstanceInfoWithEvents(Long.parseLong(instanceId));
            ActivityLifeCycleEventsType events = client.getActivityLifeCycleFilter(Long.parseLong(instanceId));
            ActivityLifeCycleEventsListType eventList = events.getEventInfoList();
            if (eventList != null) {
                infoArray = eventList.getEventInfo();
            } else {
                infoArray = null;
            }

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
<carbon:breadcrumb
        label="bpel.instance.view"
        resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<div id="middle">
    <div id="instance-view-main">
        <h2><fmt:message key="bpel.instance.view"/></h2>
        <div id="workArea">
            <div id="instance-view">
<%
    if (instanceInfo != null &&
        (isAuthenticatedForInstanceManagement || isAuthenticatedForInstanceMonitor)) {
%>
                <link rel="stylesheet" type="text/css" href="css/bpel_icon_link.css" />
                <script type="text/javascript" src="js/instance_view.js"></script>
                <script type="text/javascript" src="js/bpel-main.js"></script>
<%
    if (isAuthenticatedForInstanceManagement) {
%>
<script type="text/javascript" src="js/instance_view_management_actions.js"></script>
<script type="text/javascript">
    BPEL.instance.resumeInstance = function(iid) {
        function resumeYes() {
            window.location.href = "instance_view.jsp?operation=resume&iid=" + iid;
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.resume.instance"/> ' + iid + "?", resumeYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };

    BPEL.instance.suspendInstance = function(iid) {
        function suspendYes() {
            window.location.href = "instance_view.jsp?operation=suspend&iid=" + iid;
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.suspend.instance"/> ' + iid + "?", suspendYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };

    BPEL.instance.terminateInstance = function(iid) {
        function terminateYes() {
            window.location.href = "instance_view.jsp?operation=terminate&iid=" + iid;
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.terminate.instance"/> ' + iid + "?", terminateYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };

    BPEL.instance.deleteInstance = function(iid) {
        function deleteYes() {
            window.location.href = "instance_view.jsp?operation=delete&iid=" + iid;
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.instance"/> ' + iid + "?", deleteYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };

    BPEL.instance.retryActivity = function(iid, aid) {
        function retryYes() {
            window.location.href = "instance_view.jsp?operation=retry&iid=" + iid + "&aid=" + aid;
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.retry.activity"/> ' + aid + '?', retryYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };

    BPEL.instance.cancelRetry = function(iid, aid) {
        function cancelYes() {
            window.location.href = "instance_view.jsp?operation=cancel&iid=" + iid + "&aid=" + aid;
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.cancel.activity"/> ' + aid + '?', cancelYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };

    BPEL.instance.faultActivity = function(iid, aid) {
        function faultYes() {
            window.location.href = "instance_view.jsp?operation=fault&iid=" + iid + "&aid=" + aid;
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.fault.activity"/> ' + aid + '?', faultYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };
</script>
<%
    }
%>

                <table width="100%">
                    <strong style="padding-bottom:5px;margin-bottom:5px;margin-top:10px;"><fmt:message
                        key="instance.information"/></strong>
                    <tr>
                        <td>
                            <table class="styledLeft" id="instanceStatic">
                                <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="instance.details"/></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td><fmt:message key="instance.id"/></td>
                                        <td><%=instanceInfo.getIid()%>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="process.id"/></td>
<%
    if (isAuthenticatedForInstanceMonitor || isAuthenticatedForProcessManagement) {
%>
                                        <td>
                                            <a href="process_info.jsp?Pid=<%=instanceInfo.getPid()%>"><%=instanceInfo.getPid()%>
                                            </a>
                                        </td>
<%
    } else {
%>
                                        <td>
                                            <%=instanceInfo.getPid()%>
                                        </td>
<%
    }
%>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="status"/></td>
                                        <td><%=instanceInfo.getStatus()%>
                                            <%
                                                if (isAuthenticatedForInstanceManagement) {
                                                    if (instanceInfo.getStatus().getValue().toUpperCase().equals("ACTIVE")) {
                                            %>
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/suspend.gif);" onclick="BPEL.instance.suspendInstance(<%=instanceInfo.getIid()%>);"><fmt:message
                                                    key="suspend"/></a>
                                            &nbsp;]
                                            &nbsp;
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/terminate.gif);"
                                               onclick="BPEL.instance.terminateInstance(<%=instanceInfo.getIid()%>);"><fmt:message
                                                    key="terminate"/></a>
                                            &nbsp;]
                                            &nbsp;
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/delete.gif);" onclick="BPEL.instance.deleteInstance(<%=instanceInfo.getIid()%>);"><fmt:message
                                                    key="delete"/></a>
                                            &nbsp;]
                                            <%
                                                } else if (instanceInfo.getStatus().getValue().toUpperCase().equals("COMPLETED") ||
                                                        instanceInfo.getStatus().getValue().toUpperCase().equals("ERROR") ||
                                                        instanceInfo.getStatus().getValue().toUpperCase().equals("FAILED")) {
                                                %>
                                            &nbsp;
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/delete.gif);" onclick="BPEL.instance.deleteInstance(<%=instanceInfo.getIid()%>);"><fmt:message
                                                    key="delete"/></a>
                                            &nbsp;]
                                            <%
                                                } else  if (instanceInfo.getStatus().getValue().toUpperCase().equals("TERMINATED")) {
                                            %>
                                            &nbsp;
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/delete.gif);" onclick="BPEL.instance.deleteInstance(<%=instanceInfo.getIid()%>);"><fmt:message
                                                    key="delete"/></a>
                                            &nbsp;]
                                            <%
                                                } else if (instanceInfo.getStatus().getValue().toUpperCase().equals("SUSPENDED")) {
                                            %>
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/resume.gif);" onclick="BPEL.instance.resumeInstance(<%=instanceInfo.getIid()%>);"><fmt:message
                                                    key="resume"/></a>
                                            &nbsp;]
                                            &nbsp;
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/terminate.gif);"
                                               onclick="BPEL.instance.terminateInstance(<%=instanceInfo.getIid()%>);"><fmt:message
                                                    key="terminate"/></a>
                                            &nbsp;]
                                            &nbsp;
                                            [&nbsp;
                                            <a href='#' class="bpel-icon-link" style="background-image:url(images/delete.gif);" onclick="BPEL.instance.deleteInstance(<%=instanceInfo.getIid()%>);">
                                                <fmt:message key="delete"/></a>
                                            &nbsp;]
                                            <%
                                                    }
                                                }
                                            %>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="date.started"/></td>
                                        <td><%=instanceInfo.getDateStarted().getTime().toString()%>
                                        </td>
                                    </tr>
                                    <%
                                        if (instanceInfo.getDateLastActive() != null) {
                                    %>
                                    <tr>
                                        <td><fmt:message key="last.active.date"/></td>
                                        <td><%=instanceInfo.getDateLastActive().getTime().toString()%>
                                        </td>
                                    </tr>
                                 <%
                                    }
                                    scopeInfo = instanceInfo.getRootScope();
                                    if (scopeInfo != null && scopeInfo.getCorrelationSets() != null &&
                                            scopeInfo.getCorrelationSets().getCorrelationSet() != null &&
                                            scopeInfo.getCorrelationSets().getCorrelationSet().length > 0 &&
                                            scopeInfo.getCorrelationSets().getCorrelationSet()[0] != null) {
                                %>
                                <tr>
                                    <td colspan="2"><strong><fmt:message
                                            key="correlation.properties"/> <%=scopeInfo.getCorrelationSets().getCorrelationSet().length%>
                                    </strong></td>
                                </tr>
                                <%
                                    for (CorrelationSet_type0 coSet : scopeInfo.getCorrelationSets().getCorrelationSet()) {
                                        if (coSet.getCorrelationProperty() != null) {
                                            for (CorrelationPropertyType coProp : coSet.getCorrelationProperty()) {
                                                String propName = coProp.getPropertyName().toString();
                                                String propVal = coProp.getString();
                                %>
                                <tr>
                                    <td><%=propName%>
                                    </td>
                                    <td><%=propVal%>
                                    </td>
                                </tr>
                                <%
                                                }
                                            }
                                        }

                                    }

                                    if (instanceInfo.getDateErrorSince() != null) {
                                %>
                                <tr>
                                    <td><fmt:message key="error.since"/></td>
                                    <td><%=instanceInfo.getDateErrorSince().getTime().toString()%>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                </tbody>
                            </table>
                        </td>
                        <td>
<%
    //TODO fill activity and scope info

    if (scopeInfo != null) {
%>
                            <table class="styledLeft" id="tableRootScope">
                                <thead>
                                <tr>
                                    <th colspan="2"><fmt:message key="root.scope.information"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td><fmt:message key="scope.id"/></td>
                                    <td><%=instanceInfo.getRootScope().getSiid()%>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="scope.name"/></td>
                                    <td><%=instanceInfo.getRootScope().getName()%>
                                    </td>
                                </tr>
                                <%
                                    //Display error/exception details for failed/terminated processes
                                    String instanceStatus = instanceInfo.getRootScope().getStatus().getValue();
                                    String errorMessage = null;
                                    int errorLineNumber = 0;
                                    QName errorName = null;
                                    int errorLine = 0;
                                    boolean isError = false;
                                    String dataValue = null;
                                    if (instanceStatus == "FAULTED" || instanceStatus == "TERMINATED") {
                                        if (instanceInfo.getFaultInfo() != null) {
                                            isError = true;
                                            errorMessage = instanceInfo.getFaultInfo().getExplanation();
                                            errorLine = instanceInfo.getFaultInfo().getLineNumber();
                                            if (instanceInfo.getFaultInfo().getData() != null) {
                                                for (OMElement omEle : instanceInfo.getFaultInfo().getData().getExtraElement()) {
                                                    if (dataValue == null) {
                                                        dataValue = omEle.toString();
                                                    } else {
                                                        dataValue += omEle.toString();
                                                    }
                                                }
                                            }
                                            errorName = instanceInfo.getFaultInfo().getName();
                                        }
                                    }
                                %>
                                <tr>
                                    <td><fmt:message key="status"/></td>
                                    <td>
                                    	<span><%=instanceStatus%></span>
                                    <%
                                        if (isError) {
                                        %>
                                        <a href="#" class="icon-link" style="float:none;margin-top:0;background-image:url(images/view.gif);"
                                           onclick="showVariableValue('error_dialog_HiddenField');">View Explanation</a>
                                        <div id='error_dialog_HiddenFieldParent'>
                                            <div id='error_dialog_HiddenField' title='Fault Explanation'
                                                 style="display: none;">
                                                <table class="styledLeft">
                                                    <tbody>
                                                    <% if (errorName != null) { %>
                                                    <tr>
                                                        <td style="padding-right: 10px;">Fault Name</td>
                                                        <td style="padding-bottom: 20px;"><%=errorName.toString()%>
                                                        </td>
                                                    </tr>
                                                    <% }
                                                        if (errorMessage != null) { %>
                                                    <tr>
                                                        <td style="padding-right: 10px;">Fault Message</td>
                                                        <td style="padding-bottom: 20px;"><pre><%=BpelUIUtil.encodeHTML(BpelUIUtil.prettyPrint(errorMessage))%></pre></td>
                                                    </tr>
                                                    <% }
                                                        if (dataValue != null) { %>
                                                    <tr>
                                                        <td style="padding-right: 10px;">Fault Data</td>
                                                        <td style="padding-bottom: 20px;"><pre><%=BpelUIUtil.encodeHTML(BpelUIUtil.prettyPrint(dataValue))%></pre></td>
                                                    </tr>
                                                    <% }
                                                        if (errorLine > 0 ) { %>
                                                    <tr>
                                                        <td style="padding-right: 10px;">Error Line</td>
                                                        <td style="padding-bottom: 20px;"><%=errorLine%></td>
                                                    </tr>
                                                    <% } %>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                        <%
                                            }
                                        %>
                                        

                                        
                                        
                                    </td>
                                </tr>
                                <%
                                    Variables_type0 vars = scopeInfo.getVariables();
                                    if (vars.getVariableInfo() != null && vars.getVariableInfo().length > 0) {
                                %>
                                <tr>
                                    <td colspan="2"><strong><fmt:message key="variables"/></strong></td>
                                </tr>
                                <%
        //                            PrettyXML.init();
                                    for (VariableInfoType varInfo : vars.getVariableInfo()) {
                                %>
                                <tr>
                                    <td><%=varInfo.getSelf().getName()%>
                                    </td>
                                    <%
                                        String varStr = "<div>Nil</div>";
                                        if (varInfo.getValue() != null && varInfo.getValue().getExtraElement() != null
                                                && varInfo.getValue().getExtraElement()[0] != null) {
                                            String id = varInfo.getSelf().getName() + "_HiddenField";
                                            String varValue = null;
                                            for (OMElement omEle : varInfo.getValue().getExtraElement()) {
                                                if (varValue == null) {
                                                    varValue = omEle.toString();
                                                } else {
                                                    varValue += omEle.toString();
                                                }
                                            }
                                            boolean isEmpty = false;
                                            if (varValue == null || varValue.length() == 0 || varValue.equals("Nil")) {
                                                varStr = "<div>Nil</div>";
                                                isEmpty = true;
                                            } else if (varValue.equals("<empty-value>Nil</empty-value>")) {
                                                isEmpty = true;
                                            } else {
                                                //TODO pretty print here
        ////                                        XMLPrettyPrinter xmlPP = new XMLPrettyPrinter(new ByteArrayInputStream(varValue.getBytes()));
        ////                                        varStr = BpelUIUtil.encodeHTML(xmlPP.xmlFormat());
        //                                        XMLPrettyPrinter xmlPP = new XMLPrettyPrinter(new ByteArrayInputStream(varValue.getBytes()));
        //                                        String ppedStr = xmlPP.xmlFormat();
        //                                        StringReader sr = new StringReader(ppedStr);
        //                                        StringWriter sw = new StringWriter();
        //                                        PrettyXML.writeFancily(sr, sw);
        //                                        sr.close();
        //                                        varStr = sw.toString();
                                                try {
                                                    //read the max variable length from configuration
                                                    int maxSize = client.getInstanceViewVariableLength() * 1000;
                                                    varStr = BpelUIUtil.encodeHTML(BpelUIUtil
                                                            .truncateString(BpelUIUtil.prettyPrint(varValue), maxSize));
                                                } catch (Exception e) {

                                                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                                                    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR,
                                                            e.getMessage(), e);
                                                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
                                                }
        //                                        varStr = varStr.replaceAll("\n", "<br/>");

                                            }
                                    %>
                                    <td>
                                        <%
                                        if (isEmpty) {
                                        %>
                                        <div>empty value</div>
                                        <%
                                        } else {
                                        %>
                                        <div id='dialog<%=id%>Parent'>
                                            <div id='dialog<%=id%>' title='Value of <%=varInfo.getSelf().getName()%>'
                                                 style="display: none;"><pre><%=varStr%></pre>
                                            </div>
                                        </div>

                                        <a href="#" class="icon-link" style="background-image:url(images/view.gif);"
                                           onclick="showVariableValue('dialog<%=id%>');">View Value</a>
                                        <%
                                            }
                                        %>
                                    </td>
                                    <%

                                    } else {
                                    %>
                                    <td><fmt:message key="error.occurres.while.getting.the.value"/></td>
                                    <%
                                        }
                                    %>
                                </tr>
                                <%
                                        }
                                    }
                                    %>
                                </tbody>
                            </table>

                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <strong style="padding-bottom:5px;margin-bottom:5px;margin-top:10px;"><fmt:message
                                    key="activity.information"/></strong>
<%
    if (infoArray != null) {
%>
                            <table class="styledLeft" id="tableActivityInfo">
                                <thead>
                                <tr>
                                    <th><fmt:message key="activity.id"/></th>
                                    <th><fmt:message key="activity.name"/></th>
                                    <th><fmt:message key="activity.type"/></th>
                                    <th><fmt:message key="scope.id"/></th>
                                    <th><fmt:message key="scope.name"/></th>
                                    <th><fmt:message key="event.type"/></th>
                                    <th><fmt:message key="timestamp"/></th>
                                    <th><fmt:message key="line.number"/></th>
                                    <th><fmt:message key="event.name"/></th>
                                    <%
                                        if (isAuthenticatedForInstanceManagement &&
                                                instanceInfo.getStatus().getValue().toUpperCase().equals("ACTIVE")) {
                                    %>
                                    <th><fmt:message key="activity.actions"/></th>
                                    <%
                                        }
                                    %>

                                </tr>
                                </thead>
                                <tbody>
<%
	Arrays.sort(infoArray, activityInfoComparator);
        for (int i = 0; i < infoArray.length; i++) {
            EventInfo info = infoArray[i];


        //for(EventInfo info : infoArray) {
%>
                                    <tr onmouseover="highLightRows('<%=info.getActivityId()%>')">
                                        <td class="activity-id"><%=info.getActivityId()%></td>
                                        <td><%=info.getActivityName()%></td>
                                        <td style="background-image: url('images/<%=info.getActivityType()%>.gif'); background-repeat: no-repeat; padding-left: 25px ! important; background-position: 3px 50%;"><%=info.getActivityType()%></td>
                                        <td title="<%=info.getScopeName()%>"><%=info.getScopeId()%></td>
                                        <td><%=info.getScopeName()%></td>
                                        <td><%=info.getType()%></td>
                                        <td><%=info.getTimestamp().getTime().toString()%></td>
                                        <td><%=info.getLineNumber()%></td>
                                        <td><%=info.getName()%></td>
                                        <%
                                            if (isAuthenticatedForInstanceManagement) {
                                                if (instanceInfo.getStatus().getValue().toUpperCase().equals("ACTIVE") &&
                                                    info.getName().equals("ActivityFailureEvent") &&
                                                    info.getIsRecoveryRequired()) {
                                        %>
                                            <td>
                                                [
                                                <a id="<%=instanceInfo.getIid() + info.getActivityId()%>" class="bpel-icon-link" style="background-image:url(images/reset.gif);" href='#<%=instanceInfo.getIid() + info.getActivityId()%>' onclick="BPEL.instance.retryActivity(<%=instanceInfo.getIid()%>, <%=info.getActivityId()%>);"><fmt:message key="retry"/></a>
                                                ]
                                                &nbsp;
                                                [
                                                <a id="<%=instanceInfo.getIid() + info.getActivityId()%>" class="bpel-icon-link" style="background-image:url(images/resume.gif);" href='#<%=instanceInfo.getIid() + info.getActivityId()%>' onclick="BPEL.instance.cancelRetry(<%=instanceInfo.getIid()%>, <%=info.getActivityId()%>);"><fmt:message key="cancel"/></a>
                                                ]
                                            </td>
                                        <%
                                                } else if (instanceInfo.getStatus().getValue().toUpperCase().equals("ACTIVE")) {
                                        %>
                                            <td></td>
                                        <%
                                                }
                                            }
                                        %>
                                    </tr>

<%
        //}
        }
%>
                                </tbody>
                            </table>
<%
    } else {
%>
                        <table class="styledLeft" id="tableActivityInfo">
                                <thead>
                        <tr><td>Event information are not available for this instance</td></tr>
                            </thead>
                            </table>
<%
    }
%>
        </td>
    </tr>
<%--<tr>
    <td colspan="3">
        <table class="styledLeft" id="bpiProcessInstanceVisualizationTable"
               style="margin-left: 0px;" width="100%">
            <thead>
            <tr>
                <th align="left"><fmt:message key="process.instance.visualization"/></th>
            </tr>
            </thead>
            <tr>
                <td>
                    <%
                        if (instanceInfo.getIsEventsEnabled()) {
                    %>
                    <jsp:include page="visualizer/instance_visualisation.jsp">
                        <jsp:param name="iid" value="<%=instanceId%>"/>
                    </jsp:include>
                    <%
                        } else {
                    %>
                    <p><fmt:message key="cannot.display.when.events.disabled"/><a href="deployment_descriptor_editor.jsp?Pid=<%=instanceInfo.getPid()%>">&nbsp;here.</a></p>
                    <%
                        }
                    %>
                </td>
            </tr>
        </table>
    </td>
</tr> --%>
<%
        } //no activities found
    } else { //else no rootscope available, due to clean-up of variable info
        if (!(isAuthenticatedForInstanceManagement || isAuthenticatedForInstanceMonitor)) {
%>
                    <p><fmt:message key="do.not.have.permission.to.view.instance.details"/></p>
<%
        }
%>
                    <p><fmt:message key="bpel.instance.details.not.available"/></p>
<%
    }
%>
                </table>
            </div>
        </div>
    </div>
</div>
<style type="text/css">
    .selected {
        background-color: #EDEDED;
    }
.ui-dialog-container{
  background-color:#ccc;
}
.ui-dialog-content{
width:100% !important;
height:85% !important;
overflow:scroll;
background:#fff;
}
</style>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript">
    function highLightRows(activityID) {
        clearClass("tableActivityInfo");
        var tds = YAHOO.util.Dom.getElementsByClassName('activity-id');

        for (var i = 0; i < tds.length; i++) {
            if (tds[i].innerHTML == activityID) {
                var theTr = tds[i].parentNode;
                YAHOO.util.Dom.addClass(theTr, 'selected');
            }
        }
    }
    function clearClass(elementID) {
        var theTable = document.getElementById(elementID);
        for (var i = 0; i < theTable.rows.length; i++) {
            YAHOO.util.Dom.removeClass(theTable.rows[i], 'selected');
        }
    }
</script>
</fmt:bundle>

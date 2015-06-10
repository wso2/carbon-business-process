<!--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.*" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.*" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.InstanceFilterUtil" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.wso2.carbon.bpel.ui.InstanceFilter" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="java.net.URLEncoder" %>
<jsp:useBean id="instanceFilter" scope="session" class="org.wso2.carbon.bpel.ui.InstanceFilter"/>
<jsp:setProperty name="instanceFilter" property="*"/>
<%
    InstanceManagementServiceClient client;
    PaginatedInstanceList instanceList = null;
    response.setHeader("Cache-Control",
            "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    int pageNumberInt = 0;
    int numberOfPages=0;
    String parameters = null;
    Boolean failedActivityExists = false; // flag to check if there are any failed activities
    final String ENCODING_SCHEME = "UTF-8";
    String instanceListFilter;
    String instanceListOrderBy;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
%>
<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
<carbon:breadcrumb
        label="Retry failed activities"
        resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<link rel="stylesheet" type="text/css" href="css/bpel_icon_link.css"/>
<script type="text/javascript" src="js/instance_view.js"></script>
<script type="text/javascript" src="js/bpel-main.js"></script>
<script type="text/javascript">

    BPEL.instance.retryActivity = function (iid, aid) {

        function retryYes() {
            window.location.href = "instance_view.jsp?operation=retry&iid=" + iid + "&aid=" + aid;
        }

        sessionAwareFunction(function () {
            CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.retry.activity"/> ' + aid + '?', retryYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };

    BPEL.instance.cancelRetry = function (iid, aid) {
        function cancelYes() {
            window.location.href = "instance_view.jsp?operation=cancel&iid=" + iid + "&aid=" + aid;
        }

        sessionAwareFunction(function () {
            CARBON.showConfirmationDialog('<fmt:message key ="do.you.want.to.cancel.activity"/>' + aid + '?', cancelYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };
</script>
<%
    boolean isAuthenticatedForInstanceManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/instances");
    boolean isAuthenticatedForInstanceMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");
    if (pageNumber == null || pageNumber.isEmpty()) {
        pageNumber = "0";
    }
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException e) {
        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
    }
    instanceFilter = new InstanceFilter();
    instanceFilter.setAsdec("Ascending");
    instanceFilter.setOrderby("last-active");
    instanceListFilter = InstanceFilterUtil.createInstanceFilterStringFromFormData(instanceFilter);
    instanceListOrderBy = InstanceFilterUtil.getOrderByFromFormData(instanceFilter);
    parameters = URLEncoder.encode("filter=" + instanceListFilter + "&order=" + instanceListOrderBy, ENCODING_SCHEME);
    client = new InstanceManagementServiceClient(cookie, backendServerURL, configContext);
%>
<%
    try {
        instanceList = client.getPaginatedInstanceList(instanceListFilter, instanceListOrderBy, 200, pageNumberInt);
        numberOfPages = instanceList.getPages();
        if (numberOfPages < 1) {
            numberOfPages = 0;
        }
    } catch (Exception e) {
        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    if (isAuthenticatedForInstanceManagement || isAuthenticatedForInstanceMonitor) {
        if (instanceList != null && instanceList.getInstance() != null) { %>

<div id="workArea">
    <strong style="padding-bottom:5px;margin-bottom:5px;margin-top:10px;"><fmt:message
            key="activity.failure"/></strong>
    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                      page="retry_instances.jsp" pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%= parameters%>"/>
    <table class="styledLeft">
        <thead>
        <tr>
            <th><fmt:message key="instance.id"/></th>
            <th><fmt:message key="activity.id"/></th>
            <th><fmt:message key="timestamp"/></th>
            <th><fmt:message key="details"/></th>
            <th><fmt:message key="retries"/></th>
            <%
                if (isAuthenticatedForInstanceManagement) {
            %>
            <th colspan="3"><fmt:message key="recovery.actions"/></th>
            <%
                }
            %>
        </tr>
        </thead>
        <%
            for (LimitedInstanceInfoType instanceInfo : instanceList.getInstance()) {

                if (isAuthenticatedForInstanceManagement && instanceInfo.getStatus().getValue().toUpperCase().equals("ACTIVE")) {
                    try {

                        ActivityRecoveryInfoType[] failedActivities = client.getFailedActivities(Long.parseLong(instanceInfo.getIid()));
                        if (failedActivities != null && failedActivities.length > 0) {
                            ActivityRecoveryInfoType failedActivity;
                            failedActivityExists = true;
        %>
        <tbody>
        <%
            for (int i = 0; i < failedActivities.length; i++) {
                failedActivity = failedActivities[i];
        %>
        <tr>
            <td><a href="instance_view.jsp?iid=<%=instanceInfo.getIid()%>"><%=instanceInfo.getIid()%>
            </a></td>
            <td><%=failedActivity.getActivityID()%>
            </td>
            <td><%=failedActivity.getDateTime()%>
            </td>
            <td style="width: 50%"><%=failedActivity.getReason()%>
            </td>
            <td><%=failedActivity.getRetires()%>
            </td>
            <td>
                [
                <a id="<%=instanceInfo.getIid() + failedActivity.getActivityID()%>" class="bpel-icon-link"
                   style="background-image:url(images/reset.gif);"
                   href='#<%=instanceInfo.getIid() + failedActivity.getActivityID()%>'
                   onclick="BPEL.instance.retryActivity(<%=instanceInfo.getIid()%>, <%=failedActivity.getActivityID()%>);"><fmt:message
                        key="retry"/></a>
                ]
                <br>
                [
                <a id="<%=instanceInfo.getIid() + failedActivity.getActivityID()%>" class="bpel-icon-link"
                   style="background-image:url(images/resume.gif);"
                   href='#<%=instanceInfo.getIid() + failedActivity.getActivityID()%>'
                   onclick="BPEL.instance.cancelRetry(<%=instanceInfo.getIid()%>, <%=failedActivity.getActivityID()%>);"><fmt:message
                        key="cancel"/></a>
                ]
            </td>
        </tr>

        <%
            }
        %>
        <div>&nbsp;</div>
        <%
            }
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
            }%>

        </tbody>
    </table>
    <br>
    <%if (!failedActivityExists) {%>
    <fmt:message key="no.failed.instances"/>
    <%
            }

        }
    } else {
    %>
    <p><fmt:message key="do.not.have.permission.to.view.instance.details"/></p>
    <% } %>

    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                      page="retry_instances.jsp" pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%= parameters%>"/>
</div>
<style type="text/css">
    .selected {
        background-color: #EDEDED;
    }

    .ui-dialog-container {
        background-color: #ccc;
    }

    .ui-dialog-content {
        width: 100% !important;
        height: 85% !important;
        overflow: scroll;
        background: #fff;
    }
</style>
</fmt:bundle>


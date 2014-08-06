<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.humantask.stub.mgt.types.DeployedTaskDefinitionsPaginated" %>
<%@ page import="org.wso2.carbon.humantask.ui.clients.HumanTaskPackageManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.humantask.stub.mgt.types.TaskDefinition_type0" %>
<%@ page import="org.wso2.carbon.humantask.stub.mgt.types.UndeployStatus_type0" %>
<%@ page import="org.wso2.carbon.humantask.stub.mgt.types.TaskStatusType" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<!--
~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

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
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    HumanTaskPackageManagementServiceClient htPackageMgtClient = null;
    DeployedTaskDefinitionsPaginated taskDefinitionsPaginated = null;
    String parameters = "";
    int numberOfPages = 0;
    int pageNumberInt = 0;
    int linkNum = 0;

    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String packageName = CharacterEncoder.getSafeText(request.getParameter("packageName"));


    boolean isAuthorizedToManagePackages =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/humantask/packages");
    boolean isAuthorizedToMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/humantask");


    if (isAuthorizedToManagePackages || isAuthorizedToMonitor) {

        try {
            htPackageMgtClient = new HumanTaskPackageManagementServiceClient(cookie, backendServerURL, configContext);
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

    // unDeploy
    if (isAuthorizedToManagePackages && operation != null && packageName != null &&
        operation.equals("unDeploy")) {
        try {

            UndeployStatus_type0 unDeployStatus = htPackageMgtClient.unDeployPackage(packageName);
            if (UndeployStatus_type0.FAILED.equals(unDeployStatus)) {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                CarbonUIMessage uiMsg = new CarbonUIMessage(
                        CarbonUIMessage.ERROR,
                        "HumanTask package " + packageName + " unDeployment failed.",
                        null);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
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


    if (pageNumber == null) {
        pageNumber = "0";
    }
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    try {
        taskDefinitionsPaginated = htPackageMgtClient.getPaginatedTaskDefinitions(pageNumberInt);
        numberOfPages = taskDefinitionsPaginated.getPages();
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


<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">
    <carbon:breadcrumb
            label="humantask.deployed.tasks"
            resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <jsp:include page="../dialog/display_messages.jsp"/>
    <div id="middle">

        <div id="task-list-main">
            <h2><fmt:message key="humantask.deployed.tasks"/></h2>

            <div id="workArea">
                <div id="task-def-list">

                    <%
                        if (isAuthorizedToManagePackages || isAuthorizedToMonitor) {
                            if (taskDefinitionsPaginated != null && taskDefinitionsPaginated.getTaskDefinition() != null &&
                                taskDefinitionsPaginated.getTaskDefinition().length > 0) {
                    %>
                    <carbon:paginator pageNumber="<%=pageNumberInt%>"
                                      numberOfPages="<%=numberOfPages%>"
                                      page="humantask_definition_list.jsp"
                                      pageNumberParameterName="pageNumber"
                                      resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
                                      prevKey="prev" nextKey="next"
                                      parameters="<%= parameters%>"/>

                    <table id="taskConfigurationList" class="styledLeft" width="100%">

                        <thead>
                        <tr>
                            <th><fmt:message key="humantask.package.name"/></th>
                            <th><fmt:message key="humantask.task.name"/></th>
                            <th><fmt:message key="humantask.taskview.type"/></th>
                            <th><fmt:message key="humantask.task.status"/></th>
                        </tr>
                        </thead>

                        <tbody>

                        <%
                            for (TaskDefinition_type0 taskDef : taskDefinitionsPaginated.getTaskDefinition()) {
                        %>
                        <tr>
                            <td>
                                <%
                                    if(!TaskStatusType.UNDEPLOYING.equals(taskDef.getState())) {
                                %>

                                <a href="humantask_package_dashboard.jsp?packageName=<%=taskDef.getPackageName()%>"><%=taskDef.getPackageName()%>
                                </a>

                                <%
                                    } else {
                                %>
                                    <%=taskDef.getPackageName()%>
                                <%
                                    }
                                %>

                            </td>
                            <td>

                                <%
                                    if(!TaskStatusType.UNDEPLOYING.equals(taskDef.getState())) {
                                %>

                                <a href="./task_definition_info.jsp?taskDefId=<%=taskDef.getTaskName()%>"><%=taskDef.getTaskName()%>
                                </a>

                                <%
                                    } else {
                                %>
                                    <%=taskDef.getTaskName()%>
                                <%
                                    }
                                %>
                            </td>

                            <td>
                                <%=taskDef.getType().toString()%>
                            </td>

                            <td>
                                <%=taskDef.getState().toString()%>
                            </td>
                        </tr>
                        <%
                            }
                        %>

                        </tbody>


                    </table>
                    <carbon:paginator pageNumber="<%=pageNumberInt%>"
                                      numberOfPages="<%=numberOfPages%>"
                                      page="humantask_definition_list.jsp"
                                      pageNumberParameterName="pageNumber"
                                      resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
                                      prevKey="prev" nextKey="next"
                                      parameters="<%=parameters%>"/>

                    <%
                    } else {
                    %>
                    <p><fmt:message
                            key="humantask.there.are.no.deployed.task.configurations"/></p>
                    <%
                        }
                    } else {
                    %>
                    <p><fmt:message
                            key="humantask.do.not.have.permission.to.view.task.details"/></p>
                    <%
                        }
                    %>


                </div>
            </div>

        </div>
    </div>

</fmt:bundle>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.humantask.stub.mgt.types.Task_type0" %>
<%@ page import="org.wso2.carbon.humantask.ui.clients.HumanTaskPackageManagementServiceClient" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
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
        Task_type0[] taskDefinitionsInPackage = null;


        String packageName = CharacterEncoder.getSafeText(request.getParameter("packageName"));
        String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
        boolean isErroneous = false;

        boolean isAuthorizedToManagePackages =
                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/humantask/packages");
        boolean isAuthorizedToMonitor =
                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/humantask");

        if (isAuthorizedToMonitor || isAuthorizedToManagePackages) {
            try {
                htPackageMgtClient = new HumanTaskPackageManagementServiceClient(cookie, backendServerURL,
                                                                                 configContext);
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


        //The package name cannot be null or empty. .
        if (packageName == null || packageName.isEmpty()) {

            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, "The package name cannot be null or empty");
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
    <jsp:include page="../admin/error.jsp"/>
    <%
            return;

        }

        // We have to retrieve the list of task definitions under the package to display the list.
        if (isAuthorizedToManagePackages) {
            try {
                taskDefinitionsInPackage = htPackageMgtClient.listTasksInPackage(packageName);
               isErroneous =  HumanTaskUIUtil.isErroneous(taskDefinitionsInPackage);
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
        label="humantask.package.info"
        resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>
    <div id="middle">
        <div id="package-list-main">
            <h2><fmt:message key="humantask.headertext_package_dashboard"/> (<%=packageName%>)</h2>

            <div id="workArea">
                <div id="humantask-package-dashboard">

                    <%
                        if (isAuthorizedToManagePackages) {
                    %>
                    <table id="packageActionTable" class="styledLeft" width="100%">
                        <thead>
                        <tr>
                            <th>
                                <nobr><fmt:message key="humantask.package.actions"/></nobr>
                            </th>
                        </tr>
                        </thead>

                        <tbody>

                        <%--unDeploy action --%>
                        <tr>
                            <td>

                                <a id="<%=packageName%>"
                                   class="icon-link-nofloat registryWriteOperation"
                                   style="background-image:url(images/undeploy.gif);"
                                   href="<%=HumanTaskUIUtil.getUnDeployLink(packageName)%>">Undeploy</a>


                                <script type="text/javascript">
                                    jQuery('#<%=packageName%>').click(function() {
                                        function handleYes<%=packageName%>() {
                                            window.location = jQuery('#<%=packageName%>').attr('href');
                                        }
                                        sessionAwareFunction(function() {
                                        CARBON.showConfirmationDialog(
                                                "Do you want to undeploy package <%=packageName%>?",
                                                handleYes<%=packageName%>,
                                                null);
                                        }, "<fmt:message key="session.timed.out"/>");
                                        return false;
                                    });
                                </script>
                            </td>
                        </tr>

                        <tr>
                                <td>
                                <a id="<%=packageName%>" class="icon-link-nofloat"
                                   style="background-image:url(images/icon-download.jpg);"
                                   href="humantask_package_download-ajaxprocessor.jsp?packageName=<%=packageName%>"
                                   target="_self">Download</a>

                                </td>
                        </tr>

                        </tbody>


                    </table>
                    <%
                        }
                    %>


                    <table>
                        <tr>&nbsp;</tr>
                    </table>


                    <table id="packageListTable" class="styledLeft" width="100%">
                        <thead>
                        <tr>
                            <th>
                                <nobr><fmt:message key="humantask.definitions"/></nobr>
                            </th>
                        </tr>
                        </thead>

                        <tbody>

                        <% if (taskDefinitionsInPackage != null) {
                            for (Task_type0 aTaskDefinitionsInPackage : taskDefinitionsInPackage) {

                        %>
                        <tr>
                            <td>
                                <a href="./task_definition_info.jsp?taskDefId=<%=aTaskDefinitionsInPackage.getName()%>">
                                    <%=aTaskDefinitionsInPackage.getName()%>
                                </a>

                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>

                        </tbody>
                    </table>

                    <%
                        if(isErroneous) {
                    %>


                    <table>
                        <tr>&nbsp;</tr>
                    </table>


                    <table id="deploymentErrorListTbl" class="styledLeft" width="100%" style="color:red">
                        <thead>
                        <tr>
                            <th>
                                <nobr><fmt:message key="humantask.deployment.errors"/></nobr>
                            </th>
                        </tr>
                        </thead>

                        <tbody>

                        <% if (taskDefinitionsInPackage != null) {
                            for (Task_type0 aTaskDefinitionsInPackage : taskDefinitionsInPackage) {

                        %>
                        <tr>
                            <td>
                                 <%=aTaskDefinitionsInPackage.getName()%> : <%=aTaskDefinitionsInPackage.getDeploymentError()%>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>

                        </tbody>
                    </table>

                     <%
                         }
                     %>
                </div>
            </div>
        </div>
    </div>

</fmt:bundle>
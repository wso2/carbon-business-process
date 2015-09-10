<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.*" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.BPELPackageManagementServiceClient" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.File" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
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

    BPELPackageManagementServiceClient client = null;
    PackageType processesInPackage = null;
    LimitedProcessInfoType[] processList = null;

    String operation = Encode.forXml(request.getParameter("operation"));
    String packageName = Encode.forXml(request.getParameter("packageName"));

    boolean isAuthorizedToManagePackages =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/packages");
    boolean isAuthorizedToMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");

    if (isAuthorizedToManagePackages || isAuthorizedToMonitor) {
        try {
            client = new BPELPackageManagementServiceClient(cookie, backendServerURL, configContext);
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

    if (isAuthorizedToManagePackages) {
        try {
            processesInPackage = client.listProcessesInPackage(packageName);
            Version_type0[] packageVersions =
                    processesInPackage.getVersions().getVersion();
            for (Version_type0 packageVersion : packageVersions) {
                if (packageVersion.getName().equals(packageName)) {
                    processList = packageVersion.getProcesses().getProcess();
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
        }
    }

    if (isAuthorizedToManagePackages || isAuthorizedToMonitor) {
        if (isAuthorizedToManagePackages && operation != null && packageName != null &&
            operation.equals("undeploy")) {
            try {
                UndeployStatus_type0 status = client.undeploy(packageName);
                if (status.equals(UndeployStatus_type0.FAILED)) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(
                            CarbonUIMessage.ERROR,
                            "BPEL package " + packageName + " undeployment failed.",
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
    }
%>
<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
    <carbon:breadcrumb
            label="bpel.package.dashboard.headertext"
            resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <jsp:include page="../dialog/display_messages.jsp"/>
    <div id="middle">
        <div id="package-list-main">
            <h2><fmt:message key="bpel.package.dashboard.headertext"/>&nbsp;(<%=packageName%>)</h2>

            <div id="workArea">
                <div id="package-list">
                    <%
                        if (isAuthorizedToManagePackages || isAuthorizedToMonitor) {
                    %>
                    <table id="packageListTable" class="styledLeft" width="100%">
                        <thead>

                        <%
                            if (isAuthorizedToManagePackages) {
                        %>
                        <tr>
                            <th>
                                <nobr><fmt:message key="actions"/></nobr>
                            </th>
                        </tr>
                        <%
                            }
                        %>
                        </thead>

                        <tbody>

                        <%
                            if (isAuthorizedToManagePackages) {
                                //abstract only the package name from the request param
                                String name = packageName.substring(0, packageName.lastIndexOf("-"));
                        %>
                        <tr>
                            <td>
                                <%
                                    String jQueryCompliantID = BpelUIUtil.
                                            generateJQueryCompliantID(name);
                                %>
                                <a id="<%=jQueryCompliantID%>"
                                   class="icon-link-nofloat registryWriteOperation"
                                   style="background-image:url(images/undeploy.gif);"
                                   href="<%=BpelUIUtil.getUndeployLink(name)%>">Undeploy</a>

                                <a id="<%=jQueryCompliantID%>"
                                   class="icon-link-nofloat registryNonWriteOperation"
                                   style="background-image:url(images/undeploy.gif);color:#777;cursor:default;"
                                   onclick="return false"
                                   href="<%=BpelUIUtil.getUndeployLink(name)%>">Undeploy</a>

                                <script type="text/javascript">
                                    jQuery('#<%=jQueryCompliantID%>').click(function () {
                                        function handleYes<%=jQueryCompliantID%>() {
                                            window.location = jQuery('#<%=jQueryCompliantID%>').attr('href');
                                        }

                                        sessionAwareFunction(function () {
                                            CARBON.showConfirmationDialog(
                                                    "Do you want to undeploy package <%=name%>?",
                                                    handleYes<%=jQueryCompliantID%>,
                                                    null);
                                        }, "<fmt:message key="session.timed.out"/>");
                                        return false;
                                    });
                                </script>
                            </td>


                        </tr>
                        <%
                            String path = RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.PATH_SEPARATOR + "bpel" + RegistryConstants.PATH_SEPARATOR + "packages" + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + name.concat(".zip");
                            if (packageName.contains(name)) {
                        %>
                        <tr>
                            <td>
                                <a id="<%=name%>" class="icon-link-nofloat"
                                   style="background-image:url(images/icon-download.jpg);"
                                   href="javascript:sessionAwareFunction(function() {window.location = '<%=Utils.getResourceDownloadURL(request, path)%>'}, org_wso2_carbon_registry_resource_ui_jsi18n['session.timed.out']);"
                                   target="_self">Download</a>

                            </td>
                        </tr>
                        </tbody>
                        <%
                                }
                            }
                        %>
                    </table>
                    <%
                        }
                    %>

                    <table>
                        <tr>&nbsp;</tr>
                    </table>

                    <% if ((isAuthorizedToManagePackages || isAuthorizedToMonitor) && processList != null) {
                        for (LimitedProcessInfoType processInfo : processList) {
                    %>
                    <table id="packageListTable" class="styledLeft" width="100%">
                        <thead>
                        <tr>
                            <th>
                                <nobr><fmt:message key="bpel.process"/></nobr>
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        <tr>
                            <td>
                                <a href="./process_info.jsp?Pid=<%=processInfo.getPid()%>"><%=processInfo.getPid()%>
                                </a>

                            </td>
                        </tr>
                        </tbody>
                    </table>

                    <%
                            }
                        }
                    %>

                    <table>
                        <tr>&nbsp;</tr>
                    </table>

                    <%
                        if (processesInPackage != null && processesInPackage.isErrorLogSpecified() &&
                            (isAuthorizedToManagePackages || isAuthorizedToMonitor)) {
                    %>
                    <table id="packageErrorTable" class="styledLeft" width="100%" style="color:red">
                        <thead>
                        <tr>
                            <th>
                                <nobr><fmt:message key="deployment.error"/></nobr>
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        <tr>
                            <td>
                                <%=processesInPackage.getErrorLog()%>
                            </td>
                        </tr>
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
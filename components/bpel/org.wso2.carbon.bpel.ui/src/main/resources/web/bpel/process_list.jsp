<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.*" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.BPELPackageManagementServiceClient" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.businessprocesses.common.utils.CharacterEncoder" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.registry.api.Registry" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ page import="org.wso2.carbon.registry.api.RegistryException" %>
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

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
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

    BPELPackageManagementServiceClient pkgClient;
    ProcessManagementServiceClient client = null;
    DeployedPackagesPaginated packageList = null;
    String processListFilter = null;
    String processListOrderBy = null;
    String parameters = "";
    int numberOfPages = 0;
    int pageNumberInt = 0;
    int linkNum = 0;

    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String packageName = CharacterEncoder.getSafeText(request.getParameter("packageName"));
    String packageSearchString = CharacterEncoder.getSafeText(request.getParameter("packageSearchString"));
    if (packageSearchString == null) {
    packageSearchString = "";
    }
    boolean isAuthorizedToManagePackages =
                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/packages");
    boolean isAuthorizedToMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");
    boolean isAuthorizedToManageProcesses =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");


    if (isAuthorizedToManagePackages || isAuthorizedToMonitor ) {
        try{
            pkgClient = new BPELPackageManagementServiceClient(cookie, backendServerURL, configContext);
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }


        if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
        try{
            client = new ProcessManagementServiceClient(cookie, backendServerURL,
                                                        configContext, request.getLocale());
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

        if(isAuthorizedToManagePackages && operation != null && packageName != null &&
           operation.equals("undeploy")) {
            try {
                UndeployStatus_type0 status = pkgClient.undeploy(packageName);
                if (status.equals(UndeployStatus_type0.INSTANCE_DELETE_LIMIT_REACHED)) {
%>
                    <fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
                        <script type="application/javascript">
                            CARBON.showInfoDialog('<fmt:message key="bpel.undeploy.too.much.instances"/>');
                        </script>
                    </fmt:bundle>
<%
                } else if(status.equals(UndeployStatus_type0.FAILED)) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(
                            CarbonUIMessage.ERROR,
                            "BPEL package "+ packageName +" undeployment failed.",
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

    if (isAuthorizedToManageProcesses && operation != null && client != null) {
        String pid = CharacterEncoder.getSafeText(request.getParameter("processID"));
        if (operation.toLowerCase().trim().equals("retire")) {
            String rowPackageName =
                    CharacterEncoder.getSafeText(request.getParameter("retiredPackageName"));
            if (rowPackageName != null) {
                RegistryUtils.setTrustStoreSystemProperties();
                CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
                Registry configRegistry = context.getRegistry(RegistryType.SYSTEM_CONFIGURATION);

                String regPath =
                        "bpel/packages/" + rowPackageName.split("-\\d*$")[0] + "/versions/" +
                        rowPackageName;
                try {
                    if (configRegistry.resourceExists(regPath)) {
                        if (pid != null && pid.length() > 0) {
                            client.retireProcess(BpelUIUtil.stringToQName(pid));
                        }
                    }
                } catch (RegistryException e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg =
                            new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
    }

} else if (operation.toLowerCase().trim().equals("activate")) {
    if (pid != null && pid.length() > 0) {
        try {
            client.activateProcess(BpelUIUtil.stringToQName(pid));
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
    }

        if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
        if(pageNumber == null) {
            pageNumber = "0";
        }
        try{
            pageNumberInt = Integer.parseInt(pageNumber);
        } catch (NumberFormatException ignored){
        }

        processListFilter = CharacterEncoder.getSafeText(request.getParameter("filter"));
        if(processListFilter == null || processListFilter.length() == 0){
            processListFilter = "name}}* namespace=*";
        }

        processListOrderBy = CharacterEncoder.getSafeText(request.getParameter("order"));
        if(processListOrderBy == null || processListOrderBy.length() == 0) {
            processListOrderBy = "-deployed";
        }

        parameters = "filter=" + URLEncoder.encode(processListFilter, "UTF-8") + "&order=" + processListOrderBy + "&packageSearchString=" + packageSearchString;


    }

        if (isAuthorizedToMonitor || isAuthorizedToManagePackages) {
            if(pageNumber == null) {
                pageNumber = "0";
            }

            try{
                pageNumberInt = Integer.parseInt(pageNumber);
            } catch (NumberFormatException ignored) {

            }

            try{

                if(packageSearchString.equals("*")){
                               packageSearchString="";
                          }
                packageList = pkgClient.getPaginatedPackageList(pageNumberInt,packageSearchString);

                if(packageList != null) {
                    numberOfPages = packageList.getPages();
                } else {
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
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
    <carbon:breadcrumb
            label="bpel.packages"
            resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <jsp:include page="../dialog/display_messages.jsp"/>
<div id="middle">

    <div id="process-list-main">
        <h2><fmt:message key="bpel.deployed_processes"/></h2>
         <div id="workArea"><div id="searchArea" style="border: 1px solid #CCCCCC;">
             <form action="process_list.jsp" name="searchForm">
             <table class="styledLeft" width="100%">
                    <tbody>
                    <tr style="border:1;">
                            <td style="border:0; !important" width="15%">
                                    <nobr>&nbsp;&nbsp;&nbsp;
                                    <fmt:message key="bpel.package"/>
                                    <input type="text" name="packageSearchString"value="<%= packageSearchString != null? packageSearchString : ""%>"/>&nbsp;
                                    </nobr>
                            </td>
                            <td style="border:0; !important">
                                    <a class="icon-link" href="#" style="background-image: url(images/search.gif);"onclick="javascript:document.searchForm.submit(); return false;"alt="<fmt:message key="search"/>"></a>
                            </td>
                        </tr>
                    </tbody>
             </table>
             </form>
             </div>
             <br/>


    <div id="process-list">
<%
    if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
        if(packageList != null && packageList.get_package() != null &&
                packageList.get_package().length > 0) {
%>

                <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="process_list.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%= parameters%>"/>


                <table id="processListTable" class="styledLeft" width="100%">      <!--Basic structure of the info table-->
                    <thead>
                        <tr>

<%
            if (isAuthorizedToManagePackages) {
%>
                            <th><fmt:message key="package.name"/></th>
<%
            }
 %>
                            <th><fmt:message key="processid"/></th>
                            <th><fmt:message key="version"/></th>
                            <th><fmt:message key="status"/></th>
                            <th><fmt:message key="deployed.date"/></th>
<%
            if(isAuthorizedToManageProcesses){
%>
                            <th><fmt:message key="manage"/></th>
<%
            }
%>
                        </tr>
                    </thead>
                    <tbody>
<%
            for(PackageType packageInfo : packageList.get_package()) {
                for (Version_type0 packageWithVersion : packageInfo.getVersions().getVersion()) {
                         if( packageSearchString!="" && !packageWithVersion.getIsLatest()){
                         continue;
                         }
                    if (isAuthorizedToManagePackages || isAuthorizedToMonitor) {
%>
<tr>
    <%
                // We need to differentiate process operation links(anchor tags) to attach onclick event callback.
                // So here we are generating link based on a integer count.
                String linkID = "processOperation" + linkNum;
                linkNum++;
    %>
    <td rowspan="<%= packageWithVersion.getProcesses().getProcess() == null ? 1 : packageWithVersion.getProcesses().getProcess().length%>">
        <%
                    if (packageWithVersion.getIsLatest()) {
        %>

        <a href="package_dashboard.jsp?packageName=<%=packageWithVersion.getName()%>"><%=packageWithVersion.getName()%>
        </a>

        <%
                    } else {
        %>
                        <%=packageWithVersion.getName()%>
        <%
                    }
        %>
    </td>
     <%
         int processIndex = 0;
         if (packageWithVersion.getProcesses().getProcess() != null) {
         for (LimitedProcessInfoType processInfo : packageWithVersion.getProcesses().getProcess()) {
             if (processIndex != 0) {
     %>
        <tr>
    <%
        }
        processIndex++;
    %>
    <td><a href="./process_info.jsp?Pid=<%=URLEncoder.encode(processInfo.getPid(),"UTF-8")%>"><%=processInfo.getPid()%></a></td> <!--./process_info.jsp?Pid=<%=processInfo.getPid()%>-->
    <td><%=processInfo.getVersion()%></td>
    <td><%=processInfo.getStatus().toString()%></td>
    <td><%=processInfo.getDeployedDate().getTime().toString()%></td>
<%
                if(isAuthorizedToManageProcesses) {
                    if (processInfo.getOlderVersion() == 0) {
%>
                            <td>
<%
                    if(processInfo.getStatus().toString().trim().equals("ACTIVE")){
%>
                                <a id="<%=linkID%>" class="icon-link-nofloat" style="background-image:url(images/deactivate.gif);" href="<%=BpelUIUtil.getRetireLink(processInfo.getPid(), processListFilter, processListOrderBy, pageNumberInt, processInfo.getPackageName())%>">Retire</a>
                                <script type="text/javascript">
                                    jQuery('#<%=linkID%>').click(function(){
                                        function handleYes<%=linkID%>(){
                                            window.location = jQuery('#<%=linkID%>').attr('href');
                                        }
                                        sessionAwareFunction(function() {
                                        CARBON.showConfirmationDialog(
                                                "Do you want to retire process <%=processInfo.getPid()%>?",
                                                handleYes<%=linkID%>,
                                                null);
                                        }, "<fmt:message key="session.timed.out"/>");
                                        return false;
                                    });
                                </script>
<%
                    } else {
%>
                                <a id="<%=linkID%>" class="icon-link-nofloat" style="background-image:url(images/activate.gif);" href="<%=BpelUIUtil.getActivateLink(processInfo.getPid(), processListFilter, processListOrderBy, pageNumberInt)%>">Activate</a>
                                <script type="text/javascript">
                                    jQuery('#<%=linkID%>').click(function(){
                                        function handleYes<%=linkID%>(){
                                            window.location = jQuery('#<%=linkID%>').attr('href');
                                        }
                                        sessionAwareFunction(function() {
                                        CARBON.showConfirmationDialog(
                                                "Do you want to activate process <%=processInfo.getPid()%>?",
                                                handleYes<%=linkID%>,
                                                null);
                                        }, "<fmt:message key="session.timed.out"/>");
                                        return false;
                                    });
                                </script>
<%
                    }
%>
                            </td>
<%
                    } else {
%>
                            <td></td>
<%
                }
            }
%>
            </tr>
<%
                 }
                } else {
%>
            <td colspan="5" style="color: #ff0000;"><fmt:message key="deployment.error"/></td>
<%
                 }
            }
        }

    }
%>
                    </tbody>
                </table>
                <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                                  page="process_list.jsp" pageNumberParameterName="pageNumber"
                                  resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
                                  prevKey="prev" nextKey="next"
                                  parameters="<%=parameters%>"/>
                <%
                } else {
                    if(packageSearchString.equals("")){
                %>
                 <script type="text/javascript">
                document.getElementById('searchArea').style.display = 'none';
                 </script>
                <%
                }
                %>
                <p><fmt:message key="there.are.no.processes.available"/></p>
                <%
                    }
                } else {
                %>
                <p><fmt:message key="do.not.have.permission.to.view.process.details"/></p>
                <%
                    }
                %>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    alternateTableRows('processListTable', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>

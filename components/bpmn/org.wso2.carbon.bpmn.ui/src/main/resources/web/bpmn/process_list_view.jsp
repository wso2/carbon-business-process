<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bpmn.ui.WorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNProcess" %>
<%@ page import="org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNDeployment" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
WorkflowServiceClient client;
    BPMNDeployment[] deployments;
    try {
        client = new WorkflowServiceClient(cookie, serverURL, configContext);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }

    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String processId = CharacterEncoder.getSafeText(request.getParameter("processID"));
    if(operation != null && operation.equals("startProcess")){
        client.startProcess(processId);
    }
    String deploymentName = CharacterEncoder.getSafeText(request.getParameter("deploymentName"));
    if(operation != null && operation.equals("undeploy")){
            client.undeploy(deploymentName);
    }
    deployments = client.getDeployments();
%>
<%
    if(operation != null && operation.equals("packageInfo")){
%>
    <jsp:include page="package_dashboard.jsp"/>
<%
    }else if(operation != null && operation.equals("processDef")){
%>
    <jsp:include page="process_definition.jsp"/>
<%  }else{ %>
<link rel="stylesheet" type="text/css" href="css/bpmn_icon_link.css" />
<script>
    function startProcess(pid) {
        function startYes() {
            $.ajax({
            type: 'POST',
            url: location.protocol + "//" + location.host + "/carbon/bpmn/process_list_view.jsp?region=region1&item=bpmn_menu&operation=startProcess&processID=" + pid,
            success: function(data){
                window.location = location.protocol + "//" + location.host + "/carbon/bpmn/process_list_view.jsp";
               }
            });
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.start.process"/> ' + pid + "?", startYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };
</script>
<jsp:include page="../dialog/display_messages.jsp"/>
    <carbon:breadcrumb
            label="bpmn.deployed.processes"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
<div id="middle">
    <h2><fmt:message key="bpmn.deployed.processes"/></h2>

    <div id="workArea">
        <table class="styledLeft" id="moduleTable">
            <thead>
            <tr>
                <th width="20%"><fmt:message key="bpmn.package.name"/></th>
                <th width="30%"><fmt:message key="bpmn.process.id"/></th>
                <th width="10%"><fmt:message key="bpmn.process.version"/></th>
                <th width="30%"><fmt:message key="bpmn.process.deployedDate"/></th>
                <th width="10%"><fmt:message key="bpmn.process.manage"/></th>
            </tr>
            </thead>
            <tbody>
                <% if(deployments!=null && deployments.length>0){ %>
                <% for(BPMNDeployment deployment: deployments){ %>
                    <% BPMNProcess[] processes = client.getProcessListByDeploymentID(deployment.getDeploymentId());
                       boolean firstRow = true;
                    %>
                    <% if(processes!=null && processes.length>0){ %>
                    <% for(BPMNProcess process: processes){ %>
                        <tr>
                            <% if(firstRow){ %>
                                <td rowspan=<%=processes.length%>><a href=<%="process_list_view.jsp?operation=packageInfo&deploymentName=" + deployment.getDeploymentName()%>><%=deployment.getDeploymentName() + "-" + deployment.getDeploymentId()%></a></td>
                                <% firstRow = false; %>
                            <% } %>
                            <td><a href=<%="process_list_view.jsp?operation=processDef&processID=" + process.getProcessId()%>>
                                   <%=process.getProcessId()%></a></td>
                            <td><%=process.getVersion()%></td>
                            <td><%=deployment.getDeploymentTime().toString()%></td>
                            <td><a class="bpmn-icon-link" style="background-image:url(images/start.gif);" href="#" onclick="startProcess('<%=process.getProcessId()%>');"><fmt:message key="bpmn.process.start"/></a></td>
                        </tr>
                    <% }} %>
                <% }}else{ %>
                    <tr>
                        <td colspan="5"><fmt:message key="processes.available.state"/></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</div>
<%  } %>
</fmt:bundle>

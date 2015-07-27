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
<%@ page import="org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNInstance" %>
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
    BPMNInstance[] bpmnInstances;

    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String instanceId = CharacterEncoder.getSafeText(request.getParameter("instanceID"));
    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    int currentPage = 0;
    if(pageNumber != null && pageNumber != ""){
        currentPage = Integer.parseInt(pageNumber);
    }
    int start = currentPage * 10;
    int numberOfPages;
    try {
        client = new WorkflowServiceClient(cookie, serverURL, configContext);


        if(operation != null && operation.equals("deleteProcessInstance")){
            client.deleteCompletedInstance(instanceId);
        }else if(operation != null && operation.equals("suspendProcessInstance")){
            client.suspendProcessInstance(instanceId);
        }else if(operation != null && operation.equals("activateProcessInstance")){
            client.activateProcessInstance(instanceId);
        }else if(operation != null && operation.equals("deleteAllProcessInstances")){
            client.deleteAllCompletedInstances();
        }

        bpmnInstances = client.getPaginatedHistoryInstances(start, 10);
        numberOfPages = (int) Math.ceil(client.getHistoryInstanceCount()/10.0);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>
<%
    if(operation != null && operation.equals("instanceInfo")){
%>
    <jsp:include page="instance_variables.jsp"/>
<%  }else{ %>
<link rel="stylesheet" type="text/css" href="css/bpmn_icon_link.css" />
<script>
    function deleteAllInstance(iid) {
        function deleteYes() {
        $.ajax({
        type: 'POST',
        url: location.protocol + "//" + location.host + "/carbon/bpmn/history_instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=deleteAllProcessInstances",
        success: function(data){
            window.location = location.protocol + "//" + location.host + "/carbon/bpmn/process_list_view.jsp?region=region1&item=bpmn_menu";
           }
        });
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.all.instances"/> ' + "?", deleteYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };
    function deleteInstance(iid) {
        function deleteYes() {
        $.ajax({
        type: 'POST',
        url: location.protocol + "//" + location.host + "/carbon/bpmn/history_instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=deleteProcessInstance&instanceID=" + iid,
        success: function(data){
	        window.location = location.protocol + "//" + location.host + "/carbon/bpmn/history_instance_list_view.jsp?region=region1&item=bpmn_instace_menu";
           }
        });
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.instance"/> ' + iid + "?", deleteYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };
    function suspendInstance(iid) {
        function suspendYes() {
        $.ajax({
        type: 'POST',
        url: location.protocol + "//" + location.host + "/carbon/bpmn/history_instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=suspendProcessInstance&instanceID=" + iid,
        success: function(data){
	        window.location = location.protocol + "//" + location.host + "/carbon/bpmn/history_instance_list_view.jsp?region=region1&item=bpmn_instace_menu";
           }
        });
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.suspend.instance"/> ' + iid + "?", suspendYes, null);

        }, "<fmt:message key="session.timed.out"/>");
        return false;
    };
    function activateInstance(iid) {
            function activateYes() {
            $.ajax({
            type: 'POST',
            url: location.protocol + "//" + location.host + "/carbon/bpmn/history_instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=activateProcessInstance&instanceID=" + iid,
            success: function(data){
	window.location = location.protocol + "//" + location.host + "/carbon/bpmn/history_instance_list_view.jsp?region=region1&item=bpmn_instace_menu";
           }
        });
        }
            sessionAwareFunction(function() {
            CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.activate.instance"/> ' + iid + "?", activateYes, null);
            }, "<fmt:message key="session.timed.out"/>");
            return false;
        };
</script>
    <carbon:breadcrumb
            label="bpmn.instances.created"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
<div id="middle">
    <h2><fmt:message key="bpmn.instances.created"/></h2>

    <div id="workArea">
        <table class="styledLeft" id="moduleTable">
            <thead>
            <tr>
                <th width="20%"><fmt:message key="bpmn.instance.id"/></th>
                <th width="30%"><fmt:message key="bpmn.process.id"/></th>
                <th width="20%"><fmt:message key="bpmn.instance.createdDate"/></th>
                <th width="20%"><fmt:message key="bpmn.instance.completeDate"/></th>
                <th width="10%"><fmt:message key="bpmn.instance.manage"/></th>
            </tr>
            </thead>
            <tbody>
                <% if(bpmnInstances!=null && bpmnInstances.length>0){ %>
                <%  for(BPMNInstance bpmnInstance: bpmnInstances){ %>
                    <tr>
                        <td><%=bpmnInstance.getInstanceId()%></td>
                        <td><a href=<%="process_list_view.jsp?operation=processDef&processID=" + bpmnInstance.getProcessId()%>><%=bpmnInstance.getProcessId()%></a></td>
                        <td><%=bpmnInstance.getStartTime().toString()%></td>
                        <td><%=bpmnInstance.getEndTime().toString()%></td>
                        <td>
                            [&nbsp;<a href="#" class="bpmn-icon-link" style="background-image:url(images/delete.gif);" onclick="deleteInstance('<%=bpmnInstance.getInstanceId()%>')"><fmt:message key="bpmn.instance.delete"/></a>&nbsp;]
                        </td>
                    </tr>
                <% }}else{ %>
                    <tr>
                        <td colspan="5"><fmt:message key="instance.available.state"/></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
        <br/>
        <carbon:paginator pageNumber="<%=currentPage%>" numberOfPages="<%=numberOfPages%>"
                          page="history_instance_list_view.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="region=region1&item=bpmn_instace_menu"/>
        <% if(bpmnInstances!=null && bpmnInstances.length>0){ %>
            <br/>
            <a href="#" class="bpmn-icon-link" style="background-image:url(images/delete.gif);" onclick="deleteAllInstance()"><fmt:message key="bpmn.instance.deleteAll"/></a>
        <% } %>
    </div>
</div>
<%  } %>
</fmt:bundle>

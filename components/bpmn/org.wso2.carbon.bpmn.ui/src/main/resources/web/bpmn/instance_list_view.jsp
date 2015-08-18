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
<%@ page import="org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNProcess" %>
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
    BPMNProcess[] bpmnProcesses;

    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String instanceId = CharacterEncoder.getSafeText(request.getParameter("instanceID"));
    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    String state = CharacterEncoder.getSafeText(request.getParameter("state"));
    String iid = CharacterEncoder.getSafeText(request.getParameter("iid"));
    String pid = CharacterEncoder.getSafeText(request.getParameter("pid"));
    String startAfter = CharacterEncoder.getSafeText(request.getParameter("startAfter"));
    String startBefore = CharacterEncoder.getSafeText(request.getParameter("startBefore"));
    String variableName = CharacterEncoder.getSafeText(request.getParameter("variable"));
    String variableValue = CharacterEncoder.getSafeText(request.getParameter("value"));

    String parameters = "region=region1&item=bpmn_menu";
    boolean finished = false;
    if (state != null && state.equals("completed")) {
        parameters += "&state=completed";
        finished = true;
    } else {
        parameters += "&state=active";
    }
    if (iid != null && !iid.equals("")) {
        parameters += ("&iid=" + iid);
    }
    if (pid != null && !pid.equals("")) {
        parameters += ("&pid=" + pid);
    }
    if (startAfter != null && !startAfter.equals("")) {
        parameters += ("&startAfter=" + startAfter);
    }
    if (startBefore != null && !startBefore.equals("")) {
        parameters += ("&startBefore=" + startBefore);
    }
    if (variableName != null && !variableName.equals("")) {
        parameters += ("&variable=" + variableName);
    }
    if (variableValue != null && !variableValue.equals("")) {
        parameters += ("&value=" + variableValue);
    }
    int currentPage = 0;
    if(pageNumber != null && !pageNumber.equals("")){
        currentPage = Integer.parseInt(pageNumber);
    }
    int start = currentPage * 10;
    int numberOfPages;
    try {
        client = new WorkflowServiceClient(cookie, serverURL, configContext);


        if(operation != null && operation.equals("deleteProcessInstance")){
            client.deleteProcessInstance(instanceId);
        }else if(operation != null && operation.equals("suspendProcessInstance")){
            client.suspendProcessInstance(instanceId);
        }else if(operation != null && operation.equals("activateProcessInstance")){
            client.activateProcessInstance(instanceId);
        }else if(operation != null && operation.equals("deleteAllProcessInstances")){
            client.deleteAllProcessInstances();
        }

        bpmnInstances = client.getPaginatedInstanceByFilter(finished, iid, startAfter, startBefore, pid,
                variableName, variableValue, start, 10);
        bpmnProcesses = client.getProcessList();
        numberOfPages = (int) Math.ceil(client.getInstanceCount()/10.0);
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
    function deleteAllInstance() {
        function deleteYes() {
        $.ajax({
        type: 'POST',
        url: location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=deleteAllProcessInstances",
        success: function(data){
            window.location = location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_menu&state=completed";
           }
        });
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.all.instances"/> ' + "?", deleteYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    }
    function deleteInstance(iid) {
        function deleteYes() {
        $.ajax({
        type: 'POST',
        url: location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=deleteProcessInstance&instanceID=" + iid,
        success: function(data){
	        window.location = location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_instace_menu&state=completed";
           }
        });
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.instance"/> ' + iid + "?", deleteYes, null);
        }, "<fmt:message key="session.timed.out"/>");
        return false;
    }
    function suspendInstance(iid) {
        function suspendYes() {
        $.ajax({
        type: 'POST',
        url: location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=suspendProcessInstance&instanceID=" + iid,
        success: function(data){
	        window.location = location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_instace_menu";
           }
        });
        }
        sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.suspend.instance"/> ' + iid + "?", suspendYes, null);

        }, "<fmt:message key="session.timed.out"/>");
        return false;
    }
    function activateInstance(iid) {
            function activateYes() {
            $.ajax({
            type: 'POST',
            url: location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_instace_menu&operation=activateProcessInstance&instanceID=" + iid,
            success: function(data){
	window.location = location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?region=region1&item=bpmn_instace_menu";
           }
        });
        }
            sessionAwareFunction(function() {
            CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.activate.instance"/> ' + iid + "?", activateYes, null);
            }, "<fmt:message key="session.timed.out"/>");
            return false;
        }
    function toggleAdvFilter() {
        var element = document.getElementById("advFilter");
        var styleString = element.style.display;
        if (styleString == "none") {
            element.style.display = "block";
        } else {
            element.style.display = "none";
        }
    }
    function instanceFilter() {
        <% if (finished) { %>
            var query = "region=region1&item=bpmn_menu&state=completed";
        <% } else {%>
            var query = "region=region1&item=bpmn_menu&state=active";
        <% } %>
        var iid = document.getElementById("instanceId").value;
        var pid = document.getElementById("processId").value;
        var after = document.getElementById("startAfter").value;
        var before = document.getElementById("startBefore").value;
        var variable = document.getElementById("variableName").value;
        var value = document.getElementById("variableValue").value;
        if (iid != null && iid !== "") {
            query += "&iid=" + iid;
        }
        if (pid != null && pid !== "" && pid !== "all") {
            query += "&pid=" + pid;
        }
        if (after != null && after !== "") {
            query += "&startAfter=" + after;
        }
        if (before != null && before !== "") {
            query += "&startBefore=" + before;
        }
        if (variable != null && variable !== "" && value != null && value !== "") {
            query += "&variable=" + variable + "&value=" + value;
        }
        window.location = location.protocol + "//" + location.host + "/carbon/bpmn/instance_list_view.jsp?" + query;
    }
    function clearFilter() {
        document.getElementById("instanceId").value = "";
        document.getElementById("processId").selectedIndex = 0;
        document.getElementById("startAfter").value = "";
        document.getElementById("startBefore").value = "";
        document.getElementById("variableName").value = "";
        document.getElementById("variableValue").value = "";
    }

    //set datepicker for start date in advanced search
    $(function() {
        $( "#startAfter" ).datepicker({
            showButtonPanel: true
        });
    });

    //set datepicker for before date in advanced search
    $(function() {
        $( "#startBefore" ).datepicker({
            showButtonPanel: true
        });
    });

</script>
    <carbon:breadcrumb
            label="bpmn.instances.created"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
<div id="middle">
    <h2><fmt:message key="bpmn.instances.created"/></h2>

    <div id="workArea">
        <table id="filter">
            <thead>
            <tr>
                <th width="10%"></th>
                <th width="8%"></th>
                <th width="10%"></th>
                <th width="65%"></th>
                <th width="7%"></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td id="cell"><a href="#" onclick="toggleAdvFilter()"><fmt:message key="bpmn.advanced.filter"/></a></td>
                <td id="cell"><a id="cellLink" href="instance_list_view.jsp?region=region1&item=bpmn_menu&state=active"
                                 style="background-image: url('images/bpmn-ins-active.gif')">
                    <fmt:message key="bpmn.active"/>
                </a></td>
                <td id="cell"><a id="cellLink" href="instance_list_view.jsp?region=region1&item=bpmn_menu&state=completed"
                                 style="background-image: url('images/bpmn-ins-completed.gif')">
                    <fmt:message key="bpmn.completed"/>
                </a></td>
                <% if(finished) { %>
                <td id="cell">&nbsp;</td>
                <td id="cell" style="border-right: none">
                    <a href="#" id="cellLink" style="background-image:url(images/delete.gif);" onclick="deleteAllInstance()">
                        <fmt:message key="bpmn.instance.deleteAll"/></a>
                </td>
                <% } %>
            </tr>
            </tbody>
        </table>
        <div id="advFilter" style="display: none; border: solid 1px #cccccc">
        <table cellspacing="7px" style="padding-left: 15px">
            <thead>
            <tr>
                <th width="20%"></th>
                <th width="80%"></th>
            </tr>
            </thead>
            <tbody>
            <form>
            <tr>
                <td><fmt:message key="bpmn.instance.id"/></td>
                <td>
                    <input type="number" id="instanceId" value="<%=iid%>" />
                </td>
            </tr>
            <tr>
                <td><fmt:message key="bpmn.process.id"/></td>
                <td>
                    <select id="processId">
                        <option value="all">All</option>
                        <% if (bpmnProcesses != null && bpmnProcesses.length > 0) { %>
                        <% for (BPMNProcess process: bpmnProcesses) {
                            if (pid != null && pid.equals(process.getProcessId())) {
                        %>
                                <option value="<%=process.getProcessId()%>" selected><%=process.getProcessId()%></option>
                            <% } else { %>
                                <option value="<%=process.getProcessId()%>"><%=process.getProcessId()%></option>
                        <%     }
                            }
                        }
                        %>
                    </select>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="bpmn.stated.after"/></td>
                <td>
                    <input type="text" id="startAfter" value="<%if (startAfter == null) {
                                                                    out.print("");
                                                                } else {
                                                                    out.print(startAfter);
                                                                }%>" placeholder="mm/dd/yyyy"/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="bpmn.stated.before"/></td>
                <td>
                    <input type="text" id="startBefore" value="<%if (startBefore == null) {
                                                                    out.print("");
                                                                 } else {
                                                                    out.print(startBefore);
                                                                 }%>" placeholder="mm/dd/yyyy"/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="bpmn.variable.like"/>&nbsp;&nbsp;&nbsp;</td>
                <td>
                    <% if (variableName == null || variableName.equals("") || variableValue == null || variableValue.equals("")) { %>
                    <input type="text" id="variableName" placeholder="Variable Name"/> =
                    <input type="text" id="variableValue" placeholder="Variable value like"/>
                    <% } else { %>
                    <input type="text" id="variableName" placeholder="Variable Name" value="<%=variableName%>"/> =
                    <input type="text" id="variableValue" placeholder="Variable value like" value="<%=variableValue%>"/>
                    <% } %>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="button" onclick="instanceFilter()" value="Filter"/> &nbsp;
                    <input type="button" onclick="clearFilter()" value="Clear">
                </td>
            </tr>
                <tr><td colspan="2"></td></tr>
            </form>
            </tbody>
        </table>
        </div>
        <br/>
        <table class="styledLeft" id="moduleTable">
            <thead>
            <tr>
                <% if (!finished) { %>
                <th width="10%"><fmt:message key="bpmn.instance.id"/></th>
                <th width="30%"><fmt:message key="bpmn.process.name"/></th>
                <th width="30%"><fmt:message key="bpmn.process.id"/></th>
                <th width="20%"><fmt:message key="bpmn.instance.createdDate"/></th>
                <th width="10%" colspan="2"><fmt:message key="bpmn.instance.manage"/></th>
                <% } else { %>
                <th width="10%"><fmt:message key="bpmn.instance.id"/></th>
                <th width="20%"><fmt:message key="bpmn.process.name"/></th>
                <th width="20%"><fmt:message key="bpmn.process.id"/></th>
                <th width="20%"><fmt:message key="bpmn.instance.createdDate"/></th>
                <th width="20%"><fmt:message key="bpmn.instance.completeDate"/></th>
                <th width="10%"><fmt:message key="bpmn.instance.manage"/></th>
                <% } %>
            </tr>
            </thead>
            <tbody>
                <% if(bpmnInstances!=null && bpmnInstances.length>0){ %>
                <%  for(BPMNInstance bpmnInstance: bpmnInstances){ %>
                    <tr>
                        <td>
                            <a href=<%="instance_list_view.jsp?operation=instanceInfo&instanceID=" + bpmnInstance.getInstanceId()%>><%=bpmnInstance.getInstanceId()%></a>
                        </td>
                        <td><%if(bpmnInstance.getProcessName()!=null) {
                                    out.print(bpmnInstance.getProcessName().toString());
                                } else {
                                    out.print("Not Available");
                                }%></td>
                        <td><a href=<%="process_list_view.jsp?operation=processDef&processID=" + bpmnInstance.getProcessId()%>><%=bpmnInstance.getProcessId()%></a></td>
                        <td><%=bpmnInstance.getStartTime().toString()%></td>
                        <% if (!finished) { %>
                        <td>
                            <% if(!bpmnInstance.getSuspended()){ %>
                                <a href="#" onclick="suspendInstance('<%=bpmnInstance.getInstanceId()%>')"><fmt:message key="bpmn.instance.suspend"/></a>
                            <% }else{ %>
                                <a href="#" onclick="activateInstance('<%=bpmnInstance.getInstanceId()%>')"><fmt:message key="bpmn.instance.activate"/></a>
                            <% } %>
                        </td>
                        <% } else { %>
                            <td><%=bpmnInstance.getEndTime().toString()%></td>
                        <% } %>
                        <td>
                            [&nbsp;<a href="#" class="bpmn-icon-link" style="background-image:url(images/delete.gif);" onclick="deleteInstance('<%=bpmnInstance.getInstanceId()%>')"><fmt:message key="bpmn.instance.delete"/></a>&nbsp;]
                        </td>
                    </tr>
                <% }}else{ %>
                    <tr>
                        <td colspan="6"><fmt:message key="instance.available.state"/></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
        <br/>
        <carbon:paginator pageNumber="<%=currentPage%>" numberOfPages="<%=numberOfPages%>"
                          page="instance_list_view.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=parameters%>"/>
        <% if(bpmnInstances!=null && bpmnInstances.length>0){ %>
            <br/>
        <% } %>
    </div>
</div>
<%  } %>
</fmt:bundle>

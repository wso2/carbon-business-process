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
<%@ page import="org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNVariable" %>
<%@ page import="org.wso2.carbon.businessprocesses.common.utils.CharacterEncoder" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    WorkflowServiceClient client;
    BPMNInstance bpmnInstance;
    BPMNVariable[] variables;

    String tableHeaders[] = {"Key", "Name", "Assignee", "Description", "Category", "Due Date", "Form Key", "Owner",
            "Priority", "Skip", "Candidate Group Ids", "Candidate User Ids"};
    String taskDefinitions[][];

    String instanceId = CharacterEncoder.getSafeText(request.getParameter("instanceID"));

    try {
        client = new WorkflowServiceClient(cookie, serverURL, configContext);
        bpmnInstance = client.getProcessInstanceById(instanceId);
        variables = bpmnInstance.getVariables();
        taskDefinitions = client.getCurrentTaskInformation(instanceId);
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
    <carbon:breadcrumb
            label="bpmn.instance.info"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
<div id="middle">
    <h2><fmt:message key="bpmn.instance.info"/> (<%=instanceId%>)</h2>

    <div id="workArea">
        <table class="styledLeft" id="moduleTable">
            <thead>
            <tr>
                <th width="20%"><fmt:message key="bpmn.instance.variables.name"/></th>
                <th width="20%"><fmt:message key="bpmn.instance.variables.value"/></th>
                <td border="none" width="60%"></td>
            </tr>
            </thead>
            <tbody>
                <% if(variables != null && variables.length>0){ %>
                <%
                    List<BPMNVariable> bpmnVariableList = Arrays.asList(variables);
                    Collections.sort(bpmnVariableList, new Comparator<BPMNVariable>() {
                        public int compare(BPMNVariable a, BPMNVariable b) {
                            return a.getName().compareToIgnoreCase(b.getName());
                        }
                    });
                %>
                <% for(BPMNVariable variable: bpmnVariableList){ %>
                    <tr>
                        <td><%=variable.getName()%></td>
                        <td><%=variable.getValue()%></td>
                    </tr>
                <% }
                }else{ %>
                   <tr>
                       <td colspan="2"><fmt:message key="instance.variables.state"/></td>
                   </tr>
                <% } %>
            </tbody>
        </table>
        <br/><br/>
        <%
            String imgString;
            String titleKey;
            try {
                imgString = client.getProcessInstanceDiagram(instanceId);
                titleKey = "bpmn.process.active.diagram";
            } catch (Exception e) {
                //could not load instance Diagram, try to load process diagram
                try {
                    String processId = client.getProcessInstanceById(instanceId).getProcessId();
                    imgString = client.getProcessDiagram(processId);
                    titleKey = "bpmn.process.diagram";
                } catch (Exception e1) {
                    //handled if imageString null
                    titleKey = "bpmn.process.active.diagram";
                    imgString = null;
                }
            }
        %>
        <table class="styledLeft" id="moduleTable">
            <thead>
            <tr>
                <th width="100%"><fmt:message key="<%=titleKey%>"/></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <% if (imgString != null) { %>
                <td><img src="<%=imgString%>"/></td>
                <% } else { %>
                <td><fmt:message key="error.loading.image"/></td>
                <% } %>
            </tr>
            </tbody>
        </table>
        <br><br>

        <%--this table will show the information of the current task of an active bpmn process instance.--%>
        <table class="styledLeft" id="outerTaskTable">
            <thead>
            <tr>
                <th width="100%">Information of the current task(s)</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <table style="width: 100%;" id="taskTable">
                        <%for(int i=0; i<taskDefinitions.length; i++){%>
                        <%for (int j = 0; j < 12; j++) {%>
                        <%if(taskDefinitions[i][j] != null){%>
                        <tr>
                            <th style="border: 1px solid #888888; background-color:#cccccc; font-weight:normal; height:22px;" width="30%"><strong><%=tableHeaders[j]%></strong></th>
                            <td style="border: 1px solid #cccccc; vertical-align: middle; height:24px;" width="70%"><%=taskDefinitions[i][j]%></td>
                        </tr>
                        <%}%>
                        <%}%>
                        <%}%>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</fmt:bundle>

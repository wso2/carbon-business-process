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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bpmn.ui.WorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.registry.api.Registry" %>
<%@ page import="org.wso2.carbon.registry.api.Resource" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">

    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        WorkflowServiceClient client;
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

        String[] processInstanceCheckBoxValues = request.getParameterValues("processInstance");
        String[] taskInstanceCheckBoxValues;

        String thriftUrl, username, password;
        CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        Registry configRegistry = context.getRegistry(RegistryType.SYSTEM_CONFIGURATION);

        if (configRegistry.resourceExists("bpmn/BPMN_data_analytics_publisher")) {
            Resource resource = configRegistry.get("bpmn/BPMN_data_analytics_publisher");
            thriftUrl = resource.getProperty("data_receiver_thrift_url");
            username = resource.getProperty("username");
            password = resource.getProperty("password");
        } else {
            thriftUrl = request.getParameter("thrift_url");
            username = request.getParameter("username");
            password = request.getParameter("password");
            if ((!thriftUrl.isEmpty() && thriftUrl.startsWith("tcp://")) || (!username.isEmpty()) || (!password.isEmpty())) {
                Resource resource = configRegistry.newResource();
                resource.addProperty("data_receiver_thrift_url", thriftUrl);
                resource.addProperty("username", username);
                resource.addProperty("password", password);
                configRegistry.put("bpmn/BPMN_data_analytics_publisher", resource);
            } else {
    %>
    <script type="text/javascript">
        CARBON.showWarningDialog('Please provide the correct thrift API configuration parameters.');
    </script>
    <%
            }
        }

    if((processInstanceCheckBoxValues != null) && (processInstanceCheckBoxValues.length > 0)){
        out.println("Process:" + processInstanceCheckBoxValues.length);
    %>
    <script type="text/javascript">
        CARBON.showWarningDialog("Process instances available.");
    </script>
    <%
        }else{
    %>
    <script type="text/javascript">
        CARBON.showWarningDialog("Process instances not available.");
    </script>
    <%
        }
    %>
    <script type="text/javascript">
        function validate() {
            var thriftUrl = document.bpmnDataPublisher.thrift_url.value;
            var processInstanceCheckBoxes = document.bpmnDataPublisher.processInstance;
            var taskInstanceCheckBoxes = document.bpmnDataPublisher.taskInstance;
            var procCheckLength = 0, taskCheckLength = 0;
            for (var i = 0; i < processInstanceCheckBoxes.length; i++) {
                if (processInstanceCheckBoxes[i].checked)
                    procCheckLength++;
            }
            for (var j = 0; j < taskInstanceCheckBoxes.length; j++) {
                if (taskInstanceCheckBoxes[j].checked)
                    taskCheckLength++;
            }

            if (((procCheckLength == 0) && (taskCheckLength == 0)) || (thriftUrl.length == 0)) {
                CARBON.showWarningDialog('Please provide correct thrift API configuration or select required fields to publish bpmn instances.');
            } else {
                document.bpmnDataPublisher.submit();
            }

        }
    </script>

    <carbon:breadcrumb
            label="bpmn.data.publisher.dashboard"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="bpmn.data.publisher.dashboard"/></h2>

        <div id="workArea">
            <div id="formset">
                <form id="bpmn_data_publish_form" method="post" name="bpmnDataPublisher" action="">

                    <table class="styledLeft" id="thriftUrlTable">
                        <thead>
                        <tr>
                            <th colspan="6"><fmt:message key="bpmn.data.publisher.thrift.configuration"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="tableEvenRow">
                            <td>Thrift URL</td>
                            <td>
                                <input id="thriftUrl" type="text" style="width:100%" name="thrift_url"
                                       value="<%=thriftUrl%>"
                                       placeholder="tcp://<ip address>/7611">
                            </td>
                            <td>Username</td>
                            <td>
                                <input id="username" type="text" style="width:100%" name="username"
                                       value="<%=username%>"
                                       placeholder="">
                            </td>
                            <td>Password</td>
                            <td>
                                <input id="password" type="password" style="width:100%" name="password"
                                       value="<%=password%>"
                                       placeholder="">
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <br>

                    <table class="styledLeft" id="instanceTable">
                        <thead>
                        <tr>
                            <th width="50%"><fmt:message key="bpmn.data.publisher.process.instances"/></th>
                            <th width="50%"><fmt:message key="bpmn.data.publisher.task.instances"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        <tr class="tableEvenRow">
                            <td>
                                <input value="processDefinitionId" id="processDefId" type="checkbox"
                                       name="processInstance"><fmt:message key="bpmn.process.definition.id"/><br>
                            </td>
                            <td>
                                <input value="taskDefinitionId" id="taskDefId" type="checkbox"
                                       name="taskInstance"><fmt:message key="bpmn.task.definition.id"/><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <input value="processInstanceId" id="processInsId" type="checkbox"
                                       name="processInstance"><fmt:message key="bpmn.process.instance.id"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceId" id="taskInsId" type="checkbox"
                                       name="taskInstance"><fmt:message key="bpmn.task.instance.id"/><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <input value="startActivityId" id="startActId" type="checkbox"
                                       name="processInstance"><fmt:message key="bpmn.start.activity.id"/><br>
                            </td>
                            <td>
                                <input value="assignee" id="assignee" type="checkbox" name="taskInstance"><fmt:message
                                    key="bpmn.task.instance.assignee"/><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <input value="startUserId" id="startUserId" type="checkbox"
                                       name="processInstance"><fmt:message key="bpmn.start.user.id"/><br>
                            </td>
                            <td>
                                <input value="taskProcessInstanceId" id="taskProcessId" type="checkbox"
                                       name="taskInstance"><fmt:message key="bpmn.process.instance.id"/><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <input value="processInstanceStartTime" id="processInstanceStartTime" type="checkbox"
                                       name="processInstance"><fmt:message key="bpmn.instance.start.time"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceStartTime" id="taskInstanceStartTime" type="checkbox"
                                       name="taskInstance"><fmt:message key="bpmn.instance.start.time"/><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <input value="processInstanceEndTime" id="processInstanceEndTime" type="checkbox"
                                       name="processInstance"><fmt:message key="bpmn.instance.end.time"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceEndTime" id="taskInstanceEndTime" type="checkbox"
                                       name="taskInstance"><fmt:message key="bpmn.instance.end.time"/><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <input value="processInstanceDuration" id="processInstanceDuration" type="checkbox"
                                       name="processInstance"><fmt:message key="bpmn.instance.duration"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceDuration" id="taskInstanceDuration" type="checkbox"
                                       name="taskInstance"><fmt:message key="bpmn.instance.duration"/><br>
                            </td>
                        </tr>
                        </tbody>
                    </table>

                    <table class="styledLeft">
                        <tr>
                            <td class="buttonRow">
                                <input name="publish" class="button registryWriteOperation" type="button"
                                       style="float: right;"
                                       value="<fmt:message key="bpmn.publish"/>" onclick="validate()"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>
    </div>

</fmt:bundle>
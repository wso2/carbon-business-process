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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page
        import="org.wso2.carbon.authenticator.stub.AuthenticationAdminStub" %>
<%@ page
        import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory" %>
<%@ page
        import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput" %>
<%@ page
        import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow" %>
<%@ page
        import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet" %>
<%@ page import="org.wso2.carbon.humantask.ui.clients.HumanTaskClientAPIServiceClient" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%

    response.setHeader("Cache-Control",
                       "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");


    // Pagination related;
    int numberOfPages = 0;
    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    int pageNumberInt = 0;
    String parameters = null;


    if (pageNumber == null) {
        pageNumber = "0";
    }
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {

    }

    String queryType = CharacterEncoder.getSafeText(request.getParameter("queryType"));

    boolean isAuthorisedToViewTaskList = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/humantask/viewtasks");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    HumanTaskClientAPIServiceClient taskAPIClient;
    TTaskSimpleQueryResultSet taskResults = null;
    parameters = "queryType=" + queryType;

    try {
        if (isAuthorisedToViewTaskList) {
            taskAPIClient = new HumanTaskClientAPIServiceClient(cookie, backendServerURL, configContext);
            TSimpleQueryInput queryInput = new TSimpleQueryInput();
            queryInput.setPageNumber(pageNumberInt);
            queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNED_TO_ME);

            if (queryType != null && !"".equals(queryType)) {
                if ("allTasks".equals(queryType)) {
                    queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ALL_TASKS);
                } else if ("assignedToMe".equals(queryType)) {
                    queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNED_TO_ME);
                } else if ("adminTasks".equals(queryType)) {
                    queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNABLE);
                } else if ("claimableTasks".equals(queryType)) {
                    queryInput.setSimpleQueryCategory(TSimpleQueryCategory.CLAIMABLE);
                }  else if ("notifications".equals(queryType)) {
                    queryInput.setSimpleQueryCategory(TSimpleQueryCategory.NOTIFICATIONS);
                }  else if ("advancedQuery".equals(queryType)) {
                    queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ADVANCED_QUERY);
                }
            }
            taskResults = taskAPIClient.taskListQuery(queryInput);
            numberOfPages = taskResults.getPages();
        }


    } catch (Exception e) {
%>
<jsp:include page="../admin/error.jsp"/>
<%
    }
%>


<%
    if (taskResults != null) {
%>
<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">
    <carbon:breadcrumb
        label="humantask.task.info"
        resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
    <jsp:include page="../dialog/display_messages.jsp"/>
<link href="css/humantask-gadget.css" rel="stylesheet"/>
<script type="text/javascript">

    jQuery(document).ready(function() {
        var queryType = '<%=queryType%>';
        highlightSelectedCriteria(queryType);
    });

    highlightSelectedCriteria = function (type) {
        if ('null' != type) {
            var id = '#' + type;
            var tabId = '#' + type + 'Tab';
            jQuery(id).addClass("selected");
            jQuery(tabId).show();
        } else {
            jQuery('#assignedToMe').addClass("selected");
            jQuery('#assignedToMeTab').show();
        }
    };

    function selectTaskFilteringTab(selectedTab) {
        //unselect all tabs
        var tabs = document.getElementById('tabs_task').getElementsByTagName('a');
        for (var i = 0; i < tabs.length; i++) {
            tabs[i].className = "";
        }
        //hide all the tabs
        var tabContent = document.getElementById("tabContent");
        for (var i = 0; i < tabContent.childNodes.length; i++) {
            if (tabContent.childNodes[i].nodeName == "DIV") {
                tabContent.childNodes[i].style.display = "none";
            }
        }

        selectedTab.className = "selected";
        if (selectedTab.rel != undefined && selectedTab.rel != null) {
            document.getElementById(selectedTab.rel).style.display = "";
        }

        window.location.href = "task_list.jsp?queryType=" + selectedTab.id;
    }



</script>


<div id="middle">
    <div id="task-instance-list-main">
    <h2><fmt:message key="humantask.my.task.list"/></h2>

        <div id="workArea">

            <div id="contentPlacerDiv" class="contentPlacer">
                <div class="tabs_task" id="tabs_task">
                    <ul>

                        <li><a onclick="selectTaskFilteringTab(this)" id="assignedToMe" rel="assignedToMeTab">My Tasks</a></li>
                        <li><a onclick="selectTaskFilteringTab(this)" id="claimableTasks" rel="claimableTasksTab">Claimable</a></li>
                        <li><a onclick="selectTaskFilteringTab(this)" id="adminTasks" rel="adminTasksTab">Admin Tasks</a></li>
                        <li><a onclick="selectTaskFilteringTab(this)" id="notifications" rel="notificationsTab">Notifications</a></li>
                        <li><a onclick="selectTaskFilteringTab(this)" id="allTasks" rel="allTasksTab">All</a></li>
                            <%--<li><a onclick="selectTab(this)" class="selected" rel="advancedFilterTab">Advanced--%>
                            <%--Filter</a>--%>
                            <%--</li>--%>
                    </ul>
                </div>
                <div class="tabContent" id="tabContent">
                    <div id="assignedToMeTab" class="tabContentData" style="display:none">My Tasks</div>
                    <div id="claimableTasksTab" class="tabContentData" style="display:none">Claimable</div>
                    <div id="adminTasksTab" class="tabContentData" style="display:none">Admin Tasks</div>
                    <div id="notificationsTab" class="tabContentData" style="display:none">Notifications</div>
                    <div id="allTasksTab" class="tabContentData" style="display:none">All Tab Data</div>
                        <%--<div id="advancedFilterTab" class="tabContentData" style="display:none">--%>
                        <%--<table class="normal">--%>
                        <%--<tbody>--%>
                        <%--<tr>--%>
                        <%--<th>Task</th>--%>
                        <%--<td>--%>
                        <%--<select name="taskId">--%>
                        <%--<option value="all" selected="">All</option>--%>
                        <%--</select>--%>
                        <%--</td>--%>
                        <%--</tr>--%>
                        <%--<tr>--%>
                        <%--<th>Task Status</th>--%>

                        <%--<td>--%>
                        <%--<input type="checkbox" name="status" value="active" checked="">--%>
                        <%--In Progress--%>
                        <%--<input type="checkbox" name="status" value="completed">--%>
                        <%--Open--%>
                        <%--<input type="checkbox" name="status" value="suspended">--%>
                        <%--Suspended--%>
                        <%--<input type="checkbox" name="status" value="terminated">--%>
                        <%--Closed--%>
                        <%--<input type="checkbox" name="status" value="failed">--%>
                        <%--Failed--%>
                        <%--</td>--%>

                        <%--</tr>--%>
                        <%--<tr>--%>
                        <%--<th>Status</th>--%>

                        <%--<td>--%>
                        <%--<input type="checkbox" name="status" value="active" checked="">--%>

                        <%--<input type="checkbox" name="status" value="completed">--%>
                        <%--Completed--%>
                        <%--<input type="checkbox" name="status" value="suspended">--%>
                        <%--Suspended--%>
                        <%--<input type="checkbox" name="status" value="terminated">--%>
                        <%--Terminated--%>
                        <%--<input type="checkbox" name="status" value="failed">--%>
                        <%--Failed--%>
                        <%--</td>--%>

                        <%--</tr>--%>
                        <%--<tr>--%>
                        <%--<th>--%>
                        <%--Created:--%>
                        <%--</th>--%>
                        <%--<td>--%>

                        <%--<input type="radio" name="startedopt" value="onb">--%>
                        <%--On Or Before--%>
                        <%--<input type="radio" name="startedopt" value="ona">--%>
                        <%--On Or After--%>
                        <%--<input type="text" id="starteddate" size="20" name="starteddate"--%>
                        <%--value="">--%>


                        <%--</td>--%>
                        <%--</tr>--%>
                        <%--<tr>--%>
                        <%--<th>--%>
                        <%--Last Updated:--%>
                        <%--</th>--%>
                        <%--<td>--%>
                        <%--<input type="radio" name="ladateopt" value="onb">--%>
                        <%--On Or Before--%>
                        <%--<input type="radio" name="ladateopt" value="ona">--%>
                        <%--On Or After--%>
                        <%--<input type="text" id="ladate" size="20" name="ladate" value="">--%>
                        <%--</td>--%>
                        <%--</tr>--%>
                        <%--<tr>--%>
                        <%--<th>Order by:</th>--%>
                        <%--<td>--%>

                        <%--<input type="radio" name="asdec" value="Ascending" checked="">--%>
                        <%--Ascending--%>
                        <%--<input type="radio" name="asdec" value="Descending">--%>
                        <%--Descending--%>
                        <%--<select name="orderby">--%>
                        <%--<option value="taskId">--%>
                        <%--Task Id--%>
                        <%--</option>--%>
                        <%--<option value="status">--%>
                        <%--Status--%>
                        <%--</option>--%>
                        <%--<option value="started">--%>
                        <%--Date Created--%>
                        <%--</option>--%>
                        <%--<option value="last-updated" selected="">--%>
                        <%--Last Updated--%>
                        <%--</option>--%>
                        <%--</select>--%>

                        <%--</td>--%>
                        <%--</tr>--%>
                        <%--<tr>--%>
                        <%--<td></td>--%>
                        <%--<td>--%>
                        <%--<input type="submit" class="button" name="filtersubmit" value="Filter">--%>
                        <%--<input type="reset" class="button" name="cancelsubmit" value="Cancel">--%>
                        <%--</td>--%>
                        <%--</tr>--%>
                        <%--</tbody>--%>

                        <%--</table>--%>
                        <%--</div>--%>
                </div>

                <h2></h2>

                <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                                  page="task_list.jsp" pageNumberParameterName="pageNumber"
                                  resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
                                  prevKey="prev" nextKey="next"
                                  parameters="<%=parameters%>"/>

                <table class="dataTable">

                    <%
                        if (taskResults != null && taskResults.getRow() != null && taskResults.getRow().length > 0) {
                    %>
                    <tr>
                        <th>
                            <fmt:message key="humantask.tasklist.table.column.id"/>
                        </th>
                        <th>
                            <fmt:message key="humantask.tasklist.table.column.subject"/>
                        </th>
                        <th>
                            <fmt:message key="humantask.tasklist.table.column.status"/>
                        </th>
                        <th>
                            <fmt:message key="humantask.tasklist.table.column.priority"/>
                        </th>
                        <th>
                            <fmt:message key="humantask.tasklist.table.column.created"/>
                        </th>
                    </tr>

                    <%
                    } else {
                    %>
                    <tr>
                        <th>
                            <fmt:message key="task.there.arent.any.matching.tasks"/>
                        </th>
                    </tr>
                    <%
                        }
                    %>


                    <% if (taskResults != null && taskResults.getRow() != null && taskResults.getRow().length > 0) {
                        TTaskSimpleQueryResultRow[] rows = taskResults.getRow();
                        for (TTaskSimpleQueryResultRow row : rows) {
                    %>

                    <tr>
                        <td>
                            <a href="basic_task_view.jsp?taskClient=carbon&taskId=<%=row.getId().toString()%>"><%=row.getId().toString()%>
                                -<%=row.getName().getLocalPart()%>
                            </a></td>
                        <td>
                            <%
                                String presentationName = HumanTaskUIUtil.getTaskPresentationHeader(row.getPresentationSubject(),
                                                                                                    row.getPresentationName());
                            %>
                            <%=presentationName%>
                        </td>
                        <td>
                            <%= row.getStatus().toString() %>
                        </td>
                        <td>
                            <%= row.getPriority() %>
                        </td>
                        <td>
                            <%= row.getCreatedTime().getTime().toString() %>
                        </td>
                    </tr>

                    <%
                            }
                        }
                    %>
                </table>
            </div>
        </div>
    </div>
</div>

</fmt:bundle>

<%
    }
%>
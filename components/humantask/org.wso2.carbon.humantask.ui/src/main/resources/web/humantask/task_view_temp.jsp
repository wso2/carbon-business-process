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
<%@ page import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAbstract" %>
<%@ page
        import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAuthorisationParams" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.humantask.ui.constants.HumanTaskUIConstants" %>
<%@ page import="org.wso2.carbon.humantask.stub.mgt.types.TaskStatusType" %>
<%@ page import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TStatus" %>
<%
    TTaskAbstract task = (TTaskAbstract) request.getAttribute("LoadedTask");
    boolean isNotification = task.getTaskType().equals("NOTIFICATION");
    String taskId =  (String) request.getAttribute("taskId");
    TTaskAuthorisationParams authParams = (TTaskAuthorisationParams) request.getAttribute("TaskAuthorisationParams");
    String taskClient = (String) request.getAttribute("taskClient");
    String requestJSPContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-input.jsp";
    String outputJspContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-output.jsp";
    String responseJspContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-response.jsp";
    String taskListLink = HumanTaskUIUtil.getTaskListURL(taskClient);
    taskListLink = "/carbon" + taskListLink;

    String taskListLogout = taskListLink + "?logout=true";
%>
<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">

<div id="task-instance-list-main">

    <link href="css/humantask-gadget.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/humantask-util.js"></script>
    <script type="text/javascript" src="js/humantask.js"></script>
    <script type="text/javascript">

    jQuery(document).ready(function() {
        //forceScrolling();
        HUMANTASK.ready('<%=taskId%>', '<%=taskClient%>', <%=isNotification%>);
    });

    forceScrolling = function() {
        jQuery('#contentPlacer').attr('style', 'height: 450px; overflow-y: auto;');
    };


</script>

<div>
    <div>
    <a class="backToTaskList" href="<%=taskListLink%>" style="display:block;padding:0px 0px 10px 0px"><< <fmt:message key="humantask.taskview.back.to.list"/></a>
    <%
        if(HumanTaskUIConstants.CLIENTS.GADGET.equals(taskClient)) {
            %>
    <a class="taskListLogout" href="<%=taskListLogout%>" style="display:block;padding:0px 0px 10px 0px"><fmt:message key="humantask.taskview.logout"/></a>
    <%
        }
    %>
    </div>
    <div class="titleStrip" id="taskSubjectTxt"><div class="titleStripSide">&nbsp;</div></div>
    <div id="errorStrip" style="display:none;"></div>
    <div class="contentPlacer">
        <div class="tabLinks" id="tabs_task">
            <ul>
                <li id="claimLinkLi" style="display:none;"><a id="claimLink"><fmt:message key="humantask.taskview.claim"/></a></li>
                <li id="startLinkLi" style="display:none;"><a id="startLink"><fmt:message key="humantask.taskview.start"/></a></li>
                <li id="stopLinkLi" style="display:none;"><a id="stopLink"><fmt:message key="humantask.taskview.stop"/></a></li>
                <li id="releaseLinkLi" style="display:none;"><a id="releaseLink"><fmt:message key="humantask.taskview.release"/></a></li>
                <li id="suspendLinkLi" style="display:none;"><a id="suspendLink"><fmt:message key="humantask.taskview.suspend"/></a></li>
                <li id="resumeLinkLi" style="display:none;"><a id="resumeLink"><fmt:message key="humantask.taskview.resume"/></a></li>
                <li id="commentLinkLi" style="display:none;"><a onclick="toggleMe('commentSection')"><fmt:message key="humantask.taskview.comment"/></a></li>
                <li id="delegateLinkLi" style="display:none;"><a onclick="HUMANTASK.handleDelegateSelection('delegateSection')"><fmt:message key="humantask.taskview.assign"/></a></li>
                <li id="changePriorityLinkLi" style="display:none;"><a onclick="HUMANTASK.handleChangePrioritySelection('changePrioritySection')"><fmt:message key="humantask.taskview.change.priority"/></a></li>
                <li id="removeLinkLi" style="display:none;"><a id="removeLink"><fmt:message key="humantask.taskview.remove"/></a></li>
                <li id="skipLinkLi" style="display:none;"><a id="skipLink"><fmt:message key="humantask.taskview.skip"/></a></li>
                <li id="failLinkLi" style="display:none;"><a id="failLink"><fmt:message key="humantask.taskview.fail"/></a></li>
                <li id="moreActionsLinkLi" style="display:none;"><a id="moreActionsLink"><fmt:message key="humantask.taskview.more.actions"/></a></li>
            </ul>
        </div>
        <div id="commentSection" style="display:none">
            <textarea id="commentTextAreaId" class="commentTextArea"></textarea>
            <input id="addCommentButton" type="button" class="button" value="Add Comment" />
            <div style="clear:both;"></div>
        </div>
        <div id="delegateSection" class="delegateDiv" style="display:none">
            <select id="assignableUserList"></select><input id="delegateButton" type="button" class="button" value="Assign" />
        </div>
        <div id="changePrioritySection" class="delegateDiv" style="display:none">
            <select id="priorityList"></select><input id="changePriorityButton" type="button" class="button" value="Set Priority" />
        </div>
        <div class="tabLessContent-noBorder" id="tabContent">
            <div id="actionTab" class="tabContentData">
                <fieldset>
                    <legend><a onclick="toggleMe('details')"><h3><fmt:message key="humantask.taskview.details"/>:</h3></a></legend>
                    <div id="details">
                        <table class="normal">
                            <tr>
                                <td class="cellHSeperator">
                                    <table class="normal">
                                        <tbody>
                                            <tr><th><fmt:message key="humantask.taskview.type"/>:</th><td id="taskTypeTxt"></td></tr>
                                            <tr><th><fmt:message key="humantask.taskview.priority"/>:</th><td id="taskPriorityTxt"></td></tr>
                                            <tr><th><fmt:message key="humantask.taskview.createdon"/>:</th><td id="taskCreatedOnDateTxt"></td></tr>
                                            <tr><th><fmt:message key="humantask.taskview.updatedon"/>:</th><td id="taskUpdatedOnDateTxt"></td></tr>
                                        </tbody>
                                    </table>
                                </td>
                                <td>
                                    <table class="normal">
                                        <tbody>
                                            <tr><th>Status:</th><td id="taskStatusTxt"></td></tr>
                                            <tr id="taskPreviousStatusTR" style="display:none;"><th><fmt:message key="humantask.taskview.previous.state"/>:</th><td id="taskPreviousStatusTxt"></td></tr>
                                        </tbody>
                                     </table>
                                </td>
                            </tr>
                        </table>

                    </div>
                </fieldset>



                <fieldset>
                    <legend><a onclick="toggleMe('description')"><h3><fmt:message key="humantask.taskview.description"/>:</h3></a></legend>
                    <div id="description">
                        <div id="descriptionTxtDiv">
                        </div>
                    </div>
                </fieldset>
                <%  // Following fields are only valid for Task
                    if ("TASK".equals(task.getTaskType().toString())) {
                %>
                <fieldset>
                    <legend><a onclick="toggleMe('people')"><h3><fmt:message key="humantask.taskview.people"/>:</h3></a></legend>
                    <div id="people">
                        <table class="normal">
                        <tbody>
                            <tr id="taskCreatedByTR" style="display:none;"><th><fmt:message key="humantask.taskview.createdby"/>:</th><td id="taskCreatedByTxt"></td></tr>
                            <tr><th>Owner:</th><td id="taskOwnerTxt"></td></tr>
                        </tbody>
                        </table>
                    </div>
                </fieldset>


                <fieldset id="requestFieldSet" style="display: none;">
                    <legend><a onclick="toggleMe('requestDiv')"><h3><fmt:message key="humantask.taskview.request"/>:</h3></a></legend>
                    <div id="requestDiv" class="dynamicContent">
                          <jsp:include page="<%=requestJSPContextPath%>"/>
                    </div>
                </fieldset>
                <%
                    if ("IN_PROGRESS".equals(task.getStatus().toString())) {
                %>
                <fieldset id="responseFormFieldSet" style="display: none;">
                    <legend><a onclick="toggleMe('responseFormDiv')"><h3><fmt:message key="humantask.taskview.response"/>:</h3></a></legend>
                    <div id="responseFormDiv" class="dynamicContent">
                          <jsp:include page="<%=outputJspContextPath%>"/>
                    </div>
                    <div id="completeButtonDiv">
                        <button id="saveTaskButton" value="Save" style="float: left; margin-right:10px;"><fmt:message key="humantask.taskview.save"/></button>
                        <button id="completeTaskButton" value="Complete" style="float: left;"><fmt:message key="humantask.taskview.complete"/></button>
                    </div>
                </fieldset>
                <%
                    } else if ("COMPLETED".equals(task.getStatus().toString())) {
                %>
                <fieldset id="responseFieldSet" style="display: none;">
                    <legend><a onclick="toggleMe('responseDiv')"><h3><fmt:message key="humantask.taskview.response"/>:</h3></a></legend>
                    <div id="responseDiv" class="dynamicContent">
                        <jsp:include page="<%=responseJspContextPath%>"/>
                    </div>
                </fieldset>
                <%
                    }
                } else if ("NOTIFICATION".equals(task.getTaskType().toString())) {
                %>
                <fieldset>
                    <legend><a onclick="toggleMe('notificationDiv')"><h3><fmt:message
                            key="humantask.taskview.request"/>:</h3></a></legend>
                    <div id="notificationDiv" class="dynamicContent">
                        <jsp:include page="<%=requestJSPContextPath%>"/>
                    </div>
                </fieldset>
                <%
                    }
                %>
            </div>
        </div>

        <%  // Following fields are only valid for Task
            if ("TASK".equals(task.getTaskType().toString())) {
        %>
        <div class="tabs_task" id="tabsDown">
            <ul>
                <%--<li><a onclick="selectTab({me:this,tabContainer:'tabsDown',tabContentContainer:'tabContentDown'})" class="selected" rel="commentsTab">Comments</a></li>--%>
                <li><a id="commentTabLink" onclick="HUMANTASK.handleTabSelection('commentsTab')" class="selected" rel="commentsTab"><fmt:message key="humantask.taskview.comments"/></a></li>
                <li><a id="eventTabLink" onclick="HUMANTASK.handleTabSelection('eventsTab')" rel="eventsTab"><fmt:message key="humantask.taskview.history"/></a></li>
                <li><a id="attachmentsTabLink" onclick="HUMANTASK.handleTabSelection('attachmentsTab')" rel="attachmentsTab"><fmt:message key="humantask.taskview.attachments"/></a></li>
            </ul>
        </div>
        <div class="tabContent" id="tabContentDown">
            <div id="commentsTab" tabindex="100" class="tabContentData">
                <%-- The task comments are populated and appended at this div --%>
            </div>
            <div id="eventsTab" tabindex="101" class="tabContentData" style="display:none;">
                <%-- The task events are populated and appended at this div --%>
            </div>
            <div id="attachmentsTab" tabindex="102" class="tabContentData" style="display:none;">
                <%-- The task attachments are populated and appended at this div --%>
            </div>
        </div>
        <%
            }
        %>
    </div>
</div>
</div>

</fmt:bundle>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ page import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAbstract" %>
<%@ page
        import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAuthorisationParams" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%
    TTaskAbstract task = (TTaskAbstract) request.getAttribute("LoadedTask");
    TTaskAuthorisationParams authParams = (TTaskAuthorisationParams) request.getAttribute("TaskAuthorisationParams");
    String client = (String) request.getAttribute("taskClient");
    String jspContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-input.jsp";
    String outputJspContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-output.jsp";
    String responseJspContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-response.jsp";
%>
<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">

<div id="task-instance-list-main">
<script type="text/javascript">

    jQuery(document).ready(function () {
        jQuery('#completeTaskButton').click(function () {
            var OUTPUT_XML = createTaskOutput();
            $.getJSON("task-operations-ajaxprocessor.jsp?operation=complete&taskClient=<%=client%>&taskId=<%=task.getId().toString().trim()%>&payLoad=" + OUTPUT_XML,
                    function (json) {
                        if (json.TaskCompleted == 'true') {
                            location.reload(true);
                        } else {
                            CARBON.showErrorDialog("<fmt:message key="humantask.task.completion.failed"/>");
                            return true;
                        }
                    });
        });


        jQuery('#claimTaskButton').click(function () {
            $.getJSON("task-operations-ajaxprocessor.jsp?operation=claim&taskClient=<%=client%>&taskId=<%=task.getId().toString().trim()%>",
                    function (json) {
                        if (json.TaskClaimed == 'true') {
                            location.reload(true);
                        } else {
                            CARBON.showErrorDialog("<fmt:message key="humantask.task.claim.failed"/>");
                            return true;
                        }
                    });
        });

        jQuery('#releaseTaskButton').click(function () {
            $.getJSON("task-operations-ajaxprocessor.jsp?operation=release&taskClient=<%=client%>&taskId=<%=task.getId().toString().trim()%>",
                    function (json) {
                        if (json.TaskReleased == 'true') {
                            location.reload(true);
                        } else {
                            CARBON.showErrorDialog("<fmt:message key="humantask.task.release.failed"/>");
                            return true;
                        }
                    });
        });

        jQuery('#startTaskButton').click(function () {
            $.getJSON("task-operations-ajaxprocessor.jsp?operation=start&taskClient=<%=client%>&taskId=<%=task.getId().toString().trim()%>",
                    function (json) {
                        if (json.TaskStarted == 'true') {
                            location.reload(true);
                        } else {
                            CARBON.showErrorDialog("<fmt:message key="humantask.task.start.failed"/>");
                            return true;
                        }
                    });
        });

        jQuery('#stopTaskButton').click(function () {
            $.getJSON("task-operations-ajaxprocessor.jsp?operation=stop&taskClient=<%=client%>&taskId=<%=task.getId().toString().trim()%>",
                    function (json) {
                        if (json.TaskStopped == 'true') {
                            location.reload(true);
                        } else {
                            CARBON.showErrorDialog("<fmt:message key="humantask.task.stop.failed"/>");
                            return true;
                        }
                    });
        });
    });


</script>

<%

    String presentationName = HumanTaskUIUtil.getTaskPresentationHeader(task.getPresentationSubject(),
            task.getPresentationName());

%>
<h2><%=presentationName%>
</h2>


<div id="workArea">
    <div id="task-instance-list-dashboard" style="overflow-y:scroll;">
        <%
            if (task != null) {
        %>
        <table class="styledLeft" id="taskViewTable">
            <thead>
            <tr>
                <th class="tvTableHeader" colspan="2">
                        <span style="float:left;padding-top:3px">
                            <%=task.getPresentationName()%>
                        </span>
                </th>
            </tr>
            </thead>

            <tbody>
            <tr>
                <td><fmt:message key="humantask.taskview.actions"/></td>
                <td>
                    <span id="buttonspan"
                          style="float:left;font-size:9px;padding-right:5px;padding-top:3px;padding-bottom:3px;">
                        <%
                            if (authParams.getAuthorisedToClaim()) {
                        %>
                                <button id="claimTaskButton" value="Claim">Claim</button>
                        <%
                            }
                        %>

                        <%
                            if (authParams.getAuthorisedToRelease()) {
                        %>
                                <button id="releaseTaskButton" value="Release">Release</button>
                        <%
                            }
                        %>


                        <%
                            if (authParams.getAuthorisedToStart()) {
                        %>
                                <button id="startTaskButton" value="Start">Start</button>
                        <%
                            }
                        %>


                        <%
                            if (authParams.getAuthorisedToStop()) {
                        %>
                                <button id="stopTaskButton" value="Stop">Stop</button>
                        <%
                            }
                        %>

                    </span>
                </td>

            </tr>

            <tr>
                <td><fmt:message key="humantask.taskview.subject"/></td>
                <td><%=task.getPresentationSubject()%>
                </td>
            </tr>

            <tr>
                <td><fmt:message key="humantask.taskview.type"/></td>
                <td><%=task.getTaskType()%>
                </td>
            </tr>

            <tr>
                <td><fmt:message key="humantask.taskview.status"/></td>
                <td><%=task.getStatus()%>
                </td>
            </tr>

            <tr>
                <td><fmt:message key="humantask.taskview.priority"/></td>
                <td><%=task.getPriority()%>
                </td>
            </tr>

            <tr>
                <td><fmt:message key="humantask.taskview.owner"/></td>
                <td>
                    <%
                        if (task.getActualOwner() != null) {
                            out.print(task.getActualOwner().getTUser());
                        } else {
                    %>
                    <fmt:message
                            key="humantask.taskview.cannot.determine.actual.owner"/>
                    <%
                        }
                    %>
                </td>
            </tr>

            <tr>
                <td><fmt:message key="humantask.taskview.createdon"/></td>
                <td><%=task.getCreatedTime().getTime().toString()%>
                </td>
            </tr>


            <tr>
                <td colspan="2">
                    <table class="noBorders" style="border:none;width:100%;">
                        <tbody>
                        <tr>
                            <td id="taskRequestHeader"
                                style="border-bottom:1px solid #CCCCCC;padding-left:0px">
                                        <span class="tv_underline_header">
                                            <fmt:message key="humantask.taskview.request"/>
                                        </span>
                            </td>
                        </tr>

                        <tr>
                            <td id="taskRequestContent" style="padding-left:0px">
                                <div style="background-color:transparent;border:1px solid #CCCCCC;color:#333333;font-family:monospace;padding:4px;">
                                    <jsp:include page="<%=jspContextPath%>"/>
                                </div>
                            </td>
                        </tr>

                        <tbody>
                        <tr>
                            <td id="taskResponseHeader"
                                style="border-bottom:1px solid #CCCCCC;padding-left:0px">
                                        <span class="tv_underline_header">
                                            <fmt:message key="humantask.taskview.response"/>
                                        </span>
                            </td>
                        </tr>

                        <tr>
                            <td id="taskResponseContent" style="padding-left:0px">
                                <div style="background-color:transparent;border:1px solid #CCCCCC;color:#333333;font-family:monospace;padding:4px;">
                                    <%
                                        if (!"NOTIFICATION".equals(task.getTaskType().toString())) {
                                            if ("COMPLETED".equals(task.getStatus().toString())) {
                                    %>
                                    <jsp:include page="<%=responseJspContextPath%>"/>
                                    <%
                                    } else if (authParams.getAuthorisedToComplete()) {
                                    %>
                                    <jsp:include page="<%=outputJspContextPath%>"/>
                                    <%
                                            }
                                        }
                                    %>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>

            <%
                if (authParams.getAuthorisedToComplete()) {
            %>
            <tr>
                <td></td>
                <td>
                                <span id="completeButtonSpan"
                                      style="float:left;font-size:9px;padding-right:5px;padding-top:3px;padding-bottom:3px;">
                                    <button id="completeTaskButton" value="Complete">Complete
                                    </button>
                                </span>
                </td>
            </tr>

            <%
                }
            %>
            </tbody>

        </table>
        <%
        } else {
        %>
        <fmt:message key="humantask.taskview.nulltask"/>
        <%
            }
        %>
    </div>
</div>
</div>

</fmt:bundle>
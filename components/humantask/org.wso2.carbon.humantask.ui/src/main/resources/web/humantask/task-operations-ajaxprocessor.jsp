<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.axis2.databinding.types.URI" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.humantask.ui.clients.HumanTaskClientAPIServiceClient" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Log log = LogFactory.getLog("task_view_new.jsp");
    response.setHeader("Cache-Control",
                       "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
    response.setContentType("application/json; charset=UTF-8");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String payLoad = CharacterEncoder.getSafeText(request.getParameter("payLoad"));
    String taskId = CharacterEncoder.getSafeText(request.getParameter("taskId"));
    String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
    String taskClient = CharacterEncoder.getSafeText(request.getParameter("taskClient"));
    String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);

    String cookie = null;
    if (taskClient != null && !"".equals(taskClient) && "gadget".equals(taskClient)) {
        String cookieString = request.getHeader("Cookie");
        cookie = HumanTaskUIUtil.getCookieSessionId(cookieString);
    } else {
        cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    }


    String taskOperationJson = "";

    boolean isAuthenticatedToManageHumanTasks = true;

    if (isAuthenticatedToManageHumanTasks) {
        HumanTaskClientAPIServiceClient taskOperationsClient;

        try {
            taskOperationsClient = new HumanTaskClientAPIServiceClient(cookie, backendServerURL,
                                                                       configContext);
            JSONObject taskOperationJsonObject = new JSONObject();
	if (cookie != null) {
            if ("complete".equals(operation) && payLoad != null) {
                taskOperationsClient.complete(new URI(taskId), payLoad);
                taskOperationJsonObject.put("TaskCompleted", "true");
            } else if ("save".equals(operation) && payLoad != null) {
                taskOperationsClient.setTaskOutput(new URI(taskId), payLoad);
                taskOperationJsonObject.put("TaskSetOutput", "true");
            }else if ("start".equals(operation)) {
                taskOperationsClient.start(new URI(taskId));
                taskOperationJsonObject.put("TaskStarted", "true");
            } else if ("stop".equals(operation)) {
                taskOperationsClient.stop(new URI(taskId));
                taskOperationJsonObject.put("TaskStopped", "true");
            } else if ("claim".equals(operation)) {
                taskOperationsClient.claim(new URI(taskId));
                taskOperationJsonObject.put("TaskClaimed", "true");
            } else if ("release".equals(operation)) {
                taskOperationsClient.release(new URI(taskId));
                taskOperationJsonObject.put("TaskReleased", "true");
            } else if ("addComment".equals(operation)) {
                String commentText = request.getParameter("commentText");
                taskOperationsClient.addComment(new URI(taskId), commentText);
                taskOperationJsonObject.put("CommentAdded", "true");
            } else if ("deleteComment".equals(operation)) {
                String commentId = request.getParameter("commentId");
                taskOperationsClient.deleteComment(new URI(taskId), new URI(commentId));
                taskOperationJsonObject.put("CommentDeleted", "true");
            } else if ("suspend".equals(operation)) {
                taskOperationsClient.suspend(new URI(taskId));
                taskOperationJsonObject.put("TaskSuspended", "true");
            } else if ("resume".equals(operation)) {
                taskOperationsClient.resume(new URI(taskId));
                taskOperationJsonObject.put("TaskResumed", "true");
            } else if ("delegate".equals(operation)) {
                String delegatee = request.getParameter("delegatee");
                taskOperationsClient.delegate(new URI(taskId) , delegatee);
                taskOperationJsonObject.put("TaskDelegated", "true");
            } else if ("skip".equals(operation)) {
                taskOperationsClient.skip(new URI(taskId));
                taskOperationJsonObject.put("TaskSkipped", "true");
            } else if ("fail".equals(operation)) {
                taskOperationsClient.fail(new URI(taskId));
                taskOperationJsonObject.put("TaskFailed", "true");
            } else if ("remove".equals(operation)) {
                taskOperationsClient.remove(new URI(taskId));
                taskOperationJsonObject.put("TaskRemoved", "true");
            } else if ("changePriority".equals(operation)) {
                String priorityStr = request.getParameter("priority");
                int priorityInt = Integer.valueOf(priorityStr);
                taskOperationsClient.changePriority(new URI(taskId), priorityInt);
                taskOperationJsonObject.put("TaskPriorityChanged", "true");
            }

            taskOperationJson = taskOperationJsonObject.toJSONString();

    } else {
        log.warn("Session timeout");
        String msg = "Session is expired";
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        if (webContext != null) {
            response.sendRedirect(webContext + "/admin/login.jsp");
        } else {
            response.sendRedirect("" + "/admin/login.jsp");
        }
    }
        } catch (Exception e) {
            log.error("Error occurred in the task-operations " + operation + "method", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
    }
    out.print(taskOperationJson);
    out.flush();
%>


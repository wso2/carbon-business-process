<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.axis2.databinding.types.URI" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.humantask.ui.clients.HumanTaskClientAPIServiceClient" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.*" %>
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

    String taskId = CharacterEncoder.getSafeText(request.getParameter("taskId"));
    String taskClient = CharacterEncoder.getSafeText(request.getParameter("taskClient"));
    String loadParam = CharacterEncoder.getSafeText(request.getParameter("loadParam"));

    String cookie = null;
    if (taskClient != null && !"".equals(taskClient) && "gadget".equals(taskClient)) {
        String cookieString = request.getHeader("Cookie");
        cookie = HumanTaskUIUtil.getCookieSessionId(cookieString);
    } else {
        cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    }

    String taskDetailsJSONString = "";

    HumanTaskClientAPIServiceClient taskOperationsClient;

    try {
        taskOperationsClient = new HumanTaskClientAPIServiceClient(cookie, backendServerURL,
                                                                   configContext);
        if ("taskDetails".equals(loadParam)) {
            TTaskAbstract task = taskOperationsClient.loadTask(new URI(taskId));
            taskDetailsJSONString = HumanTaskUIUtil.loadTaskDetailsJSONString(task);
        } else if ("taskComments".equals(loadParam)) {
            TComment[] comments = taskOperationsClient.getComments(new URI(taskId));
            taskDetailsJSONString = HumanTaskUIUtil.loadTaskCommentsJSONString(comments);
        } else if ("authParams".equals(loadParam)) {
            TTaskAuthorisationParams authParams = taskOperationsClient.getTaskParams(new URI(taskId));
            taskDetailsJSONString = HumanTaskUIUtil.loadTaskAuthParamsJSONString(authParams);
        } else if ("taskEvents".equals(loadParam)) {
            TTaskEvents taskEvents = taskOperationsClient.getTaskEvents(new URI(taskId));
            taskDetailsJSONString = HumanTaskUIUtil.loadTaskEventsJSONString(taskEvents);
        } else if ("assignableUsers".equals(loadParam)) {
            TUser[] assignableUsers = taskOperationsClient.getTaskAssignableUsers(new URI(taskId));
            taskDetailsJSONString = HumanTaskUIUtil.loadUserJSONString(assignableUsers);
        } else if ("taskAttachments".equals(loadParam)) {
            TAttachmentInfo[] attachmentInfos = taskOperationsClient.getAttachmentInfos(new URI(taskId));
            taskDetailsJSONString = HumanTaskUIUtil.loadTaskAttachmentsJSONString(attachmentInfos);
        }

    } catch (Exception e) {
        log.error("Error occurred when loading task", e);
        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

    out.print(taskDetailsJSONString);
    out.flush();
%>


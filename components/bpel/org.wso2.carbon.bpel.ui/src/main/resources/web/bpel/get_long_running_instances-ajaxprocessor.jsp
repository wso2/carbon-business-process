<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.InstanceManagementServiceClient" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    response.setHeader("Cache-Control",
                "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
    response.setContentType("application/json; charset=UTF-8");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String longRunningJson = "";

    boolean isAuthenticatedForProcessManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");
    boolean isAuthenticatedForInstanceManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/instances");
    boolean isAuthenticatedForInstanceMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");

    /**
     * rationale for this logical condition is user should have monitor/bpel permission in-order to
     * view summary
     */
    if (isAuthenticatedForInstanceMonitor && (isAuthenticatedForInstanceManagement ||
                                              isAuthenticatedForProcessManagement)) {
        String processId = CharacterEncoder.getSafeText(request.getParameter("processId"));
        if (processId == null) {
            processId = "all";
        }

        InstanceManagementServiceClient instanceClient;

        try {
            instanceClient = new InstanceManagementServiceClient(cookie, backendServerURL, configContext);
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }

        if (processId.equalsIgnoreCase("all")) {
            String instanceFilter = "status=ACTIVE";
            String orderBy = "+started";
            int limit = 10;
            try {
                PaginatedInstanceList longRunningInstances = instanceClient.
                                        getPaginatedInstanceList(instanceFilter, orderBy, limit, 0);
                longRunningJson = BpelUIUtil.
                        createLongRunningInstanceJSONString(longRunningInstances);
            } catch (Exception e) {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                return;
            }
        } else {
            String instanceFilter = "pid=" + processId + " status=ACTIVE";
            String orderBy = "+started";
            int limit = 10;
            try {
                PaginatedInstanceList longRunningInstances = instanceClient.
                                        getPaginatedInstanceList(instanceFilter, orderBy, limit, 0);
                longRunningJson = BpelUIUtil.
                        createLongRunningInstanceJSONString(longRunningInstances);
            } catch (Exception e) {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                return;
            }
        }
    }
out.print(longRunningJson);
out.flush();
%>


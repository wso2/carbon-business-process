<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.InstanceManagementServiceClient" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.InstanceSummaryE" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
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

    // Can use to filter instance summary based on processes
    String processId = CharacterEncoder.getSafeText(request.getParameter("processId")).trim();

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ProcessManagementServiceClient processMgtClient;
    InstanceManagementServiceClient instanceMgtClient;
    QName pid = QName.valueOf(processId);
    JSONObject instanceSummaryJSON = new JSONObject();
    ProcessInfoType processInfo;
    InstanceSummaryE instanceSummary;

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
        try {
            processMgtClient = new ProcessManagementServiceClient(cookie, backendServerURL,
                                                                  configContext,
                                                                  request.getLocale());
            instanceMgtClient = new InstanceManagementServiceClient(cookie, backendServerURL,
                                                                  configContext);
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }

        if(processId.equalsIgnoreCase("all")) {
            try{
                instanceSummary = instanceMgtClient.getInstanceSummary();
                instanceSummaryJSON = BpelUIUtil.createInstanceSummaryJSONObject(instanceSummary);
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
            try {
                processInfo = processMgtClient.getProcessInfo(pid);
                instanceSummaryJSON = BpelUIUtil.createInstanceSummaryJSONObject(processInfo.getInstanceSummary().getInstances());
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

    out.print(instanceSummaryJSON);
    out.flush();
%>


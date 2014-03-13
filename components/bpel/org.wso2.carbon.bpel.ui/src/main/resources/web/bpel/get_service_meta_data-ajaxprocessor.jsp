<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
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
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String serviceName = CharacterEncoder.getSafeText(request.getParameter("serviceName"));
    String serviceMetaDataJson = "";

    boolean isAuthenticatedForProcessManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");

    if (isAuthenticatedForProcessManagement && serviceName != null) {
        ProcessManagementServiceClient processMgtClient;

        try {
            processMgtClient = new ProcessManagementServiceClient(cookie, backendServerURL,
                                                                  configContext,
                                                                  request.getLocale());
            ServiceMetaData serviceMetaData = processMgtClient.getServiceMetaData(serviceName);
            if (serviceMetaData != null) {
                JSONObject serviceJsonObject = new JSONObject();
                if (serviceMetaData.getSecurityScenarioId() != null) {
                    serviceJsonObject.put("SecurityScenarioId",
                                          serviceMetaData.getSecurityScenarioId());
                    serviceMetaDataJson = serviceJsonObject.toJSONString();
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
        if (serviceMetaDataJson.equals("")) {
            JSONObject serviceJsonObject = new JSONObject();
            serviceJsonObject.put("SecurityScenarioId", "-1");
            serviceMetaDataJson = serviceJsonObject.toJSONString();
        }
    }
out.print(serviceMetaDataJson);
out.flush();
%>


<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics" %>
<%@ page import="org.wso2.carbon.statistics.ui.StatisticsAdminClient" %>
<%@ page import="org.wso2.carbon.statistics.ui.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    response.setHeader("Cache-Control", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    StatisticsAdminClient client = new StatisticsAdminClient(cookie, backendServerURL,
                                                             configContext, request.getLocale());

    SystemStatistics systemStats;
    JSONObject memInfo = null;
    try {
        systemStats = client.getSystemStatistics();
        if(systemStats.getUsedMemory() != null){
            memInfo = BpelUIUtil.getMemoryInfoJSONObject(systemStats.getUsedMemory(), systemStats.getTotalMemory());
        }
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

    if(memInfo != null){
        out.print(memInfo);
    }else {
        out.print("");
    }
    out.flush();
%>

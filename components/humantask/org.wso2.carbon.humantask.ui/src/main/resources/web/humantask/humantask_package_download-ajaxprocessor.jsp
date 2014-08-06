<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.humantask.ui.clients.HumanTaskPackageManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    HumanTaskPackageManagementServiceClient packageManagementServiceClient;

    String humanTaskPackageName = CharacterEncoder.getSafeText(request.getParameter("packageName"));
    out.clear();
    out = pageContext.pushBody();
    out.clearBuffer();
    packageManagementServiceClient = new HumanTaskPackageManagementServiceClient(cookie, backendServerURL, configContext);
    packageManagementServiceClient.downloadHumanTaskPackage(humanTaskPackageName, response);
    out.close();

%>
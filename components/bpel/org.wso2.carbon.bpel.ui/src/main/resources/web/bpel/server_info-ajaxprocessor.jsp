<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics" %>
<%@ page import="org.wso2.carbon.statistics.ui.StatisticsAdminClient" %>
<%@ page import="org.wso2.carbon.statistics.ui.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

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
    try {
        systemStats = client.getSystemStatistics();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
    <table class="styledLeft" width="100%">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="server.info"/></th>
                    </tr>
                    </thead>
<%
            if( systemStats.getTotalMemory() == null){
%>
                    <tr class="tableOddRow">
                        <td><fmt:message key="server.info.not.available"/></td>
                    </tr>
<%
            } else {
%>
                    <tr class="tableOddRow">
                        <td width="30%"><fmt:message key="host"/></td>
                        <td><%= systemStats.getServerName()%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="server.start.time"/></td>
                        <td><%= systemStats.getServerStartTime()%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="system.up.time"/></td>
                        <td><%= systemStats.getSystemUpTime()%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="memory.allocated"/></td>
                        <td><%= systemStats.getTotalMemory().getValue()%><%= systemStats.getTotalMemory().getUnit()%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="memory.usage"/></td>
                        <td><%= systemStats.getUsedMemory().getValue()%><%= systemStats.getUsedMemory().getUnit()%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="java.version"/></td>
                        <td><%= systemStats.getJavaVersion()%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="server.version"/></td>
                        <td><%= systemStats.getWso2WsasVersion()%>
                        </td>
                    </tr>
<%
        }
%>
                </table>
</fmt:bundle>
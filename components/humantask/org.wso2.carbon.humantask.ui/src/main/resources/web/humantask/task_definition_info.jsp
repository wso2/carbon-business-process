<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.wso2.carbon.humantask.stub.mgt.types.TaskInfoType" %>
<%@ page import="org.wso2.carbon.humantask.ui.clients.HumanTaskPackageManagementServiceClient" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    response.setHeader("Cache-Control",
                       "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    boolean isAuthorizedToMonitor = true; // TODO - set permissions
    boolean isAuthorizedToManageProcesses = true; // TODO - set permissions

    HumanTaskPackageManagementServiceClient serviceClient;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


    String taskDefId = CharacterEncoder.getSafeText(request.getParameter("taskDefId"));

    QName taskId = QName.valueOf(taskDefId);
    TaskInfoType taskInfoType = null;
    String taskDefPrettyPrinted = null;
    try {
        serviceClient = new HumanTaskPackageManagementServiceClient(cookie, backendServerURL,
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


%>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">
    <carbon:breadcrumb
            label="humantask.task.info"
            resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <div id="package-list-main">
            <h2><fmt:message key="humantask.task.definition"/>&nbsp;(<%=taskDefId%>)</h2>

            <div id="workArea">

                <%
                    if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
                %>
                <div id="humantask-task-definition-dashboard">
                    <%
                        taskInfoType = serviceClient.getTaskInfo(taskId);
                        taskDefPrettyPrinted = HumanTaskUIUtil.prettyPrint(HumanTaskUIUtil.getTaskDefinition(taskInfoType));
                    %>
                    <table width="100%" cellspacing="0" cellpadding="0" border="0">

                        <tr>
                            <td colspan="3">&nbsp;</td>
                        </tr>

                        <tr>
                            <td colspan="3">
                                <table class="styledLeft" id="taskDefinitionTable"
                                       style="margin-left: 0px;" width="100%">
                                    <thead>
                                    <tr>
                                        <th align="left">Task Definition:</th>
                                    </tr>
                                    </thead>
                                    <tr>
                                        <td>
                                            <textarea id="xmlPay" name="design"
                                                      style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                                      rows="50"><%=taskDefPrettyPrinted%>
                                            </textarea>
                                            <script type="text/javascript">
                                                jQuery(document).ready(function() {
                                                    document.getElementById("xmlPay").value = editAreaLoader.getValue("xmlPay");
                                                });
                                            </script>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="3">&nbsp;</td>
                        </tr>

                    </table>


                </div>

                <%
                } else {
                %>
                <p><fmt:message key="do.not.have.permission.to.view.process.details"/></p>
                <%
                    }
                %>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        editAreaLoader.init({
                                id : "xmlPay"        // textarea id
                                ,syntax: "xml"            // syntax to be uses for highgliting
                                ,start_highlight: true        // to display with highlight mode on start-up
                                ,is_editable: false
                            });
    </script>

</fmt:bundle>
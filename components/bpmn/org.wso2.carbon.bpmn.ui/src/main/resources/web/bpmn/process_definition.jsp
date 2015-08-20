<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="java.awt.Image" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bpmn.ui.WorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNProcess" %>
<%@ page import="org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNDeployment" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">
    <%
        String processId = CharacterEncoder.getSafeText(request.getParameter("processID"));
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                                                                          .getAttribute(
                                                                                  CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        WorkflowServiceClient client;
        BPMNProcess process;
        try {
            client = new WorkflowServiceClient(cookie, serverURL, configContext);
            process = client.getProcessById(processId);
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
            return;
        }
    %>

    <link href="css/prettify.css" type="text/css" rel="stylesheet"/>
    <script type="text/javascript" src="js/prettify.js"></script>
    <script type="text/javascript" src="js/run_prettify.js"></script>
    <script type="text/javascript">
        window.onload = prettyPrint();
    </script>
    <carbon:breadcrumb
            label="bpmn.process.info"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="bpmn.process.info"/> (<%=processId%>)</h2>

        <div id="workArea">
            <table class="styledLeft" id="moduleTable">
                <thead>
                <tr>
                    <th colspan="2" width="40%"><fmt:message key="bpmn.process.details"/></th>
                    <td border="none" width="60%"></td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><fmt:message key="bpmn.process.id"/></td>
                    <td><%=process.getProcessId()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bpmn.process.name"/></td>
                    <td><%=process.getName()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bpmn.process.version"/></td>
                    <td><%=process.getVersion()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bpmn.process.deployedDate"/></td>
                    <td><%=process.getDeploymentTime().toString()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bpmn.package.name"/></td>
                    <td><%=process.getDeploymentName() + "-" + process.getDeploymentId()%>
                    </td>
                </tr>
                </tbody>
            </table>
            <br/><br/>
            <table class="styledLeft" id="moduleTable">
                <thead>
                <tr>
                    <th width="100%"><fmt:message key="bpmn.process.definition"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <!--?prettify lang=html linenums=true?-->
                        <pre class="prettyprint linenums"
                             style="height: 35em; overflow:scroll; white-space: pre-wrap;">
                              <%=client.getProcessModel(processId)%>
                        </pre>
                    </td>
                </tr>
                </tbody>
            </table>
            <br/><br/>
            <table class="styledLeft" id="moduleTable">
                <thead>
                <tr>
                    <th width="100%"><fmt:message key="bpmn.process.diagram"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <%
                        try {
                            String processDiagramString = client.getProcessDiagram(processId);
                            if (processDiagramString == null) {
                    %>
                    <td><fmt:message key="bpmn.diagram.not.define"/></td>
                    <%
                    } else {
                    %>
                    <td><img src="<%=processDiagramString%>"/></td>
                    <%
                        }
                    } catch (Exception e) {
                    %>
                    <td><fmt:message key="error.loading.image"/></td>
                    <%
                        }
                    %>

                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>

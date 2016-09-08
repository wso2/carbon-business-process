<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bpmn.ui.WorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.businessprocesses.common.utils.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">
<%
    String deploymentName = CharacterEncoder.getSafeText(request.getParameter("deploymentName"));
%>
    <script type="text/javascript">
        function deletePackage() {
            function deleteYes() {
                $.ajax({
                    type: 'POST',
                    url: location.protocol + "//" + location.host + "/carbon/bpmn/process_list_view.jsp?region=region1&item=bpmn_menu&operation=undeploy&deploymentName=<%=deploymentName%>",
                    success: function(data){
                        window.location = location.protocol + "//" + location.host + "/carbon/bpmn/process_list_view.jsp?region=region1&item=bpmn_menu";
                    }
                });
            }
            sessionAwareFunction(function() {
                CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.package"/>' + ' <%=deploymentName%>?', deleteYes, null);
            }, "<fmt:message key="session.timed.out"/>");
            return false;
        }
    </script>
    <carbon:breadcrumb
            label="bpmn.package.dashboard"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
<div id="middle">

    <h2><fmt:message key="bpmn.package.dashboard"/> (<%=deploymentName%>)</h2>

    <div id="workArea">
        <table class="styledLeft" id="moduleTable">
            <thead>
            <tr>
                <th width="40%"><fmt:message key="bpmn.package.dashboard.actions"/></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><a href="javascript:deletePackage();">
                    <img src="images/undeploy.gif">&nbsp;<fmt:message key="bpmn.package.undeploy"/></a></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</fmt:bundle>
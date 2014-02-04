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
    <%@ page import="org.apache.http.HttpStatus" %>
    <%@ page import="org.wso2.carbon.CarbonConstants" %>
    <%@ page import="org.wso2.carbon.bpel.stub.mgt.types.EndpointRef_type0" %>
    <%@ page import="org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType" %>
    <%@ page import="org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus" %>
    <%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
    <%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
    <%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
    <%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
    <%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
    <%@ page import="org.wso2.carbon.utils.ServerConstants" %>
    <%@ page import="javax.xml.namespace.QName" %>
    <%@ page import="java.util.HashMap" %>
    <%@ page import="java.util.Map" %>
    <%@ page import="java.util.TreeMap" %>
    <%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
    <%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

    <fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">

    <%
        response.setHeader("Cache-Control", "no-cache");

        boolean isAuthorizedToManageProcesses =
                    CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");
        boolean isAuthorizedToMonitor =
                    CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");
        boolean isAuthorizedToManageServices =
                    CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/modify/service");

        String processID = CharacterEncoder.getSafeText(request.getParameter("Pid"));
        String operation = CharacterEncoder.getSafeText(request.getParameter("operation"));
        String packageName = null;
        String processDefPrettyPrinted = null;
        String processVersion = null;
        String processOlderVersion = null;
        String processStatus = null;
        String processDeployedDate = null;
        String totalNoOfInstances = null;
        ProcessManagementServiceClient processMgtClient;

        Map<String, QName> refMap = new TreeMap<String, QName>(); //Used to get partnerlinks
        Map<String, EndpointRef_type0> partnerLinkEprMap = new HashMap<String, EndpointRef_type0>();

        String userAgent = request.getHeader( "User-Agent" );
        //boolean isFirefox = ( userAgent != null && userAgent.indexOf( "Firefox/" ) != -1 );
        boolean isMSIE = ( userAgent != null && userAgent.indexOf( "MSIE" ) != -1 );


        if (processID != null && processID.trim().length() > 0 &&
            (isAuthorizedToMonitor || isAuthorizedToManageProcesses)) {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                                                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            QName pid = QName.valueOf(processID);
            ProcessInfoType processInfo = null;
            try {
                processMgtClient = new ProcessManagementServiceClient(cookie, backendServerURL,
                                                                      configContext,
                                                                      request.getLocale());
            } catch (Exception e) {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
    <jsp:include page="../admin/error.jsp"/>
    <%
                return;
            }

            if (operation != null && isAuthorizedToManageProcesses) {
                if (operation.toLowerCase().equals("retire")) {
                    if (processID != null && processID.length() > 0) {

                        try {
                            processMgtClient.retireProcess(pid);

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
                } else if (operation.toLowerCase().equals("activate")) {
                    if (processID != null && processID.length() > 0) {
                        try {

                            processMgtClient.activateProcess(pid);

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
            }

            if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
                try {
                    processInfo = processMgtClient.getProcessInfo(pid);
                } catch (Exception e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
    <jsp:include page="../admin/error.jsp"/>
    <%
                    return;
                }

                packageName = processInfo.getDeploymentInfo().getPackageName();
                processVersion = String.valueOf(processInfo.getVersion());
                processOlderVersion = String.valueOf(processInfo.getOlderVersion());
                processStatus = processInfo.getStatus().getValue();
                processDeployedDate = processInfo.getDeploymentInfo().getDeployDate().getTime().toString();

                processDefPrettyPrinted = BpelUIUtil.prettyPrint(BpelUIUtil.getProcessDefinition(processInfo));

                refMap = BpelUIUtil.getEndpointRefsMap(processInfo);
                partnerLinkEprMap = BpelUIUtil.getEndpointReferences(processInfo);

                totalNoOfInstances = Integer.toString(BpelUIUtil.getTotalInstance(processInfo));
    %>

    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript" src="js/bpel-main.js"></script>
    <!--[if IE]><script type="text/javascript" src="js/flot/excanvas.min.js"></script><![endif]-->
    <script type="text/javascript" src="js/flot/jquery.flot.js"></script>

    <script type="text/javascript">



        jQuery(document).ready(function() {
            //BPEL.process.drawInstanceSummaryGraph();
            BPEL.summary.drawInstanceSummary("<%=processInfo.getPid()%>", "process-instance-summary")
            var wid = jQuery("#process-details").width();
            jQuery("#process-definition").width(wid);
            jQuery("#bpel2svg").width(wid - 2);

            $(window).resize(function(){
                var wid = jQuery("#process-details").width();         //TODO: Update
                jQuery("#processDefinitionTable").width(wid);
                jQuery("#bpel2svg").width(wid - 2);

            });
        });

        function submitHiddenForm(action) {
            document.getElementById("hiddenField").value = location.href + "&ordinal=1";
            document.dataForm.action = action;
            document.dataForm.submit();
        }

        /**
         * change the link parameters inside the #wsdlTable based on the #partnerLinkSelectorForWSDLTable selected value
         * @param partnerLinkName
         */
        function changepartnerLinkValueForWSDLTable(partnerLinkName) {
            jQuery(document).ready(function() {
                //jQuery.noConflict();
                //var serviceLocalName = document.getElementById(partnerLinkName).title;
                //var serviceName = document.getElementById(partnerLinkName).title;
                var partnerLinkURLJSON = {
    <%
            for(Map.Entry<String, EndpointRef_type0> entry : partnerLinkEprMap.entrySet()) {
                String serviceLocationValue = null;
                if(entry.getValue().getServiceLocations().getServiceLocation()[0].endsWith("?tryit")) {
                    serviceLocationValue =  entry.getValue().getServiceLocations().getServiceLocation()[0].split("\\?")[0];
                } else if (entry.getValue().getServiceLocations().getServiceLocation()[1].endsWith("?tryit")) {
                    serviceLocationValue =  entry.getValue().getServiceLocations().getServiceLocation()[1].split("\\?")[0];
                }
    %>
                    "<%=entry.getKey()%>" : "<%=serviceLocationValue%>",
    <%
            }
        }
    %>
                    " ":" "
                };

                var partnerLinkWSDLURLJSON = {
                    <%
                        for(Map.Entry<String, EndpointRef_type0> entry : partnerLinkEprMap.entrySet()) {
                            String serviceLocationValue = null;
                            if(entry.getValue().getServiceLocations().getServiceLocation()[0].endsWith("?wsdl")) {
                                serviceLocationValue =  entry.getValue().getServiceLocations().getServiceLocation()[1].split("\\?")[0];
                            } else if (entry.getValue().getServiceLocations().getServiceLocation()[1].endsWith("?wsdl")) {
                                serviceLocationValue =  entry.getValue().getServiceLocations().getServiceLocation()[1].split("\\?")[0];
                            }
                    %>
                        "<%=entry.getKey()%>" : "<%=serviceLocationValue%>",
                    <%
                            }
                    %>
                    " ":" "
                };

                jQuery("#qos_tryit").attr("href", partnerLinkURLJSON[partnerLinkName] + "?tryit");
                jQuery("#qos_wsdl").attr("href", partnerLinkWSDLURLJSON[partnerLinkName] + "?wsdl");
                jQuery("#qos_wsdl2").attr("href", partnerLinkWSDLURLJSON[partnerLinkName] + "?wsdl2");
            });
        }

        /**
         * change the link parameters inside the #serviceOperationsParentTable based on the #partnerLinkSelectorForQoS selected value
         * @param partnerLinkName
         */
        function changepartnerLinkValueForQoS(partnerLinkName) {
            var serviceLocalName = document.getElementById(partnerLinkName).title;

            $.getJSON("get_service_meta_data-ajaxprocessor.jsp?serviceName=" + serviceLocalName,
                    function(json) {
                if (json.SecurityScenarioId == -1) {
                    jQuery("#qos_security_icon").html(
                            "<img src='../service-mgt/images/unsecured.gif' title='Unsecured'/>");
                } else {
                    jQuery("#qos_security_icon").html(
                            "<img src='../service-mgt/images/secured.gif' title='Secured using " +
                            json.SecurityScenarioId +"'/>");
                }
            });

            jQuery("#qos_security").attr("href", "../securityconfig/index.jsp?serviceName=" + serviceLocalName);
            jQuery("#qos_policy").attr("href", "../service-mgt/policy_editor_proxy.jsp?serviceName=" + serviceLocalName);
            jQuery("#qos_rm").attr("href", "../rm/index.jsp?serviceName=" + serviceLocalName + "&backURL="+ location.href);
            jQuery("#qos_transport").attr("href", "../transport-mgt/service_transport.jsp?serviceName=" + serviceLocalName);
            jQuery("#qos_caching").attr("href", "../caching/index.jsp?serviceName=" + serviceLocalName + "&backURL="+ location.href);
            jQuery("#qos_modules").attr("href", "../modulemgt/service_modules.jsp?serviceName=" + serviceLocalName);
            jQuery("#qos_throttling").attr("href", "../throttling/index.jsp?serviceName=" + serviceLocalName + "&backURL="+ location.href);
            jQuery("#qos_operations").attr("href", "../operation/index.jsp?serviceName=" + serviceLocalName);

            return serviceLocalName;
        }
    </script>
    <%
        }
    %>
    <style type="text/css">
        #process-instance-summary {
            width: 400px;
            height: 250px;
            margin-top: 10px;
        }
    </style>

    <link type="text/css" rel="stylesheet" href="css/style.css"/>
    <%--<link media="all" type="text/css" rel="stylesheet" href="css/xmlverbatim.css">--%>
    <carbon:breadcrumb
            label="bpel.process_info"
            resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <jsp:include page="../dialog/display_messages.jsp"/>
    <div id="middle">

    <h2><fmt:message key="bpel.process_info"/> (<%=processID%>)</h2>

        <div id="workArea">
    <%
        if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
    %>
            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td id="process_basic_info">
                        <jsp:include page="process_basic_info_frame.jsp">
                            <jsp:param name="processID" value="<%= processID%>"/>
                            <jsp:param name="packageName"
                                       value="<%= packageName%>"/>
                            <jsp:param name="processVersion" value="<%= processVersion%>"/>
                            <jsp:param name="processOlderVersion" value="<%= processOlderVersion%>"/>
                            <jsp:param name="processStatus" value="<%= processStatus%>"/>
                            <jsp:param name="processDeployedDate"
                                       value="<%= processDeployedDate%>"/>
                            <jsp:param name="noOfInstances" value="<%= totalNoOfInstances%>"/>
                        </jsp:include>
                    </td>
                    <td width="10px">&nbsp;</td>
                    <td rowspan="3" >
                        <table class="styledLeft" id="instance_summary">
                            <thead>
                            <tr>
                                <th><fmt:message key="instance.summary.graph"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td style="text-align: center;">
                                    <div id="process-instance-summary"></div>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <table><tbody>
                        <tr>
                            <td colspan="3">&nbsp;</td>
                        </tr>
                        </tbody></table>
                    <%
                        boolean isFirst = true;                 //this is due to an assumption of first element of EPRMap is the process itself
                        if (processStatus.equals(ProcessStatus.ACTIVE.getValue())) {
                    %>
                        <table class="styledLeft" id="wsdlTable">
                            <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="wsdl.details"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td colspan="2">
                                    <span class="icon-text"
                                          style="background-image:url(../service-mgt/images/service.gif);"><fmt:message
                                            key="partnerlinks"/>&nbsp;&nbsp;</span>
                                    <select id="partnerLinkSelectorForWSDLTable"
                                            onclick="changepartnerLinkValueForWSDLTable(document.getElementById('partnerLinkSelectorForWSDLTable').options[document.getElementById('partnerLinkSelectorForWSDLTable').selectedIndex].text)"
                                            onchange="changepartnerLinkValueForWSDLTable(document.getElementById('partnerLinkSelectorForWSDLTable').options[document.getElementById('partnerLinkSelectorForWSDLTable').selectedIndex].text)"
                                            style="margin-top:2px !important;">
    <%
            for (String partnerLink : refMap.keySet()) {
                String serviceName = refMap.get(partnerLink).getLocalPart();
                if (!isFirst) {
    %>
                                        <option title="<%= serviceName%>"
                                                id="<%= partnerLink%>"><%= partnerLink%>
                                        </option>
    <%
                } else {
                    isFirst = false;
    %>
                                        <option title="<%= serviceName%>" id="<%= partnerLink%>"
                                                selected="true"><%= partnerLink%>
                                        </option>
    <%
                }
            }
    %>
                                    </select>
                                    <script type="text/javascript">
                                            jQuery(document).ready(function() {
                                                changepartnerLinkValueForWSDLTable(document.getElementById('partnerLinkSelectorForWSDLTable').options[document.getElementById('partnerLinkSelectorForWSDLTable').selectedIndex].text);
                                            });
                                    </script>

                                </td>
                            </tr>

                            <tr>
                                <td colspan="2">
                                    <a id="qos_tryit" href=""
                                       class="icon-link"
                                       style="background-image:url(../service-mgt/images/tryit.gif);"
                                       target="_blank">
                                        <fmt:message key="tryit"/>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%">
                                    <a id="qos_wsdl" href=""
                                       class="icon-link"
                                       style="background-image:url(../service-mgt/images/wsdl.gif);"
                                       target="_blank">
                                        WSDL1.1
                                    </a>
                                </td>
                                <td width="50%">
                                    <a id="qos_wsdl2" href=""
                                       class="icon-link"
                                       style="background-image:url(../service-mgt/images/wsdl.gif);"
                                       target="_blank">
                                        WSDL2.0
                                    </a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">&nbsp;</td>
                </tr>
                <tr>
                    <td>
    <%
        if(isAuthorizedToManageProcesses && isAuthorizedToManageServices &&
           processStatus.equals(ProcessStatus.ACTIVE.getValue())) {
    %>
                        <table class="styledLeft" id="serviceOperationsParentTable"
                               style="margin-left: 0px;" width="100%">
                            <thead>
                                <tr>
                                    <th colspan="2" align="left"><fmt:message
                                            key="quality.of.service.configuration"/></th>
                                </tr>
                            </thead>
                            <tr>
                                <td colspan="2">
                                    <span class="icon-text"
                                          style="background-image:url(../service-mgt/images/service.gif);"><fmt:message
                                            key="partnerlinks"/>&nbsp;&nbsp;</span>
                                    <select id="partnerLinkSelectorForQoS"
                                            onclick="changepartnerLinkValueForQoS(document.getElementById('partnerLinkSelectorForQoS').options[document.getElementById('partnerLinkSelectorForQoS').selectedIndex].text)"
                                            onchange="changepartnerLinkValueForQoS(document.getElementById('partnerLinkSelectorForQoS').options[document.getElementById('partnerLinkSelectorForQoS').selectedIndex].text)"
                                            style="margin-top:2px !important;">
    <%
            isFirst = true;                 //this is due to an assumption of first element of EPRMap is the process itself
            for (String partnerLink : refMap.keySet()) {
                String serviceName =refMap.get(partnerLink).getLocalPart();
                if (!isFirst) {
    %>
                                        <option title="<%= serviceName%>" id="<%= partnerLink%>"><%= partnerLink%>
                                        </option>
    <%
                } else {
                    isFirst = false;
    %>
                                        <option title="<%= serviceName%>" id="<%= partnerLink%>"
                                                selected="true"><%= partnerLink%>
                                        </option>

    <%
                }
            }
    %>
                                    </select>
                                    <script type="text/javascript">
                                            jQuery(document).ready(function() {
                                                changepartnerLinkValueForQoS(document.getElementById('partnerLinkSelectorForQoS').options[document.getElementById('partnerLinkSelectorForQoS').selectedIndex].text);
                                                changepartnerLinkValueForWSDLTable(document.getElementById('partnerLinkSelectorForWSDLTable').options[document.getElementById('partnerLinkSelectorForWSDLTable').selectedIndex].text);
                                            });
                                    </script>

                                </td>
                            </tr>

                            <tr>
                                <td>
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/securityconfig/")) {
    %>
                                    <a id="qos_security" href=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/security.gif);">
                                        <fmt:message key="security"/>&nbsp;&nbsp;&nbsp;
                                        <div id="qos_security_icon" style="display: inline;"></div>
                                    </a>
    <%
        }
    %>
                                </td>
                                <td>
                                    <a id="qos_policy" href=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/policies.gif);">
                                        <fmt:message key="policies"/>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td>
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/rm/")) {
    %>
                                    <a id ="qos_rm" href=""
                                       onclick=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/rm.gif);">
                                        <fmt:message key="reliable.messaging"/>
                                    </a>
    <%
        }
    %>
                                </td>
                                <td>
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/transport-mgt/")) {
    %>
                                    <a id="qos_transport" href=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/transports.gif);">
                                        <fmt:message key="transports"/>
                                    </a>
    <%
        }
    %>
                                </td>

                            </tr>
                            <tr>
                                <td>
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/caching/")) {
    %>
                                    <a id="qos_caching" href=""
                                       onclick=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/caching.gif);">
                                        <fmt:message key="response.caching"/>
                                    </a>
    <%
        }
    %>
                                </td>
                                <td>
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/modulemgt/")) {
    %>
                                    <a id="qos_modules" href=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/modules.gif);">
                                        <fmt:message key="modules"/>
                                    </a>
    <%
        }
    %>
                                </td>
                            </tr>
                            <tr>
                                <td>
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/throttling/")) {
    %>
                                    <a id="qos_throttling" href=""
                                       onclick=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/throttling.gif);">
                                        <fmt:message key="access.throttling"/>
                                    </a>
    <%
        }
    %>
                                </td>
                                <td>
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/operation/")) {
    %>
                                    <a id="qos_operations" href=""
                                       class="icon-link-nofloat"
                                       style="background-image:url(../service-mgt/images/operations.gif);">
                                        <fmt:message key="operations"/>
                                    </a>
    <%
        }
    %>
                                </td>
                            </tr>
                            <%--<tr>
                                <td colspan="2">
                                    <a id="qos_tryit" href=""
                                       class="icon-link"
                                       style="background-image:url(../service-mgt/images/tryit.gif);"
                                       target="_blank">
                                        <fmt:message key="create.instance"/>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%">
                                    <a id="qos_wsdl" href=""
                                       class="icon-link"
                                       style="background-image:url(../service-mgt/images/wsdl.gif);"
                                       target="_blank">
                                        WSDL1.1
                                    </a>
                                </td>
                                <td width="50%">
                                    <a id="qos_wsdl2" href=""
                                       class="icon-link"
                                       style="background-image:url(../service-mgt/images/wsdl.gif);"
                                       target="_blank">
                                        WSDL2.0
                                    </a>
                                </td>
                            </tr>--%>

                        </table>
    <%
        }
    %>
                    </td>
                    <td width="10px">&nbsp;</td>
                </tr>

                <tr>
                    <td colspan="3">&nbsp;</td>
                </tr>

                <tr>
                    <td colspan="3">
                        <table class="styledLeft" id="processDefinitionTable" style="margin-left: 0px;" width="100%">
                            <thead>
                                <tr>
                                    <th align="left"><fmt:message key="process.definition"/></th>
                                </tr>
                            </thead>
                            <tr>
                                <td>
                                    <%--<div id="process-definition">
                                        <%=processDefPrettyPrinted%>
                                    </div>--%>
                                    <textarea id="xmlPay" name="design"
                                          style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                          rows="50"><%=processDefPrettyPrinted%>
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

                <tr>
                    <td colspan="3">
                        <table class="styledLeft" id="processVisualizationTable" style="margin-left: 0px;" width="100%">
                            <thead>
                                <tr>
                                    <th align="left"><fmt:message key="process.visualization"/></th>
                                </tr>
                            </thead>
                            <tr>
                                <td>
                                    <div id="bpel2svg" style="height:auto;text-align:center;">
    <%
        if (isMSIE) {
    %>
                                            <%="<img src=../png?pid=" + processID + " />"%>
    <%}
       else {
    %>
                                        <%="<object type=\"image/svg+xml\" data=../svg?pid=" + processID + " />"%>
    <%
        }
    %>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <%--<tr>--%>
                    <%--<td colspan="3">--%>
                        <%--<table class="styledLeft" id="bpiProcessVisualizationTable" style="margin-left: 0px;" width="100%">--%>
                            <%--<thead>--%>
                                <%--<tr>--%>
                                    <%--<th align="left"><fmt:message key="process.visualization"/></th>--%>
                                <%--</tr>--%>
                            <%--</thead>--%>
                            <%--<tr>--%>
                                <%--<td>--%>
                                    <%--<jsp:include page="visualizer/process_visualisation.jsp">--%>
                                        <%--<jsp:param name="processID" value="<%=processID%>" />--%>
                                    <%--</jsp:include>--%>
                                <%--</td>--%>
                            <%--</tr>--%>
                        <%--</table>--%>
                    <%--</td>--%>
                <%--</tr>--%>
            </table>
    <%
            } else {
    %>
        <p><fmt:message key="do.not.have.permission.to.view.process.details"/></p>
    <%
        }
    %>
        </div>
    </div>
    <script type="text/javascript">
        editAreaLoader.init({
            id : "xmlPay"		// textarea id
            ,syntax: "xml"			// syntax to be uses for highgliting
            ,start_highlight: true		// to display with highlight mode on start-up
            ,is_editable: false
        });

        alternateTableRows('serviceOperationsParentTable', 'tableEvenRow', 'tableOddRow');
    </script>
    </fmt:bundle>

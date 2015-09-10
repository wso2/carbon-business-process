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
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.*" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">

<jsp:useBean id="deployDescriptorUpdater" scope="session"
             class="org.wso2.carbon.bpel.ui.DeploymentDescriptorUpdater"/>
<jsp:setProperty name="deployDescriptorUpdater" property="*"/>

<%
    response.setHeader("Cache-Control", "no-cache");
    // check whether has permission to edit, if not disable elements to modify
    boolean isAuthorizedToManageProcesses =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");
    boolean isAuthorizedToMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");

    String processID = Encode.forXml(request.getParameter("Pid"));
    String operation = Encode.forXml(request.getParameter("operation"));
    ProcessManagementServiceClient processMgtClient;
    ProcessDeployDetailsList_type0 processDeployDetailsListType;
    ArrayList<String> scopeNames = new ArrayList<String>();

    if (processID != null && processID.trim().length() > 0 &&
            (isAuthorizedToMonitor || isAuthorizedToManageProcesses)) {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        QName pid = QName.valueOf(processID);

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

    /*
     *  Obtain deployment descriptor(deploy.xml) information to be displayed in editor
     */
    try {
        processDeployDetailsListType = processMgtClient.getProcessDeploymentInfo(pid);

        if (processDeployDetailsListType != null &&
                processDeployDetailsListType.getProcessEventsList() != null &&
                processDeployDetailsListType.getProcessEventsList().getScopeEventsList() != null &&
                processDeployDetailsListType.getProcessEventsList().getScopeEventsList().getScopeEvent() != null) {
            for (ScopeEventType scopeEventType :
                    processDeployDetailsListType.getProcessEventsList().getScopeEventsList().getScopeEvent()) {
                scopeNames.add(scopeEventType.getScope()); //scope names in the scope level events is taken into
                                                           // arrayllist to be used after it is updated
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

    /*
     * sets backend data into deployDescriptorUpdater bean class object
     */

    if (operation != null && operation.equals("deployinfo")) {

        // when the submit button of editor form is clicked, this will update backend
        try {
            String[] selecttype = request.getParameterValues("scopeevents");
            BpelUIUtil.updateBackEnd(processMgtClient, processDeployDetailsListType,
                    deployDescriptorUpdater, selecttype, scopeNames);
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
        if (processDeployDetailsListType != null) {  //create a new bean instance with processDeployDetailsList_type0 data
            BpelUIUtil.configureDeploymentDescriptorUpdater(processDeployDetailsListType,
                    deployDescriptorUpdater);
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
    }
%>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="js/bpel-main.js"></script>
<!--[if IE]><script type="text/javascript" src="js/flot/excanvas.min.js"></script><![endif]-->
<script type="text/javascript" src="js/flot/jquery.flot.js"></script>
<script type="text/javascript">

    function submitHiddenForm(action) {
        document.getElementById("hiddenField").value = location.href + "&ordinal=1";
        document.dataForm.action = action;
        document.dataForm.submit();
    }


    function refresh() {
        location.reload(true);
    }

    function checkAll(field) {
        for (var i = 0; i < field.length; i++) {
            field[i].checked = "checked";
        }
    }

    function clearAll(field) {
        for (var i = 0; i < field.length; i++) {
            field[i].checked = false;
        }

    }

    function enableAll(field) {
        if (document.deployInfoForm.gentype[0].checked) {
            field.checked = false;
            CARBON.showWarningDialog("<fmt:message key="sorry.this.option.is.not.available"/>");

        }
        else if (document.deployInfoForm.gentype[1].checked) {
            field.checked = "checked";
            CARBON.showWarningDialog("<fmt:message key="sorry.this.option.is.not.available"/>");
        }
    }

/*    function checkForCleanupsCorrect() {
        if (((document.getElementById("successInstance").checked) && (!document.getElementById("successVariables").checked
                || !document.getElementById("successCorrelations").checked)) || (document.getElementById("failureInstance").checked && (!document.getElementById("failureVariables").checked
                || !document.getElementById("failureCorrelations").checked))) {
            CARBON.showWarningDialog("<fmt:message key="cleanups.instance.category.requires.both.correlations.and.variables.specified.together"/>");
        } else {
            CARBON.showInfoDialog("Process:" + "<%= processID%>" + " " + "<fmt:message key="updated.successfully"/>", function() {
                document.getElementById('deployInfoForm').submit();
            });

        }
    }*/

    function checkSuccessVariablesAndCorrelations() {
        if ((document.getElementById("successInstance").checked)) {
            document.getElementById("successVariables").checked = true;
            document.getElementById("successCorrelations").checked = true;
        }
    }

    function checkFailureVariablesAndCorrelations() {
        if ((document.getElementById("failureInstance").checked)) {
            document.getElementById("failureVariables").checked = true;
            document.getElementById("failureCorrelations").checked = true;
        }
    }

</script>
<%
    }
%>

<link type="text/css" rel="stylesheet" href="css/style.css"/>
<%--<link media="all" type="text/css" rel="stylesheet" href="css/xmlverbatim.css">--%>
<carbon:breadcrumb
        label="dd.info"
        resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<div id="middle">

<h2><fmt:message key="dd.info"/></h2>

<div id="workArea">

<%
    if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
%>
    <%-- filling the deploy info table in the editor with DeployDescriptorUpdater bean class --%>

<%
    if (deployDescriptorUpdater != null) {
%>

<form id="deployInfoForm" name="deployInfoForm" method="POST"
      action="deployment_descriptor_editor.jsp?operation=deployinfo&Pid=<%=processID%>">

<table class="styledLeft" id="deploydescriptorinfotable" style="margin-left: 0px;" width="100%">
    <thead>
    <tr>
        <th colspan="2"><fmt:message key="process.deployment.information"/></th>
    </tr>
    </thead>

    <tbody>

    <tr>
        <td colspan="2">


            <table class="styledLeft" id="generalInfoTable"
                   style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
                   width="100%" border="0">
                <thead>
                <tr>
                    <th colspan="3"><fmt:message key="general.information"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><fmt:message key="process.id"/></td>
                    <td><%=processID%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="is.inmemory"/></td>
                    <td>
                        <input type="radio" name="inmemorystatus" value="true" disabled="true"
                                <%= BpelUIUtil.isInMemoryTypeChecked(deployDescriptorUpdater, "true")%>
                                <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                                <%= deployDescriptorUpdater.isInMemoryTrueSelected()%>>True <br/>
                        <input type="radio" name="inmemorystatus" value="false" disabled="true"
                                <%= BpelUIUtil.isInMemoryTypeChecked(deployDescriptorUpdater, "false")%>
                                <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                                <%= deployDescriptorUpdater.isInMemoryFalseSelected()%>>False
                    </td>
                </tr>

                </tbody>
            </table>

            <table class="styledLeft" id="providedServicesTable"
                   style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
                   width="100%" border="0">
                <thead>
                <tr>
                    <th colspan="3"><fmt:message key="provided.services"/></th>
                </tr>
                </thead>
                <tbody>

                <tr>
                    <td><i><fmt:message key="partner.link"/></i></td>
                    <td><i><fmt:message key="related.service"/></i></td>
                    <td><i><fmt:message key="associated.port"/></i></td>
                </tr>


                <% if (deployDescriptorUpdater.getProvideServiceList() != null) {
                    for (ProvidedServiceType provideServices : deployDescriptorUpdater.getProvideServiceList().getProvidedService()) {
                %>

                <tr>
                    <td>
                        <%= provideServices.getPartnerLink()%>
                    </td>

                    <%
                        Service_type0 service = provideServices.getService();
                    %>

                    <td><%= service.getName()%>
                    </td>
                    <td><%= service.getPort()%>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>

            <table class="styledLeft" id="invokedServicesTable"
                   style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
                   width="100%" border="0">
                <thead>
                <tr>
                    <th colspan="3"><fmt:message key="invoked.services"/></th>
                </tr>
                </thead>

                <tbody>
                <tr>
                    <td><i><fmt:message key="partner.link"/></i></td>
                    <td><i><fmt:message key="related.service"/></i></td>
                    <td><i><fmt:message key="associated.port"/></i></td>
                </tr>
                        <% InvokeServiceListType invokeList = deployDescriptorUpdater.getInvokedServiceList();
                    if (invokeList != null) {
                        for (InvokedServiceType invokedServices : invokeList.getInvokedService()) {
            %>
                <tr>
                    <td><%= invokedServices.getPartnerLink()%>
                    </td>
                    <%
                        Service_type1 service = invokedServices.getService();
                    %>
                    <td><%= service.getName()%>
                    </td>
                    <td><%= service.getPort()%>
                    </td>
                </tr>
                        <%
                }}else {
            %>
                <tbody>
                <tr>
                    <td colspan="3"><i><fmt:message key="no.invoked.services"/></i></td>
                </tr>
                </tbody>
                        <%
        }
    %>
    </tbody>
</table>

<table class="styledLeft" id="mexInterpretersTable"
       style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
       width="100%" border="0">
    <thead>
    <tr>
        <th colspan="2"><fmt:message key="mexinterceptors"/></th>
    </tr>
    </thead>
    <%
        MexInterpreterListType mexList = deployDescriptorUpdater.getMexInterceptors();
        if (mexList != null && mexList.getMexinterpreter() != null) {
    %>
    <%
        for (String mexIntName : mexList.getMexinterpreter()) {
    %>
    <tbody>
    <tr>
        <td>Name</td>
        <td><%= mexIntName %>
        </td>
    </tr>

    </tbody>
    <%
        }
    %>
    <%
    } else {
    %>
    <tbody>
    <tr>
        <td><i><fmt:message key="none"/></i></td>
    </tr>
    </tbody>
    <%
        }
    %>
</table>

<table class="styledLeft" style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
       width="100%" border="0">
    <thead>
    <tr>
        <th colspan="2"><fmt:message key="process.level.monitoring.events"/></th>
    </tr>
    </thead>
    <tbody>


    <tr>
        <td>
            <input type="radio" name="gentype" value="none" disabled="true"
                   onclick="clearAll(document.deployInfoForm.events)"
                    <%= BpelUIUtil.isGenerateTypeChecked(deployDescriptorUpdater, "none")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isTypeNoneSelected()%>><fmt:message key="none"/><br/>
            <input type="radio" name="gentype" value="all" disabled="true"
                   onclick="checkAll(document.deployInfoForm.events)"
                    <%= BpelUIUtil.isGenerateTypeChecked(deployDescriptorUpdater, "all")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isTypeAllSelected()%>><fmt:message key="all"/><br/>
            <input type="radio" name="gentype" value="selected" disabled="true"
                    <%= BpelUIUtil.isGenerateTypeChecked(deployDescriptorUpdater, "selected")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isTypeSelectedSelected()%>><fmt:message key="selected"/><br/>
        </td>
        <td>
            <%
                String[] eventsList = deployDescriptorUpdater.getEvents();
                deployDescriptorUpdater.setEvents(new String[0]);
            %>
            <input type="checkbox" name="events" value="instanceLifecycle" disabled="true" onclick="enableAll(this)"
                    <%= BpelUIUtil.isGivenEventChecked(eventsList, "instanceLifecycle")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isInsLifSelected()%>>Instance Life Cycle
            <br/>
            <input type="checkbox" name="events" value="activityLifecycle" disabled="true" onclick="enableAll(this)"
                    <%= BpelUIUtil.isGivenEventChecked(eventsList, "activityLifecycle")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isActLifSelected()%>>Activity Life Cycle
            <br/>
            <input type="checkbox" name="events" value="dataHandling" disabled="true" onclick="enableAll(this)"
                    <%= BpelUIUtil.isGivenEventChecked(eventsList, "dataHandling")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isDataHandSelected()%>>Data Handling<br/>
            <input type="checkbox" name="events" value="scopeHandling" disabled="true" onclick="enableAll(this)"
                    <%= BpelUIUtil.isGivenEventChecked(eventsList, "scopeHandling")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isScopeHandSelected()%>>Scope Handling<br/>
            <input type="checkbox" name="events" value="correlation" disabled="true" onclick="enableAll(this)"
                    <%= BpelUIUtil.isGivenEventChecked(eventsList, "correlation")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isCorrelatnSelected()%>>Correlation<br/>
        </td>
    </tr>
    </tbody>
</table>

<table class="styledLeft" id="scopeEventsTable"
       style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
       width="100%" border="0">
    <thead>
    <tr>
        <th colspan="6"><fmt:message key="scope.level.monitoring.events"/></th>
    </tr>
    </thead>

    <%
        ScopeEventType[] scopeEvents = deployDescriptorUpdater.getScopeEvents();
        if (scopeEvents != null) {

    %>
    <tbody>
    <tr>
        <td/>
        <td><fmt:message key="instance.life.cycle"/></td>
        <td><fmt:message key="activity.life.cycle"/></td>
        <td><fmt:message key="data.handling"/></td>
        <td><fmt:message key="scope.handling"/></td>
        <td><fmt:message key="correlation"/></td>

    </tr>
    <tr>
        <%
            for (ScopeEventType scopeEvent : scopeEvents) {
        %>
        <td>
            <%= scopeEvent.getScope()%>
        </td>

        <%
            EnableEventListType scopeLevelenableEventList = scopeEvent.getEnabledEventList();
            String[] scopeLevelEnabledEvents = scopeLevelenableEventList.getEnableEvent();
        %>
        <td>
            <input type="checkbox" name="scopeevents" value="instanceLifecycle" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(scopeLevelEnabledEvents, "instanceLifecycle")%>>
            <input type="hidden" name="scopeevents" value="0"/>
        </td>
        <td>
            <input type="checkbox" name="scopeevents" value="activityLifecycle" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(scopeLevelEnabledEvents, "activityLifecycle")%>>
            <input type="hidden" name="scopeevents" value="0"/>
        </td>
        <td>
            <input type="checkbox" name="scopeevents" value="dataHandling" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(scopeLevelEnabledEvents, "dataHandling")%>>
            <input type="hidden" name="scopeevents" value="0"/>
        </td>
        <td>
            <input type="checkbox" name="scopeevents" value="scopeHandling" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(scopeLevelEnabledEvents, "scopeHandling")%>>
            <input type="hidden" name="scopeevents" value="0"/>
        </td>
        <td>
            <input type="checkbox" name="scopeevents" value="correlation" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(scopeLevelEnabledEvents, "correlation")%>>
            <input type="hidden" name="scopeevents" value="0"/>
        </td>

    </tr>
    <%
        }
    %>
    </tbody>
    <%
    } else {
    %>
    <tbody>
    <tr>
        <td><i><fmt:message key="no.scope.level.events"/></i></td>
    </tr>
    </tbody>
    <%
        }
    %>
</table>

<table class="styledLeft" id="processPropertiesTable"
       style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
       width="100%" border="0">
    <thead>
    <tr>
        <th colspan="2"><fmt:message key="property.list"/></th>
    </tr>
    </thead>


    <%
        if (deployDescriptorUpdater.getPropertyList() != null && deployDescriptorUpdater.getPropertyList().getProcessProperty() != null) {
    %>
    <tbody>
    <tr>
        <td style="font-style:italic;">Property Name</td>
        <td style="font-style:italic;">Property Value</td>
    </tr>
    <%
        for (ProcessProperty_type0 property : deployDescriptorUpdater.getPropertyList().getProcessProperty()) {
    %>

    <tr>
        <td><%= property.getName()%>
        </td>
        <td><%= property.getValue()%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>

    <%
    } else {
    %>
    <tbody>
    <tr>

        <td colspan="2"><i><fmt:message key="no.given.properties"/></i></td>
    </tr>
    </tbody>
    <%
        }
    %>
</table>


<table class="styledLeft" id="cleanUpsTable"
       style="margin-left: 2px;margin-top: 15px;margin-bottom: 15px;"
       width="100%" border="0">
    <thead>
    <tr>
        <th colspan="6"><fmt:message key="process.instance.cleanup.details"/></th>
    </tr>
    </thead>

    <tbody>
    <tr>
        <td/>
        <td><fmt:message key="instance"/></td>
        <td><fmt:message key="variable"/></td>
        <td><fmt:message key="messages"/></td>
        <td><fmt:message key="correlations"/></td>
        <td><fmt:message key="events"/></td>

    </tr>

    <tr>
        <td><fmt:message key="success"/></td>
        <%
            String[] successCategoryList = deployDescriptorUpdater.getSuccesstypecleanups();
            deployDescriptorUpdater.setSuccesstypecleanups(new String[0]);
        %>

        <td>
            <input type="checkbox" id="successInstance" name="successtypecleanups" value="instance" disabled="true"
                   onclick="checkSuccessVariablesAndCorrelations()"
                    <%= BpelUIUtil.isGivenEventChecked(successCategoryList, "instance")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isSucInsCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="successVariables" name="successtypecleanups" value="variables" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(successCategoryList, "variables")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isSucVarCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="successMessages" name="successtypecleanups" value="messages" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(successCategoryList, "messages")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isSucMesCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="successCorrelations" name="successtypecleanups" value="correlations" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(successCategoryList, "correlations")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isSucCorCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="successEvents" name="successtypecleanups" value="events" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(successCategoryList, "events")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isSucEveCreated()%>>
        </td>
    </tr>

    <tr>
        <td><fmt:message key="failure"/></td>
        <%
            String[] failureCategoryList = deployDescriptorUpdater.getFailuretypecleanups();
            deployDescriptorUpdater.setFailuretypecleanups(new String[0]);
        %>
        <td>
            <input type="checkbox" id="failureInstance" name="failuretypecleanups" value="instance" disabled="true"
                   onclick="checkFailureVariablesAndCorrelations()"
                    <%= BpelUIUtil.isGivenEventChecked(failureCategoryList, "instance")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isFailInsCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="failureVariables" name="failuretypecleanups" value="variables" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(failureCategoryList, "variables")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isFailVarCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="failureMessages" name="failuretypecleanups" value="messages" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(failureCategoryList, "messages")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isFailMesCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="failureCorrelations" name="failuretypecleanups" value="correlations" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(failureCategoryList, "correlations")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isFailCorCreated()%>>
        </td>
        <td>
            <input type="checkbox" id="failureEvents" name="failuretypecleanups" value="events" disabled="true"
                    <%= BpelUIUtil.isGivenEventChecked(failureCategoryList, "events")%>
                    <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                    <%= deployDescriptorUpdater.isFailEveCreated()%>>
        </td>
    </tr>
    </tbody>
</table>

<%--<table>

    <tbody>
    <tr style="border:none;">
        <td style="border:none;"></td>
        <td style="border:none;" colspan="2">
            <input id="updatebutton" type="button"
                   value="Update Process Configuration" <%= BpelUIUtil.isElementDisabled(isAuthorizedToManageProcesses)%>
                   style="text-align:left; float:right; height: 25px; font-size: 12px;"
                   onclick="checkForCleanupsCorrect()">
        </td>
    </tr>
    </tbody>
</table>--%>
</form>
<%
    }
%>
    <%--   End of the process deployment information table --%>

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
    alternateTableRows('providedServicesTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('invokedServicesTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('mexInterpretersTable', 'tableEvenRow', 'tableOddRow')
    alternateTableRows('scopeEventsTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('processPropertiesTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('cleanUpsTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('generalInfoTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>
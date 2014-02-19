<%--
 ~ Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.ui.UnifiedEndpointAdminClient" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointTransport" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointSerializer" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointCluster" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>


<link type="text/css" rel="stylesheet" href="css/style.css"/>

<fmt:bundle basename="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="unifiedendpoint"
        resourceBundle="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>


<script type="text/javascript">
    var allowTabChange = true;
    var emtpyEntries = false;

    $(function() {
        var $myTabs = $("#tabs");

        $myTabs.tabs({
            select: function(event, ui) {
                if (!allowTabChange) {
                    alert("Tab selection is disabled, while you are in the middle of a workflow");
                }
                return allowTabChange;
            },

            show: function(event, ui) {
                var selectedTab = $myTabs.tabs('option', 'selected');
                allowTabChange = true;
            }
        });

        $myTabs.tabs('select', 0);
        if (emtpyEntries) {
            $myTabs.tabs('select', 1);
        }
    });
</script>

<script type="text/javascript">
    function saveUep() {
        if (validateUep()) {
            uep_form.submit();
            return true;
        }
        return false;
    }

    function cancelUep() {
        window.location.href = "index.jsp";
        return false;
    }

    function validateUep() {

        /*ToDo Check for more mandatory fields*/
        var regPath = document.getElementById('regPath').value;
        if (regPath == null || regPath == '') {
            CARBON.showErrorDialog("Specify Registry Location");
            return false;
        }

        var myRegExp = /conf:|gov:/;
        var matchPos1 = regPath.search(myRegExp);

        if (matchPos1 != -1 && matchPos1 == 0) {
            /*Ok to save*/
            return true
        }
        return false;
    }

    /*LB FO*/
    function showMessageOutputPane() {
        var messageOutputOptionsRow = document.getElementById('messageOutputOptionsRow');
        var link = document.getElementById('messageOutputOptionsExpandLink');
        if (messageOutputOptionsRow.style.display == 'none') {
            messageOutputOptionsRow.style.display = '';
            link.style.backgroundImage = 'url(images/up.gif)';
        } else {
            messageOutputOptionsRow.style.display = 'none';
            link.style.backgroundImage = 'url(images/down.gif)';
        }
    }

    /*Error Handling*/
    function showErrorHandlingPane() {
        var errorHandlingOptionsRow = document.getElementById('errorHandlingOptionsRow');
        var link = document.getElementById('errorHandlingOptionsExpandLink');
        if (errorHandlingOptionsRow.style.display == 'none') {
            errorHandlingOptionsRow.style.display = '';
            link.style.backgroundImage = 'url(images/up.gif)';
        } else {
            errorHandlingOptionsRow.style.display = 'none';
            link.style.backgroundImage = 'url(images/down.gif)';
        }
    }


</script>

<%
        String[] formatOptions = {"soap11", "soap12", "POX", "REST", "GET", "leave-as-is"};
        String[] optimizeOptions = {"SWA", "MTOM", "leave-as-is"};

        /*ToDo hcoded values*/
        boolean isSoap11 = true;
        boolean isSoap12 = false;
        boolean isPox = false;
        boolean isRest = false;
        boolean isGet = false;
        boolean isformatDefault = false;


        boolean isSWA = true;
        boolean isMTOM = false;
        boolean isOptimizeDefault = false;


        UnifiedEndpoint unifiedEndpoint = new UnifiedEndpoint();
        UnifiedEndpointTransport uepTransport = new UnifiedEndpointTransport();
        UnifiedEndpointCluster unifiedEndpointCluster = new UnifiedEndpointCluster();

        String uepName = request.getParameter("namex");
        String mode = request.getParameter("mode");
        String action = request.getParameter("action");


        String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
                session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        UnifiedEndpointAdminClient client = new UnifiedEndpointAdminClient(cookie, url, configContext);


        if (action != null && mode != null) {
            if (action.equals("none")) {
                if (mode.equals("add")) {
                    /*ToDO*/
                } else if (mode.equals("edit")) {
                    unifiedEndpoint = client.getEndpoint(uepName);

                    uepTransport = unifiedEndpoint.getTransport();
                    request.setAttribute("uepTransport", uepTransport);
                }
            } else if (action.equals("design")) {
                /*ToDo*/
            }
        } else {

            //none edit/add/cluster default location... raw Add
            if (session.getAttribute("parentUEP") != null) {
                System.out.println("Raw adding... ");
                Object originalObj = session.getAttribute("parentUEP");
                if (originalObj != null && (originalObj instanceof UnifiedEndpoint)) {
                    UnifiedEndpoint originalUep = (UnifiedEndpoint) originalObj;
                    unifiedEndpoint = originalUep;
                    if (originalUep.getUnifiedEndpointCluster() != null) {
                        if (originalUep.getUnifiedEndpointCluster().getClusteredUnifiedEndpointList() != null) {
                            System.out.println("RAW ADD Cluster Size : "
                                    + originalUep.getUnifiedEndpointCluster().getClusteredUnifiedEndpointList().size());
                        }
                    }
                    System.out.println("Original NN Parent : " + unifiedEndpoint.toString());
                    //session.removeAttribute("parentUEP");
                }
            }

            if (unifiedEndpoint.getTransport() != null) {
                uepTransport = unifiedEndpoint.getTransport();
                request.setAttribute("uepTransport", uepTransport);
            }

            if (unifiedEndpoint.getUnifiedEndpointCluster() != null) {
                unifiedEndpointCluster = unifiedEndpoint.getUnifiedEndpointCluster();
                request.setAttribute("uepCluster", unifiedEndpointCluster);
            }
    }


    /* ---------------------------------- Actions on Submission -----------------------------------------*/

    boolean submitted = "true".equals(request.getParameter("formSubmitted"));


    if(submitted)
    {
        String uepId = request.getParameter("uep_id");
        String wsaAddress = request.getParameter("uep_address");
        String regLocation = request.getParameter("regPath");
        System.out.println("Original Submission str : "+unifiedEndpoint.toString());
        request.setAttribute("uepTransport",uepTransport);

%>
<jsp:include page="inc/transports_processer.jsp"/>
<%
        unifiedEndpoint = new UnifiedEndpoint();
        unifiedEndpoint.setAddress(wsaAddress);
        unifiedEndpoint.setUepId(uepId);

        unifiedEndpoint.setTransport(uepTransport);
        if (unifiedEndpoint.getUnifiedEndpointCluster() == null) {
            unifiedEndpoint.setUnifiedEndpointCluster(unifiedEndpointCluster);
        }



        if(mode!=null&&mode.equals("cluster"))
        {
            Object parentObj = session.getAttribute("parentUEP");
            if(parentObj != null && (parentObj instanceof UnifiedEndpoint))
            {
                UnifiedEndpoint parenetUEP = (UnifiedEndpoint)parentObj;
                UnifiedEndpointCluster uepCluster = null;

                if (parenetUEP.getUnifiedEndpointCluster() != null ) {
                    System.out.println("Submiting Child EP -------------- ");
                    if (parenetUEP.getUnifiedEndpointCluster().getClusteredUnifiedEndpointList() != null) {
                        System.out.println("Size ---- " + parenetUEP.getUnifiedEndpointCluster().getClusteredUnifiedEndpointList().size());
                    }
                    parenetUEP.getUnifiedEndpointCluster().addClusteredUnifiedEndpoint(unifiedEndpoint);

                    //uepCluster.addClusteredUnifiedEndpoint(unifiedEndpoint);
                } else {
                    unifiedEndpointCluster.addClusteredUnifiedEndpoint(unifiedEndpoint);
                    unifiedEndpoint.setUnifiedEndpointCluster(unifiedEndpointCluster);

                    /*System.out.println("Submit... Empty Cluster");
                    uepCluster = new UnifiedEndpointCluster();
                    uepCluster.addClusteredUnifiedEndpoint(unifiedEndpoint);
                    parenetUEP.setUnifiedEndpointCluster(uepCluster);*/
                }

                //System.out.println("Parent found ..." + parenetUEP.getUnifiedEndpointCluster().getClusteredUnifiedEndpointList().get(0).getAddress());
             }

%>
<script type="text/javascript">
    window.location.href = './unified_ep_design.jsp';
</script>

<%
        }
        else
        {
            /*Add or Edit*/
            //unifiedEndpoint.setUnifiedEndpointCluster(unifiedEndpointCluster);
            session.setAttribute("parentUEP",unifiedEndpoint);


            /*if (session.getAttribute("parentUEP") == null) {
                    session.setAttribute("parentUEP", unifiedEndpoint);
                } else {
                    Object originalObj = session.getAttribute("parentUEP");
                    if (originalObj != null && (originalObj instanceof UnifiedEndpoint)) {
                        System.out.println("original found");
                        UnifiedEndpoint originalUep = (UnifiedEndpoint) originalObj;
                        if (originalUep.getUnifiedEndpointCluster() != null) {
                            System.out.println("Cluster found");
                        }
                        System.out.println("Original ID : " + originalUep.getUepId());
                    }
                }
            */

            UnifiedEndpointSerializer serializer = new UnifiedEndpointSerializer();
            OMElement uepOm = serializer.serializeUnifiedEndpoint(unifiedEndpoint);


            if(uepOm != null && regLocation != null && (!regLocation.equals(""))){
                client.saveUEP(regLocation + uepId + ".xml",uepOm.toString());
                session.removeAttribute("parentUEP");
            }
        }
%>
<script type="text/javascript">
    window.location.href = './index.jsp';
</script>

<%
    }
%>

<div id="middle">
    <h2><fmt:message key="uep.menu.text"/></h2>

    <div id="workArea">

        <form id="uep_form" method="POST" action="">
            <input type="hidden" name="formSubmitted" value="true"/>
            <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="uep.configuration"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td colspan="2" style="padding-bottom:10px;">
                        <table>
                            <tr>
                                <td style="width:130px;"><fmt:message key="uep.id"/>
                                    <span
                                            class="required">*</span></td>
                                <td>
                                    <input id="uep_id" type="text" name="uep_id" size="40"
                                           value="<%= unifiedEndpoint.getUepId() != null ? unifiedEndpoint.getUepId() : "" %>"/>
                                </td>
                            </tr>
                            <tr>
                                <td style="width:130px;"><fmt:message key="uep.wsa.address"/><span
                                        class="required">*</span></td>
                                <td>
                                    <input id="uep_address"
                                           value="<%= unifiedEndpoint.getAddress() != null ? unifiedEndpoint.getAddress() : "" %>"
                                           type="text" name="uep_address" size="60"/>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>


                    <%--Message Output--%>
                <tr>
                    <td>
                        <table id="messageOutputOptionsTable" <%--class="styledInner"--%> cellspacing="0" width="100%">
                            <thead>
                            <tr>
                                <th colspan="2">
                                    <a id="messageOutputOptionsExpandLink" class="icon-link"
                                       style="background-image: url(images/down.gif);"
                                       onclick="showMessageOutputPane()"><fmt:message key="uep.message.output"/></a>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr id="messageOutputOptionsRow" style="display:none;">
                                <td colspan="2" style="padding-bottom:10px;">
                                    <table>

                                        <tr>
                                            <td width="180px"><fmt:message key="uep.msg.op.format"/></td>
                                            <td><select name="format">
                                                <option value="<%=formatOptions[0]%>" <%=isSoap11
                                                    ?
                                                    "selected=\"selected\""
                                                    :
                                                    ""%>>
                                                    <fmt:message key="uep.msg.op.format.soap.1.1"/></option>
                                                <option value="<%=formatOptions[1]%>" <%=isSoap12
                                                    ?
                                                    "selected=\"selected\""
                                                    :
                                                    ""%>>
                                                    <fmt:message key="uep.msg.op.format.soap.1.2"/></option>
                                                <option value="<%=formatOptions[2]%>" <%=isPox
                                                    ?
                                                    "selected=\"selected\""
                                                    :
                                                    ""%>>
                                                    <fmt:message key="uep.msg.op.format.pox"/></option>
                                                <option value="<%=formatOptions[3]%>" <%=isRest
                                                    ?
                                                    "selected=\"selected\""
                                                    :
                                                    ""%>>
                                                    <fmt:message
                                                            key="uep.msg.op.format.rest"/></option>
                                                <option value="<%=formatOptions[4]%>" <%=isGet
                                                    ?
                                                    "selected=\"selected\""
                                                    :
                                                    ""%>>
                                                    <fmt:message key="uep.msg.op.format.get"/></option>
                                                <option value="<%=formatOptions[5]%>" <%=isformatDefault
                                                    ?
                                                    "selected=\"selected\""
                                                    :
                                                    ""%>>
                                                    <fmt:message
                                                            key="uep.leave.as.is"/></option>
                                            </select>
                                            </td>
                                        </tr>

                                        <tr>
                                            <td><fmt:message key="uep.msg.op.optimize"/></td>
                                            <td><select name="optimize">
                                                <option value="<%=optimizeOptions[0]%>" <%=isSWA
                                                    ?
                                                    "selected"
                                                    :
                                                    ""%>>
                                                    <fmt:message key="uep.msg.op.optimize.swa"/></option>
                                                <option value="<%=optimizeOptions[1]%>" <%=isMTOM
                                                    ?
                                                    "selected"
                                                    :
                                                    ""%>>
                                                    <fmt:message key="uep.msg.op.optimize.mtom"/></option>
                                                <option value="<%=optimizeOptions[2]%>" <%=isOptimizeDefault
                                                    ?
                                                    "selected"
                                                    :
                                                    ""%>>
                                                    <fmt:message key="uep.leave.as.is"/></option>
                                            </select></td>
                                        </tr>


                                    </table>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>



                    <%--  Error Handling --%>
                <tr>
                    <td>
                        <table id="errorHandlingOptionsTable" <%--class="styledInner"--%> cellspacing="0" width="100%">
                            <thead>
                            <tr>
                                <th colspan="2">
                                    <a id="errorHandlingOptionsExpandLink" class="icon-link"
                                       style="background-image: url(images/down.gif);"
                                       onclick="showErrorHandlingPane()"><fmt:message key="uep.error.handling"/></a>
                                </th>
                            </tr>
                            </thead>

                            <tbody>
                            <tr id="errorHandlingOptionsRow" style="display:none;">
                                <td colspan="5" style="padding-bottom:10px;">
                                    <table id="markSuspensionOnFailure" cellspacing="2" width="100%" class="normal-nopadding">
                                        <thead>
                                        <tr>
                                            <th colspan="2"><fmt:message key="uep.mark.suspend.on.failure"/></th>
                                        </tr>
                                        </thead>

                                        <tr>
                                            <td style="width:130px;"><fmt:message key="uep.error.codes"/></td>
                                            <td>
                                                <input id="error_codes_mark_sus_id"
                                                       value=""
                                                       type="text" name="error_mark_sus_codes" size="60"/>
                                            </td>
                                        </tr>

                                        <tr>
                                            <td><fmt:message key="uep.suspend.initial.duration"/></td>
                                            <td>
                                                <input id="init_duration_id"
                                                       value=""
                                                       type="text" name="init_duration" size="40"/>
                                            </td>
                                        </tr>

                                        <tr>
                                            <td><fmt:message key="uep.suspend.progression.factor"/></td>
                                            <td>
                                                <input id="factor_id"
                                                       value=""
                                                       type="text" name="factor" size="40"/>
                                            </td>
                                        </tr>

                                        <tr>
                                            <td><fmt:message key="uep.suspend.max.duration"/></td>
                                            <td>
                                                <input id="max_duration_id"
                                                       value=""
                                                       type="text" name="max_duration" size="40"/>
                                            </td>
                                        </tr>

                                    </table>
                                    <table id="suspendOnFailure" cellspacing="2" width="100%" class="normal-nopadding">
                                        <thead>
                                        <tr>
                                            <th colspan="2"><fmt:message key="uep.suspend.on.failure"/></th>
                                        </tr>
                                        </thead>

                                        <tr>
                                            <td style="width:130px;"><fmt:message key="uep.error.codes"/></td>
                                            <td>
                                                <input id="error_codes_id"
                                                       value=""
                                                       type="text" name="error_codes" size="60"/>
                                            </td>
                                        </tr>

                                        <tr>
                                            <td><fmt:message key="uep.suspend.retries"/></td>
                                            <td>
                                                <input id="retries_id"
                                                       value=""
                                                       type="text" name="retries" size="40"/>
                                            </td>
                                        </tr>

                                        <tr>
                                             <td><fmt:message key="uep.suspend.retry.delay"/></td>
                                            <td>
                                                <input id="retry_delay_id"
                                                       value=""
                                                       type="text" name="retry_delay" size="40"/>
                                            </td>
                                        </tr>
                                    </table>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>





                    <%-- Transports Options--%>
                <tr>
                    <td colspan="2">
                        <jsp:include page="inc/transports.jsp"/>
                    </td>
                </tr>

                <tr>
                    <td colspan="2">
                        <jsp:include page="inc/clusters.jsp"/>
                    </td>
                </tr>


                <tr>
                    <td>
                        <span class="required">*</span>
                        <input class="longInput" type="text" id="regPath"
                               name="regPath"
                               value="conf:" readonly="true" size="60"/>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           style="padding-left:20px;padding-right:20px"
                           onclick="showRegistryBrowser('regPath', '/_system/config')"><fmt:message
                                key="conf.registry"/></a>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           style="padding-left:20px;padding-right:20px"
                           onclick="showRegistryBrowser('regPath', '/_system/governance')"><fmt:message
                                key="gov.registry"/></a>
                    </td>
                </tr>

                <tr>
                    <td colspan="2" class="buttonRow">
                        <button class="button" onclick="saveUep(); return false;">Save</button>
                        <button class="button" onclick="cancelUep(); return false;">Cancel</button>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>


    </div>
</div>

</fmt:bundle>
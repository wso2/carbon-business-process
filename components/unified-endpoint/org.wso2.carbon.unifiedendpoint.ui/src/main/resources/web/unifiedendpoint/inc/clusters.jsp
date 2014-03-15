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
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint" %>
<%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointCluster" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources"
               request="<%=request%>"/>


<script type="text/javascript">


    var childEPs = Array();
    var childEPCount = 0;


    function addClusterEP() {
        document.forms.uep_form.submit();
        window.location.href = "unified_ep_design.jsp?mode=cluster&action=none";
    }

    function showClusterOptionsPane() {
        var clusterOptionsRow = document.getElementById('clusterOptionsRow');
        var link = document.getElementById('clusterOptionsExpandLink');
        if (clusterOptionsRow.style.display == 'none') {
            clusterOptionsRow.style.display = '';
            link.style.backgroundImage = 'url(images/up.gif)';
        } else {
            clusterOptionsRow.style.display = 'none';
            link.style.backgroundImage = 'url(images/down.gif)';
        }
    }

    function dbrDeleteCurrentRow(obj) {
        var delRow = obj.parentNode.parentNode;
        var tbl = delRow.parentNode.parentNode;
        var rIndex = delRow.sectionRowIndex;
        var rowArray = new Array(delRow);
        dbrDeleteRows(rowArray);
        if (tbl.tBodies[0].rows.length == 0) {
            tbl.style.display = 'none';
        }
    }

    function dbrDeleteRows(rowObjArray) {
        for (var i = 0; i < rowObjArray.length; i++) {
            var rIndex = rowObjArray[i].sectionRowIndex;
            rowObjArray[i].parentNode.deleteRow(rIndex);
        }
    }

    /*function addServiceParams() {
        var headerName = document.getElementById('headerName').value;
        var headerValue = document.getElementById('headerValue').value;
        // trim the input values
        headerName = headerName.replace(/^\s*//*, "").replace(/\s*$/, "");
        headerValue = headerValue.replace(/^\s*//*, "").replace(/\s*$/, "");
        if (headerName != '' && headerValue != '') {
            if (isParamAlreadyExist(headerName)) {
                CARBON.showWarningDialog("Parameter Already exists");
                return;
            }
            addServiceParamRow(headerName, headerValue);
        } else {
            CARBON.showWarningDialog("Empty Key Value");
        }
    }*/

    function isParamAlreadyExist(paramName) {
        var i;
        for (i = 0; i < childEPCount; i++) {
            if (childEPs[i]['name'] == paramName) {
                return true;
            }
        }
        return false;
    }


    function addServiceParamRow(key, value) {
        addRow(key, value, 'childEpTable', 'deleteServiceParamRow');
        childEPs[childEPCount] = new Array(2);
        childEPs[childEPCount]['name'] = key;
        childEPs[childEPCount]['value'] = value;
        childEPCount++;

        /*document.getElementById('headerName').value = "";
        document.getElementById('headerValue').value = "";*/
    }

    function addRow(param1, param2, table, delFunction) {
        var tableElement = document.getElementById(table);
        var param1Cell = document.createElement('td');
        param1Cell.appendChild(document.createTextNode(param1));

        var param2Cell = document.createElement('td');
        param2Cell.appendChild(document.createTextNode(param2));

        var delCell = document.createElement('td');
        delCell.innerHTML = '<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

        var rowtoAdd = document.createElement('tr');
        rowtoAdd.appendChild(param1Cell);
        rowtoAdd.appendChild(param2Cell);
        rowtoAdd.appendChild(delCell);

        tableElement.tBodies[0].appendChild(rowtoAdd);
        tableElement.style.display = "";
    }


    function deleteServiceParamRow(index) {
        CARBON.showConfirmationDialog("Cluster EP deletion?" , function() {
            document.getElementById('childEpTable').deleteRow(index);
            childEPs.splice(index-1, 1);
            childEPCount--;
            if (childEPCount == 0) {
                document.getElementById('childEpTable').style.display = 'none';
            }
        });
    }


</script>

<%
    List<UnifiedEndpoint> childEPList = new LinkedList<UnifiedEndpoint>();
    Object obj = request.getAttribute("uepCluster");

    if (obj != null && (obj instanceof UnifiedEndpointCluster) ) {
        UnifiedEndpointCluster unifiedEndpointCluster = (UnifiedEndpointCluster) obj;
        if (unifiedEndpointCluster.getClusteredUnifiedEndpointList() != null ) {
            childEPList = unifiedEndpointCluster.getClusteredUnifiedEndpointList();
        }

    }


%>


<div id="clustersContent" align="center">
    <table id="clusterOptionsTable" class="styledInner" cellspacing="0" width="80%">
        <thead>
        <tr>
            <th colspan="2">
                <a id="clusterOptionsExpandLink" class="icon-link"
                   style="background-image: url(images/down.gif);"
                   onclick="showClusterOptionsPane()"><fmt:message key="uep.cluster.options"/></a>
            </th>
        </tr>
        </thead>

        <%--<input type="hidden" value="<%= parentEpName %>" id="parent_ep_name_txt" name="parent_ep_name"/>--%>

        <tbody>
        <tr id="clusterOptionsRow" style="display:none;">
            <td style="padding: 0px !important;">
                <table class="styledInner" cellpadding="0" cellspacing="0" style="margin-left: 0px;">
                    <tr>
                        <td colspan="2" style="padding-top:10px;">
                            <table class="normal-nopadding">
                                <tr>
                                    <td width="20%">
                                        <fmt:message key="uep.cluster.eps"/>
                                    </td>
                                    <td>
                                        <div id="nameValueAdd">
                                            <table>
                                                <tbody>
                                                <tr>
                                                    <%--<td>
                                                        <fmt:message key="name"/>: <input type="text"
                                                                                          id="headerName"/>
                                                    </td>
                                                    <td class="nopadding">
                                                        <fmt:message key="value"/>: <input type="text"
                                                                                           id="headerValue"/>
                                                    </td>--%>
                                                    <td class="nopadding">
                                                        <a class="icon-link"
                                                           href="#addNameLink"
                                                           onclick="addClusterEP();"
                                                           style="background-image: url(../admin/images/add.gif);"><fmt:message
                                                                key="uep.cluster.add.ep"/>
                                                        </a>
                                                    </td>

                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                        <div>
                                            <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
                                                   id="childEpTable">
                                                <thead>
                                                <tr>
                                                    <th style="width:40%"><fmt:message key="uep.name"/></th>
                                                    <th style="width:40%"><fmt:message key="uep.wsa.address"/></th>
                                                    <th style="width:40%" colspan="2"><fmt:message key="uep.action"/></th>
                                                </tr>
                                                </thead>

                                                <tbody>

                                                <%
                                                    for (UnifiedEndpoint uep : childEPList) {
                                                        System.out.println("Adding table rows....");

                                                %>
                                                <tr id="">
                                                   
                                                    <td width="5%" style="white-space:nowrap;">
                                                        <%= uep.getUepId() %>
                                                    </td>
                                                    <td width="5%" style="white-space:nowrap;">
                                                        <%= uep.getAddress() %>
                                                    </td>

                                                    <td width="5%" style="white-space:nowrap;">
                                                        <a class="icon-link"
                                                           href="#editEPLink"
                                                           onclick=""
                                                           style="background-image: url(../admin/images/edit.gif);"><fmt:message key="uep.edit"/>
                                                        </a>
                                                    </td>
                                                    <td width="5%" style="white-space:nowrap;">
                                                        <a class="icon-link"
                                                           href="#delEPLink"
                                                           onclick=""
                                                           style="background-image: url(../admin/images/delete.gif);"><fmt:message key="uep.delete"/>
                                                        </a>
                                                        </td>

                                                </tr>
                                                <%
                                                    }
                                                %>

                                                </tbody>


                                            </table>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                                <%--<table class="styledInner" width="60%" id="transportOptions">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="uep.cluster.ep.name"/></th>
                                        <th id="prop_value_th"><fmt:message key="uep.cluster.ep.type"/></th>
                                        <th><fmt:message key="uep.trp.action"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>


                                    </tbody>
                                </table>
                                <div>
                                    <a class="icon-link" style="background-image: url(../admin/images/add.gif);"
                                       onclick="addClusterEP();"><fmt:message
                                            key="uep.trp.add.property"/></a>
                                </div>--%>
                        </td>
                    </tr>

                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>


</fmt:bundle>
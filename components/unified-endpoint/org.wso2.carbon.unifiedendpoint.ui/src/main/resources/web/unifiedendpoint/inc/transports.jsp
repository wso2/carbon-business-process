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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.unifiedendpoint.ui.i18n.Resources"
                   request="<%=request%>"/>
    <script type="text/javascript">
        function showTransportOptionsPane() {
            var wsdlOptionsRow = document.getElementById('transportOptionsRow');
            var link = document.getElementById('transportOptionsExpandLink');
            if (wsdlOptionsRow.style.display == 'none') {
                wsdlOptionsRow.style.display = '';
                link.style.backgroundImage = 'url(images/up.gif)';
            } else {
                wsdlOptionsRow.style.display = 'none';
                link.style.backgroundImage = 'url(images/down.gif)';
            }
        }

        function dbrAddStmtResultsTableRow(tableName, num) {
            var tbl = document.getElementById(tableName);
            var nextRow = tbl.tBodies[0].rows.length;
            var noTwo = 0;


            var trp_props_size = document.getElementById(("trp_props_size_txt"));


            if (trp_props_size != null) {
                noTwo = trp_props_size.value;
            }

            if (tbl.tBodies[0].rows.length == 0) {
                tbl.style.display = '';
            }

            if (num == null) {
                num = nextRow;
            }


            var row = tbl.tBodies[0].insertRow(num);

            var cell0 = row.insertCell(0);
            var txtInp = document.createElement('input');
            txtInp.setAttribute('type', 'text');
            txtInp.setAttribute('name', ('trp_prop_name' + noTwo));
            txtInp.setAttribute('id', ('trp_prop_name_txt' + noTwo));
            txtInp.setAttribute('value', "");
            cell0.appendChild(txtInp);

            var cell1 = row.insertCell(1);
            YAHOO.util.Dom.addClass(cell1, 'trpValueCol');

            txtInp = document.createElement('input');
            txtInp.setAttribute('type', 'text');
            txtInp.setAttribute('value', "");
            txtInp.setAttribute('name', ('trap_prop_val' + noTwo));
            txtInp.setAttribute('id', ('trap_prop_val_txt' + noTwo));
            cell1.appendChild(txtInp);

            var cell2 = row.insertCell(2);
            cell2.innerHTML = "<a onclick=\"dbrDeleteCurrentRow(this);return false;\" class=\"delete-icon-link\">Delete</a>";

            trp_props_size.value++;
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


    </script>




    <%@ page import="org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointTransport" %>
    <%@ page import="java.util.Map" %>
    <%@ page import="java.util.HashMap" %>

    <%
        UnifiedEndpointTransport uepTransport = null;
        Map<String, String> trpPropMap = new HashMap<String, String>();
        Object obj = request.getAttribute("uepTransport");
        if (obj != null) {

            uepTransport = (UnifiedEndpointTransport) obj;

            trpPropMap = uepTransport.getTransportProperties();

            for (Map.Entry<String, String> entry : trpPropMap.entrySet()) {
                entry.getKey();
                entry.getValue();
            }
        }
    %>

    <div id="transportsContent" align="center">
        <table id="transportOptionsTable" class="styledInner" cellspacing="0" width="80%">
            <thead>
            <tr>
                <th colspan="2">
                    <a id="transportOptionsExpandLink" class="icon-link"
                       style="background-image: url(images/down.gif);"
                       onclick="showTransportOptionsPane()">Transport Options</a>
                </th>
            </tr>
            </thead>
                <input type="hidden" value="<%= trpPropMap.size() %>" id="trp_props_size_txt" name="trp_props_size"/>

            <tbody>
            <tr id="transportOptionsRow" style="display:none;">
                <td style="padding: 0px !important;">
                    <input name="availableTransportsList" id="availableTransportsList" type="hidden" value=""/>
                    <table class="styledInner" cellpadding="0" cellspacing="0" style="margin-left: 0px;">
                        <tr>
                            <td colspan="2" style="padding-top:10px;">
                                <table class="styledInner" width="60%" id="transportOptions">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="uep.trp.name"/></th>
                                        <th id="prop_value_th"><fmt:message key="uep.trp.value"/></th>
                                        <th><fmt:message key="uep.trp.action"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <% int i = 0;
                                            for (Map.Entry<String, String> entry : trpPropMap.entrySet()) {

                                        %>


                                        <tr id="">
                                          <td width="5%" style="white-space:nowrap;">
                                              <input id="trp_prop_name_txt<%= i %>" name="trp_prop_name<%= i %>"
                                                     value="<%= entry.getKey() %>"/>
                                          </td>
                                          <td width="5%" class="trpValueCol" style="white-space:nowrap;">
                                              <input id="trap_prop_val_txt<%= i %>" name="trap_prop_val<%= i %>"
                                                     value="<%= entry.getValue() %>"/>
                                          </td>
                                          <td><a onclick="dbrDeleteCurrentRow(this);" class="delete-icon-link" href="#"><fmt:message key="delete"/></a></td>
                                      </tr>

                                        <%      i++;
                                            } %>

                                    </tbody>
                                </table>
                                <div>
                                    <a class="icon-link" style="background-image: url(../admin/images/add.gif);"
                                       onclick="dbrAddStmtResultsTableRow('transportOptions', null)"><fmt:message
                                            key="uep.trp.add.property"/></a>
                                </div>
                            </td>
                        </tr>

                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </div>


</fmt:bundle>
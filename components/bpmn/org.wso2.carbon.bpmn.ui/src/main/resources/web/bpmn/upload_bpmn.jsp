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
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
%>
<script type="text/javascript" src="js/bpmn-main.js"></script>
<script>
function validate() {
    if (document.bpmnUpload.bpmnFileName.value != null) {
        var jarinput = document.bpmnUpload.bpmnFileName.value;
        if (jarinput == '') {
            CARBON.showWarningDialog('Please select required fields to upload a bpmn package');
        } else if (jarinput.lastIndexOf(".bar") == -1) {
            CARBON.showWarningDialog('Please select a .bar file');
        } else {
            document.bpmnUpload.submit();
        }
    } else if (document.bpmnUpload.bpmnFileName[0].value != null) {
        var validFileNames = true;
        for (var i = 0; i < document.bpmnUpload.bpmnFileName.length; i++) {
            var jarinput = document.bpmnUpload.bpmnFileName[i].value;
            if (jarinput == '') {
                CARBON.showWarningDialog('Please select required fields to upload a bpmn package');
                validFileNames = false;
                break;
            } else if (jarinput.lastIndexOf(".bar") == -1) {
                CARBON.showWarningDialog('Please select a .bar file');
                validFileNames = false;
                break;
            } else {
                document.bpmnUpload.submit();
            }
        }
    }
}
</script>

<div id="middle">
    <h2><fmt:message key="bpmn.newpackage"/></h2>

    <div id="workArea">
        <div id="formset">
            <form id="bpmn_upload_form" method="post" name="bpmnUpload" action="../../fileupload/bpmn"
                      enctype="multipart/form-data"
                      target="_self">
                <label style="font-weight:bold;">&nbsp;<fmt:message key="bpmn.uploadpackge"/></label>
                <br/><br/>
                <table class="styledLeft" id="bpmnTbl" bgcolor="#efecf5">
                    <tr>
                        <td class="formRow" width="20%">
                            <label><fmt:message key="bpmn.package"/><font color="red">*</font></label>
                        </td>
                        <td class="formRow">
                            <input type="file" name="bpmnFileName" size="50"/>&nbsp;
                            <input type="button" width='20px' class="button" onclick="addRow();" value=" + "/>
                        </td>
                    </tr>
                </table>

                <table class="styledLeft">
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" class="button registryWriteOperation" type="button"
                                   value="<fmt:message key="upload"/>" onclick="validate()"/>
                            <input name="upload" class="button registryNonWriteOperation" type="button"
                                   value="<fmt:message key="upload"/>" disabled="disabled"/>
                            <input type="button" class="button" value="<fmt:message key="button.cancel"/>"
                                   onclick="javascript:location.href='../bpmn/process_list_view.jsp'"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</div>
</fmt:bundle>
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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<script type="text/javascript">
    function uploadAttachment() {
        document.attachmentUpload.submit();
    }
</script>

<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">

    <div id="middle">
        <h2>Attachment Upload Form</h2>

        <div id="workArea">
            <div id="attachment_uploader">
                <form id="attachment_upload_form" method="post" name="attachmentUpload" action="../../fileupload/attachment-mgt?<csrf:tokenname/>=<csrf:tokenvalue/>"
                      enctype="multipart/form-data" target="_self">
                    <table>
                        <tbody>
                        <tr>
                            <td>Name</td>
                            <td><input type="text" name="Name"/></td>
                        </tr>
                        <tr>
                            <td>contentType</td>
                            <td><input type="text" name="contentType"/></td>
                        </tr>
                        <tr>
                            <td>owner</td>
                            <td><input type="text" name="owner"/></td>
                        </tr>
                        <tr>
                            <td>File</td>
                            <td><input type="file" name="fileToUpload"/></td>
                        </tr>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td><input name="attachmentUpload" class="button" type="button"
                                       value="upload" onclick="uploadAttachment();"/></td>
                        </tr>
                        </tfoot>
                    </table>
                </form>
            </div>
        </div>
    </div>
</fmt:bundle>
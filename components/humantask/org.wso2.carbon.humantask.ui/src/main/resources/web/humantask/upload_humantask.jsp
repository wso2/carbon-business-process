<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<!--
~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>
<carbon:breadcrumb
        label="humantask.newpackage"
        resourceBundle="org.wso2.carbon.humantask.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">

    <div id="middle">
        <%
            boolean isAuthorizedToAddPackages =
                    CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/humantask/packages/add");
            if (isAuthorizedToAddPackages) {
        %>

        <script type="text/javascript">
            //this script contains the i18n related functions
            if (typeof i18n == "undefined" || i18n) {
                var i18n = {};
            }

            i18n.humantaskPckgUplodedSuccess = function () {
                CARBON.showInfoDialog("<fmt:message key="humantask.package.uploaded.successfully"/>");
                return true;
            };

            i18n.fileUplodedFailed = function () {
                CARBON.showErrorDialog("<fmt:message key="file.uploading.failed"/>");
                return true;
            };

            i18n.humantaskPckgUplodedFailed = function (msg) {
                CARBON.showErrorDialog("<fmt:message key="humantask.package.upload.failed"/>" + "\n" + msg);
                return true;
            };
        </script>

        <script type="text/javascript" src="js/humantask-main.js"></script>
        <script type="text/javascript" src="js/jquery.form.js"></script>
        <script type="text/javascript">
            function setupHumanTaskUploadForm() {
                $('#humantask_upload_form').ajaxForm({
                    beforeSubmit:function (a, f, o) {
                        o.dataType = "script";
                        var form = f[0];
                        if (!form.humantaskFileName.value) {
                            CARBON.showWarningDialog("<fmt:message key="humantask.emptyupload"/>");
                            return false;
                        }

                        var regex = /[ \~\!\@\#\$\;\%\^\*\(\)\+\=\{\}\[\]\/\\|\<\>\`\"\']+/gi;
                        var packageName = "";
                        jQuery.each(jQuery.browser, function (i) {
                            if (jQuery.browser.msie || jQuery.browser.safari) {
                                packageName = form.humantaskFileName.value.substring(form.humantaskFileName.value.lastIndexOf("\\") + 1);
                            } else {
                                packageName = form.humantaskFileName.value;
                            }
                        });

                        if (packageName.match(regex)) {
                            CARBON.showWarningDialog("<fmt:message key="humantask.invalid.humantask"/>");
                            return false;
                        }
                    },
                    success:function () {
                        return false;
                    },
                    resetForm:true

                });
            }
            $(document).ready(setupHumanTaskUploadForm);

            function validate() {

                if (document.humantaskUpload.humantaskFileName.value != null) {
                    var jarinput = document.humantaskUpload.humantaskFileName.value;
                    if (jarinput == '') {
                        CARBON.showWarningDialog('Please select required fields to upload a Humantask package');
                    } else if (jarinput.lastIndexOf(".zip") == -1) {
                        CARBON.showWarningDialog('Please select a .zip file');
                    } else {
                        document.humantaskUpload.submit();
                    }
                } else if (document.humantaskUpload.humantaskFileName[0].value != null) {
                    var validFileNames = true;
                    for (var i = 0; i < document.humantaskUpload.humantaskFileName.length; i++) {
                        var jarinput = document.humantaskUpload.humantaskFileName[i].value;
                        if (jarinput == '') {
                            CARBON.showWarningDialog('Please select required fields to upload a Humantask package');
                            validFileNames = false;
                            break;
                        } else if (jarinput.lastIndexOf(".zip") == -1) {
                            CARBON.showWarningDialog('Please select a .zip file');
                            validFileNames = false;
                            break;
                        } else {
                            document.humantaskUpload.submit();
                        }
                    }
                }
            }

            function submitHumantask() {
                validate();
            }
        </script>


        <h2><fmt:message key="humantask.newpackage"/></h2>

        <div id="workArea">
            <div id="formset">
                <form id="humantask_upload_form" method="post" name="humantaskUpload"
                      action="../../fileupload/humantask"
                      enctype="multipart/form-data"
                      target="_self">
                    <label style="font-weight:bold;">&nbsp;<fmt:message key="humantask.uploadpackge"/></label>
                    <br/><br/>
                    <table class="styledLeft" id="humantaskTbl">
                        <tr>
                            <td class="formRow" width="20%">
                                <label><fmt:message key="humantask.humantask.package"/><font
                                        color="red">*</font></label>
                            </td>
                            <td class="formRow">
                                <input type="file" name="humantaskFileName" size="50"/>&nbsp;
                                <input type="button" width='20px' class="button" onclick="addRow();" value=" + "/>
                            </td>
                        </tr>
                    </table>

                    <table class="styledLeft">
                        <tr>
                            <td class="buttonRow">
                                <input name="upload" class="button registryWriteOperation"
                                       type="button"
                                       value="<fmt:message key="upload"/>" onclick="validate();"/>
                                <input name="upload" class="button registryNonWriteOperation"
                                       type="button"
                                       value="<fmt:message key="upload"/>" disabled="disabled"/>
                                <input type="button" class="button" value="<fmt:message key="button.cancel"/>"
                                       onclick="javascript:location.href='../humantask/humantask_definition_list.jsp'"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>
        <%
        } else {
        %>
        <p><fmt:message key="humantask.do.not.have.permission.to.deploy.humantask.packages"/></p>
        <%
            }
        %>
    </div>

    <script type="text/javascript">
        alternateTableRows('humantaskTbl', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>

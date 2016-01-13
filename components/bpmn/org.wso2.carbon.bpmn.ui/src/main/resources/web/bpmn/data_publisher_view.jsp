<!--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.bpmn.ui.WorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.registry.api.Registry" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ page import="org.wso2.carbon.registry.api.RegistryException" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.api.Resource" %>
<%@ page import="org.wso2.carbon.core.util.CryptoUtil" %>
<%@ page import="org.wso2.carbon.businessprocesses.common.utils.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">
    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                                                                          .getAttribute(
                                                                                  CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        WorkflowServiceClient client;
        try {
            client = new WorkflowServiceClient(cookie, serverURL, configContext);
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
            return;
        }
        RegistryUtils.setTrustStoreSystemProperties();

        String thriftUrl = CharacterEncoder.getSafeText(request.getParameter("thrift_url"));
        String authUrl = CharacterEncoder.getSafeText(request.getParameter("auth_url"));
        String username = CharacterEncoder.getSafeText(request.getParameter("username"));
        String password = CharacterEncoder.getSafeText(request.getParameter("password"));
        String publisherEnable =
                CharacterEncoder.getSafeText(request.getParameter("publisher_enable"));
        String selectType = CharacterEncoder.getSafeText(request.getParameter("publisher_type"));
        String buttonVal = CharacterEncoder.getSafeText(request.getParameter("publishBtn"));

        CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        Registry configRegistry = context.getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        String registryPath = "bpmn/data_analytics_publisher/publisher_configuration";

        try {
            if (configRegistry.resourceExists(registryPath)) {
                Resource resource = configRegistry.get(registryPath);
                if ("POST".equalsIgnoreCase(request.getMethod()) &&
                    "Save".equalsIgnoreCase(buttonVal)) {
                    if (thriftUrl != null && username != null && password != null &&
                        authUrl != null && publisherEnable != null && selectType != null) {
                        String passwordFromReg = resource.getProperty("password");
                        byte[] decryptedPasswordBinary = CryptoUtil.getDefaultCryptoUtil()
                                                                   .base64DecodeAndDecrypt(
                                                                           passwordFromReg);
                        String decryptedPlainPassword = new String(decryptedPasswordBinary);
                        if (thriftUrl.equals(resource.getProperty("receiverURLSet")) &&
                            username.equals(resource.getProperty("username")) &&
                            password.equals(decryptedPlainPassword) &&
                            authUrl.equals(resource.getProperty("authURLSet")) &&
                            publisherEnable.equals(resource.getProperty("dataPublishingEnabled")) &&
                            selectType.equals(resource.getProperty("type"))) {
    %>
    <script type="text/javascript">CARBON.showInfoDialog("Publisher Configuration is already exists.");</script>
    <%
    } else {
    %>
    <script type="text/javascript">CARBON.showInfoDialog("Publisher Configuration is saved successfully.");</script>
    <%
                }
            }
        }
        if (publisherEnable == null) {
            if (resource.getProperty("dataPublishingEnabled") != null) {
                publisherEnable = resource.getProperty("dataPublishingEnabled");
            }
        } else if (!publisherEnable.equals(resource.getProperty("dataPublishingEnabled"))) {
            resource.setProperty("dataPublishingEnabled", publisherEnable);
            configRegistry.put(registryPath, resource);
        }
        if (selectType == null) {
            if (resource.getProperty("type") != null) {
                selectType = resource.getProperty("type");
            } else {
                selectType = "Default";
            }
        } else if (!selectType.equals(resource.getProperty("type"))) {
            resource.setProperty("type", selectType);
            configRegistry.put(registryPath, resource);
        }
        //if resource is available then get properties and set them to the text fields
        if (thriftUrl == null) {
            //if thrift url is null then set value from the registry
            if (resource.getProperty("receiverURLSet") != null) {
                thriftUrl = resource.getProperty("receiverURLSet");
            }
        } else if (!thriftUrl.equals(resource.getProperty("receiverURLSet"))) {
            //else if user updates the thrift url then update the registry property
            resource.setProperty("receiverURLSet", thriftUrl);
            configRegistry.put(registryPath, resource);
        }
        if (authUrl == null) {
            //if auth url is null then set value from the registry
            if (resource.getProperty("authURLSet") != null) {
                authUrl = resource.getProperty("authURLSet");
            }
        } else if (!authUrl.equals(resource.getProperty("authURLSet"))) {
            //else if user updates the auth url then update the registry property
            resource.setProperty("authURLSet", authUrl);
            configRegistry.put(registryPath, resource);
        }
        if (username == null) {
            //if username is null then set value from the registry
            if (resource.getProperty("username") != null) {
                username = resource.getProperty("username");
            }
        } else if (!username.equals(resource.getProperty("username"))) {
            //else if user updates the username then update the registry property
            resource.setProperty("username", username);
            configRegistry.put(registryPath, resource);
        }
        if (password == null) {
            //if password is null then set value from the registry
            if (resource.getProperty("password") != null) {
                byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                        resource.getProperty("password"));
                password = new String(decryptedPassword);
            }
        } else if (!password.equals(resource.getProperty("password"))) {
            //else if user updates the password then update the registry property
            String encryptedPassword =
                    CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(password.getBytes());
            resource.setProperty("password", encryptedPassword);
            configRegistry.put(registryPath, resource);
        }
    } else {
        //if resource doesn't exists then create a new resource and add properties to it.
        if ((thriftUrl != null && thriftUrl.startsWith("tcp://")) ||
            (authUrl != null && authUrl.startsWith("ssl://")) || (username != null) ||
            (password != null) || (publisherEnable != null)) {
            Resource resource = configRegistry.newResource();
            resource.addProperty("receiverURLSet", thriftUrl);
            resource.addProperty("username", username);
            resource.addProperty("password", CryptoUtil.getDefaultCryptoUtil()
                                                       .encryptAndBase64Encode(
                                                               password.getBytes()));
            resource.addProperty("authURLSet", authUrl);
            resource.addProperty("dataPublishingEnabled", publisherEnable);
            configRegistry.put(registryPath, resource);
    %>
    <script type="text/javascript">CARBON.showInfoDialog("Publisher Configuration is saved successfully.");</script>
    <%
                }
            }
        } catch (RegistryException e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        }
    %>
    <script type="text/javascript">

        if (typeof String.prototype.startsWith != 'function') {
            String.prototype.startsWith = function (str) {
                return str.length > 0 && this.substring(0, str.length) === str;
            }
        }
        ;

        function validate() {
            var thriftUrl = document.bpmnDataPublisher.thrift_url.value;
            var username = document.bpmnDataPublisher.username.value;
            var password = document.bpmnDataPublisher.password.value;
            var authUrl = document.bpmnDataPublisher.auth_url.value;

            if ((thriftUrl.length == 0) || (username.length == 0) || (password.length == 0) || authUrl.length == 0) {
                CARBON.showWarningDialog('Please provide correct publisher configuration or select required fields to publish bpmn instances.');
            } else if ((!thriftUrl.startsWith("tcp"))) {
                CARBON.showWarningDialog('Incorrect thrift url is provided.');
            } else if (!authUrl.startsWith("ssl")) {
                CARBON.showWarningDialog('Incorrect thrift ssl url is provided.');
            } else {
                document.bpmnDataPublisher.publishBtn.value = document.bpmnDataPublisher.publish.value;
                document.bpmnDataPublisher.submit();
                // CARBON.showInfoDialog("Thrift Configuration is saved successfully.");
            }
        }

    </script>

    <carbon:breadcrumb
            label="bpmn.data.publisher.dashboard"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="bpmn.data.publisher.dashboard"/></h2>

        <div id="workArea">
            <div id="formset">
                <form id="bpmn_data_publish_form" method="post" name="bpmnDataPublisher" action="">

                    <table class="styledLeft" id="thriftUrlTable">
                        <thead>
                        <tr>
                            <th colspan="6"><fmt:message
                                    key="bpmn.data.publisher.configuration"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="tableEvenRow">
                            <td>Thrift URL<span style="color:red">*</span></td>
                            <td>
                                <input id="thriftUrl" type="text" style="width:100%"
                                       name="thrift_url"
                                       value="<%=(thriftUrl == null) ? "" : thriftUrl%>"
                                       placeholder="tcp://<ip address>:7611">
                            </td>
                            <td>Username<span style="color:red">*</span></td>
                            <td>
                                <input id="username" type="text" style="width:100%" name="username"
                                       value="<%=(username == null) ? "" : username%>"
                                       placeholder="">
                            </td>
                            <td>Password<span style="color:red">*</span></td>
                            <td>
                                <input id="password" type="password" style="width:100%"
                                       name="password"
                                       value="<%=(password == null) ? "" : password%>"
                                       placeholder="">
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>Thrift SSL URL<span style="color:red">*</span></td>
                            <td>
                                <input id="authUrl" type="text" style="width:100%" name="auth_url"
                                       value="<%=(authUrl == null) ? "" : authUrl%>"
                                       placeholder="ssl://<ip address>:7611">
                            </td>
                            <td>Type</td>
                            <td>
                                <select id="publisherType" name="publisher_type">
                                    <option <%
                                        if (selectType == null) {
                                    %>
                                            <%="selected='selected'"%>
                                            <%
                                            } else {
                                            %>
                                            <%=(selectType.equals("Default")) ?
                                               "selected='selected'" : ""%>
                                            <%
                                                }%> value="">
                                        <fmt:message key="bpmn.data.agent.default"/>
                                    </option>
                                    <option <%
                                        if (selectType != null) {
                                    %>
                                            <%=(selectType.equals("Thrift")) ?
                                               "selected='selected'" : ""%>
                                            <%
                                                }%> value="Thrift">
                                        <fmt:message key="bpmn.data.agent.thrift"/>
                                    </option>
                                    <option <%
                                        if (selectType != null) {
                                    %>
                                            <%=(selectType.equals("Binary")) ?
                                               "selected='selected'" : ""%>
                                            <%
                                                }%> value="Binary">
                                        <fmt:message key="bpmn.data.agent.binary"/>
                                    </option>
                                </select>
                            </td>
                            <td>Enable</td>
                            <td>
                                <input id="publisherEnable" type="radio" name="publisher_enable" <%if (publisherEnable != null) {
                                    %>
                                        <%=(publisherEnable.equals("true")) ? "checked" : ""%>
                                        <%
                                }%> value="true">
                                <fmt:message key="bpmn.data.publisher.enable.true"/>
                                <input id="publisherDisable" type="radio" name="publisher_enable" <%if (publisherEnable == null) {
                                    %>
                                        <%="checked"%>
                                        <%
                                } else {
                                    %>
                                        <%=(publisherEnable.equals("false")) ? "checked" : ""%>
                                        <%
                                }%> value="false">
                                <fmt:message key="bpmn.data.publisher.enable.false"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <input name="publishBtn" type="hidden">
                    <table class="styledLeft">
                        <tr>
                            <td class="buttonRow">
                                <input name="publish" class="button registryWriteOperation"
                                       type="button"
                                       style="float: right;"
                                       value="<fmt:message key="bpmn.publish"/>"
                                       onclick="validate()"/>
                            </td>
                        </tr>
                    </table>
                    <br>

                    <table class="styledLeft" id="instanceTable">
                        <thead>
                        <tr>
                            <th width="50%"><fmt:message
                                    key="bpmn.data.publisher.process.instances"/></th>
                            <th width="50%"><fmt:message
                                    key="bpmn.data.publisher.task.instances"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        <tr class="tableOddRow">
                            <td>
                                <label><fmt:message key="bpmn.process.definition.id"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.task.definition.id"/></label><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <label><fmt:message key="bpmn.process.instance.id"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.task.instance.id"/></label><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <label><fmt:message key="bpmn.process.tenant.id"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.task.create.time"/></label><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <label><fmt:message key="bpmn.start.activity.id"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.task.instance.assignee"/></label><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <label><fmt:message key="bpmn.start.user.id"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.process.instance.id"/></label><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <label><fmt:message key="bpmn.instance.start.time"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.instance.start.time"/></label><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <label><fmt:message key="bpmn.instance.end.time"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.instance.end.time"/></label><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <label><fmt:message key="bpmn.instance.duration"/></label><br>
                            </td>
                            <td>
                                <label><fmt:message key="bpmn.instance.duration"/></label><br>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </form>
            </div>
        </div>
    </div>

</fmt:bundle>
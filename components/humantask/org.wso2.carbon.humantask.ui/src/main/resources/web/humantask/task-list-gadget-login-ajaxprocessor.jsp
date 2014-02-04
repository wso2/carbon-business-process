<!--
~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<!DOCTYPE HTML>

<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>

<%
    String displayMessage = CharacterEncoder.getSafeText(request.getParameter("displayMsg"));

%>
<html>
<head>
    <title>Login</title>
    <script type="text/javascript" src="js/humantask-util.js"></script>
    <script type="text/javascript" src="../admin/js/jquery-1.5.2.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/jquery-ui.min.js"></script>
    <link href="css/humantask-gadget.css" rel="stylesheet"/>
    <script type="text/javascript">

        jQuery(document).ready(function() {
            var message = '<%=displayMessage%>';
            displayMessage(message);
        });
    </script>
</head>


<body>
    <div class="titleStrip"><div class="titleStripSide">&nbsp;</div>LOGIN</div>
    <div id="errorStrip" style="display:none;"></div>
    <div class="contentPlacer">
        <form method="post" action="task-list-gadget-ajaxprocessor.jsp" id="loginForm" onsubmit="return validateLoginForm()">
        <div class="loginBox">
            <label>User Name:</label><div><input id="userName" name="userName" type="text" autofocus="autofocus" onkeypress="submitLoginForm(event)" /></div>
            <label>Password:</label><div><input  name="password" type="password" onkeypress="submitLoginForm(event)" /></div>
            <label>&nbsp;</label><div><a onclick="submitLoginForm()" class="button" >Login</a></div>
        </div>
        </form>
    </div>
</body>
</html>
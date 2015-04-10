/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function submitLoginForm(e) {
    var loginForm = document.getElementById('loginForm');

    if (e == null && validateLoginForm()) {
        loginForm.submit();
    }
    if (e && e.keyCode == 13 && validateLoginForm()) {
        loginForm.submit();

    }
}
function validateLoginForm() {
    var userName = document.getElementById('userName');
    var errorStrip = document.getElementById('errorStrip');
    if (userName.value == "") {
        errorStrip.style.display = "";
        errorStrip.innerHTML = "Please enter a user name to login";
        return false;
    }
    return true;
}

function displayMessage(message) {
    var errorStrip = document.getElementById('errorStrip');
    if ('null' != message && message.value != '') {
        errorStrip.style.display = "";
        errorStrip.innerHTML = message;
    }
}


function selectTabTaskFilteringGadget(selectedTab) {
    //unselect all tabs
    var tabs = document.getElementById('tabs_task').getElementsByTagName('a');
    for (var i = 0; i < tabs.length; i++) {
        tabs[i].className = "";
    }
    //hide all the tabs
    var tabContent = document.getElementById("tabContent");
    for (var i = 0; i < tabContent.childNodes.length; i++) {
        if (tabContent.childNodes[i].nodeName == "DIV") {
            tabContent.childNodes[i].style.display = "none";
        }
    }

    selectedTab.className = "selected";
    if (selectedTab.rel != undefined && selectedTab.rel != null) {
        document.getElementById(selectedTab.rel).style.display = "";
    }

    window.location.href = "task-list-gadget-ajaxprocessor.jsp?queryType=" + selectedTab.id;
}

function toggleMe(id) {
    if (document.getElementById(id).style.display == "none") {
        document.getElementById(id).style.display = "";
    } else {
        document.getElementById(id).style.display = "none";
    }
}
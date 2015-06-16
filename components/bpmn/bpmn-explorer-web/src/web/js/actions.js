/*
 ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

var httpUrl = location.protocol + "//" + location.host;
var CONTEXT = "bpmn-explorer";

function completeTask(data, id) {
    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/tasks/" + id;
    var variables = [];
    for (var i = 0; i < data.length; i++) {
        variables.push({
            "name": data[i].name,
            "value": data[i].value
        });
    }
    var body = {
        "action": "complete",
        "variables": variables
    };

    $.ajax({
        type: 'POST',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function (data) {
            window.location = httpUrl + "/" + CONTEXT + "/inbox";
        }
    });
}

function reassign(username, id) {
    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/tasks/" + id;
    var body = {
        "assignee": username
    };

    $.ajax({
        type: 'PUT',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function (data) {
            window.location = httpUrl + "/" + CONTEXT + "/inbox";
        }
    });
}

function transfer(username, id) {
    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/tasks/" + id;
    var body = {
        "owner": username
    };

    $.ajax({
        type: 'PUT',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function (data) {
            window.location = httpUrl + "/" + CONTEXT + "/inbox";
        }
    });
}


function startProcess(processDefId) {
    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/process-instances";
    var body = {
        "processDefinitionId": processDefId
    };

    $.ajax({
        type: 'POST',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function (data) {
            window.location = httpUrl + "/" + CONTEXT + "/inbox";
        }
    });
}

function startProcessWithData(data, id) {
    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/process-instances";
    var variables = [];
    for (var i = 0; i < data.length; i++) {
        variables.push({
            "name": data[i].name,
            "value": data[i].value
        });
    }
    var body = {
        "processDefinitionId": id,
        "variables": variables
    };
    $.ajax({
        type: 'POST',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function (data) {
            showStatusForProcessWithData(id, 1);
        },
        error: function (xhr, status, error) {
            showStatusForProcessWithData(id, 0);
        }

    });
}

function showStatusForProcessWithData(id, status) {
    var statusContent = "";
    if (status == 1) {
        statusContent = "Successfully started process";
    }
    else {
        statusContent = "Failed to start process";
    }
    $('.btn-primary').popover({title: "Process Status", content: statusContent + " " + id,
        placement: "right"});
    $('.btn-primary').popover('show');
    setTimeout(function () {
        $('.btn-primary').popover('hide')
    }, 3000);
}


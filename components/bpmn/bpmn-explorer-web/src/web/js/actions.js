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
var appName = "bpmn-explorer";
var httpUrl = location.protocol + "//" + location.host;
var CONTEXT = "";

if (BPSTenant != undefined && BPSTenant.length > 0) {
    CONTEXT = "t/" + BPSTenant + "/jaggeryapps/" + appName;
} else {
    CONTEXT = appName;
}

$( document ).ready(function() {

    function getFileType(){
        var fileName = document.getElementById('files').value;
        if(fileName!== null) {
            var extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            return extension;
        }
    }
    // Send attachment info the server
    $('#attachForm').ajaxForm({
        beforeSubmit: function(arr, formData, options) {
            for (var i=0; i < arr.length; i++) {
                if (!arr[i].value) {

                    var errorMessage = "Form incomplete"

                    if (arr[i].name == "name") {
                        errorMessage = "Please enter file name";
                    } else if (arr[i].name == "file") {
                        errorMessage = "Please select file";;
                    }

                    $('#submit-attachment-div').popover({ content: errorMessage,
                        placement: "bottom",
                        trigger:"manual",
                        title:"Error"});

                    $('#submit-attachment-div').popover('show');

                    //popover is shown for 5 seconds with error message
                    setTimeout(function () {
                        $('#submit-attachment-div').popover('hide');
                        $('#submit-attachment-div').popover('destroy');
                    }, 5000);

                    return false;
                }
            }

            var fileType = getFileType();
            if(fileType) {
                arr.push({name: 'type', value: fileType})
            }
        },

        success : function(res){
            var taskUrl = res.taskUrl;
            var taskId = taskUrl.substr(taskUrl.lastIndexOf('/') + 1);
            window.location = httpUrl + "/" + CONTEXT + "/task?id=" + taskId ;

        },
        error :  function(res){
            document.getElementById("error_content").style.visibility='visible';
        }
    });
});

function displayAttachmentData(id){
    window.location = httpUrl + "/" + CONTEXT + "/task?id=" + id ;
}

function completeTask(data, id) {
    document.getElementById("completeButton").style.display='none';
    document.getElementById("loadingCompleteButton").hidden = false;
    var url = "/" + CONTEXT + "/send?req=/bpmn/form/form-data%3FtaskId=" + id;
    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success: function (responseData) {
            var vData = JSON.parse(responseData).formProperties;

            var variables = [];
            var emptyVar=true;
            for (var i = 0; i < data.length; i++) {

                for(var j = 0; j < vData.length; j++){
                    if(vData[j].name==data[i].name){
                        if (vData[j].required && vData[j].writable && data[i].value == "") {
                            document.getElementById("commonErrorSection").hidden = false;
                            document.getElementById("errorMsg").innerHTML = "Enter valid inputs for all the required fields";
                            $(document.body).scrollTop($('#commonErrorSection').offset().top);
                            emptyVar = false;
                            document.getElementById("loadingCompleteButton").hidden = true;
                            document.getElementById("completeButton").style.display='';
                            return;
                        }
                    }
                }
                variables.push({
                    "name": data[i].name,
                    "value": data[i].value
                });
            }
            if (emptyVar == true) {
                var body = {
                    "action": "complete",
                    "variables": variables
                };

                var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/tasks/" + id;
                $.ajax({
                    type: 'POST',
                    contentType: "application/json",
                    url: httpUrl + url,
                    data: JSON.stringify(body),
                    success: function (data) {
                        document.getElementById("loadingCompleteButton").hidden = true;
                        document.getElementById("completeButton").style.display='';
                        window.location = httpUrl + "/" + CONTEXT + "/myTasks";
                    },
                    error: function (xhr, status, error) {
                        document.getElementById("loadingCompleteButton").hidden = true;
                        document.getElementById("completeButton").style.display='';
                        document.getElementById("commonErrorSection").hidden = false;
                        document.getElementById("errorMsg").innerHTML = "Task completion failed: " + xhr.responseText;
                        $(document.body).scrollTop($('#commonErrorSection').offset().top);
                        emptyVar = false;
                        return;
                    }
                });
            }

        },
        error: function (xhr, status, error) {
            document.getElementById("loadingCompleteButton").hidden = true;
            document.getElementById("completeButton").style.display='';
            document.getElementById("commonErrorSection").hidden = false;
            document.getElementById("errorMsg").innerHTML = "Task completion failed: " + xhr.responseText;
            $(document.body).scrollTop($('#commonErrorSection').offset().top);
            emptyVar = false;
            return;
        }
    });
}

function reassign(username, id) {
    username = username.trim();
    if (username.length > 0) {
        var url = "/" + CONTEXT + "/backendRequest?operation=userExists&username=" + username;
        $.ajax({
            type: 'GET',
            contentType: "application/json",
            url: httpUrl + url,
            //data: JSON.stringify(body),
            success: function (data) {
                if (data.valid) {
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
                            window.location = httpUrl + "/" + CONTEXT + "/myTasks";
                        }
                    });
                } else {
                    $('#reassignErrMsg').html("Please enter valid username to assignee");
                    $('#reassignErrorMessageArea').show();
                    //set callback to remove error message when hiding the modal
                    $('#reassign').on('hide.bs.modal', function (e) {
                            $('#reassignErrorMessageArea').hide();
                    });
                }
            }
        });
    } else {
        $('#reassignErrMsg').html("Please enter the name of the assignee");
        $('#reassignErrorMessageArea').show();
        //set callback to remove error message when hiding the modal
        $('#reassign').on('hide.bs.modal', function (e) {
            $('#reassignErrorMessageArea').hide();
        });
    }
}

function claim(username, id){
    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/tasks/" + id;
    var body = {
        "assignee" : username
    };

    $.ajax({
        type: 'PUT',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function(data){
            window.location = httpUrl + "/" + CONTEXT + "/task?id=" + id;
        }
    });
}


function transfer(username, id) {
    username = username.trim();
    if (username.length > 0) {
        var url = "/" + CONTEXT + "/backendRequest?operation=userExists&username=" + username;
        $.ajax({
            type: 'GET',
            contentType: "application/json",
            url: httpUrl + url,
            //data: JSON.stringify(body),
            success: function (data) {
                if (data.valid) {
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
                            window.location = httpUrl + "/" + CONTEXT + "/myTasks";
                        }
                    });
                } else {
                    $('#transferErrMsg').html("Please enter valid username to assignee");
                    $('#transferErrorMessageArea').show();
                    //set callback to remove error message when hiding the modal
                    $('#transfer').on('hide.bs.modal', function (e) {
                            $('#transferErrorMessageArea').hide();
                    });
                }
            }
        });
    } else {
        $$('#transferErrMsg').html("Please enter the username");
        $('#transferErrorMessageArea').show();
        //set callback to remove error message when hiding the modal
        $('#transfer').on('hide.bs.modal', function (e) {
            $('#transferErrorMessageArea').hide();
        });
    }
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
            window.location = httpUrl + "/" + CONTEXT + "/process?startProcess=" + processDefId;
        },
        error: function (xhr, status, error) {
            var errorJson = eval("(" + xhr.responseText + ")");
            window.location = httpUrl + "/" + CONTEXT + "/process?errorProcess=" + id + "&errorMessage=" + errorJson.errorMessage;
        }
    });
}

function startProcessWithData(data, id) {
    document.getElementById("startProcessButton").style.display='none';
    document.getElementById("loadingStartProcessButton").hidden = false;
    var url = "/" + CONTEXT + "/send?req=/bpmn/process-definition/" + id + "/properties";
    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success: function (responseData) {
            var vData = JSON.parse(responseData).data;

            var variables = [];
            var emptyVar=true;
            for (var i = 0; i < data.length; i++) {

                for(var j = 0; j < vData.length; j++){
                    if(vData[j].name==data[i].name){
                        if (vData[j].required && vData[j].writable && data[i].value == "") {
                            document.getElementById("commonErrorSection").hidden = false;
                            document.getElementById("errorMsg").innerHTML = "Enter valid inputs for all the required fields";
                            $(document.body).scrollTop($('#commonErrorSection').offset().top);
                            emptyVar = false;
                            document.getElementById("startProcessButton").style.display='';
                            document.getElementById("loadingStartProcessButton").hidden = true;
                            return;
                        }
                    }
                }
                variables.push({
                    "name": data[i].name,
                    "value": data[i].value
                });
            }
            var body = {
                "processDefinitionId": id,
                "variables": variables
            };
            var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/process-instances";
            $.ajax({
                type: 'POST',
                contentType: "application/json",
                url: httpUrl + url,
                data: JSON.stringify(body),
                success: function (data) {
                    document.getElementById("startProcessButton").style.display='';
                    document.getElementById("loadingStartProcessButton").hidden = true;
                    window.location = httpUrl + "/" + CONTEXT + "/process?startProcess=" + id;
                },
                error: function (xhr, status, error) {
                    document.getElementById("startProcessButton").style.display='';
                    document.getElementById("loadingStartProcessButton").hidden = true;
                    var errorJson = eval("(" + xhr.responseText + ")");
                    var errorJson = eval("(" + xhr.responseText + ")");
                    window.location = httpUrl + "/" + CONTEXT + "/process?errorProcess=" + id + "&errorMessage=" + errorJson.errorMessage;
                }
            });
        },
        error: function (xhr, status, error) {
            document.getElementById("loadingCompleteButton").hidden = true;
            document.getElementById("completeButton").style.display='';
            document.getElementById("commonErrorSection").hidden = false;
            document.getElementById("errorMsg").innerHTML = "Task completion failed: " + xhr.responseText;
            $(document.body).scrollTop($('#commonErrorSection').offset().top);
            emptyVar = false;
            return;
        }
    });    
}




/**
 * Function to process search inputs before submission
 */
function processSearch(){
    //disable startDate input to avoid adding it to the query parameters
    document.getElementById("startDate").disabled = true;
    document.getElementById("endDate").disabled = true;

    //disable form inputs if user havn't entered values
    if (document.getElementById("taskName").value.length == 0) {
        document.getElementById("taskName").disabled = true;
    } else {
        var tempTaskName = document.getElementById("taskName").value;
        document.getElementById("taskName").value = "%" + tempTaskName + "%";
    }
    /*if (document.getElementById("taskDescription").value.length == 0) {
     document.getElementById("taskDescription").disabled = true;
     }*/
    if (document.getElementById("taskCandidateUser").value.length == 0) {
        document.getElementById("taskCandidateUser").disabled = true;
    }
    if (document.getElementById("taskAssignee").value.length == 0) {
        if (document.getElementById("taskUnassigned").checked == true) {
            document.getElementById("taskAssignee").disabled = false;
            document.getElementById("taskUnassigned").disabled = true;
        } else {
            document.getElementById("taskAssignee").disabled = true;
        }
    }
    if (document.getElementById("taskOwner").value.length == 0) {
        document.getElementById("taskOwner").disabled = true;
    }
    if (document.getElementById("taskProcessDefName").value.length == 0) {
        document.getElementById("taskProcessDefName").disabled = true;
    } else {
        var tempTaskDefName = document.getElementById("taskProcessDefName").value;
        var selectState=document.getElementById("taskStatus").value;
        if(selectState == "COMPLETED"){
            document.getElementById("taskProcessDefName").value = tempTaskDefName;
        } else {
            document.getElementById("taskProcessDefName").value = "%" + tempTaskDefName + "%";
        }
    }
    if (document.getElementById("taskProcessInstanceID").value.length == 0) {
        document.getElementById("taskProcessInstanceID").disabled = true;
    }
    if (document.getElementById("instanceSuspensionStatus").value.length == 0) {
        document.getElementById("instanceSuspensionStatus").disabled = true;
    }
    if (document.getElementById("taskMinPriority").value.length == 0) {
        document.getElementById("taskMinPriority").disabled = true;
    }
    if (document.getElementById("taskMaxPriority").value.length == 0) {
        document.getElementById("taskMaxPriority").disabled = true;
    }
    if (document.getElementById("taskProcessDefName").value.length == 0) {
        document.getElementById("taskProcessDefName").disabled = true;
    }

    //add start date in ISO Date format
    var SDate = document.getElementById("startDate");
    if (SDate.value.length > 0) {
        var startDateTemp = new Date(SDate.value);
        var startDateISOTemp = document.getElementById("startDateISO");
        startDateISOTemp.value = startDateTemp.toISOString();
        startDateISOTemp.value = startDateTemp.toISOString().split('.')[0] + 'Z';
    } else {
        //disable startDateISO since it's not entered by the user
        document.getElementById("startDateISO").disabled = true;
    }

    //add end date in ISO Date format
    var EDate = document.getElementById("endDate");
    if (EDate.value.length > 0) {
        var endDateTemp = new Date(EDate.value);
        endDateTemp.setHours(23);
        endDateTemp.setMinutes(59);
        endDateTemp.setSeconds(59);
        console.log(endDateTemp);
        console.log(endDateTemp.toISOString());
        var endDateISOTemp = document.getElementById("endDateISO");
        endDateISOTemp.value = endDateTemp.toISOString();
        endDateISOTemp.value = endDateTemp.toISOString().split('.')[0] + 'Z';
    } else {
        //disable startDateISO since it's not entered by the user
        document.getElementById("endDateISO").disabled = true;
    }


}

/**
 Function to set date picker to date input elements
 */
function setDatePicker (dateElement) {
    var elementID = '#' + dateElement;
    $(elementID).daterangepicker({
        singleDatePicker: true,
        showDropdowns: true,
        locale: {
            format: 'MM/DD/YYYY'
        }
    });
}

//Average Task duration for each process

function selectProcessForChart(){
    var x = document.getElementById("selectProcess").value;
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=taskduration&id=" + x ;

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {

            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));

            function drawChart(data) {
                var dataArr = [['Task Key', 'Avg Duration']];
                for(var i = 0;i < data.length;i++){
                    dataArr.push([data[i][0] , data[i][1]]);

                }

                var data = google.visualization.arrayToDataTable(dataArr);

                var options = {
                    title:x,
                    pieHole: 0.6,
                    pieSliceTextStyle: {
                        color: 'black'
                    },
                    sliceVisibilityThreshold: 0
                };
                var chart = new google.visualization.PieChart(document.getElementById('taskDurationChart'));
                chart.draw(data, options);
            }
        }
    });
}

//User Performance of Tasks Completed and Tasks Started Over time i.e. months

function selectUserForPerformance(){
    var x = document.getElementById("selectUser").value;
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=userperformance&id=" + x ;

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {
            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));

            function drawChart(data) {

                var dataArr = [['Month', 'Started Tasks', 'Completed Tasks']];
                for(var i = 0;i < data.length;i++){
                    dataArr.push([data[i][0] , data[i][1], data[i][2]]);

                }
                var data = google.visualization.arrayToDataTable(dataArr);
                var max=0;
                var count = 0;

                for(var i=0; i<dataArr.length;i++){
                    if (dataArr[i][1] > max) {
                        max = dataArr[i][1];
                    }
                    if(dataArr[i][2] > max){
                        max = dataArr[i][2];
                    }
                    if(dataArr[i][1] > 0 ){
                        count++;
                    }
                    if(dataArr[i][2] > 0 ){
                        count++;
                    }
                }

                if(max >= 100 && count > 1){
                    var vTitle='Number of Tasks Completed/Started(log scale)';
                    var logScaleEnabled = true;
                }
                else{
                    var vTitle='Number of Tasks Completed/Started';
                    var logScaleEnabled = false;
                }

                var chartAreaHeight = data.getNumberOfRows() * 32;
                var chartHeight = chartAreaHeight + 30;

                var options = {
                    vAxis: {title: vTitle,  titleTextStyle: { color: 'grey' },logScale:logScaleEnabled},
                    hAxis: {title: 'Months', titleTextStyle: {color: 'grey'}},
                    colors:['#be2d28','#afaeae'],
                    height: chartHeight,
                    bar: {groupWidth: "70%"},
                    chartArea: {
                        width: '75%'
                    },
                    legend: { position: "top"}

                };
                var chart = new google.visualization.ColumnChart(document.getElementById('taskOfUserVariation'));
                chart.draw(data, options);
            }
        }
    });
}

// Average duration of Processes Instances

function selectProcessForAvgTimeDuration(){
    var x = document.getElementById("selectOption").value;
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=avgprocessduration&id=" + x ;

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {

            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));

            if (array.length == 0) {
                var chartDiv=document.getElementById('barChartAvgTime');
                chartDiv.style.fontSize = "20px";
                chartDiv.innerHTML = "<br> No data";
                chartDiv.style.textAlign = "center";
            } else {
                function drawChart(data) {
                    var dataArr = [['Process Name', 'Time Duration in Minutes']];
                    for (var i = 0; i < data.length; i++) {
                        dataArr.push([data[i][0], data[i][1]]);
                    }
                    var data = google.visualization.arrayToDataTable(dataArr);

                    var chartAreaHeight=((data.getNumberOfRows()+2) * 100);
                    var options = {
                        vAxis: {
                            title: 'Process Name',
                            titleTextStyle: {color: 'grey'}
                        },
                        hAxis: {
                            title: 'Average Time Time Duration in Minutes',
                            titleTextStyle: {color: 'grey'}
                        },
                        height: ((data.getNumberOfRows()+2) * 100) + 200,
                        chartArea:{top:10,height:chartAreaHeight},
                        bar: {groupWidth: "30%"},
                        colors: ['#be2d28']
                    };

                    var chart = new google.visualization.BarChart(document.getElementById('barChartAvgTime'));
                    chart.draw(data, options);
                }
            }
        }
    });
}



//Process Instance Count : Filters the 10 processes with the maximum and the minimum instance counts
function selectProcessForInstanceCount(){
    var x = document.getElementById("processInstanceCount").value;
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=processinstancecount&id=" + x ;

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {

            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));


            function drawChart(data) {
                var dataArr = [['Process Name', 'Instance Count']];
                for(var i = 0;i < data.length;i++){
                    dataArr.push([data[i][0] , data[i][1]]);
                }
                var data = google.visualization.arrayToDataTable(dataArr);
                var max=0;
                var count=0;

                for(var i=0; i<dataArr.length;i++){
                    if (dataArr[i][1] > max) {
                        max = dataArr[i][1];
                    }
                    if(dataArr[i][1]>0){
                        count++;
                    }
                }

                if(max >= 100 && count > 1){
                    var hTitle='Process instance count (log scale)';
                    var logScaleEnabled = true;
                }
                else{
                    var hTitle='Process instance count';
                    var logScaleEnabled = false;
                }
                var chartAreaHeight=((data.getNumberOfRows()+2) * 100);
                var options = {
                    vAxis: {title: 'Process Name',  titleTextStyle: { color: 'grey' }},
                    hAxis: {title: hTitle, titleTextStyle: {color: 'grey'},logScale:logScaleEnabled},
                    colors:['#be2d28'],
                    height: ((data.getNumberOfRows()+2) * 100) + 200,
                    chartArea:{top:10,height:chartAreaHeight},
                    bar: {groupWidth: "35%"}
                };

                var chart = new google.visualization.BarChart(document.getElementById('barChart'));
                chart.draw(data, options);
            }
        }
    });
}

//User Vs Number of tasks completed uptodate
function userVsTasksCompleted(){
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=uservstaskscompleted";

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {

            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));

            if (array.length == 0) {
                var chartDiv=document.getElementById('colChartUserVsTasks');
                chartDiv.style.fontSize = "20px";
                chartDiv.innerHTML = " <br> No data";
                chartDiv.style.textAlign = "center";
                chartDiv.style.height="50px";
            } else {

                function drawChart(data) {
                    var dataArr = [['User', 'No. of tasks completed todate']];
                    for (var i = 0; i < data.length; i++) {
                        dataArr.push([data[i][0], data[i][1]]);
                    }
                    var data = google.visualization.arrayToDataTable(dataArr);

                    var max = 0;
                    var count = 0;
                    for (var i = 0; i < dataArr.length; i++) {
                        if (dataArr[i][1] > max) {
                            max = dataArr[i][1];
                        }
                        if (dataArr[i][1] > 0) {
                            count++;
                        }
                    }

                    if (max >= 100 && count > 1) {
                        var hTitle = 'Number of tasks completed todate(log scale)';
                        var logScaleEnabled = true;
                    }
                    else {
                        var hTitle = 'Number of tasks completed todate';
                        var logScaleEnabled = false;
                    }
                    var chartAreaHeight=((data.getNumberOfRows()+2) * 100);
                    var options = {
                        hAxis: {
                            title: hTitle,
                            titleTextStyle: {color: 'grey'},
                            logScale: logScaleEnabled
                        },
                        vAxis: {title: 'User', titleTextStyle: {color: 'grey'}},
                        colors: ['#be2d28'],
                        height: ((data.getNumberOfRows()+2) * 100) + 200,
                        chartArea:{top:10,height:chartAreaHeight},
                        bar: {groupWidth: "35%"}
                    };

                    var chart = new google.visualization.BarChart(document.getElementById('colChartUserVsTasks'));
                    chart.draw(data, options);

                }
            }
        }
    });
}

// Average time taken by user to complete tasks
function avgTimeForUserForTasks(){
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=uservsavgtime";

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {

            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));

            if (array.length == 0) {
                var chartDiv=document.getElementById('userVsAvgTaskDuration');
                chartDiv.style.fontSize = "20px";
                chartDiv.innerHTML = " <br> No data";
                chartDiv.style.textAlign = "center";
                chartDiv.style.height="50px";
            } else {
                function drawChart(data) {
                    var dataArr = [['User', 'Average Time Taken to Complete Tasks in Seconds']];
                    for (var i = 0; i < data.length; i++) {
                        dataArr.push([data[i][0], data[i][1]]);
                    }
                    var data = google.visualization.arrayToDataTable(dataArr);
                    var max = 0;
                    var count = 0;
                    for (var i = 0; i < dataArr.length; i++) {
                        if (dataArr[i][1] > max) {
                            max = dataArr[i][1];
                        }
                        if (dataArr[i][1] > 0) {
                            count++;
                        }
                    }

                    if (max >= 100 && count > 1) {
                        var hTitle = 'Average Time Taken to Complete Tasks in Seconds(log scale)';
                        var logScaleEnabled = true;
                    }
                    else {
                        var hTitle = 'Average Time Taken to Complete Tasks in Seconds';
                        var logScaleEnabled = false;
                    }
                    var chartAreaHeight=((data.getNumberOfRows()+2) * 100);
                    var options = {
                        hAxis: {
                            title: hTitle,
                            titleTextStyle: {color: 'grey'}
                        },
                        vAxis: {
                            title: 'Users',
                            titleTextStyle: {color: 'grey'}
                        },
                        colors: ['#be2d28'],
                        height: ((data.getNumberOfRows()+2) * 100) + 200,
                        chartArea:{top:10,height:chartAreaHeight},
                        bar: {groupWidth: "35%"}
                    };

                    var chart = new google.visualization.BarChart(document.getElementById('userVsAvgTaskDuration'));
                    chart.draw(data, options);

                }
            }
        }
    });
}

// No.of tasks started and completed overtime
function taskVariationOverTime(){
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=taskvariation";

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {

            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));


            function drawChart(data) {
                var dataArr = [['Months', 'Tasks Started','Tasks Completed']];
                for(var i = 0;i < data.length;i++){
                    dataArr.push([data[i][0] , data[i][1],data[i][2]]);
                }
                var data = google.visualization.arrayToDataTable(dataArr);

                var max=0;
                var count = 0;

                for(var i=0; i<dataArr.length;i++){
                    if (dataArr[i][1] > max) {
                        max = dataArr[i][1];
                    }
                    if(dataArr[i][2] > max){
                        max = dataArr[i][2];
                    }
                    if(dataArr[i][1] > 0 ){
                        count++;
                    }
                    if(dataArr[i][2] > 0 ){
                        count++;
                    }
                }

                if(max >= 100 && count > 1){
                    var vTitle='Number of Tasks Completed/Started(log scale)';
                    var logScaleEnabled = true;
                }
                else{
                    var vTitle='Number of Tasks Completed/Started';
                    var logScaleEnabled = false;
                }

                var chartAreaHeight = data.getNumberOfRows() * 32;
                var chartHeight = chartAreaHeight + 32;

                var options = {
                    vAxis: {title: vTitle,  titleTextStyle: { color: 'grey' },logScale:logScaleEnabled},
                    hAxis: {title: 'Months', titleTextStyle: {color: 'grey'}},
                    colors:['#be2d28','#afaeae'],
                    height: chartHeight,
                    bar: {groupWidth: "70%"},
                    chartArea: {
                        width: '75%'
                    },
                    legend: { position: "top"},
                    format: 'decimal'
                };

                var chart = new google.visualization.ColumnChart(document.getElementById('taskVariationOverTime'));

                chart.draw(data, options);

            }
        }
    });
}

// No.of processes started and completed overtime
function processVariationOverTime(){
    var url = httpUrl + "/" + CONTEXT + "/reports?update=true&option=processvariation";

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: url,
        success: function (data) {

            var array = eval('('+data+')');
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart(array));

            function drawChart(data) {
                var dataArr = [['Months', 'Started Processes','Completed Processes']];
                for(var i = 0;i < data.length;i++){
                    dataArr.push([data[i][0] , data[i][1],data[i][2]]);
                }
                var data = google.visualization.arrayToDataTable(dataArr);
                var max=0;
                var count = 0;

                for(var i=0; i<dataArr.length;i++){
                    if (dataArr[i][1] > max) {
                        max = dataArr[i][1];
                    }
                    if(dataArr[i][2] > max){
                        max = dataArr[i][2];
                    }
                    if(dataArr[i][1] > 0 ){
                        count++;
                    }
                    if(dataArr[i][2] > 0 ){
                        count++;
                    }
                }

                if(max >= 100 && count > 1){
                    var vTitle='Number of Processes Completed/Started(log scale)';
                    var logScaleEnabled = true;
                }
                else{
                    var vTitle='Number of Processes Completed/Started';
                    var logScaleEnabled = false;
                }

                var chartAreaHeight = data.getNumberOfRows() * 32;
                var chartHeight = chartAreaHeight + 32;

                var options = {
                    vAxis: {title: vTitle,  titleTextStyle: { color: 'grey' },logScale:logScaleEnabled},
                    hAxis: {title: 'Months', titleTextStyle: {color: 'grey'}},
                    colors:['#be2d28','#afaeae'],
                    height: chartHeight,
                    bar: {groupWidth: "70%"},
                    chartArea: {
                        width: '75%'
                    },
                    legend: { position: "top"},
                    format: 'decimal'
                };

                var chart = new google.visualization.ColumnChart(document.getElementById('processVariationOverTime'));

                chart.draw(data, options);

            }
        }
    });
}

// Generate the report view by displaying the graphs
function generateReport(){

    selectProcessForInstanceCount();
    selectProcessForAvgTimeDuration();
    userVsTasksCompleted();
    avgTimeForUserForTasks();
    taskVariationOverTime();
    processVariationOverTime();

    var barChartDisplay= document.getElementById("barChartDisplay");
    barChartDisplay.hidden= false;

    var pieChartDisplay= document.getElementById("pieChartDisplay");
    pieChartDisplay.hidden= false;

    var genButton= document.getElementById("generate")
    genButton.hidden= true;

    var h3= document.getElementById("h3")
    h3.hidden= true;

}

//Gets the details of the user-tasks in a completed process instance
function getUserTasksOfCompletedProcessInstances(id){

    var url = "/" + CONTEXT + "/send?req=/bpmn/history/historic-task-instances?processInstanceId=" + id;
    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success: function (data) {

            $("#userTasks").html("");
            var completedTaskInstances = JSON.parse(data);
            var DIV = "<div style='height:100%;overflow:auto;'><table id ='table1'><thead><td>State</td><td>Task Definition Key</td><td>Task Name</td><td>Start time</td><td>End time</td><td>Assignee</td><td>Duration</td></thead><tbody>"
            for (var k = 0; k < completedTaskInstances.data.length; k++) {

                var state = "Completed";
                var taskDefKey = completedTaskInstances.data[k].taskDefinitionKey;
                var taskName = completedTaskInstances.data[k].name;
                var startTime = completedTaskInstances.data[k].startTime;
                var endTime = completedTaskInstances.data[k].endTime;
                var assignee = completedTaskInstances.data[k].assignee;
                var duration = completedTaskInstances.data[k].durationInMillis;
                              
                DIV = DIV + "<tr><td>"+state+"</td><td style='word-wrap: break-word'>"+taskDefKey+"</td><td style='word-wrap: break-word'>"+taskName+"</td><td>"+startTime+"</td><td>"+endTime+"</td><td>"+assignee+"</td><td>"+duration+"</td></tr>";

            }
            DIV = DIV+"</tbody></table></div>"
            $("#userTasks").html(DIV);

        }
    });
}

//Gets the details of the variables in a completed process instance
function getVariablesOfCompletedProcessInstances(id){

    var url = "/" + CONTEXT + "/send?req=/bpmn/history/historic-variable-instances?processInstanceId=" + id;
    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success: function (data) {

            $("#variables").html("");
            var variableInfo = JSON.parse(data);
            if (variableInfo.data.length == 0) {
                var DIV = "<h3> No variables for this process instance </h3>";
                $("#variables").html(DIV);
            } else {
                var DIV = "<div style='height:100%;overflow:auto;'><table id ='table1'><thead><td>Name</td><td>Type</td><td>Value</td><td>Scope</td></thead><tbody>"
                for (var k = 0; k < variableInfo.data.length; k++) {
                    var name = variableInfo.data[k].variable.name;
                    var type =variableInfo.data[k].variable.type;
                    var value = variableInfo.data[k].variable.value;
                    var scope = variableInfo.data[k].variable.scope;
                    
                    DIV = DIV + "<tr><td style='word-wrap: break-word'>"+name+"</td><td>"+type+"</td><td style='word-wrap: break-word'>"+value+"</td><td>"+scope+"</td></tr>";

                }
                DIV = DIV+"</tbody></table></div>"
                $("#variables").html(DIV);
            }

        }
    });
}

//Gets the details of all the activities in a completed process instance
function getAuditLogForCompletedProcessInstances(id){

    var url = "/" + CONTEXT + "/send?req=/bpmn/history/historic-activity-instances?processInstanceId=" + id;    

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success: function (data) {

            $("#auditLog").html("");
            var completedTaskInstances = JSON.parse(data);
            var DIV = "<div style='height:100%;overflow:auto;'><table id ='table1'><thead><td>State</td><td>Activity Name</td><td>Activity Type</td><td>Start Time</td><td>End Time</td><td>Task Id</td><td>Activity Instance Id</td></thead><tbody>"
            for (var k = 0; k < completedTaskInstances.data.length; k++) {

                var state = "Completed";
                var activityName  = completedTaskInstances.data[k].activityName;
                var activityType  = completedTaskInstances.data[k].activityType;
                var activityStartTime  = completedTaskInstances.data[k].startTime;
                var activityEndTime  = completedTaskInstances.data[k].endTime;
                var taskId  = completedTaskInstances.data[k].taskId;
                var activityInstanceId  = completedTaskInstances.data[k].id;
                if (taskId == null) {
                    taskId = "N/A";
                }
               
                DIV = DIV + "<tr><td>"+state+"</td><td style='word-wrap: break-word'>"+activityName+"</td><td>"+activityType+"</td><td>"+activityStartTime+"</td><td>"+activityEndTime+"</td><td>"+taskId+"</td><td>"+activityInstanceId+"</td></tr>";

            }
            DIV = DIV+"</tbody></table></div>"
            $("#auditLog").html(DIV);

        }
    });
}

//Gets the details of any called process instances for a completed process instance
function getCalledProcessInstancesOfCompleted(id){

    var url = "/" + CONTEXT + "/send?req=/bpmn/history/historic-process-instances/" + id;

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success:

            function innerFunction(data){
                var calledPId= JSON.parse(data).superProcessInstanceId;
                $("#calledInstances").html("");
                if (calledPId == null) {
                    var result = "<h3> No Called Process Instances </h3>";
                    $("#calledInstances").html(result);
                } else {

                    var url1 = "/" + CONTEXT + "/send?req=/bpmn/history/historic-process-instances/" + calledPId ;

                    $.ajax({
                        type: 'GET',
                        contentType: "application/json",
                        url: httpUrl + url1,

                        success: function (data){

                            var calledPInfo = JSON.parse(data);
                            $("#calledInstances").html("");
                            var DIV = "<div style='height:100%;overflow:auto;'><table id ='table1'><thead><td>Instance Id </td><td>Process Definition</td><td>Start Time</td><td>End Time</td><td>Time Duration</td></thead><tbody>"

                            var id  = calledPInfo.id;
                            var processDefinitionId= calledPInfo.processDefinitionId;
                            var startTime  = calledPInfo.startTime;
                            var endTime  = calledPInfo.endTime;
                            var durationInMillis  = calledPInfo.durationInMillis;
                          
                            DIV = DIV + "<tr><td>"+id+"</td><td style='word-wrap: break-word'>"+processDefinitionId+"</td><td>"+startTime+"</td><td>"+endTime+"</td><td>"+durationInMillis+"</td></tr>";
                            DIV = DIV+"</tbody></table></div>"

                            $("#calledInstances").html(DIV);

                        }
                    });
                }

            }
    });
}

//Generates the details for a completed process instance
function completedProcessInstances(id){
   
    getAuditLogForCompletedProcessInstances(id);
    getUserTasksOfCompletedProcessInstances(id);
    getVariablesOfCompletedProcessInstances(id);
    getCalledProcessInstancesOfCompleted(id);

}

//Gets the details of all the activities in a running/active process instance
function getAuditLogForRunningProcessInstances(pid,id){

    var url = "/" + CONTEXT + "/send?req=/bpmn/stats/processTaskServices/allTasks/" + pid;

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success:

            function innerFunction(data){

                var taskList = JSON.parse(data);
                
                var url1 = "/" + CONTEXT + "/send?req=/bpmn/history/historic-activity-instances?processInstanceId=" + id;

                $.ajax({
                    type: 'GET',
                    contentType: "application/json",
                    url: httpUrl + url1,
                    success: function (data){

                        $("#auditLog").html("");
                        var taskList2 = JSON.parse(data);
                        
                        var DIV = "<div style='height:100%;overflow:auto;'><table id ='table1'><thead><td>State</td><td>Activity Name</td><td>Activity Type</td><td>Start Time</td><td>End Time</td><td>Task Id</td><td>Activity Instance Id</td></thead><tbody>"

                        for (var k = 0; k < taskList.data.length; k++) {

                            var activityName  = taskList.data[k].name;
                            var taskDefKey  = taskList.data[k].taskDefinitionKey;
                            var activityType  = taskList.data[k].type;

                            for (var j = 0; j < taskList2.data.length; j++) {

                                var activityId  = taskList2.data[j].activityId;
                                var startTime = taskList2.data[j].startTime;
                                var endTime = taskList2.data[j].endTime;
                                var taskId = taskList2.data[j].taskId;
                                var activityInstanceId = taskList2.data[j].id;
                                if (taskId == null) {
                                    taskId = "N/A";
                                }
                                if (taskDefKey == activityId && endTime !== null ) {
                                    var state = "Completed";
                                    break;
                                } else if (taskDefKey == activityId && endTime == null ) {
                                    var state = "Active";
                                    var endTime = "N/A";
                                    break;
                                } else {
                                    var state = "Not Started";
                                    var startTime = "N/A";
                                    var endTime = "N/A";
                                    var activityInstanceId = "N/A";
                                    var taskId = "N/A";
                                }

                            }
                         
                            DIV = DIV + "<tr><td>"+state+"</td><td style='word-wrap: break-word'>"+activityName+"</td><td>"+activityType+"</td><td>"+startTime+"</td><td>"+endTime+"</td><td>"+taskId+"</td><td>"+activityInstanceId+"</td></tr>";

                        }
                        DIV = DIV+"</tbody></table></div>"
                        $("#auditLog").html(DIV);

                    }
                });
            }
    });
}

//Gets the details of the variables in a running/active process instance
function getVariablesOfRunningProcessInstances(id){

    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/process-instances/"+id+"/variables";

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success: function (data) {

            $("#variables").html("");
            var variableInfo = JSON.parse(data);
            if (variableInfo.restVariables.length == 0) {
                var DIV = "<h3> No variables for this process instance </h3>";
                $("#variables").html(DIV);
            } else {
                var DIV = "<div style='height:100%;overflow:auto;'><table id ='table1'><thead><td>Name</td><td>Type</td><td>Value</td><td>Scope</td></thead><tbody>"
                for (var k = 0; k < variableInfo.restVariables.length; k++) {
                    var name = variableInfo.restVariables[k].name;
                    var type =variableInfo.restVariables[k].type;
                    var value = variableInfo.restVariables[k].value;                    
                    var scope = variableInfo.restVariables[k].variableScope;

                    DIV = DIV + "<tr><td style='word-wrap: break-word'>"+name+"</td><td>"+type+"</td><td style='word-wrap: break-word'>"+value+"</td><td>"+scope+"</td></tr>";
                }
                DIV = DIV+"</tbody></table></div>"
                $("#variables").html(DIV);
            }
        }
    });
}

//Gets the details of the user-tasks in a running/active process instance
function getUserTasksOfRunningProcessInstances(pid,id){

    var url = "/" + CONTEXT + "/send?req=/bpmn/stats/processTaskServices/allTasks/" + pid;

    $.ajax({
        type: 'GET',
        contentType: "application/json",
        url: httpUrl + url,
        success:

            function innerFunction(data){

                var taskList = JSON.parse(data);
                var url1 = "/" + CONTEXT + "/send?req=/bpmn/history/historic-activity-instances?processInstanceId=" + id;

                $.ajax({
                    type: 'GET',
                    contentType: "application/json",
                    url: httpUrl + url1,
                    success: function (data){

                        $("#userTasks").html("");
                        var taskList2 = JSON.parse(data);

                        var DIV = "<div style='height:100%;overflow:auto;'><table id ='table1'><thead><td>State</td><td>Task Name</td><td>Task Definition Key</td><td>Start Time</td><td>End Time</td><td>Time Duration</td><td>Assignee</td></thead><tbody>"

                        for (var k = 0; k < taskList.data.length; k++) {

                            var activityName  = taskList.data[k].name;
                            var taskDefKey  = taskList.data[k].taskDefinitionKey;
                            var activityType  = taskList.data[k].type;
                            if (activityType == "userTask") {

                                for (var j = 0; j < taskList2.data.length; j++) {

                                    var activityId  = taskList2.data[j].activityId;
                                    var startTime = taskList2.data[j].startTime;
                                    var endTime = taskList2.data[j].endTime;
                                    var assignee = taskList2.data[j].assignee;
                                    var duration =  taskList2.data[j].durationInMillis;

                                    if (taskDefKey == activityId && endTime !== null ) {
                                        var state = "Completed";

                                        break;
                                    } else if (taskDefKey == activityId && endTime == null ) {
                                        var state = "Active";
                                        var endTime = "N/A";
                                        var duration = "N/A";
                                        if (assignee == null) {
                                            assignee = "Unassigned";
                                        }

                                        break;
                                    } else {
                                        var state = "Not Started";
                                        var startTime = "N/A";
                                        var endTime = "N/A";
                                        var duration = "N/A";
                                        var assignee = "Unassigned";

                                    }
                                }                
                               
                                DIV = DIV + "<tr><td>"+state+"</td><td style='word-wrap: break-word'>"+activityName+"</td><td style='word-wrap: break-word'>"+taskDefKey+"</td><td>"+startTime+"</td><td>"+endTime+"</td><td>"+duration+"</td><td>"+assignee+"</td></tr>";
                            } else {
                            }
                        }
                        DIV = DIV+"</tbody></table></div>"
                        $("#userTasks").html(DIV);

                    }
                });
            }
    });
}

//Gets the details of any called process instance in a running/active process instance
function getCalledProcessInstancesOfRunning(id){


    $.ajax({
        type: 'GET',
        contentType: "application/json",
        success: function (data){
            $("#calledInstances").html("");
            var result = "<h3> Complete the process instance to view any called process-instances </h3>";
            $("#calledInstances").html(result);

        }
    });
}

//Generates the details for a running/active process instance
function runningProcessInstances(pid,id){

    getAuditLogForRunningProcessInstances(pid,id);
    getVariablesOfRunningProcessInstances(id);
    getUserTasksOfRunningProcessInstances(pid,id);
    getCalledProcessInstancesOfRunning(id);

}

//Styling for the tab click in process monitoring
function tabClick(){
    var tabs = $('input[name=tab-group-1]');
    for (var i = 0; i < tabs.length; i++) {
        if (tabs[i].checked) {
            $(tabs[i]).siblings("div").attr("style", "");
        } else {
            $(tabs[i]).siblings("div").attr("style", "display: none");
        }
    }

}
/**
 * Function to process search inputs before submission for advanced filtering
 */
function validateFilter(){

     //disable startDate input to avoid adding it to the query parameters
    document.getElementById("startDate").disabled = true;
    document.getElementById("endDate").disabled = true;

    if (document.getElementById("instanceId").value.length == 0) {
        document.getElementById("instanceId").disabled = true;
    }
    if (document.getElementById("variableName").value.length == 0) {
        document.getElementById("variableName").disabled = true;
    }
     if (document.getElementById("variableValue").value.length == 0) {
        document.getElementById("variableValue").disabled = true;
    }

    //add start date in ISO Date format
    var SDate = document.getElementById("startDate");
    if (SDate.value.length > 0) {
        var startDateTemp = new Date(SDate.value);
        var startDateISOTemp = document.getElementById("startDateISO");
        startDateISOTemp.value = startDateTemp.toISOString().split('.')[0] + 'Z';
    } else {
        //disable startDateISO since it's not entered by the user
        document.getElementById("startDateISO").disabled = true;
    }

    //add end date in ISO Date format
    var EDate = document.getElementById("endDate");
    if (EDate.value.length > 0) {
        var endDateTemp = new Date(EDate.value);
        endDateTemp.setHours(23);
        endDateTemp.setMinutes(59);
        endDateTemp.setSeconds(59);
        console.log(endDateTemp);
        console.log(endDateTemp.toISOString());
        var endDateISOTemp = document.getElementById("endDateISO");
        endDateISOTemp.value = endDateTemp.toISOString().split('.')[0] + 'Z';
    } else {
        //disable startDateISO since it's not entered by the user
        document.getElementById("endDateISO").disabled = true;
    }

}
/**
 * Function to filter the records for the instance id in advanced filtering
 */
function filterResults(id){
   
    window.location = httpUrl + "/" + CONTEXT + "/processMonitoring?instanceId=" + id ;   
}


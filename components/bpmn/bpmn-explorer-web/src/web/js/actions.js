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
    var url = "/" + CONTEXT + "/send?req=/bpmn/runtime/tasks/" + id;
    var variables = [];
    var emptyVar=true;
    for (var i = 0; i < data.length; i++) {

        if (data[i].value == "") {
            document.getElementById("commonErrorSection").hidden = false;
            document.getElementById("errorMsg").innerHTML = "Enter valid inputs for all the fields";
            $(document.body).scrollTop($('#commonErrorSection').offset().top);
            emptyVar = false;
            break;
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

        $.ajax({
            type: 'POST',
            contentType: "application/json",
            url: httpUrl + url,
            data: JSON.stringify(body),
            success: function (data) {
                window.location = httpUrl + "/" + CONTEXT + "/myTasks";
            }
        });
    }
}

function reassign(username, id) {
    
    username = username.trim();
    if (username.length > 0) {
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
    if (username.length > 0) {
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
        $('#transferErrMsg').html("Please enter the username");
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
            window.location = httpUrl + "/" + CONTEXT + "/process?startProcess=" + id;
        },
        error: function (xhr, status, error) {
            var errorJson = eval("(" + xhr.responseText + ")");
            window.location = httpUrl + "/" + CONTEXT + "/process?errorProcess=" + id + "&errorMessage=" + errorJson.errorMessage;
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

                var dataArr = [['Month', 'Completed Tasks', 'Started Tasks']];
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
                var dataArr = [['Months', 'Completed Tasks','Tasks Started']];
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
                var dataArr = [['Months', 'Completed Processes','Started Processes']];
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The ODE  object is the global object used by ODE MMC's JavaScript
 * library.
 */
if (typeof BPEL == "undefined" || BPEL) {
    /**
     * The BPEL global namespace object. If BPEL is already defined, the
     * existing BPEL object will not be overwritten so that defined
     * namespaces are preserved
     */
    var BPEL = {};
}

/**
 * Returns the namespace specified and creates if it doesn't exist
 *
 * @method namespace
 * @static
 * @param {String*} arguments 1-n namespaces to create
 * @return {Object} A reference to the last namespace object created
 */

BPEL.namespace = function() {
    var args = arguments, o = null, i, j , d;
    for (i = 0; i < args.length; i = i + 1) {
        d = args[i].split(".");
        o = BPEL;

        // BPEL is implied, so it is ignored if it is included
        for (j = (d[0] == "BPEL") ? 1 : 0; j < d.length; j = j + 1) {
            o[d[j]] = o[d[j]] || {};
            o = o[d[j]];
        }
    }

    return o;
};

(function() {
    BPEL.namespace("process", "instance", "widget", "deployment", "summary");
})();

BPEL.urlEncode = function(stringToEncode) {
    function utf8Eencode(string) {
        string = string.replace(/\r\n/g, "\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {

            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if ((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }

        return utftext;
    }

    return escape(utf8Eencode(stringToEncode));

};

var serverInfoIntervalObject;

BPEL.summary.drawInstanceSummaryBarChart = function(summaryJSON, id, processId) {
    var chartContainer = jQuery("#" + id);
    chartContainer.width(450);
    chartContainer.height(325);

    var chartLegendContainer = jQuery("#" + id + "-legend");
    chartLegendContainer.width(450);

    function lableFormat(label, data) {
        return label + ": " + data.data[0][1];
    }

    var ticksArray = [];

    if (processId != null) {
        ticksArray = [
            [0, '<a href="list_instances.jsp?filter=pid=' + processId + ' status%3Dactive&order=-last-active">Active</a>'],
            [1, '<a href="list_instances.jsp?filter=pid=' + processId + ' status%3Dsuspended&order=-last-active">Suspended</a>'],
            [2, '<a href="list_instances.jsp?filter=pid=' + processId + ' status%3Dcompleted&order=-last-active">Completed</a>'],
            [3, '<a href="list_instances.jsp?filter=pid=' + processId + ' status%3Dterminated&order=-last-active">Terminated</a>'],
            [4, '<a href="list_instances.jsp?filter=pid=' + processId + ' status%3Dfailed&order=-last-active">Failed</a>'],
            [5, ""]];
    } else {
        ticksArray = [
            [0, "<a href=list_instances.jsp?filter=status%3Dactive&order=-last-active>Active</a>"],
            [1, "<a href=list_instances.jsp?filter=status%3Dsuspended&order=-last-active>Suspended</a>"],
            [2, "<a href=list_instances.jsp?filter=status%3Dcompleted&order=-last-active>Completed</a>"],
            [3, "<a href=list_instances.jsp?filter=status%3Dterminated&order=-last-active>Terminated</a>"],
            [4, "<a href=list_instances.jsp?filter=status%3Dfailed&order=-last-active>Failed</a>"],
            [5, ""]];

    }

    var options = {
        yaxis:{tickDecimals:0, min: 0},
        xaxis:{ tickLength: 0, ticks:ticksArray},
        bars: { show: true, autoScale: true, fill: 0.8, align: "center", lineWidth: 0 },
        legend:{show:true ,position:"ne",backgroundOpacity:0.4, labelFormatter:lableFormat, container: chartLegendContainer, noColumns:3}
    };

    var dataISet = [
        {label: "Active", data:[[0, summaryJSON.ACTIVE]], color: "#FF5B00"},
        {label: "Suspended", data:[[1, summaryJSON.SUSPENDED]], color: "#84002E"},
        {label: "Completed", data:[[2, summaryJSON.COMPLETED]], color: "#FFC200"},
        {label: "Terminated", data:[[3, summaryJSON.TERMINATED]], color: "#4AC0F2"},
        {label: "Failed", data:[[4, summaryJSON.FAILED]], color: "#B80028"}
    ];

    jQuery.plot(chartContainer, dataISet, options);

};

BPEL.summary.drawInstanceSummaryAgainstProcessStackedBarChart = function(summaryJSON, id,
                                                                         showProcessLink) {
    var chartContainer = jQuery("#" + id);
    chartContainer.width(450);
    chartContainer.height(280);

    var chartLegendContainer = jQuery("#" + id + "-legend");
    chartLegendContainer.width(450);

    var yAxis = [];
    var stack1Data = [];
    var stack2Data = [];
    var stack3Data = [];
    var stack4Data = [];
    var stack5Data = [];


    function lableFormat(label, data) {
        return label + ": " + data.data[0][1];
    }

    var counter = 0;
    jQuery.each(summaryJSON, function(key, value) {
        if (value.TOTAL == 0) {
            return true;
        }
        stack1Data[counter] = [value.ACTIVE, counter + 1];
        stack2Data[counter] = [value.SUSPENDED, counter + 1];
        stack3Data[counter] = [value.COMPLETED, counter + 1];
        stack4Data[counter] = [value.TERMINATED, counter + 1];
        stack5Data[counter] = [value.FAILED, counter + 1];

        processName = key.substring(key.lastIndexOf("}") + 1);
        if (processName.length > 14) {
            processName = processName.substring(0, 13) + "...";
        }

        if (showProcessLink) {
            yAxis[counter] = [counter + 1, '<a href="process_info.jsp?Pid=' + key + '" title="' +
                                        key + '">' + processName + '</a>'];
        } else {
            yAxis[counter] = [counter + 1, '<p " title="' + key + '">' + processName + '</p>'];
        }
        counter++;
    });

    var options = {
        series: {
            stack: true,
            lines: { show: false, steps: false },
            bars: { show: true, fill: 0.8, align: "center", lineWidth: 0, horizontal: true, barWidth: 0.6}
        },
        xaxis: {min: 0, tickDecimals: 0},
        yaxis: { tickLength: 0, ticks: yAxis},
        legend: {container: chartLegendContainer, noColumns:3}
    };

    var stack1Map = {label: 'Active', data: stack1Data, color: "#FF5B00"};
    var stack2Map = {label: 'Suspended', data: stack2Data, color: "#84002E"};
    var stack3Map = {label: 'Completed', data: stack3Data, color: "#FFC200"};
    var stack4Map = {label: 'Terminated', data: stack4Data, color: "#4AC0F2"};
    var stack5Map = {label: 'Failed', data: stack5Data, color: "#B80028"};

    var dataISet = [stack1Map, stack2Map, stack3Map, stack4Map, stack5Map];

    jQuery.plot(chartContainer, dataISet, options);

};

BPEL.summary.drawLongRunningInstanceStackedBarChart = function(longRunningJSON, id) {
    var xAxis = [];
    var stack1Data = [];
    var stack2Data = [];

    function yAxisFormatter(v, axis) {
        return v.toFixed(axis.tickDecimals) +"s";
    }

    function yAxisSuffixFormatter(val, yaxis) {
        if (val == 0)
            return val.toFixed(yaxis.tickDecimals);
        else if (val > (3600000 * 24))
            return (val / (3600000 * 24.0)).toFixed(yaxis.tickDecimals) + " days";
        else if (val > 3600000)
            return (val / 3600000.0).toFixed(yaxis.tickDecimals) + " h";
        else if (val > 60000)
            return (val / 60000.0).toFixed(yaxis.tickDecimals) + " min";
        else if (val > 1000)
            return (val / 1000.0).toFixed(yaxis.tickDecimals) + " s";
        else
            return val.toFixed(yaxis.tickDecimals) + " ms";
    }

    var counter = 0;
    jQuery.each(longRunningJSON, function(key, value) {
        stack1Data[counter] = [counter + 1, value.CREATED_TO_LASTACTIVE];
        stack2Data[counter] = [counter + 1, value.LASTACTIVE_TO_NOW];
        xAxis[counter] = [counter + 1, '<a href="instance_view.jsp?iid=' + key + '">' + key + '</a>'];
        counter++;
    });

    var options = {
        series: {
            stack: true,
            lines: { show: false, steps: false },
            bars: { show: true, barWidth: 0.6, fill: 0.8, align: "center", lineWidth: 0}
        },
        xaxis: { tickLength: 0, ticks: xAxis},
        yaxis: { tickFormatter: yAxisSuffixFormatter, tickDecimals: 2, min: 0},
        legend: {container: jQuery("#" + id + "-legend"), noColumns:3}
    };

    var stack1Map = {label: 'Till last active', data: stack1Data, color: "#FF5B00"};
    var stack2Map = {label: 'Last active to now', data: stack2Data, color: "#B80028"};

    var dataISet = [stack1Map, stack2Map];

    var chartContainer = jQuery("#" + id);
    chartContainer.width(450);
    chartContainer.height(280);

    jQuery.plot(chartContainer, dataISet, options);
};

BPEL.summary.drawOverallInstanceSummary = function(id){
    $.getJSON("instance_summary-ajaxprocessor.jsp?processId=all", function(json) {
        BPEL.summary.drawInstanceSummaryBarChart(json, id, null);
    });
};

BPEL.summary.drawInstanceSummary = function(processId, id){
    $.getJSON("instance_summary-ajaxprocessor.jsp?processId=" + processId, function(json) {
        BPEL.summary.drawInstanceSummaryBarChart(json, id, processId);
    });
};

BPEL.summary.drawLongRunningInstanceSummary = function(id){
    $.getJSON("get_long_running_instances-ajaxprocessor.jsp?processId=all", function(json) {
        BPEL.summary.drawLongRunningInstanceStackedBarChart(json, id);
    });
};

BPEL.summary.drawInstanceSummaryAgainstProcess = function(id, showProcessLink){
    $.getJSON("get_instance_summery_against_processes-ajaxprocessor.jsp?processId=all", function(json) {
        BPEL.summary.drawInstanceSummaryAgainstProcessStackedBarChart(json, id, showProcessLink);
    });
};

BPEL.summary.drawServerInformation = function(id){
    $.ajax({
        url: "server_info-ajaxprocessor.jsp",
        success:function(response){
            $("#"+id).html(response);
        },
        error:function(){
            if(serverInfoIntervalObject){
                clearInterval(serverInfoIntervalObject);
            }
        }
    });
}

BPEL.widget.showConfirmationDialogForInstanceDelete = function(message, handleYes, handleYesWithOptional, handleNo, closeCallback){
    /* This function always assume that your second parameter is handleYes function and third parameter is handleNo function.
     * If you are not going to provide handleYes function and want to give handleNo callback please pass null as the second
     * parameter.
     */
    var strDialog = "<div id='dialog' title='WSO2 Carbon'><div id='messagebox-confirm'><p>" +
                    message + "</p></div></div>";

    handleYes = handleYes || function(){return true};

    handleNo = handleNo || function(){return false};

    jQuery("#dcontainer").html(strDialog);

    jQuery("#dialog").dialog({
        close:function() {
            jQuery(this).dialog('destroy').remove();
            jQuery("#dcontainer").empty();
            if (closeCallback && typeof closeCallback == "function") {
                closeCallback();
            }
            return false;
        },
        buttons:{
            "Delete":function() {
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                handleYes();
            },
            "Delete With Message Exchanges": function(){
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                handleYesWithOptional();
            },
            "No":function(){
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                handleNo();
            }
        },
        height:160,
        width:500,
        minHeight:160,
        minWidth:330,
        modal:true
    });
    return false;
}

BPEL.instance.onDelete = function(){
    var instanceId = $(this).attr("id").substring(14);
    var url = $(this).attr("href");
    var deleteMexUrl = url + "&deleteMex=true";
    var message = "Do you want to delete the instance " + instanceId + "?\n Optional: If you want to delete message exchange "+
            "information please use 'Delete With Message Exchanges'.";
    sessionAwareFunction(function() {
    BPEL.widget.showConfirmationDialogForInstanceDelete(message, function(){window.location = url;}, function(){window.location = deleteMexUrl;}, function(){} );
    }, "Session timed out. Please login again.");
    return false;
}

BPEL.instance.onDeleteInstances = function(){
    var url = $("#linkDeleteInstances").attr("href");
    var deleteMexUrl = url + "&deleteMex=true";
    var message = "Do you want to delete the instances makred by current instance filter?\n Optional: If you want to delete message exchange "+
            "information please use 'Delete With Message Exchanges'.";
    sessionAwareFunction(function() {
    BPEL.widget.showConfirmationDialogForInstanceDelete(message, function(){window.location = url;}, function(){window.location = deleteMexUrl;}, function(){} );
    }, "Session timed out. Please login again.");
    return false;
}

BPEL.instance.onSuspend = function(){
    return true;
}

BPEL.instance.onResume = function(){
    return true;
}

BPEL.instance.onTerminate = function(){
    return true;
}

var rows = 1;
//add a new row to the table
BPEL.deployment.addRow = function() {
    rows++;

    //add a row to the rows collection and get a reference to the newly added row
    var newRow = document.getElementById("bpelTbl").insertRow(-1);
    newRow.id = 'file' + rows;

    var oCell = newRow.insertCell(-1);
    oCell.innerHTML = '<label>BPEL Package (.zip)<font color="red">*</font></label>';
    oCell.className = "formRow";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input type='file' name='bpelFileName' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"BPEL.deployment.deleteRow('file" + rows + "');\" />";
    oCell.className = "formRow";

    alternateTableRows('bpelTbl', 'tableEvenRow', 'tableOddRow');
}

BPEL.deployment.deleteRow = function(rowId) {
    var tableRow = document.getElementById(rowId);
    tableRow.parentNode.deleteRow(tableRow.rowIndex);
    alternateTableRows('bpelTbl', 'tableEvenRow', 'tableOddRow');
}
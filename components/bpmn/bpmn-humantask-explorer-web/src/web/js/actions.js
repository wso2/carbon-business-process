var httpUrl = location.protocol + "//" + location.host;

function completeTask(data, id){
	var url = "/bpmn-humantask/send?req=/bpmnrest/runtime/tasks/" + id;
	var variables = [];
	for(var i=0; i<data.length; i++){
		variables.push({
			"name":data[i].name,
			"value":data[i].value
		});
	}
	var body = {
		"action" : "complete",
		"variables" : variables
	};

	$.ajax({
        type: 'POST',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function(data){
        	window.location=httpUrl+"/bpmn-humantask/inbox";
        }
    });
}

function reassign(username, id){
	var url = "/bpmn-humantask/send?req=/bpmnrest/runtime/tasks/" + id;
	var body = { 
		"assignee" : username
	};

	$.ajax({
        type: 'PUT',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function(data){
        	window.location=httpUrl+"/bpmn-humantask/inbox";
        }
    });
}

function transfer(username, id){
	var url = "/bpmn-humantask/send?req=/bpmnrest/runtime/tasks/" + id;
	var body = { 
		"owner" : username
	};

	$.ajax({
        type: 'PUT',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function(data){
        	window.location=httpUrl+"/bpmn-humantask/inbox";
        }
    });
}

function startProcess(processDefId){
    var url = "/bpmn-humantask/send?req=/bpmnrest/runtime/process-instances";
    var body = { 
      "processDefinitionId": processDefId
    };

    $.ajax({
        type: 'POST',
        contentType: "application/json",
        url: httpUrl + url,
        data: JSON.stringify(body),
        success: function(data){
            window.location=httpUrl+"/bpmn-humantask/inbox";
        }
    });
}
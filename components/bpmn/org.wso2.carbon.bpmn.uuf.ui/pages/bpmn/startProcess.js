function onRequest(context) {
    var str = context.request.queryString;
    if(str.indexOf("&") == -1) {
        var processInstanceData = callOSGiService("org.wso2.carbon.bpmn.uuf.ui.service.BPMNExplorerService", "getFormData", [str.split("=")[1], context.app.config.bpsHost,context.app.config.bpsPort, context.app.config.bpsUsername, context.app.config.bpsPassword]);
        var returnProcessInstanceData = JSON.parse(processInstanceData);
        if(returnProcessInstanceData.formData != null) {
           return generateForm(returnProcessInstanceData.formData, returnProcessInstanceData.processDefId);
        }
        else {
           return returnProcessInstanceData;
        }
    }

    else {
        var processInstanceDataForm = callOSGiService("org.wso2.carbon.bpmn.uuf.ui.service.BPMNExplorerService", "createProcessInstanceWithData", [context.request.queryString, context.app.config.bpsHost,context.app.config.bpsPort, context.app.config.bpsUsername, context.app.config.bpsPassword]);
        var returnProcessInstanceDataForm = JSON.parse(processInstanceDataForm);
        return returnProcessInstanceDataForm;
    }
}

function generateForm(data, processDefId, disabled) {
    var formContent = {elements: []};
    var enumValues = {enumData: []};
    for (var i = 0; i < data.length; i++) {
        if (data[i].type == "boolean") {
            if (data[i].value) {
                var checked='';
                if (data[i].value != null && data[i].value === "true") {
                    checked = "checked" ;
                }
                if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                }
                formContent.elements.push({"isCheckboxWithValues" : true, "data" : data[i], "disabled" : disabled, "checked" : checked}); // if this is a previously declared variable
            }
            else {
                var checked='';
                if (data[i].value != null && data[i].value === "true") {
                    checked = "checked" ;
                }
                if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                }
                formContent.elements.push({"isCheckbox" : true, "data" : data[i], "disabled" : disabled, "checked" : checked});
            }
        }

        else if (data[i].type == "string") {
            if (data[i].value) {
                if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                }
                formContent.elements.push({"isTextBoxWithValues" : true, "data" : data[i], "disabled" : disabled});
            }
            else {
                 if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                 }
                formContent.elements.push({"isTextBox" : true, "data" : data[i], "disabled" : disabled});
            }
        }

        else if (data[i].type == "long" || data[i].type == "double") {
            if (data[i].value) {
                if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                }
                formContent.elements.push({"isNumberBoxWithValues" : true, "data" : data[i], "disabled" : disabled});
            }
            else {
                if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                }
                formContent.elements.push({"isNumberBox" : true, "data" : data[i], "disabled" : disabled});
            }
        }
        else if (data[i].type == "enum") {
            var selected = '';
            if (disabled == true || data[i].writable == "false") {
                disabled = true;
            }
            for (var j = 0; j < data[i].enumValues.length; j++) {
                if (data[i].value == data[i].enumValues[j].name) {
                    selected = "selected";
                }
                enumValues.enumData.push({"id" : data[i].enumValues[j].id, "name" : data[i].enumValues[j].name, "selected" : selected});
            }
            formContent.elements.push({"isSelect" : true, "data" : data[i], "disabled" : disabled, "enumData" : enumData});
        }

        else if (data[i].type == "date") {
            if (data[i].value) {
                if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                }
                formContent.elements.push({"isDatepickerWithValues" : true, "data" : data[i] });
            }
            else {
                if (disabled == true || data[i].writable == "false") {
                    disabled = true;
                }
                formContent.elements.push({"isDatepicker" : true, "data" : data[i], "disabled" : disabled});
            }

        }
    }
    formContent.elements.push({"process-def-id" : processDefId});
    return formContent;
}



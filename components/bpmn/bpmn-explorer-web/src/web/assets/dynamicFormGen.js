/*
 ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
function genSelect(data, disabled) {
    var content = "<tr>";
    content += "<td style='padding-right:15px; padding-top:10px;'>";
    content += data.name + ": ";
    content += "</td><td style='padding-top:10px'>";
    if (disabled == true || data.writable == false) {
        content += "<select name=\"" + data.id + "\" class=\"form-control\" disabled=\"true\">";
        for (var i = 0; i < data.enumValues.length; i++) {
            var selected='';
            if (data.value == data.enumValues[i].name) {
             selected = 'selected';
            }
            content += "<option value=\"" + data.enumValues[i].id + "\" "+selected +">" + data.enumValues[i].name + "</option>"
        }
    } else {
         if(data.required == true) {
             content += "<select name=\"" + data.id + "\" class=\"form-control\" required>";
             for (var i = 0; i < data.enumValues.length; i++) {
                var selected='';
                if (data.value == data.enumValues[i].name) {
                 selected = 'selected';
                }
                content += "<option value=\"" + data.enumValues[i].id + "\" "+selected +">" + data.enumValues[i].name + "</option>"
             }
         }
         else {
              content += "<select name=\"" + data.id + "\" class=\"form-control\">";
              for (var i = 0; i < data.enumValues.length; i++) {
                var selected='';
                if (data.value == data.enumValues[i].name) {
                 selected = 'selected';
                }
                content += "<option value=\"" + data.enumValues[i].id + "\" "+selected +">" + data.enumValues[i].name + "</option>"
              }
         }
    }
    content += "</select></td></tr>";

    return content;
}

function genCheckbox(data, disabled) {
    var content = "<tr>";
    content += "<td/><td style='padding-right:15px; padding-top:10px;' colspan='2'>";
    var checked='';
    if (data.value != null && data.value === "true"){
        checked = 'checked';
    }
    if (disabled == true || data.writable == "false") {
         content += "<input value=\"true\" name=\"" + data.id + "\" type=\"checkbox\" disabled=\"true\" "+checked +"/> " + data.name;
    } else {
         if(data.required == true) {
             content += "<input value=\"true\" name=\"" + data.id + "\"  type=\"checkbox\" "+checked +" required/> " + data.name;
         }
         else {
             content += "<input value=\"true\" name=\"" + data.id + "\"  type=\"checkbox\" "+checked +" /> " + data.name;
         }
    }
    content += "</td></tr>";
    return content;
}
function genCheckboxWithValues(data, disabled) {
    var checked='';
    if (data.value != null && data.value === "true") {
        checked = 'checked';
    }
    var content = "<tr>";
    content += "<td/><td style='padding-right:15px; padding-top:10px;' colspan='2'>";
    if (disabled == true || data.writable == "false") {
         content += "<input value=\"true\" name=\"" + data.id + "\" type=\"checkbox\" disabled=\"true\" "+checked +"/> " + data.name;
    } else {
        if(data.required == true) {
            content += "<input value=\"true\" name=\"" + data.id + "\"  type=\"checkbox\" "+checked +" required/> " + data.name;
        }
        else {
            content += "<input value=\"true\" name=\"" + data.id + "\"  type=\"checkbox\" "+checked +" /> " + data.name;
        }
    }
    content += "</td></tr>";
    return content;
}

function genTextBox(data,disabled) {
    var content = "<div class='small-med'>";
    content += "<div class='small-col' style='padding-right:15px; padding-top:10px;'>";
    content += data.name + ": ";
    content += "</div><div class='small-col' style='padding-top:10px'>";
    if (disabled == true || data.writable == false) {
        content += "<input name=\"" + data.id + "\" type=\"text\"  class=\"form-control\" disabled=\"true\"/>";
    } else {
        if(data.required == true) {
            content += "<input name=\"" + data.id + "\" type=\"text\"  class=\"form-control\" required/>";
        }
        else {
            content += "<input name=\"" + data.id + "\" type=\"text\"  class=\"form-control\"/>";
        }
    }
    content += "</div></div>";
    return content;
}
function genTextBoxWithValues(data,disabled) {
    var content = "<tr>";
    content += "<td style='padding-right:15px; padding-top:10px;'>";
    content += data.name + ": ";
    content += "</td><td style='padding-top:10px'>";
    if (disabled == true || data.writable == false) {
        content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"text\" class=\"form-control\" disabled=\"true\"/>";
        content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"hidden\" class=\"form-control\" />";
    } else {
        if(data.required == true) {
            content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"text\" class=\"form-control\" required/>";
        }
        else {
            content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"text\" class=\"form-control\" />";
        }
    }
    content += "</td></tr>";
    return content;
}

function genNumberBox(data, disabled) {
    var content = "<div class='small-med'>";
    content += "<div class='small-col' style='padding-right:15px; padding-top:10px;'>";
    content += data.name + ": ";
    content += "</div><div class='small-col' style='padding-top:10px'>";
    if (disabled == true || data.writable == false) {
        content += "<input name=\"" + data.id + "\"  type=\"number\"  class=\"form-control\" disabled=\"true\"/>";
    } else {
        if(data.required == true) {
           content += "<input name=\"" + data.id + "\"  type=\"number\"  class=\"form-control\" required/>";
        }
        else {
            content += "<input name=\"" + data.id + "\"  type=\"number\"  class=\"form-control\"/>";
        }
    }
    content += "</div></div>";
    return content;
}
function genNumberBoxWithValues(data, disabled) {
    var content = "<tr>";
    content += "<td style='padding-right:15px; padding-top:10px;'>";
    content += data.name + ": ";
    content += "</td><td style='padding-top:10px'>";
    if (disabled == true || data.writable == false) {
        content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"number\"  class=\"form-control\" disabled=\"true\"/>";
        content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"hidden\" class=\"form-control\" />";
    } else {
        if(data.required == true) {
            content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"number\"  class=\"form-control\" required/>";
        }
        else {
            content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"number\"  class=\"form-control\"/>";
        }
    }
    content += "</td></tr>";
    return content;
}

function genDatepicker(data, disabled) {
    var content = "<tr>";
    content += "<td style='padding-right:15px; padding-top:10px;'>";
    content += data.name + ": ";
    content += "</td><td style='padding-top:10px'>";
    if (disabled == true || data.writable == false) {
         content += "<input name=\"" + data.id + "\" type=\"date\"   class=\"form-control\" disabled=\"true\"/>";
    } else {
        if(data.required == true) {
            content += "<input name=\"" + data.id + "\" type=\"date\"   class=\"form-control\" required/>";
        }
        else {
            content += "<input name=\"" + data.id + "\" type=\"date\"   class=\"form-control\"/>";
        }
    }
    content += "</td></tr>";
    return content;
}
function genDatepickerWithValues(data, disabled) {
    var content = "<tr>";
    content += "<td style='padding-right:15px; padding-top:10px;'>";
    content += data.name + ": ";
    content += "</td><td style='padding-top:10px'>";
    if (disabled == true || data.writable == false) {
          content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"date\"   class=\"form-control\" disabled=\"true\"/>";
    } else {
        if(data.required == true) {
            content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"date\"   class=\"form-control\" required/>";
        }
        else {
            content += "<input name=\"" + data.id + "\" value=\"" + data.value + "\" type=\"date\"   class=\"form-control\"/>";
        }
    }
    content += "</td></tr>";
    return content;
}

function generateForm(data, disabled) {
    var formContent = "";
    for (var i = 0; i < data.length; i++) {
        if (data[i].type == "boolean") {
            if (data[i].value) {
                formContent += genCheckboxWithValues(data[i], disabled); // if this is a previously declared variable
            } else {
                formContent += genCheckbox(data[i], disabled);
            }

        } else if (data[i].type == "string") {
            if (data[i].value) {

                formContent += genTextBoxWithValues(data[i],disabled);
            } else {
                formContent += genTextBox(data[i],disabled);
            }
        } else if (data[i].type == "long" || data[i].type == "double") {
            if (data[i].value) {
                formContent += genNumberBoxWithValues(data[i],disabled);
            } else {
                formContent += genNumberBox(data[i],disabled);
            }

        } else if (data[i].type == "enum") {
            formContent += genSelect(data[i], disabled);
        } else if (data[i].type == "date") {
            if (data[i].value) {
                formContent += genDatepickerWithValues(data[i]);
            } else {
                formContent += genDatepicker(data[i], disabled);
            }

        }
    }
    return formContent;
}
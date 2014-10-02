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
function genSelect(data){
	var content = "<tr>";
	content += "<td style='padding-right:15px; padding-top:10px;'>";
	content += data.name + ": ";
	content += "</td><td style='padding-top:10px'>";
	content += "<select name=\"" + data.id + "\" class=\"form-control\">";
	for(var i=0; i < data.enumValues.length; i++){
		content += "<option value=\"" + data.enumValues[i].id + "\">" + data.enumValues[i].name + "</option>"
	}
	content +="</select></td></tr>";
	return content;
}

function genCheckbox(data){
	var content = "<tr>";
	content += "<td/><td style='padding-right:15px; padding-top:10px;' colspan='2'>";
	content += "<input name=\"" + data.id + "\" type=\"checkbox\"/> " + data.name;
	content +="</td></tr>";
	return content;
}

function genTextBox(data){
	var content = "<tr>";
	content += "<td style='padding-right:15px; padding-top:10px;'>";
	content += data.name + ": ";
	content += "</td><td style='padding-top:10px'>";
	content += "<input name=\"" + data.id + "\" type=\"text\"  class=\"form-control\"/>";
	content +="</td></tr>";
	return content;
}

function genNumberBox(data){
	var content = "<tr>";
	content += "<td style='padding-right:15px; padding-top:10px;'>";
	content += data.name + ": ";
	content += "</td><td style='padding-top:10px'>";
	content += "<input name=\"" + data.id + "\" type=\"number\"  class=\"form-control\"/>";
	content +="</td></tr>";
	return content;
}

function genDatepicker(data){
	var content = "<tr>";
	content += "<td style='padding-right:15px; padding-top:10px;'>";
	content += data.name + ": ";
	content += "</td><td style='padding-top:10px'>";
	content += "<input name=\"" + data.id + "\" type=\"date\"   class=\"form-control\"/>";
	content +="</td></tr>";
	return content;
}

function generateForm(data){
	var formContent = "";
	for(var i=0; i< data.length; i++){
		if(data[i].type=="boolean"){
			formContent += genCheckbox(data[i]);
		} else if(data[i].type=="string"){
			formContent += genTextBox(data[i]);
		} else if(data[i].type=="long"){
			formContent += genNumberBox(data[i]);
		} else if(data[i].type=="enum"){
			formContent += genSelect(data[i]);
		} else if(data[i].type=="date"){
			formContent += genDatepicker(data[i]);
		}
	}
	return formContent;
}
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
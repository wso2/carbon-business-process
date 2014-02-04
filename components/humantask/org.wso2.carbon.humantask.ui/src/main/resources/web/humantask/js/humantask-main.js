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
var rows = 1;
//add a new row to the table
function addRow() {
    rows++;

    //add a row to the rows collection and get a reference to the newly added row
    var newRow = document.getElementById("humantaskTbl").insertRow(-1);
    newRow.id = 'file' + rows;

    var oCell = newRow.insertCell(-1);
    oCell.innerHTML = '<label>Humantask Package (.zip)<font color="red">*</font></label>';
    oCell.className = "formRow";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input type='file' name='humantaskFileName' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file" + rows + "');\" />";
    oCell.className = "formRow";

    alternateTableRows('humantaskTbl', 'tableEvenRow', 'tableOddRow');
}

function deleteRow(rowId) {
    var tableRow = document.getElementById(rowId);
    tableRow.parentNode.deleteRow(tableRow.rowIndex);
    alternateTableRows('humantaskTbl', 'tableEvenRow', 'tableOddRow');
}
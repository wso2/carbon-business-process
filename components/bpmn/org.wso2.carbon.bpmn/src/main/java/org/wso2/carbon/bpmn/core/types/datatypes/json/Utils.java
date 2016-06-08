/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.bpmn.core.types.datatypes.json;

import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.bpmn.core.types.datatypes.BPMNDataTypeException;

import java.util.Map;

public class Utils {

    public static JSONObject jsObjectToJsonObject (ScriptObject scriptObject) throws BPMNDataTypeException {
        JSONObject jsonObject = new JSONObject();

        for (Map.Entry entry : scriptObject.entrySet()) {
            Object entryValue = entry.getValue();
            if (entryValue instanceof String || entryValue instanceof Integer || entryValue instanceof Byte ||
                    entryValue instanceof Character || entryValue instanceof Short || entryValue instanceof Long ||
                    entryValue instanceof Float || entryValue instanceof Double || entryValue instanceof Boolean) {

                jsonObject.put((String) entry.getKey(), entryValue);

            } else if (entryValue instanceof ScriptObject &&
                    ((ScriptObject)entryValue).getClass().getName().startsWith("jdk.nashorn.internal.scripts.JO")) {

                jsonObject.put((String)entry.getKey(), jsObjectToJsonObject((ScriptObject)entryValue));

            } else if (entryValue instanceof NativeArray) {

                jsonObject.put((String)entry.getKey(), jsObjArrayToJsonArray((NativeArray) entryValue));

            } else {
                //this means the scriptObject is not representing json object or the json object format is wrong
                //so throw an exception
                throw new BPMNDataTypeException("scriptObject is not representing json object or the json object format is wrong");

            }
        }
        return jsonObject;
    }


    public static JSONArray jsObjArrayToJsonArray (NativeArray objectArray) throws BPMNDataTypeException {
        JSONArray jsonArray = new JSONArray();

        for (Object obj : objectArray.asObjectArray()) {
            if (obj instanceof ScriptObject &&
                    obj.getClass().getName().startsWith("jdk.nashorn.internal.scripts.JO")) {

                jsonArray.put(jsObjectToJsonObject((ScriptObject)obj));

            } else if (obj instanceof NativeArray) {

                jsonArray.put(jsObjArrayToJsonArray((NativeArray)obj));

            } else {
                //this means the scriptObject is not representing json object or the json object format is wrong
                //so throw an exception
                throw new BPMNDataTypeException("scriptObject is not representing json object or the json object format is wrong");
            }
        }

        return jsonArray;
    }



}

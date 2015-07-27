/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.analytics.publisher.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateConverter uses to convert given string date to Date format
 */
public class DateConverter {
    private static final Log log = LogFactory.getLog(DateConverter.class);

    public static Date convertStringToDate(String dateString) {
        String[] dateStringArray = dateString.split(" ");
        String modifiedDateString =
                dateStringArray[0] + ", " + dateStringArray[1] + " " + dateStringArray[2] + " " +
                        dateStringArray[5] + " " + dateStringArray[3];
        DateFormat df = new SimpleDateFormat("E, MMM dd yyyy HH:mm:ss");
        try {
            return df.parse(modifiedDateString);
        } catch (ParseException e) {
            String errMsg = "Date converter parse error.";
            log.error(errMsg, e);
            return null;
        }
    }
}

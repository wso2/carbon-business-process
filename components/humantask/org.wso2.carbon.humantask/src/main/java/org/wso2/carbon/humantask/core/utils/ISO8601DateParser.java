/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * ISO 8601 date parsing utility. Most date parsing libraries only apply one pattern but
 * can't be used to parse ISO 8601 that are a set a pattern (mostly elements can be omitted
 * like time of just seconds).
 */
public final class ISO8601DateParser {
    private ISO8601DateParser() {
    }

    public static Date parse(String date) throws java.text.ParseException {
        String pattern;
        StringBuilder buffer = new StringBuilder(date.trim());
        boolean timezoned = false;

        switch (buffer.length()) {
            case 4:
                // Year: yyyy (eg 1997)
                pattern = "yyyy";
                break;
            case 7:
                // Year and month: yyyy-MM (eg 1997-07)
                pattern = "yyyy-MM";
                break;
            case 10:
                // Complete date: yyyy-MM-dd (eg 1997-07-16)
                pattern = "yyyy-MM-dd";
                break;
            default:
                // Complete date plus hours and minutes: yyyy-MM-ddTHH:mmTZD (eg 1997-07-16T19:20+01:00)
                // Complete date plus hours, minutes and seconds: yyyy-MM-ddTHH:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
                // Complete date plus hours, minutes, seconds and a decimal fraction of a second: yyyy-MM-ddTHH:mm:ss.STZD (eg 1997-07-16T19:20:30.45+01:00)
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";

                if (buffer.length() == 16) {
                    // add seconds
                    buffer.append(":00");
                }
                if (buffer.length() > 16 && buffer.charAt(16) != ':') {
                    // insert seconds
                    buffer.insert(16, ":00");
                }
                if (buffer.length() == 19) {
                    // add milliseconds
                    buffer.append(".000");
                }
                if (buffer.length() > 19 && buffer.charAt(19) != '.') {
                    // insert milliseconds
                    buffer.insert(19, ".000");
                }
                if (buffer.charAt(19) == '.' && (buffer.length() < 22 ||
                        (buffer.lastIndexOf("-") < 23 && buffer.lastIndexOf("-") > 19) ||
                        (buffer.lastIndexOf("+") < 23 && buffer.lastIndexOf("+") > 0))) {
                    buffer.insert(20, "0");
                }
                if (buffer.charAt(19) == '.' && (buffer.length() < 22 ||
                        (buffer.lastIndexOf("-") < 23 && buffer.lastIndexOf("-") > 19) ||
                        (buffer.lastIndexOf("+") < 23 && buffer.lastIndexOf("+") > 0))) {
                    buffer.insert(20, "0");
                }
                if (buffer.length() > 23) {
                    // append timezone
                    pattern = pattern + "Z";
                    timezoned = true;
                }
                if (buffer.length() == 24 && buffer.charAt(23) == 'Z') {
                    // replace 'Z' with '+0000'
                    buffer.replace(23, 24, "+0000");
                }
                if (buffer.length() == 29 && buffer.charAt(26) == ':') {
                    // delete '.' from 'HH:mm'
                    buffer.deleteCharAt(26);
                }
        }

        // always set time zone on formatter
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        if (timezoned) {
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        return format.parse(buffer.toString());
    }

    public static Calendar parseCal(String date) throws java.text.ParseException {
        Date d = parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal;
    }

    public static String format(Date date) {
        TimeZone timeZone = TimeZone.getDefault();
        boolean utc = TimeZone.getTimeZone("UTC").equals(timeZone) ||
                TimeZone.getTimeZone("GMT").equals(timeZone);

        String pattern = utc ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'HH:mm:ssZ";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(timeZone);

        StringBuilder buffer = new StringBuilder(format.format(date));
        if (!utc) {
            buffer.insert(buffer.length() - 2, ':');
        }

        return buffer.toString();
    }

}
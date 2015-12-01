package org.wso2.carbon.bpmn.rest.common;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.Map;

public class RequestUtil {

    private static final FastDateFormat shortDateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
    private static final FastDateFormat longDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssz");

    public static boolean getBoolean(Map<String, String> requestParams, String name, boolean defaultValue) {
        boolean value = defaultValue;
        if (requestParams.get(name) != null) {
            value = Boolean.valueOf(requestParams.get(name));
        }
        return value;
    }

    public static int getInteger(Map<String, String> requestParams, String name, int defaultValue) {
        int value = defaultValue;
        if (requestParams.get(name) != null) {
            value = Integer.valueOf(requestParams.get(name));
        }
        return value;
    }

    public static Date getDate(Map<String, String> requestParams, String name) {
        Date value = null;
        if (requestParams.get(name) != null) {

            String input = requestParams.get(name).trim();

            //this is zero time so we need to add that TZ indicator for
            if (input.endsWith("Z")) {
                input = input.substring(0, input.length() - 1) + "GMT-00:00";
            } else {
                int inset = 6;

                String s0 = input.substring(0, input.length() - inset);
                String s1 = input.substring(input.length() - inset, input.length());

                input = s0 + "GMT" + s1;
            }

            try {
                value = longDateFormat.parse(input);
            } catch(Exception e) {
                throw new ActivitiIllegalArgumentException("Failed to parse date " + input);
            }
        }
        return value;
    }

    public static String dateToString(Date date) {
        String dateString = null;
        if(date != null) {
            dateString = longDateFormat.format(date);
        }

        return dateString;
    }

    public static Integer parseToInteger(String integer) {
        Integer parsedInteger = null;
        try {
            parsedInteger = Integer.parseInt(integer);
        } catch(Exception e) {}
        return parsedInteger;
    }

    public static Date parseToDate(String date) {
        Date parsedDate = null;
        try {
            parsedDate = shortDateFormat.parse(date);
        } catch(Exception e) {}
        return parsedDate;
    }
}

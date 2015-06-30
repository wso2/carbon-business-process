package org.wso2.carbon.bpmn.analytics.publisher.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by isuruwi on 6/26/15.
 */
public class DateConverter {
    public Date convertStringToDate(String dateString){
        String[] dateStringArray = dateString.split(" ");
        String modifiedDateString = dateStringArray[0]+", "+dateStringArray[1]+" "+dateStringArray[2]+" "+dateStringArray[5]+" "+dateStringArray[3];
        DateFormat df = new SimpleDateFormat("E, MMM dd yyyy HH:mm:ss");
        Date date = null;
        try {
            return df.parse(modifiedDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

package org.wso2.carbon.bpmn.rest.common;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DateToStringSerializer extends JsonSerializer<Date> {

    protected DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();


    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        if (date != null) {
            jsonGenerator.writeString(new DateTime(date).toString(isoFormatter));
        } else {
            jsonGenerator.writeNull();
        }
    }
}

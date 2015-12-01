package org.wso2.carbon.bpmn.rest.model.form;

import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "FormPropertyResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class FormPropertyResponse {

    private String id;
    private String name;
    private String value;
    private String type;

    @XmlElementWrapper(name = "FormPropertyEnumDataHolders")
    @XmlElement(name = "FormPropertyEnumDataHolder", type = FormPropertyEnumDataHolder.class)
    private List<FormPropertyEnumDataHolder> enumValues;

    private Boolean required;
    private Boolean readable;
    private Boolean writable;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FormPropertyEnumDataHolder> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<FormPropertyEnumDataHolder> enumValues) {
        this.enumValues = enumValues;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getReadable() {
        return readable;
    }

    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

    public Boolean getWritable() {
        return writable;
    }

    public void setWritable(Boolean writable) {
        this.writable = writable;
    }
}

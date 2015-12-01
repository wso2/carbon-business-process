package org.wso2.carbon.bpmn.rest.model.form;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "FormPropertyResponseCollection")
@XmlAccessorType(XmlAccessType.FIELD)
public class FormPropertyResponseCollection {

    @XmlElementWrapper(name = "FormPropertyResponses")
    @XmlElement(name = "FormPropertyResponse", type = FormPropertyResponse.class)
    private List<FormPropertyResponse> data;

    public List<FormPropertyResponse> getData() {
        return data;
    }

    public void setData(List<FormPropertyResponse> data) {
        this.data = data;
    }
}

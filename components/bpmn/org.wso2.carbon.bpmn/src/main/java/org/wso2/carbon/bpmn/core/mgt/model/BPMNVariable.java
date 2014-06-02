package org.wso2.carbon.bpmn.core.mgt.model;

public class BPMNVariable {

    private String name;
    private String value;

    public BPMNVariable(String name, String value) {
        this.name = name;
        this.value = value;
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
}

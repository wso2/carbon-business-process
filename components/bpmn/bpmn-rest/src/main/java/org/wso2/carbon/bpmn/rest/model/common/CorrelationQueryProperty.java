package org.wso2.carbon.bpmn.rest.model.common;

import org.activiti.engine.query.QueryProperty;

import java.util.HashMap;
import java.util.Map;

public class CorrelationQueryProperty  implements QueryProperty {

    private static final long serialVersionUID = 1L;
    private static final Map<String, CorrelationQueryProperty> properties = new HashMap();
    public static final CorrelationQueryProperty PROCESS_INSTANCE_ID_ = new CorrelationQueryProperty("RES.ID_");

    private String name;

    public CorrelationQueryProperty(String name) {
        this.name = name;
        properties.put(name, this);
    }

    public String getName() {
        return this.name;
    }

    public static CorrelationQueryProperty findByName(String propertyName) {
        return (CorrelationQueryProperty)properties.get(propertyName);
    }
}

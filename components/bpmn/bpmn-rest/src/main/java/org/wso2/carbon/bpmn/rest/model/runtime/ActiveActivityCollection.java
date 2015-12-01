package org.wso2.carbon.bpmn.rest.model.runtime;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "ActiveActivityCollection")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActiveActivityCollection {

    @XmlElementWrapper(name = "ActiveActivities")
    @XmlElement(name = "ActiveActivity")
    private List<String> activeActivityList;

    public List<String> getActiveActivityList() {
        return activeActivityList;
    }

    public void setActiveActivityList(List<String> activeActivityList) {
        this.activeActivityList = activeActivityList;
    }
}

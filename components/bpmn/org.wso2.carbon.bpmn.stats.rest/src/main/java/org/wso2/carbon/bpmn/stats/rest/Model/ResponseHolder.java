package org.wso2.carbon.bpmn.stats.rest.Model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by natasha on 12/8/15.
 */
@XmlRootElement(name = "Response")
public class ResponseHolder {

    private List<BPMNProcessInstance> deployedProcesses ;

    public List<BPMNProcessInstance> getDeployedProcesses() {
        return deployedProcesses;
    }

    public void setDeployedProcesses(List<BPMNProcessInstance> deployedProcesses) {
        this.deployedProcesses = deployedProcesses;
    }
}

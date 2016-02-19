package org.wso2.carbon.bpmn.rest.service.analytics;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.analytics.ProcessInstanceVariables;
import org.wso2.carbon.bpmn.rest.model.history.HistoricIdentityLinkResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricIdentityLinkResponseCollection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.jar.Attributes;


@Path("/process-instance-variables")
public class ProcessInstanceVariableSevice {
    //ProcessInstanceVariables processInstanceVariables;
    HashMap<String,Object> processInstanceVariables=new HashMap<String,Object>();

    @Path("/{processInstanceId}")
    public HashMap<String,Object> getProcessInstanceVariables(@PathParam("processInstanceId")  String processInstanceId){


        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricProcessInstanceQuery kk= historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId);
        HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);
        final Map<String, Object> processVariables = instance.getProcessVariables();
        System.out.println("Test SSSS:"+processVariables);

       /* List<HistoricIdentityLink> identityLinks =historyService.getHistoricIdentityLinksForProcessInstance(processInstanceId);

        if (identityLinks != null) {
            List<HistoricIdentityLinkResponse> historicIdentityLinkResponses = new RestResponseFactory()
                    .createHistoricIdentityLinkResponseList(identityLinks, uriInfo.getBaseUri
                            ().toString());
            HistoricIdentityLinkResponseCollection historicIdentityLinkResponseCollection = new
                    HistoricIdentityLinkResponseCollection();
            historicIdentityLinkResponseCollection.setHistoricIdentityLinkResponses(historicIdentityLinkResponses);
            //return //Response.ok().entity(historicIdentityLinkResponseCollection).build();
        }*/


        return processInstanceVariables;
    }

    protected HistoricProcessInstance getHistoricProcessInstanceFromRequest(String processInstanceId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", HistoricProcessInstance.class);
        }
        return processInstance;
    }
}

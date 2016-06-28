function onRequest(context) {
    var str = context.request.queryString;
    if(str == null) {
      str = "";
    }

    var processDetails = callOSGiService("org.wso2.carbon.bpmn.uuf.ui.service.BPMNExplorerService", "getProcessDetails", [str, context.app.config.bpsHost,context.app.config.bpsPort, context.app.config.bpsTenantId]);
    var returnProcessDetails = JSON.parse(processDetails);
    return {"data":returnProcessDetails};
}

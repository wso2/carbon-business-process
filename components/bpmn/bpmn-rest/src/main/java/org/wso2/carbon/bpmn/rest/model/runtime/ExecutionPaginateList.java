package org.wso2.carbon.bpmn.rest.model.runtime;

import org.wso2.carbon.bpmn.rest.common.AbstractPaginateList;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;

import javax.ws.rs.core.UriInfo;
import java.util.List;

public class ExecutionPaginateList  extends AbstractPaginateList {
    public ExecutionPaginateList(RestResponseFactory restResponseFactory, UriInfo uriInfo) {
        super(restResponseFactory, uriInfo);
    }

    @Override
    protected List processList(List list) {
        return restResponseFactory.createExecutionResponseList(list, uriInfo.getBaseUri().toString());
    }
}

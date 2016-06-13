/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.service.runtime;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.wso2.carbon.bpmn.extensions.substitution.UserSubstitutionOperations;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.runtime.SubstitutionRequest;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * REST endpoints related to automatic task reassignment and substitution.
 */
@Path("/substitute")
public class UserSubstitutionService {

    private static final Log log = LogFactory.getLog(UserSubstitutionService.class);
    @Context UriInfo uriInfo;

    /**
     * Add new addSubstituteInfo record.
     * Following request body parameters are required,
     *  assignee : optional, logged in user is used if not provided
     *  addSubstituteInfo : required
     *  startTime : optional, current timestamp if not provided, the timestamp the substitution should start in ISO format
     *  endTime : optional, considered as forever if not provided, the timestamp the substitution should end in ISO format
     *  enabled : optional, false if not provided
     * @param request
     * @return
     */
    @POST
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response substitute( SubstitutionRequest request) {

        try {
            String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            UserRealm userRealm = BPMNOSGIService.getUserRealm();
            String assignee = request.getAssignee();

            //validate the assignee
            if (assignee != null && loggedInUser != assignee) { //setting another users's addSubstituteInfo
                boolean isAuthorized = userRealm.getAuthorizationManager().isUserAuthorized(loggedInUser,"/permission/admin/manage/bpmn/addSubstituteInfo", "add");
                if (!isAuthorized || !userRealm.getUserStoreManager()
                        .isExistingUser(assignee)) {
                    throw new ActivitiIllegalArgumentException("Unauthorized action or invalid argument for assignee");
                }
            } else { //assignee is the logged in user
                assignee = loggedInUser;
            }

            //validate addSubstituteInfo
            String substitute = request.getSubstitute();
            if (substitute == null) {
                throw new ActivitiIllegalArgumentException("The substitute must be specified");
            } else if (assignee == substitute) {
                throw new ActivitiIllegalArgumentException("Substitute and assignee should be different users");
            } else if (!userRealm.getUserStoreManager()
                    .isExistingUser(substitute)) {
                throw new ActivitiIllegalArgumentException("Cannot substitute a non existing user:" + substitute);
            }

            Date endTime = null;
            Date startTime = new Date();
            DateTime requestStartTime = null;
            if (request.getStartTime() != null) {
                requestStartTime = new DateTime(request.getStartTime());
                startTime = new Date(requestStartTime.getMillis());
            }

            if (request.getEndTime() != null) {
                DateTime requestEndTime = new DateTime(request.getEndTime());
                if (requestEndTime.isBeforeNow()) {
                    throw new ActivitiIllegalArgumentException("End time should be in future");
                }
                if (request.getStartTime() != null) {

                    if (requestEndTime.isBefore(requestStartTime.getMillis())) {
                        throw new ActivitiIllegalArgumentException("Invalid Start and End time combination");
                    }
                }
                endTime = new Date(requestEndTime.getMillis());
            }

            //at this point, substitution is enabled by default
            UserSubstitutionOperations.handleNewSubstituteAddition(assignee, substitute, startTime, endTime, true, request.getTaskList());

            return Response.created(new URI("substitute/" + assignee)).build();

        } catch (UserStoreException e) {
            throw new ActivitiException("Error accessing User Store", e);
        } catch (URISyntaxException e) {
            throw new ActivitiException("Response location URI creation header", e);
        } catch (ActivitiIllegalArgumentException e) {
            throw new ActivitiIllegalArgumentException(e.getMessage());
        }
    }

}

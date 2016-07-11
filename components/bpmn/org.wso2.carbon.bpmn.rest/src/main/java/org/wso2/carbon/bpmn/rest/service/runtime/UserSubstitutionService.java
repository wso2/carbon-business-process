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
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.mgt.model.PaginatedSubstitutesDataModel;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;
import org.wso2.carbon.bpmn.people.substitution.SubstitutionDataHolder;
import org.wso2.carbon.bpmn.people.substitution.SubstitutionQueryProperties;
import org.wso2.carbon.bpmn.people.substitution.UserSubstitutionUtils;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNForbiddenException;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.runtime.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * REST endpoints related to automatic task reassignment and substitution.
 */
@Path("/substitutes")
public class UserSubstitutionService {

    private static final Log log = LogFactory.getLog(UserSubstitutionService.class);
    @Context UriInfo uriInfo;

    private static final String ASCENDING = "asc";
    private static final String DESCENDING = "desc";
    private static final String ADD_PERMISSION = "add";
    public static final String GET_PERMISSION = "get";
    private static final String DEFAULT_PAGINATION_START = "0";
    private static final String DEFAULT_PAGINATION_SIZE = "10";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final boolean subsFeatureEnabled = SubstitutionDataHolder.getInstance().isSubstitutionFeatureEnabled();

    protected static final HashMap<String, String> propertiesMap = new HashMap<>();

    static {
        propertiesMap.put("assignee", SubstitutionQueryProperties.USER);
        propertiesMap.put("substitute", SubstitutionQueryProperties.SUBSTITUTE);
        propertiesMap.put("enabled", SubstitutionQueryProperties.ENABLED);
        propertiesMap.put("start", SubstitutionQueryProperties.SUBSTITUTION_START);
        propertiesMap.put("end", SubstitutionQueryProperties.SUBSTITUTION_END);
        propertiesMap.put("start", SubstitutionQueryProperties.START);
        propertiesMap.put("size", SubstitutionQueryProperties.SIZE);
        propertiesMap.put("order", SubstitutionQueryProperties.ORDER);
        propertiesMap.put("sort", SubstitutionQueryProperties.SORT);
    }

    /**
     * Add new addSubstituteInfo record.
     * Following request body parameters are required,
     *  assignee : optional, logged in user is used if not provided
     *  substitute : required
     *  startTime : optional, current timestamp if not provided, the timestamp the substitution should start in ISO format
     *  endTime : optional, considered as forever if not provided, the timestamp the substitution should end in ISO format
     * @param request
     * @return 201 created response with the resource location. 405 if substitution disabled
     */
    @POST
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response substitute(SubstitutionRequest request) {

        try {
            if (!subsFeatureEnabled) {
                return Response.status(405).build();
            }

            String assignee = getRequestedAssignee(request.getAssignee());

            String substitute = validateAndGetSubstitute(request.getSubstitute(), assignee);

            Date endTime = null;
            Date startTime = new Date();
            DateTime requestStartTime = null;
            if (request.getStartTime() != null) {
                requestStartTime = new DateTime(request.getStartTime());
                startTime = new Date(requestStartTime.getMillis());
            }

            if (request.getEndTime() != null) {
                endTime = validateEndTime(request.getEndTime(), requestStartTime);
            }

            if (!UserSubstitutionUtils.validateTasksList(request.getTaskList(), assignee)) {
                throw new ActivitiIllegalArgumentException("Invalid task list provided, for substitution.");
            }

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            //at this point, substitution is enabled by default
            UserSubstitutionUtils
                    .handleNewSubstituteAddition(assignee, substitute, startTime, endTime, true, request.getTaskList(),
                            tenantId);

            return Response.created(new URI("substitutes/" + assignee)).build();

        } catch (UserStoreException e) {
            throw new ActivitiException("Error accessing User Store", e);
        } catch (URISyntaxException e) {
            throw new ActivitiException("Response location URI creation header", e);
        } catch (ActivitiIllegalArgumentException e) {
            throw new ActivitiIllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Update the substitute info of the given user in the request path. Use the same format used in POST method.
     * @param user - user that need to update his substitute info
     * @param request - substitute info that need to be updated
     * @return
     * @throws URISyntaxException
     */
    @PUT
    @Path("/{user}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response updateSubstituteInfo(@PathParam("user") String user, SubstitutionRequest request) throws
            URISyntaxException {
        try {
            if (!subsFeatureEnabled) {
                return Response.status(405).build();
            }
            request.setAssignee(user);
            String assignee = getRequestedAssignee(user);

            String substitute = validateAndGetSubstitute(request.getSubstitute(), assignee);

            Date endTime = null;
            Date startTime = new Date();
            DateTime requestStartTime = null;
            if (request.getStartTime() != null) {
                requestStartTime = new DateTime(request.getStartTime());
                startTime = new Date(requestStartTime.getMillis());
            }

            if (request.getEndTime() != null) {
                endTime = validateEndTime(request.getEndTime(), requestStartTime);
            }

            if (!UserSubstitutionUtils.validateTasksList(request.getTaskList(), assignee)) {
                throw new ActivitiIllegalArgumentException("Invalid task list provided, for substitution.");
            }


            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserSubstitutionUtils
                    .handleUpdateSubstitute(assignee, substitute, startTime, endTime, true, request.getTaskList(), tenantId);
            return Response.ok().build();

        } catch (UserStoreException e) {
            throw new ActivitiException("Error accessing User Store", e);
        }
    }

    /**
     * Change the substitute of the {user}. Use following request body format.
     * {"substitute":"user"}
     * @param user
     * @param request
     * @return
     * @throws URISyntaxException
     */
    @PUT
    @Path("/{user}/substitute")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response changeSubstitute(@PathParam("user") String user, SubstituteRequest request)
            throws URISyntaxException {
        try {
            if (!subsFeatureEnabled) {
                return Response.status(405).build();
            }
            String assignee = getRequestedAssignee(user);
            String substitute = validateAndGetSubstitute(request.getSubstitute(), assignee);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserSubstitutionUtils.handleChangeSubstitute(assignee, substitute, tenantId);
        } catch (UserStoreException e) {
            throw new ActivitiException("Error accessing User Store", e);
        }
        return Response.ok().build();
    }

    /**
     * Return the substitute info for the given user in path parameter
     * @param user
     * @return SubstituteInfoResponse
     * @throws URISyntaxException
     */
    @GET
    @Path("/{user}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getSubstitute(@PathParam("user") String user) throws UserStoreException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (!loggedInUser.equals(user) && !hasSubstitutionViewPermission()) {
            throw new BPMNForbiddenException("Not allowed to view others substitution details. No sufficient permission");
        }
        SubstitutesDataModel model = UserSubstitutionUtils.getSubstituteOfUser(user, tenantId);
        if (model != null) {
            SubstituteInfoResponse response = new SubstituteInfoResponse();
            response.setSubstitute(model.getSubstitute());
            response.setAssignee(model.getUser());
            response.setEnabled(model.isEnabled());
            response.setStartTime(model.getSubstitutionStart());
            response.setEndTime(model.getSubstitutionEnd());
            return Response.ok(response).build();
        } else {
            return Response.status(404).build();
        }

    }

    /**
     * Check the logged in user has permission for viewing other substitutions.
     * @return true if the permission sufficient
     * @throws UserStoreException
     */
    private boolean hasSubstitutionViewPermission() throws UserStoreException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        UserRealm userRealm = BPMNOSGIService.getUserRealm();
        return userRealm.getAuthorizationManager()
                .isUserAuthorized(loggedInUser, BPMNConstants.SUBSTITUTION_PERMISSION_PATH, ADD_PERMISSION);
    }

    /**
     * Query the substitution records based on substitute, assignee and enabled or disabled.
     * Pagination parameters, start, size, sort, order are allowed.
     * @return paginated list of substitution info records
     */
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response querySubstitutes() {

        Map<String, String> queryMap = new HashedMap();

        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            String value = uriInfo.getQueryParameters().getFirst(entry.getKey());

            if (value != null) {
                queryMap.put(entry.getValue(), value);
            }
        }

        //validate the parameters
        try {
            String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            if (queryMap.get(SubstitutionQueryProperties.USER) != null) {
                if (!queryMap.get(SubstitutionQueryProperties.USER).equals(loggedInUser) && !hasSubstitutionViewPermission()) {
                    throw new BPMNForbiddenException("Not allowed to view others substitution details. No sufficient permission");
                }
            } else if (!hasSubstitutionViewPermission()) {
                throw new BPMNForbiddenException("Not allowed to view others substitution details. No sufficient permission");
            }

            if (queryMap.get(SubstitutionQueryProperties.SUBSTITUTE) != null) {
                String substitute = queryMap.get(SubstitutionQueryProperties.SUBSTITUTE);
                if (!substitute.equals(loggedInUser) && !hasSubstitutionViewPermission()) {
                    throw new BPMNForbiddenException("Not allowed to view others substitution details. No sufficient permission");
                }
            } else if (!hasSubstitutionViewPermission()) {
                throw new BPMNForbiddenException("Not allowed to view others substitution details. No sufficient permission");
            }
        } catch (UserStoreException e) {
            throw new ActivitiException("Error accessing User Store for input validations", e);
        }

        //validate pagination parameters
        validatePaginationParams(queryMap);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<SubstitutesDataModel> dataModelList = UserSubstitutionUtils.querySubstitutions(queryMap, tenantId);
        int totalResultCount = UserSubstitutionUtils.getQueryResultCount(queryMap, tenantId);
        SubstituteInfoCollectionResponse collectionResponse = new SubstituteInfoCollectionResponse();
        collectionResponse.setTotal(totalResultCount);
        List<SubstituteInfoResponse> responseList = new ArrayList<>();

        for (SubstitutesDataModel subsData : dataModelList) {
            SubstituteInfoResponse response = new SubstituteInfoResponse();
            response.setEnabled(subsData.isEnabled());
            response.setEndTime(subsData.getSubstitutionEnd());
            response.setStartTime(subsData.getSubstitutionStart());
            response.setSubstitute(subsData.getSubstitute());
            response.setAssignee(subsData.getUser());
            responseList.add(response);
        }

        collectionResponse.setSubstituteInfoList(responseList);
        collectionResponse.setSize(responseList.size());
        String sortType = getSortType(queryMap.get(SubstitutionQueryProperties.SORT));
        collectionResponse.setSort(sortType);
        collectionResponse.setStart(Integer.parseInt(queryMap.get(SubstitutionQueryProperties.START)));
        collectionResponse.setOrder(queryMap.get(SubstitutionQueryProperties.ORDER));

        return Response.ok(collectionResponse).build();

    }

    /**
     * Change the status of a substitution record.
     *
     * @param user : assignee of the substitution
     * @param request : format {"action" : "true/false"}
     * @return HTTP 200 upon success
     * @throws UserStoreException
     */
    @POST
    @Path("/{user}/disable")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response disableSubstitution(@PathParam("user") String user, RestActionRequest request)
            throws UserStoreException {
        String assignee = getRequestedAssignee(user);
        String action = request.getAction();
        if (action != null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (action.trim().equalsIgnoreCase(TRUE)) {
                UserSubstitutionUtils.disableSubstitution(true, assignee, tenantId);
            } else if (action.trim().equalsIgnoreCase(FALSE)) {
                UserSubstitutionUtils.disableSubstitution(false, assignee, tenantId);
            } else {
                throw new ActivitiIllegalArgumentException("Invalid disable action : " + action + " specified");
            }
        } else {
            throw new ActivitiIllegalArgumentException("No disable action specified");
        }

        return Response.ok().build();
    }



    private String getSortType(String sortType) {
        switch (sortType) {
        case (SubstitutionQueryProperties.SUBSTITUTION_START):
            return "startTime";
        case (SubstitutionQueryProperties.SUBSTITUTION_END):
            return "endTime";
        case (SubstitutionQueryProperties.SUBSTITUTE):
            return "substitute";
        case (SubstitutionQueryProperties.USER):
            return "assignee";
        }

        return "";
    }

    private void validatePaginationParams(Map<String, String> queryMap) {
        String start = queryMap.get(SubstitutionQueryProperties.START);
        String size = queryMap.get(SubstitutionQueryProperties.SIZE);
        String sort = queryMap.get(SubstitutionQueryProperties.SORT);
        String order = queryMap.get(SubstitutionQueryProperties.ORDER);

        if (start != null) {
            if (Integer.parseInt(start) < 0) {
                throw new ActivitiIllegalArgumentException("Invalid argument for parameter 'start'");
            }
        } else {
            start = DEFAULT_PAGINATION_START;
        }
        queryMap.put(SubstitutionQueryProperties.START, start);

        if (size != null) {
            if (Integer.valueOf(size) <= 0) {
                throw new ActivitiIllegalArgumentException("Invalid argument for parameter 'size'");
            }
        } else {
            size = DEFAULT_PAGINATION_SIZE;
        }
        queryMap.put(SubstitutionQueryProperties.SIZE, size);

        if (sort != null) {
            switch (sort) {
            case ("startTime"):
                sort = SubstitutionQueryProperties.SUBSTITUTION_START;
                break;
            case ("endTime"):
                sort = SubstitutionQueryProperties.SUBSTITUTION_END;
                break;
            case ("substitute"):
                sort = SubstitutionQueryProperties.SUBSTITUTE;
                break;
            case ("assignee"):
                sort = SubstitutionQueryProperties.USER;
                break;
            default:
                throw new ActivitiIllegalArgumentException("Invalid argument for parameter 'sort'");
            }
        } else {
            sort = SubstitutionQueryProperties.SUBSTITUTION_START;
        }
        queryMap.put(SubstitutionQueryProperties.SORT, sort);

        if (order != null) {
            if (!ASCENDING.equalsIgnoreCase(order) && !DESCENDING.equalsIgnoreCase(order)) {
                throw new ActivitiIllegalArgumentException("Invalid argument for parameter 'order'");
            }
        } else {
            order = "asc";
        }
        queryMap.put(SubstitutionQueryProperties.ORDER, order);

    }

    /**
     * Return end time if valid
     * @param endTime - should be non null
     * @return Date
     */
    private Date validateEndTime(String endTime, DateTime startTime) {

        DateTime requestEndTime = new DateTime(endTime);
        if (requestEndTime.isBeforeNow()) {
            throw new ActivitiIllegalArgumentException("End time should be in future");
        }
        if (startTime != null) {
            if (requestEndTime.isBefore(startTime.getMillis())) {
                throw new ActivitiIllegalArgumentException("Invalid Start and End time combination");
            }
        }
        return new Date(requestEndTime.getMillis());
    }

    /**
     * Validate and get the assignee for a substitute request
     * @param user
     * @return actual assignee of the substitute request
     * @throws UserStoreException
     */
    private String getRequestedAssignee(final String user) throws UserStoreException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        UserRealm userRealm = BPMNOSGIService.getUserRealm();
        String assignee = user;

        //validate the assignee
        if (assignee != null && !assignee.trim().isEmpty() && !assignee.equals(loggedInUser)) { //setting another users
            boolean isAuthorized = userRealm.getAuthorizationManager()
                    .isUserAuthorized(loggedInUser, BPMNConstants.SUBSTITUTION_PERMISSION_PATH, ADD_PERMISSION);
            if (!isAuthorized) {
                throw new BPMNForbiddenException("Action requires BPMN substitution permission");
            }
            if (!userRealm.getUserStoreManager().isExistingUser(assignee)) {
                throw new ActivitiIllegalArgumentException("Non existing user for argument assignee : " + assignee);
            }
        } else { //assignee is the logged in user
            assignee = loggedInUser;
        }
        return assignee;
    }

    /**
     * validate requested substitute user
     * @param substitute
     * @param assignee
     * @return substitute name if valid
     * @throws UserStoreException
     */
    private String validateAndGetSubstitute(String substitute, String assignee) throws UserStoreException {
        //validate substitute
        UserRealm userRealm = BPMNOSGIService.getUserRealm();
        if (substitute == null || substitute.trim().isEmpty()) {
            throw new ActivitiIllegalArgumentException("The substitute must be specified");
        } else if (assignee.equalsIgnoreCase(substitute)) {
            throw new ActivitiIllegalArgumentException("Substitute and assignee should be different users");
        } else if (!userRealm.getUserStoreManager().isExistingUser(substitute.trim())) {
            throw new ActivitiIllegalArgumentException("Cannot substitute a non existing user: " + substitute);
        }
        return substitute;
    }

}

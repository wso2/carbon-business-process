/*
 * Copyright (c) 2015. WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * /
 */

package org.wso2.carbon.bpmn.rest.service.identity;

import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.identity.*;
import org.wso2.carbon.bpmn.rest.service.base.BaseIdentityService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityService extends BaseIdentityService {

    protected static HashMap<String, QueryProperty> groupProperties = new HashMap<String, QueryProperty>();
    protected static HashMap<String, QueryProperty> userProperties = new HashMap<>();

    static {
        groupProperties.put("id", GroupQueryProperty.GROUP_ID);
        groupProperties.put("name", GroupQueryProperty.NAME);
        groupProperties.put("type", GroupQueryProperty.TYPE);
    }

    static {
        userProperties.put("id", UserQueryProperty.USER_ID);
        userProperties.put("firstName", UserQueryProperty.FIRST_NAME);
        userProperties.put("lastName", UserQueryProperty.LAST_NAME);
        userProperties.put("email", UserQueryProperty.EMAIL);
    }

    @Context
    UriInfo uriInfo;

    /**
     * Get all the groups that match the filters given by query parameters of the request.
     * @return DataResponse
     */
    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getGroups() {
        GroupQuery query = identityService.createGroupQuery();

        Map<String, String> allRequestParams = new HashMap<>();

        String id = uriInfo.getQueryParameters().getFirst("id");
        if (id != null) {
            query.groupId(id);
            allRequestParams.put("id", id);
        }
        String name = uriInfo.getQueryParameters().getFirst("name");
        if (name != null) {
            query.groupName(name);
            allRequestParams.put("name", name);
        }
        String nameLike = uriInfo.getQueryParameters().getFirst("nameLike");
        if (nameLike != null) {
            query.groupNameLike(nameLike);
            allRequestParams.put("nameLike", nameLike);
        }
        String type = uriInfo.getQueryParameters().getFirst("type");
        if (type != null) {
            query.groupType(type);
            allRequestParams.put("type", type);
        }
        String member = uriInfo.getQueryParameters().getFirst("name");
        if (member != null) {
            query.groupMember(member);
            allRequestParams.put("member", member);
        }
        String potentialStarter = uriInfo.getQueryParameters().getFirst("potentialStarter");
        if (potentialStarter != null) {
            query.potentialStarter(potentialStarter);
            allRequestParams.put("potentialStarter", potentialStarter);
        }

        allRequestParams = Utils.prepareCommonParameters(allRequestParams, uriInfo);

        GroupPaginateList groupPaginateList = new GroupPaginateList(new RestResponseFactory(), uriInfo);
        return groupPaginateList.paginateList(allRequestParams, query, "id", groupProperties);
    }

    /**
     * Get the user group identified by given group ID.
     * @param groupId
     * @return GroupResponse
     */
    @GET
    @Path("/groups/{group-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public GroupResponse getGroup(@PathParam("group-id") String groupId) {
        return new RestResponseFactory().createGroupResponse(getGroupFromRequest(groupId), uriInfo.getBaseUri().toString());
    }

    /**
     * Get all the users that match the filters given by query parameters of the request.
     * @return DataResponse
     */
    @GET
    @Path("/users")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getUsers() {
        UserQuery query = identityService.createUserQuery();

        Map<String, String> allRequestParams = new HashMap<>();

        String id = uriInfo.getQueryParameters().getFirst("id");
        if (id != null) {
            query.userId(id);
            allRequestParams.put("id",id);
        }
        String firstName = uriInfo.getQueryParameters().getFirst("firstName");
        if (firstName != null) {
            query.userFirstName(firstName);
            allRequestParams.put("firstName", firstName);
        }
        String lastName = uriInfo.getQueryParameters().getFirst("lastName");
        if (lastName != null) {
            query.userLastName(lastName);
            allRequestParams.put("lastName", lastName);
        }
        String email = uriInfo.getQueryParameters().getFirst("email");
        if (email != null) {
            query.userEmail(email);
            allRequestParams.put("email", email);
        }
        String firstNameLike = uriInfo.getQueryParameters().getFirst("firstNameLike");
        if (firstNameLike != null) {
            query.userFirstNameLike(firstNameLike);
            allRequestParams.put("firstNameLike", firstNameLike);
        }
        String lastNameLike = uriInfo.getQueryParameters().getFirst("lastNameLike");
        if (lastNameLike != null) {
            query.userLastNameLike(lastNameLike);
            allRequestParams.put("lastNameLike", lastNameLike);
        }
        String emailLike = uriInfo.getQueryParameters().getFirst("emailLike");
        if (emailLike != null) {
            query.userEmailLike(emailLike);
            allRequestParams.put("emailLike", emailLike);
        }
        String memberOfGroup = uriInfo.getQueryParameters().getFirst("memberOfGroup");
        if (memberOfGroup != null) {
            query.memberOfGroup(memberOfGroup);
            allRequestParams.put("memberOfGroup", memberOfGroup);
        }
        String potentialStarter = uriInfo.getQueryParameters().getFirst("potentialStarter");
        if (potentialStarter != null) {
            query.potentialStarter(potentialStarter);
            allRequestParams.put("potentialStarter", potentialStarter);
        }

        allRequestParams = Utils.prepareCommonParameters(allRequestParams, uriInfo);

        return new UserPaginateList(new RestResponseFactory(), uriInfo)
                .paginateList(allRequestParams, query, "id", userProperties);
    }

    /**
     * Get the user information of the user identified by given user ID.
     * @param userId
     * @return
     */
    @GET
    @Path("/users/{user-id}/info")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<UserInfoResponse> getUserInfo(@PathParam("user-id") String userId) {
        User user = getUserFromRequest(userId);

        return new RestResponseFactory().createUserInfoKeysResponse(identityService.getUserInfoKeys(user.getId()), user.getId(), uriInfo.getBaseUri().toString());
    }

    /**
     * Get the user identified by given user ID,
     * @param userId
     * @return
     */
    @GET
    @Path("/users/{user-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public UserResponse getUser(@PathParam("user-id") String userId) {
        return new RestResponseFactory().createUserResponse(getUserFromRequest(userId), false, uriInfo.getBaseUri().toString());
    }
}

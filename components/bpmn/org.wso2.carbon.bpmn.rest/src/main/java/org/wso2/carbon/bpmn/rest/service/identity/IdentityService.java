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

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.engine.query.QueryProperty;
//import org.bouncycastle.ocsp.Req;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.identity.GroupPaginateList;
import org.wso2.carbon.bpmn.rest.model.identity.GroupResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserInfoResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserPaginateList;
import org.wso2.carbon.bpmn.rest.model.identity.UserResponse;
import org.wso2.carbon.bpmn.rest.service.base.BaseIdentityService;
import org.wso2.msf4j.Microservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.service.identity.IdentityService",
        service = Microservice.class,
        immediate = true)
public class IdentityService extends BaseIdentityService implements Microservice {

    protected static HashMap<String, QueryProperty> groupProperties =
            new HashMap<String, QueryProperty>();
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

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    /**
     * Get all the groups that match the filters given by query parameters of the request.
     *
     * @return DataResponse
     */
    @GET
    @Path("/groups")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public DataResponse getGroups(@Context HttpRequest request) {
        GroupQuery query = identityService.createGroupQuery();

        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());

        if (decoder.parameters().containsKey("id")) {
            String id = decoder.parameters().get("id").get(0);
            if (id != null) {
                query.groupId(id);
                allRequestParams.put("id", id);
            }
        }
        if (decoder.parameters().containsKey("name")) {
            String name = decoder.parameters().get("name").get(0);
            if (name != null) {
                query.groupName(name);
                allRequestParams.put("name", name);
            }
        }
        if (decoder.parameters().containsKey("nameLike")) {
            String nameLike = decoder.parameters().get("nameLike").get(0);
            if (nameLike != null) {
                query.groupNameLike(nameLike);
                allRequestParams.put("nameLike", nameLike);
            }
        }
        if (decoder.parameters().containsKey("type")) {
            String type = decoder.parameters().get("type").get(0);
            if (type != null) {
                query.groupType(type);
                allRequestParams.put("type", type);
            }
        }
        if (decoder.parameters().containsKey("name")) {
            String member = decoder.parameters().get("name").get(0);
            if (member != null) {
                query.groupMember(member);
                allRequestParams.put("member", member);
            }
        }
        if (decoder.parameters().containsKey("potentialStarter")) {
            String potentialStarter = decoder.parameters().get("potentialStarter").get(0);
            if (potentialStarter != null) {
                query.potentialStarter(potentialStarter);
                allRequestParams.put("potentialStarter", potentialStarter);
            }
        }

        allRequestParams = Utils.prepareCommonParameters(allRequestParams, decoder.parameters());

        GroupPaginateList groupPaginateList = new GroupPaginateList(new RestResponseFactory());
        return groupPaginateList.paginateList(allRequestParams, query, "id", groupProperties);
    }

    /**
     * Get the user group identified by given group ID.
     *
     * @param groupId
     * @return GroupResponse
     */
    @GET
    @Path("/groups/{group-id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public GroupResponse getGroup(@PathParam("group-id") String groupId) {
        return new RestResponseFactory().createGroupResponse(getGroupFromRequest(groupId));
    }

    /**
     * Get all the users that match the filters given by query parameters of the request.
     *
     * @return DataResponse
     */
    @GET
    @Path("/users")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public DataResponse getUsers(@Context HttpRequest request) {
        UserQuery query = identityService.createUserQuery();

        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());

        if (decoder.parameters().containsKey("id")) {
            String id = decoder.parameters().get("id").get(0);
            if (id != null) {
                query.userId(id);
                allRequestParams.put("id", id);
            }
        }
        if (decoder.parameters().containsKey("firstName")) {
            String firstName = decoder.parameters().get("firstName").get(0);
            if (firstName != null) {
                query.userFirstName(firstName);
                allRequestParams.put("firstName", firstName);
            }
        }
        if (decoder.parameters().containsKey("lastName")) {
            String lastName = decoder.parameters().get("lastName").get(0);
            if (lastName != null) {
                query.userLastName(lastName);
                allRequestParams.put("lastName", lastName);
            }
        }
        if (decoder.parameters().containsKey("email")) {
            String email = decoder.parameters().get("email").get(0);
            if (email != null) {
                query.userEmail(email);
                allRequestParams.put("email", email);
            }
        }
        if (decoder.parameters().containsKey("firstNameLike")) {
            String firstNameLike = decoder.parameters().get("firstNameLike").get(0);
            if (firstNameLike != null) {
                query.userFirstNameLike(firstNameLike);
                allRequestParams.put("firstNameLike", firstNameLike);
            }
        }
        if (decoder.parameters().containsKey("lastNameLike")) {
            String lastNameLike = decoder.parameters().get("lastNameLike").get(0);
            if (lastNameLike != null) {
                query.userLastNameLike(lastNameLike);
                allRequestParams.put("lastNameLike", lastNameLike);
            }
        }
        if (decoder.parameters().containsKey("emailLike")) {
            String emailLike = decoder.parameters().get("emailLike").get(0);
            if (emailLike != null) {
                query.userEmailLike(emailLike);
                allRequestParams.put("emailLike", emailLike);
            }
        }
        if (decoder.parameters().containsKey("memberOfGroup")) {
            String memberOfGroup = decoder.parameters().get("memberOfGroup").get(0);
            if (memberOfGroup != null) {
                query.memberOfGroup(memberOfGroup);
                allRequestParams.put("memberOfGroup", memberOfGroup);
            }
        }
        if (decoder.parameters().containsKey("potentialStarter")) {
            String potentialStarter = decoder.parameters().get("potentialStarter").get(0);
            if (potentialStarter != null) {
                query.potentialStarter(potentialStarter);
                allRequestParams.put("potentialStarter", potentialStarter);
            }
        }

        allRequestParams = Utils.prepareCommonParameters(allRequestParams, decoder.parameters());

        return new UserPaginateList(new RestResponseFactory())
                .paginateList(allRequestParams, query, "id", userProperties);
    }

    /**
     * Get the user information of the user identified by given user ID.
     *
     * @param userId
     * @return
     */
    @GET
    @Path("/users/{user-id}/info")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public List<UserInfoResponse> getUserInfo(@PathParam("user-id") String userId) {
        User user = getUserFromRequest(userId);

        return new RestResponseFactory()
                .createUserInfoKeysResponse(identityService.getUserInfoKeys(user.getId()),
                                            user.getId());
    }

    /**
     * Get the user identified by given user ID,
     *
     * @param userId
     * @return
     */
    @GET
    @Path("/users/{user-id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public UserResponse getUser(@PathParam("user-id") String userId) {
        return new RestResponseFactory().createUserResponse(getUserFromRequest(userId), false);
    }
}

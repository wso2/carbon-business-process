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

import io.netty.handler.codec.http.QueryStringDecoder;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.internal.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.identity.GroupPaginateList;
import org.wso2.carbon.bpmn.rest.model.identity.GroupResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserInfoResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserPaginateList;
import org.wso2.carbon.bpmn.rest.model.identity.UserResponse;
import org.wso2.carbon.bpmn.rest.service.base.BaseIdentityService;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

//import org.bouncycastle.ocsp.Req;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.service.identity.IdentityService",
        service = Microservice.class,
        immediate = true)
//TODO: @PATH
public class IdentityService extends BaseIdentityService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(IdentityService.class);

    @Reference(
            name = "org.wso2.carbon.bpmn.core.BPMNEngineService",
            service = BPMNEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterBPMNEngineService")
    public void setBpmnEngineService(BPMNEngineService engineService) {
        log.info("Setting BPMN engine " + engineService);

    }

    protected void unRegisterBPMNEngineService(BPMNEngineService engineService) {
        log.info("Unregister BPMNEngineService..");
    }

    protected static final Map<String, QueryProperty> GROUP_PROPERTIES;
    protected static final Map<String, QueryProperty> USER_PROPERTIES;

    static {
        HashMap<String, QueryProperty> groupMap = new HashMap<>();
        groupMap.put("id", GroupQueryProperty.GROUP_ID);
        groupMap.put("name", GroupQueryProperty.NAME);
        groupMap.put("type", GroupQueryProperty.TYPE);
        GROUP_PROPERTIES = Collections.unmodifiableMap(groupMap);
    }

    static {
        HashMap<String, QueryProperty> userMap = new HashMap<>();
        userMap.put("id", UserQueryProperty.USER_ID);
        userMap.put("firstName", UserQueryProperty.FIRST_NAME);
        userMap.put("lastName", UserQueryProperty.LAST_NAME);
        userMap.put("email", UserQueryProperty.EMAIL);
        USER_PROPERTIES = Collections.unmodifiableMap(userMap);
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
    public DataResponse getGroups(@Context Request request) {
        GroupQuery query = BPMNOSGIService.getIdentityService().createGroupQuery();

        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        if (decoder.parameters().size() > 0) {
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
        }
        allRequestParams = Utils.prepareCommonParameters(allRequestParams, decoder.parameters());

        GroupPaginateList groupPaginateList =
                new GroupPaginateList(new RestResponseFactory(), request.getUri());
        return groupPaginateList.paginateList(allRequestParams, query, "id", GROUP_PROPERTIES);
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
    public GroupResponse getGroup(@PathParam("group-id") String groupId,
                                  @Context Request request) {
        return new RestResponseFactory()
                .createGroupResponse(getGroupFromRequest(groupId), request.getUri());
    }

    /**
     * Get all the users that match the filters given by query parameters of the request.
     *
     * @return DataResponse
     */
    @GET
    @Path("/users")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public DataResponse getUsers(@Context Request request) {
        UserQuery query = BPMNOSGIService.getIdentityService().createUserQuery();

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

        return new UserPaginateList(new RestResponseFactory(), request.getUri())
                .paginateList(allRequestParams, query, "id", USER_PROPERTIES);
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
    public List<UserInfoResponse> getUserInfo(@PathParam("user-id") String userId,
                                              @Context Request request) {
        User user = getUserFromRequest(userId);

        return new RestResponseFactory()
                .createUserInfoKeysResponse(BPMNOSGIService.getIdentityService().getUserInfoKeys(user.getId()),
                                            user.getId(), request.getUri());
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
    public UserResponse getUser(@PathParam("user-id") String userId, @Context Request request) {
        return new RestResponseFactory()
                .createUserResponse(getUserFromRequest(userId), false, request.getUri());
    }
}

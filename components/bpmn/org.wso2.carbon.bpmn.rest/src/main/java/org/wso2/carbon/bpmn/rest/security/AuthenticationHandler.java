/*
 * Copyright (c) 2015-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 **/

package org.wso2.carbon.bpmn.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.IdentityService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.exception.BPMNAuthenticationException;
import org.wso2.carbon.bpmn.rest.common.RestErrorResponse;
import org.wso2.carbon.bpmn.rest.common.exception.RestApiBasicAuthenticationException;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.security.caas.api.CarbonPrincipal;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.msf4j.security.oauth2.OAuth2SecurityInterceptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Handle  REST Request authentication
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.security.AuthenticationHandler",
        service = Interceptor.class,
        immediate = true
)
public class AuthenticationHandler implements Interceptor {

    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String AUTH_TYPE_BASIC = "Basic";
    private static final String AUTH_TYPE_NONE = "None";
    private static final String AUTH_TYPE_OAuth = "Bearer";
    private static final String AUTH_URL_KEY = "AUTH_SERVER_URL";
    private Logger log = LoggerFactory.getLogger(AuthenticationHandler.class);
    private OAuth2SecurityInterceptor oAuth2SecurityInterceptor = null;

    @Override
    public boolean preCall(Request request, org.wso2.msf4j.Response responder,
                           ServiceMethodInfo serviceMethodInfo) throws Exception {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME);
        if (authHeader != null) {
            if (authHeader.startsWith(AUTH_TYPE_BASIC)) {

                String encodedCredentials = authHeader.substring("Basic ".length()).trim();
                String credentials = new String(Base64.getDecoder().decode(encodedCredentials),
                        Charset.forName("UTF-8"));
                int seperatorIndex = credentials.indexOf(':');
                String username = credentials.substring(0, seperatorIndex);
                String password = credentials.substring(seperatorIndex + 1);
                handleBasicAuth(username, password);

            } else if (authHeader.startsWith(AUTH_TYPE_OAuth)) {
                log.debug("OAuth Authentication is used");
                if (oAuth2SecurityInterceptor == null) {
                    String authUrl = RestServiceContentHolder.getInstance().getRestService().getBPMNEngineService()
                            .getProcessEngineConfiguration().getAuthServerUrl();
                    System.setProperty(AUTH_URL_KEY, authUrl);
                    oAuth2SecurityInterceptor = new OAuth2SecurityInterceptor();
                }
                oAuth2SecurityInterceptor.preCall(request, responder, serviceMethodInfo);
            } else {
                //todo:
                log.info("No authorization type is specified.");
            }
        }

        return true;
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo)
            throws Exception {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME);
        if (authHeader.startsWith(AUTH_TYPE_OAuth)) {
                log.info("Authorization type used in OAuth");
                oAuth2SecurityInterceptor.postCall(request, status, serviceMethodInfo);
        }
    }

    //Authenticate Basic auth type request
    protected Response handleBasicAuth(String userName, String password) {

        try {
            if (authenticate(userName, password)) {
                return null;
            }
        } catch (RestApiBasicAuthenticationException e) {
            log.error("Could not authenticate user : " + userName + "against carbon userStore", e);
        }
        return authenticationFail();
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
     * Checks whether a given userName:password combination authenticates correctly against
     * carbon userStore
     * Upon successful authentication returns true, false otherwise
     *
     * @param userName
     * @param password
     * @return
     * @throws RestApiBasicAuthenticationException wraps and throws exceptions occur when
     *                                             trying to authenticate
     *                                             the user
     */
    private boolean authenticate(String userName, String password)
            throws RestApiBasicAuthenticationException {

        boolean authStatus;

        try {
            User user = RestServiceContentHolder.getInstance().getRestService().getUserRealm().getIdentityStore()
                    .getUser(userName);
            IdentityService identityService = RestServiceContentHolder.getInstance().getRestService()
                    .getIdentityService();
            authStatus = identityService.checkPassword(user.getUserId(), password);

            if (!authStatus) {
                return false;
            }
        } catch (BPMNAuthenticationException | IdentityStoreException | UserNotFoundException e) {
            throw new RestApiBasicAuthenticationException(e.getMessage(), e);
        }

            /* Upon successful authentication existing thread local carbon context
             * is updated to mimic the authenticated user */
        try {
            PrivilegedCarbonContext privilegedCarbonContext =
                    (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();

            User authenticatedUser = RestServiceContentHolder.getInstance().getRestService().getUserRealm()
                    .getIdentityStore().
                            getUser(userName);

            CarbonPrincipal principal = new CarbonPrincipal(authenticatedUser);
            privilegedCarbonContext.setUserPrincipal(principal);
        } catch (IdentityStoreException | UserNotFoundException e) {
            //todo:
            String msg = "Error occured while ";
            log.error(msg, e);
        }


        return true;
    }

    private Response authenticationFail() {
        return authenticationFail(AUTH_TYPE_BASIC);
    }

    private Response authenticationFail(String authType) {
        //authentication failed, request the authentication, add the realm name if needed to
        // the value of WWW-Authenticate

        RestErrorResponse restErrorResponse = new RestErrorResponse();
        restErrorResponse.setErrorMessage("Authentication required");
        restErrorResponse.setStatusCode(Response.Status.UNAUTHORIZED.getStatusCode());
        ObjectMapper mapper = new ObjectMapper();

        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(restErrorResponse);
        } catch (IOException e) { //log the error and continue. No need to specifically handle it
            log.error("Error Json String conversion failed", e);
        }
        return Response.status(restErrorResponse.getStatusCode()).type(MediaType.APPLICATION_JSON)
                .header(WWW_AUTHENTICATE,
                        authType).entity(jsonString).build();
    }


}


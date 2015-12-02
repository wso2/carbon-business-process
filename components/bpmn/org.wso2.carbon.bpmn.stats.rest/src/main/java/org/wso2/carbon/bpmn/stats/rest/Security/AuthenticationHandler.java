package org.wso2.carbon.bpmn.stats.rest.Security;

/**
 * Created by natasha on 12/2/15.
 */

import org.activiti.engine.IdentityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.bpmn.stats.rest.Exception.*;
import org.wso2.carbon.bpmn.stats.rest.util.BPMNOsgiServices;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Map;

public class AuthenticationHandler implements RequestHandler {

    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    protected Log log = LogFactory.getLog(AuthenticationHandler.class);

    private final static String AUTH_TYPE_BASIC = "Basic";
    private final static String AUTH_TYPE_OAuth = "Bearer";


    /**
     * Implementation of RequestHandler.handleRequest method.
     * This method retrieves userName and password from Basic auth header,
     * and tries to authenticate against carbon user store
     * <p/>
     * Upon successful authentication allows process to proceed to retrieve requested REST resource
     * Upon invalid credentials returns a HTTP 401 UNAUTHORIZED response to client
     * Upon receiving a userStoreExceptions or IdentityException returns HTTP 500 internal server error to client
     *
     * @param message
     * @param classResourceInfo
     * @return Response
     */
    public Response handleRequest(Message message, ClassResourceInfo classResourceInfo) {
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        //System.out.println("Authorization Type:" + policy.getAuthorizationType());
        if (policy != null && AUTH_TYPE_BASIC.equals(policy.getAuthorizationType())) {
            return handleBasicAuth(policy);
        } else {
            return handleOAuth(message);
        }
    }

    protected Response handleBasicAuth(AuthorizationPolicy policy) {
        String username = policy.getUserName();
        String password = policy.getPassword();

        System.out.println("username:" + username);
        System.out.println("password:" + password);
        try {
            if (authenticate(username, password)) {
                System.out.println("successfull authenticated");
                return null;
            }
        } catch (RestApiBasicAuthenticationException e) {
            log.error("Could not authenticate user : " + username + "against carbon userStore", e);
        }
        return authenticationFail();
    }

    protected Response handleOAuth(Message message) {
        ArrayList<String> headers = ((Map<String, ArrayList>) message.get(Message.PROTOCOL_HEADERS)).get(AUTHORIZATION_HEADER_NAME);
        if (headers != null) {
            String authHeader = headers.get(0);
            if (authHeader.startsWith(AUTH_TYPE_OAuth)) {
                return authenticationFail(AUTH_TYPE_OAuth);
            }
        }
        return authenticationFail(AUTH_TYPE_OAuth);
    }

    /**
     * Checks whether a given userName:password combination authenticates correctly against carbon userStore
     * Upon successful authentication returns true, false otherwise
     *
     * @param userName
     * @param password
     * @return
     * @throws RestApiBasicAuthenticationException wraps and throws exceptions occur when trying to authenticate
     *                                             the user
     */
    private boolean authenticate(String userName, String password) throws RestApiBasicAuthenticationException {
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(userName);
        String userNameWithTenantDomain = tenantAwareUserName + "@" + tenantDomain;

        System.out.println("FFFFFF:" + RegistryContext.getBaseInstance().getBasePath());
        System.out.println("LLLLLLLLLLL:" + RegistryContext.getBaseInstance().getRealmService());
        RealmService realmService = RegistryContext.getBaseInstance().getRealmService();
        TenantManager mgr = realmService.getTenantManager();

        int tenantId = 0;
        try {
            tenantId = mgr.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new RestApiBasicAuthenticationException(
                    "Identity exception thrown while getting tenant ID for user : " + userNameWithTenantDomain, e);
        }

        // tenantId == -1, means an invalid tenant.
        if (tenantId == -1) {
            if (log.isDebugEnabled()) {
                log.debug("Basic authentication request with an invalid tenant : " + userNameWithTenantDomain);
            }
            System.out.println("Basic authentication request with an invalid tenant : " + userNameWithTenantDomain);
            return false;
        }

        System.out.println("came here");

        UserStoreManager userStoreManager = null;
        boolean authStatus = false;

        try {
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            authStatus = userStoreManager.authenticate(tenantAwareUserName, password);
        } catch (UserStoreException e) {
            throw new RestApiBasicAuthenticationException(
                    "User store exception thrown while authenticating user : " + userNameWithTenantDomain, e);
        }

        System.out.println("Basic authentication request completed. " +
                "Username : " + userNameWithTenantDomain +
                ", Authentication State : " + authStatus);

        IdentityService identityService = BPMNOsgiServices.getIdentityService();
        authStatus = identityService.checkPassword(userName, password);
        if (log.isDebugEnabled()) {
            log.debug("Basic authentication request completed. " +
                    "Username : " + userNameWithTenantDomain +
                    ", Authentication State : " + authStatus);
        }

        if (authStatus) {
            /* Upon successful authentication existing thread local carbon context
             * is updated to mimic the authenticated user */

            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setUsername(userName);
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
        }
        return authStatus;

    }

    private Response authenticationFail() {
        return authenticationFail(AUTH_TYPE_BASIC);
    }

    private Response authenticationFail(String authType) {
        //authentication failed, request the authetication, add the realm name if needed to the value of WWW-Authenticate
        return Response.status(401).header(WWW_AUTHENTICATE, authType).build();
    }


}
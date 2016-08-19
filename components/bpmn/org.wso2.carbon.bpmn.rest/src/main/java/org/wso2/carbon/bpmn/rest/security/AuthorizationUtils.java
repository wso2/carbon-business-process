package org.wso2.carbon.bpmn.rest.security;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.security.caas.api.CarbonPermission;

import java.security.AccessControlException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

/**
 * Utility methods for resource authorizations.
 */
public class AuthorizationUtils {

    private static Logger log = LoggerFactory.getLogger(AuthorizationUtils.class);

    /**
     * Return true if the current user is authorized for the given action against the given resource
     * @param resource
     * @param action
     * @return true if allowed
     */
    public static boolean isUserAuthorized(String resource, String action) {
        CarbonPermission carbonPermission = new CarbonPermission(resource, action);
        Principal userPrincipal = PrivilegedCarbonContext.getCurrentContext().getUserPrincipal();

        Subject subject = new Subject();
        subject.getPrincipals().add(userPrincipal);

        final SecurityManager securityManager;

        if (System.getSecurityManager() == null) {
            securityManager = new SecurityManager();
        } else {
            securityManager = System.getSecurityManager();
        }

        try {
            Subject.doAsPrivileged(subject, (PrivilegedExceptionAction) () -> {
                securityManager.checkPermission(carbonPermission);
                return null;
            }, null);
            return true;
        } catch (PrivilegedActionException | AccessControlException e) {
            if (log.isDebugEnabled()) {
                log.debug("Authorization Failed", e);
            }
            return false;
        }

    }

}

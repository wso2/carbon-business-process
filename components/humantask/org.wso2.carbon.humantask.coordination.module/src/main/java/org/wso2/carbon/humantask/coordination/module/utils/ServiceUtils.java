/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.coordination.module.utils;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.humantask.coordination.module.HumanTaskCoordinationException;
import org.wso2.carbon.humantask.coordination.module.internal.HTCoordinationModuleContentHolder;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.humantask.core.integration.utils.AnonymousServiceFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class ServiceUtils {

    private static Log log = LogFactory.getLog(ServiceUtils.class);

    /**
     * Returns URL of the of the Task engine's Protocol Handler Admin service.
     * eg: https://localhost:9443/services/HumanTaskProtocolHandler/
     *
     * @return HumanTask protocol handler admin service's url
     */
    public static String getTaskProtocolHandlerURL(ConfigurationContext serverConfigurationContext) throws HumanTaskCoordinationException {
        HumanTaskServerConfiguration serverConfig = HTCoordinationModuleContentHolder.getInstance().getHtServer().getServerConfig();
        String baseURL;
        if (serverConfig.isClusteredTaskEngines()) {
            baseURL = serverConfig.getLoadBalancerURL();
        } else {
            String scheme = CarbonConstants.HTTPS_TRANSPORT;
            String host;
            try {
                host = NetworkUtils.getLocalHostname();
            } catch (SocketException e) {
                log.error(e.getMessage(), e);
                throw new HumanTaskCoordinationException(e.getLocalizedMessage(), e);
            }

            int port = 9443;
            port = CarbonUtils.getTransportProxyPort(serverConfigurationContext, scheme);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(serverConfigurationContext, scheme);
            }
            baseURL = scheme + "://" + host + ":" + port;
        }

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }

        String tenantDomain = "";
        try {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        } catch (Throwable e) {
            tenantDomain = null;
        }

        String protocolHandlerURL = baseURL + webContext + ((tenantDomain != null &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
                "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain : "") +
                Constants.CARBON_ADMIN_SERVICE_CONTEXT_ROOT + "/"
                + Constants.HUMANTASK_ENGINE_COORDINATION_PROTOCOL_HANDLER_SERVICE;
        return protocolHandlerURL;
    }

    public static MessageContext invokeRegistrationService(MessageContext mctx, String registrationService) throws AxisFault {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConfigurationContext configurationContext = HTCoordinationModuleContentHolder.getInstance().getHtServer().getTaskStoreManager().
                getHumanTaskStore(tenantId).getConfigContext();
        OperationClient opClient = getOperationClient(mctx, configurationContext);
        mctx.getOptions().setParent(opClient.getOptions());

        opClient.addMessageContext(mctx);
        Options operationOptions = opClient.getOptions();

        operationOptions.setTo(new EndpointReference(registrationService));
        operationOptions.setAction(Constants.WS_COOR_REGISTERATION_ACTION);
//        operationOptions.setTransportInProtocol(org.apache.axis2.Constants.TRANSPORT_HTTPS);

        //Setting basic auth headers. Reading those information using HT server config.
        HumanTaskServerConfiguration serverConfig = HTCoordinationModuleContentHolder.getInstance().getHtServer().getServerConfig();
//        String tenantDomain = MultitenantUtils.getTenantDomainFromUrl(registrationService);
//        if (registrationService.equals(tenantDomain)) {
//            //this is a Super tenant registration service
//            if (log.isDebugEnabled()) {
//                log.debug("Sending Username" + serverConfig.getRegistrationServiceAuthUsername() + " - " + serverConfig.getRegistrationServiceAuthPassword());  //TODO REMOVE this
//            }
//            setBasicAccessSecurityHeaders(serverConfig.getRegistrationServiceAuthUsername(), serverConfig.getRegistrationServiceAuthPassword(), true, operationOptions);
//        } else {
//            if (log.isDebugEnabled()) {
//                log.debug("Sending ws-coor Registration request to tenant domain: " + tenantDomain);
//            }
//            // Tenant's registration service
//
//            String username = serverConfig.getRegistrationServiceAuthUsername() + "@" + tenantDomain;
//            String pass = serverConfig.getRegistrationServiceAuthPassword();
//            if (log.isDebugEnabled()) {
//                log.debug("Sending Username" + username + " - " + pass);  //TODO REMOVE this
//            }
//            setBasicAccessSecurityHeaders(username,pass,true,operationOptions);
//        }

        HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();
        basicAuthentication.setPreemptiveAuthentication(true);
        basicAuthentication.setUsername(serverConfig.getRegistrationServiceAuthUsername());
        basicAuthentication.setPassword(serverConfig.getRegistrationServiceAuthPassword());

        operationOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);

        if (log.isDebugEnabled()) {
            log.debug("Invoking Registration service");
        }
        opClient.execute(true);

        MessageContext responseMessageContext =
                opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        return responseMessageContext;

    }

    private static OperationClient getOperationClient(MessageContext partnerMessageContext,
                                                      ConfigurationContext clientConfigCtx)
            throws AxisFault {

        AxisService anonymousService =
                AnonymousServiceFactory.getAnonymousService(Constants.registrationService,
                        Constants.REGISTRATION_PORT,
                        clientConfigCtx.getAxisConfiguration(), Constants.HUMANTASK_COORDINATION_MODULE_NAME);

        anonymousService.getParent().addParameter("hiddenService", "true");
        ServiceGroupContext sgc = new ServiceGroupContext(clientConfigCtx, (AxisServiceGroup) anonymousService.getParent());
        ServiceContext serviceCtx = sgc.getServiceContext(anonymousService);

        AxisOperation axisAnonymousOperation = anonymousService.getOperation(ServiceClient.ANON_OUT_IN_OP);

        Options clientOptions = cloneOptions(partnerMessageContext.getOptions());
        clientOptions.setExceptionToBeThrownOnSOAPFault(false);
        /* This value doesn't overrideend point config. */
        clientOptions.setTimeOutInMilliSeconds(60000);

        return axisAnonymousOperation.createClient(serviceCtx, clientOptions);
    }

    public static Options cloneOptions(Options options) {

        // create new options object and set the parent
        Options clonedOptions = new Options(options.getParent());

        // copy general options
        clonedOptions.setCallTransportCleanup(options.isCallTransportCleanup());
        clonedOptions.setExceptionToBeThrownOnSOAPFault(options.isExceptionToBeThrownOnSOAPFault());
        clonedOptions.setManageSession(options.isManageSession());
        clonedOptions.setSoapVersionURI(options.getSoapVersionURI());
        clonedOptions.setTimeOutInMilliSeconds(options.getTimeOutInMilliSeconds());
        clonedOptions.setUseSeparateListener(options.isUseSeparateListener());

        // copy transport related options
        clonedOptions.setListener(options.getListener());
        clonedOptions.setTransportIn(options.getTransportIn());
        clonedOptions.setTransportInProtocol(options.getTransportInProtocol());
        clonedOptions.setTransportOut(clonedOptions.getTransportOut());


        // copy username and password options
        clonedOptions.setUserName(options.getUserName());
        clonedOptions.setPassword(options.getPassword());

        // cloen the property set of the current options object
        for (Object o : options.getProperties().keySet()) {
            String key = (String) o;
            clonedOptions.setProperty(key, options.getProperty(key));
        }

        return clonedOptions;
    }

    public static void setBasicAccessSecurityHeaders(String userName, String password, boolean rememberMe,
                                                     Options options) throws AxisFault {

        String userNamePassword = userName + ":" + password;
        String encodedString = Base64Utils.encode(userNamePassword.getBytes());

        String authorizationHeader = "Basic " + encodedString;

        List<Header> headers = new ArrayList<Header>();

        Header authHeader = new Header("Authorization", authorizationHeader);
        headers.add(authHeader);

        if (rememberMe) {
            Header rememberMeHeader = new Header("RememberMe", "true");
            headers.add(rememberMeHeader);
        }

        options.setProperty(HTTPConstants.HTTP_HEADERS, headers);
    }
}

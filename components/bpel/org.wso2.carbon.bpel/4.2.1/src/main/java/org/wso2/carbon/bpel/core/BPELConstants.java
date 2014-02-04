/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core;

import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.File;

public final class BPELConstants {
    private BPELConstants() {
    }

    public static final String BPEL_PKG_CONFIG_NS = "http://wso2.org/bps/bpel/package/config";

    public static final String BPS_CONFIG_NS = "http://wso2.org/bps/config";

    public static final String ENDPOINTREF = "endpointReference";

    public static final String ENDPOINTS = "endpoints";

    public static final String SERVICE_DESCRIPTION = "serviceDescription";

    public static final String ENABLE_SEC = "enableSec";

    public static final String ADDRESS = "address";

    public static final String SERVICE_NAME = "serviceName";

    public static final String SERVICE_PORT_NAME = "servicePort";

    public static final String SERVICE_NS = "serviceNS";

    public static final String SERVICE_DESC_LOCATION = "location";

    public static final String POLICY_LOCATION = "policyLocation";

    public static final String VALUE = "value";

    public static final String HTTP_OPTIONS = "httpOptions";

    public static final String REQUEST_CHUNK = "request-chunk";

    public static final String PROTOCOL_VERSION = "protocol-version";

    public static final String REQUEST_GZIP = "request-gzip";

    public static final String ACCEPT_GZIP = "accept-gzip";

    public static final String PROTOCOL_CONTENT_CHARSET = "protocol-content-charset";

    public static final String CONNECTION_TIMEOUT = "connection-timeout";

    public static final String SOCKET_TIMEOUT = "socket-timeout";

    public static final String PROTOCOL_MAX_REDIRECTS = "protocol-max-redirects";

    public static final String URL = "url";

    public static final String TRUE = "true";

    public static final String PROXY_HOST = "host";

    public static final String PROXY_PORT = "port";

    public static final String PROXY_DOMAIN = "domain";

    public static final String USER = "user";

    public static final String PROXY_PASSWORD = "password";

    public static final String PROXY = "proxy";

    public static final String HEADERS = "headers";

    public static final String MEX_TIMEOUT = "mex-timeout";

    /**
     * The Axis2 client options property name for the Rampart service policy
     */
    public static final String RAMPART_POLICY = "rampartPolicy";

    // hidden service parameter
    public static final String HIDDEN_SERVICE_PARAM = "hiddenService";

    public static final String PROCESS_ID = "processId";

    public static final String DB_CONF = "dbConf";

    public static final String MODE = "mode";

    public static final String DATA_SOURCE = "dataSource";

    public static final String NAME = "name";

    public static final String JNDI = "jndi";

    public static final String CONTEXT_FACTORY = "contextFactory";

    public static final String PROVIDER_URL = "providerUrl";

    public static final String EMBEDDED = "embedded";

    public static final String INTERNAL = "internal";

    public static final String JDBC_URL = "jdbcUrl";

    public static final String DRIVER = "driver";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String LOGGING = "logging";

    public static final String DB_POOL = "dp-pool";

    public static final String MAX_SIZE = "maxSize";

    public static final String MIN_SIZE = "minSize";

    public static final String BLOCKING = "blocking";

    public static final String REG_CONF = "regConf";

    public static final String TYPE = "type";

    public static final String BASE_PATH = "basePath";

    public static final String TRUST_STORE = "trustStore";

    public static final String PROCESS_DEHYDRATION = "processDehydration";

    public static final String PROCESS_MAX_COUNT = "maxCount";

    public static final String PROCESS_MAX_AGE = "maxAge";

    public static final String TRANSACTION_FACTORY = "transactionFactory";

    public static final String CLASS = "class";

    public static final String EVENT_LISTENERS = "eventListeners";

    public static final String LISTENER = "listener";

    public static final String MEX_INTERCEPTORS = "mexInterceptors";

    public static final String INTERCEPTOR = "interceptor";

    public static final String SCOPES = "scopes";

    public static final String ATOMIC_RETRY_COUNT = "atomicRetryCount";

    public static final String ATOMIC_RETRY_DELAY = "atomicRetryDelay";

    public static final String DAO_CONNECTION_FACTORY = "daoConectionFactory";

    public static final String EXTENSION_BUNDLES = "extensionBundles";

    public static final String EXTENSION = "extension";

    public static final String RUNTIME = "runtime";

    public static final String VALIDATOR = "validator";

    public static final String OPENJPA = "openJPA";

    public static final String PROPERTY = "property";

    public static final String LOCATION = "location";

    public static final String EXTENSION_RUNTIMES = "runtimes";

    public static final String EXTENSION_VALIDATORS = "validators";

    public static final String CONFIGURED_USING_BPEL_PKG_CONFIG_FILES = "confgiuredUsingBpelPkgFiles";

    public static final String WSDL_4_J_DEFINITION = "wsdl4jDefinition";

    public static final Integer ITEMS_PER_PAGE = 10;

    public static final String ENABLE_ADDRESSING = "enableAddressing";

    /* Deployer Constants */

    public static final String BPEL_TYPE = "bpel/workflow";
    public static final String BPEL_DIR = "bpel";

    /* Logging params*/

    public static final String LOGGER_MESSAGE_TRACE = "org.wso2.carbon.bpel.message-trace";
    public static final String LOGGER_DEPLOYMENT = "org.wso2.carbon.bpel.deployment";

    /* Clustering params */
    public static final String PARAM_PARENT_PROCESS_STORE = "bpel.process-store";

    /* BPEL repo directory */
    public static final String BPEL_REPO_DIRECTORY = "bpel";

    public static final String BPEL_PACKAGE_EXTENSION = "zip";
    public static final String BPEL_FILE_EXTENSION = ".bpel";
    public static final String BPEL_COMPILED_FILE_EXTENSION = ".cbp";

    public static final String BPEL_PACKAGE_TEMP_DIRECTORY = File.separator + "tmp" +
            File.separator + "bpeluploads";

    /* Constants for process store */
    public static final String REG_PATH_OF_BPEL_PACKAGES =
            RegistryConstants.PATH_SEPARATOR + "bpel" + RegistryConstants.PATH_SEPARATOR +
                    "packages" + RegistryConstants.PATH_SEPARATOR;
    public static final String BPEL_PACKAGE_VERSIONS =
            RegistryConstants.PATH_SEPARATOR +  "versions" + RegistryConstants.PATH_SEPARATOR;

    /* Commented following constant and introduced repository archives based on package names*/

    /* public static final String BPEL_PACKAGE_LATEST_ARCHIVE =
            RegistryConstants.PATH_SEPARATOR + "bpelArchive"; */

    public static final java.lang.String PATH_SEPARATOR = "/";
    public static final String BPEL_PACKAGE_PROP_LATEST_CHECKSUM = "bpel.package.latest.checksum";
    public static final String BPEL_PACKAGE_PROP_LATEST_CHECKSUM_DEPRECATED = "bpel.package.last.modified.time";
    public static final String BPEL_PACKAGE_PROP_STATUS = "bpel.package.status";
    public static final String BPEL_PACKAGE_PROP_LATEST_VERSION = "bpel.package.latest.version";
    public static final String BPEL_PACKAGE_PROP_DEPLOYMENT_ERROR_LOG = "bpel.package.error.log";
    public static final String BPEL_PACKAGE_PROP_DEPLOYMENT_STACK_TRACE = "bpel.package.error.stack.trace";

    public static final String STATUS_DEPLOYED = "DEPLOYED";
    public static final String STATUS_UPDATED = "UPDATED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_UNDEPLOYED = "UNDEPLOYED";

    /* ODE properties */
    public static final String ODE_ACQUIRE_TRANSACTION_LOCKS = "ode.acquireTransactionLocks";
    public static final String ODE_MEX_TIMEOUT = "mex.timeout";

    /* OpenJPA properties */
    public static final String OPENJPA_FLUSH_BEFORE_QUERIES = "openjpa.FlushBeforeQueries";

    /* BPS property prefix */
    public static final String BPS_PROPERTY_PREFIX = "wso2-bps.";

    public static final String BPEL_CONFIGURATION_FILE_NAME = "bps.xml";
    public static final String BPEL_DEPLOYER_NAME = "BPELDeployer";

    public static final String PROP_DB_EXTERNAL_JNDI_CTX_FAC = "ds.lookup.jndi.context.factory";
    public static final String PROP_DB_EXTERNAL_JNDI_PROVIDER_URL = "ds.lookup.jndi.provider.url";

    public static final String ODE_DETAILS_EXT_CLEAN_UP_INFO = "cleanupInfo";
    public static final String ODE_DETAILS_EXT_TRANSACTION_SIZE = "transactionSize";

    public static final int DEFAULT_TIMEOUT = 120000;

    /*  added to set updated properties of a package*/
    public static final String BPEL_INSTANCE_CLEANUP_FAILURE =  "bpel.instance.cleanup.failure: ";
    public static final String BPEL_INSTANCE_CLEANUP_SUCCESS =  "bpel.instance.cleanup.success:";
    public static final String BPEL_PROCESS_EVENT_GENERATE   =  "bpel.process.event.generate: ";
    public static final String BPEL_PROCESS_EVENTS           =  "bpel.process.events: ";
    public static final String BPEL_PROCESS_INMEMORY         =  "bpel.process.inmemory: ";
    public static final String BPEL_PROCESS_STATE            =  "bpel.process.state: ";
    public static final String BPEL_PROCESS_SCOPE_EVENT = "bpel.process.scope.event:";

    public static final String PORTS_OFFSET = "Ports.Offset";

    public static final String DAFAULT_BPEL_CLIENT = "AnonymousBPELClient";

    public static final String BAM_SERVER_PROFILE_NS = "http://wso2.org/bam/2.0";

     public static final String MESSAGE_TRACE = "org.wso2.carbon.bpel.messagetrace";

    public static final String MESSAGE_RECEIVER_INVOKE_ON_SEPARATE_THREAD =
            "messageReceiver.invokeOnSeparateThread";

    public static final String WS_ADDRESSING_NS2 = "http://www.w3.org/2006/05/addressing/wsdl";
    public static final String WS_ADDRESSING_NS3 = "http://www.w3.org/2006/02/addressing/wsdl";
    public static final String WS_ADDRESSING_NS4 = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
}

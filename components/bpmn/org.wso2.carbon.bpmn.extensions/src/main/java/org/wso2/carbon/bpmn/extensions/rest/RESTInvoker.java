/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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
 */
package org.wso2.carbon.bpmn.extensions.rest;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Utility class for invoking HTTP endpoints.
 */
public class RESTInvoker {

    private static final Log log = LogFactory.getLog(RESTInvoker.class);

    private int maxTotalConnections;
    private int maxTotalConnectionsPerRoute;
    private int connectionTimeout;

    private CloseableHttpClient client = null;
    private PoolingHttpClientConnectionManager connectionManager = null;

    {
        maxTotalConnectionsPerRoute = RESTConstants.MAX_TOTAL_CONNECTIONS_PER_ROUTE;
        maxTotalConnections = RESTConstants.MAX_TOTAL_CONNECTIONS;
        connectionTimeout = RESTConstants.CONNECTION_TIMEOUT;
    }

    public RESTInvoker(){
        configureHttpClient();
    }

    private void parseConfiguration() {
        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        String activitiConfigPath = carbonConfigDirPath + File.separator +
                                        BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
        File configFile = new File(activitiConfigPath);

        try {
            String configContent = FileUtils.readFileToString(configFile);
            OMElement configElement = AXIOMUtil.stringToOM(configContent);
            Iterator beans = configElement.getChildrenWithName(
                    new QName("http://www.springframework.org/schema/beans", "bean"));

            while (beans.hasNext()) {
                OMElement bean = (OMElement) beans.next();
                String beanId = bean.getAttributeValue(new QName(null, "id"));
                if (beanId.equals(RESTConstants.REST_CLIENT_CONFIG_ELEMENT)) {
                    Iterator beanProps = bean.getChildrenWithName(
                            new QName("http://www.springframework.org/schema/beans", "property"));

                    while (beanProps.hasNext()) {
                        OMElement beanProp = (OMElement) beanProps.next();
                        String beanName = beanProp.getAttributeValue(new QName(null, "name"));
                        if (RESTConstants.REST_CLIENT_MAX_TOTAL_CONNECTIONS.equals(beanName)) {

                            String value = beanProp.getAttributeValue(new QName(null, "value"));
                            maxTotalConnections = Integer.parseInt(value);

                            if(log.isDebugEnabled()) {
                                log.debug("Max total http connections " + maxTotalConnections);
                            }

                        } else if (RESTConstants.REST_CLIENT_MAX_CONNECTIONS_PER_ROUTE.equals(beanName)) {
                            String value = beanProp.getAttributeValue(new QName(null, "value"));
                            maxTotalConnectionsPerRoute = Integer.parseInt(value);

                            if(log.isDebugEnabled()) {
                                log.debug("Max total client connections per route " + maxTotalConnectionsPerRoute);
                            }
                        } else if(RESTConstants.REST_CLEINT_CONNECTION_TIMEOUT.equals(beanName)) {
                            String value = beanProp.getAttributeValue(new QName(null, "value"));
                            connectionTimeout = Integer.parseInt(value);
                        }
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            log.error("Error in processing http connection settings, using default settings" , e);
        }


    }

    private void configureHttpClient() {

        parseConfiguration();

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(10000)
                .build();
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Charset.defaultCharset())
                .setMessageConstraints(messageConstraints)
                .build();

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                .setConnectTimeout(connectionTimeout)
                .build();

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxTotalConnectionsPerRoute);
        connectionManager.setMaxTotal(maxTotalConnections);
        client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        if (log.isDebugEnabled()) {
            log.debug("BPMN REST client initialized with" +
                    "maxTotalConnection = " + maxTotalConnections +
                    "maxConnectionsPerRoute = " + maxTotalConnectionsPerRoute +
                    "connectionTimeout = " + connectionTimeout);
        }
    }

    public void closeHttpClient() {
        try {
            client.close();
            connectionManager.close();
        } catch (IOException e) {
            // Ignore
            log.error("Error shutting down http client");
        }
    }

    private HttpClientContext getHttpClientContextWithCredentials(String username, String password) {

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,
                        AuthScope.ANY_PORT, AuthScope.ANY_REALM, "basic"),
                new UsernamePasswordCredentials(username, password));

        HttpClientContext httpClientContext = HttpClientContext.create();
        httpClientContext.setCredentialsProvider(credentialsProvider);
        return httpClientContext;
    }

    private void processHeaderList(HttpRequestBase request, String headerList[]) {
        if (headerList != null) {
            for (String header : headerList) {
                String pair[] = header.split(":");
                if (pair.length == 1) {
                    request.addHeader(pair[0], "");
                } else {
                    request.addHeader(pair[0], pair[1]);
                }
            }
        }
    }

    /**
     * Invokes the http GET method
     * @param uri        endpoint/service url
     * @param headerList header list
     * @param username   username for authentication
     * @param password   password for authentication
     * @return response string of the GET request (can be the response body or the response status code)
     * @throws Exception
     */
    public String invokeGET(URI uri, String headerList[], String username, String password) throws IOException {

        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        String output = null;
        try {
            httpGet = new HttpGet(uri);
            processHeaderList(httpGet, headerList);
            response = client.execute(httpGet, getHttpClientContextWithCredentials(username , password));
            output = IOUtils.toString(response.getEntity().getContent());
            if (log.isTraceEnabled()) {
                log.trace("Invoked GET " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
            }
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return output;
    }

    /**
     * Invokes the http POST method
     * @param uri        endpoint/service url
     * @param headerList header list
     * @param username   username for authentication
     * @param password   password for authentication
     * @param payload    payload body passed
     * @return response string of the POST request (can be the response body or the response status code)
     * @throws Exception
     */
    public String invokePOST(URI uri, String headerList[], String username, String password, String payload) throws IOException {

        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        String output = "";
        try {
            httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(payload));
            processHeaderList(httpPost, headerList);
            response = client.execute(httpPost , getHttpClientContextWithCredentials(username, password));
            output = IOUtils.toString(response.getEntity().getContent());
            if (log.isTraceEnabled()) {
                log.trace("Invoked POST " + uri.toString() + " - Input payload: " + payload + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());

        } finally {
                if (response != null) {
                    IOUtils.closeQuietly(response);
                }
                if (httpPost != null) {
                    httpPost.releaseConnection();
                }
        }
        return output;
    }

    /**
     * Invokes the http PUT method
     *
     * @param uri        endpoint/service url
     * @param headerList header list
     * @param username   username for authentication
     * @param password   password for authentication
     * @param payload    payload body passed
     * @return response string of the PUT request (can be the response body or the response status code)
     * @throws Exception
     */
    public String invokePUT(URI uri, String headerList[], String username, String password, String payload) throws IOException {

        HttpPut httpPut = null;
        CloseableHttpResponse response = null;
        String output = "";
        try {
            httpPut = new HttpPut(uri);
            httpPut.setEntity(new StringEntity(payload));
            processHeaderList(httpPut, headerList);
            response = client.execute(httpPut, getHttpClientContextWithCredentials(username , password));
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201 ||
                    response.getStatusLine().getStatusCode() == 202) {

                output = IOUtils.toString(response.getEntity().getContent());
            } else {
                output = String.valueOf(response.getStatusLine().getStatusCode());
            }
            if (log.isTraceEnabled()) {
                log.trace("Invoked PUT " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());

        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
            }
            if (httpPut != null) {
                httpPut.releaseConnection();
            }
        }
        return output;
    }

    /**
     * Invokes the http DELETE method
     *
     * @param uri        endpoint/service url
     * @param headerList header list
     * @param username   username for authentication
     * @param password   password for authentication
     * @return response string of the DELETE (can be the response status code or the response body)
     * @throws Exception
     */
    public String invokeDELETE(URI uri, String headerList[], String username, String password) throws IOException {

        HttpDelete httpDelete = null;
        CloseableHttpResponse response = null;
        String output = "";
        try {
            httpDelete = new HttpDelete(uri);
            processHeaderList(httpDelete, headerList);
            response = client.execute(httpDelete, getHttpClientContextWithCredentials(username , password));
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 202) {
                output = IOUtils.toString(response.getEntity().getContent());
            } else {
                output = String.valueOf(response.getStatusLine().getStatusCode());
            }
            if (log.isTraceEnabled()) {
                log.trace("Invoked DELETE " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());

        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
            }
            if (httpDelete != null) {
                httpDelete.releaseConnection();
            }
        }
        return output;
    }
}

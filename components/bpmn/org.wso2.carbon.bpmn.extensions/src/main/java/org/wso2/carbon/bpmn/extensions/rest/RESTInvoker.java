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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Iterator;

/**
 * Utility class for invoking HTTP endpoints.
 */
public class RESTInvoker {

    private static final Log log = LogFactory.getLog(RESTInvoker.class);

    private final CloseableHttpClient client;

    public RESTInvoker() throws IOException, XMLStreamException {

        int maxTotal = 100;
        int maxTotalPerRoute = 100;

        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        String activitiConfigPath = carbonConfigDirPath + File.separator + BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
        File configFile = new File(activitiConfigPath);
        String configContent = FileUtils.readFileToString(configFile);
        OMElement configElement = AXIOMUtil.stringToOM(configContent);
        Iterator beans = configElement.getChildrenWithName(new QName("http://www.springframework.org/schema/beans", "bean"));
        while (beans.hasNext()) {
            OMElement bean = (OMElement) beans.next();
            String beanId = bean.getAttributeValue(new QName(null, "id"));
            if (beanId.equals(BPMNConstants.REST_CLIENT_CONFIG_ELEMENT)) {
                Iterator beanProps = bean.getChildrenWithName(new QName("http://www.springframework.org/schema/beans", "property"));
                while (beanProps.hasNext()) {
                    OMElement beanProp = (OMElement) beanProps.next();
                    if (beanProp.getAttributeValue(new QName(null, "name")).equals(BPMNConstants.REST_CLIENT_MAX_TOTAL_CONNECTIONS)) {
                        String value = beanProp.getAttributeValue(new QName(null, "value"));
                        maxTotal = Integer.parseInt(value);
                    } else if (beanProp.getAttributeValue(new QName(null, "name")).equals(BPMNConstants.REST_CLIENT_MAX_CONNECTIONS_PER_ROUTE)) {
                        String value = beanProp.getAttributeValue(new QName(null, "value"));
                        maxTotalPerRoute = Integer.parseInt(value);
                    }
                }
            }
        }

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(maxTotalPerRoute);
        cm.setMaxTotal(maxTotal);
        client = HttpClients.custom().setConnectionManager(cm).build();

        if (log.isDebugEnabled()) {
            log.debug("BPMN REST client initialized with maxTotalConnection = " + maxTotal + " and maxConnectionsPerRoute = " + maxTotalPerRoute);
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
    public String invokeGET(URI uri, String headerList[], String username, String password) throws Exception {

        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        String output = "";
        try {
            httpGet = new HttpGet(uri);
            if (username != null && password != null) {
                String combinedCredentials = username + ":" + password;
                byte[] encodedCredentials = Base64.encodeBase64(combinedCredentials.getBytes());
                String credentials = new String(encodedCredentials);
                httpGet.addHeader("Authorization", "Basic " + credentials);
            }
            if (headerList != null) {
                for (String header : headerList) {
                    String pair[] = header.split(":");
                    if (pair.length == 1) {
                        httpGet.addHeader(pair[0], "");
                    } else {
                        httpGet.addHeader(pair[0], pair[1]);
                    }
                }
            }
            response = client.execute(httpGet);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            output = result.toString();
            if (log.isTraceEnabled()) {
                log.trace("Invoked GET " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());

        } finally {
            if (response != null) {
                response.close();
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
    public String invokePOST(URI uri, String headerList[], String username, String password, String payload) throws Exception {

        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        String output = "";
        try {
            httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(payload));
            if (username != null && password != null) {
                String combinedCredentials = username + ":" + password;
                String encodedCredentials = new String(Base64.encodeBase64(combinedCredentials.getBytes()));
                httpPost.addHeader("Authorization", "Basic " + encodedCredentials);
            }
            if (headerList != null) {
                for (String header : headerList) {
                    String pair[] = header.split(":");
                    if (pair.length == 1) {
                        httpPost.addHeader(pair[0], "");
                    } else {
                        httpPost.addHeader(pair[0], pair[1]);
                    }
                }
            }
            response = client.execute(httpPost);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            output = result.toString();
            if (log.isTraceEnabled()) {
                log.trace("Invoked POST " + uri.toString() + " - Input payload: " + payload + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());

        } finally {
            if (response != null) {
                response.close();
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
    public String invokePUT(URI uri, String headerList[], String username, String password, String payload) throws Exception {

        HttpPut httpPut = null;
        CloseableHttpResponse response = null;
        String output = "";
        try {
            httpPut = new HttpPut(uri);
            httpPut.setEntity(new StringEntity(payload));
            if (username != null && password != null) {
                String combinedCredentials = username + ":" + password;
                byte[] encodedCredentials = Base64.encodeBase64(combinedCredentials.getBytes());
                String credentials = new String(encodedCredentials);
                httpPut.addHeader("Authorization", "Basic " + credentials);
            }
            if (headerList != null) {
                for (String header : headerList) {
                    String pair[] = header.split(":");
                    if (pair.length == 1) {
                        httpPut.addHeader(pair[0], "");
                    } else {
                        httpPut.addHeader(pair[0], pair[1]);
                    }
                }
            }
            response = client.execute(httpPut);
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201 ||
                    response.getStatusLine().getStatusCode() == 202) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                output = result.toString();
            } else {
                output = String.valueOf(response.getStatusLine().getStatusCode());
            }

            if (log.isTraceEnabled()) {
                log.trace("Invoked PUT " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());

        } finally {
            if (response != null) {
                response.close();
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
    public String invokeDELETE(URI uri, String headerList[], String username, String password) throws Exception {

        HttpDelete httpDelete = null;
        CloseableHttpResponse response = null;
        String output = "";
        try {
            httpDelete = new HttpDelete(uri);
            if (username != null && password != null) {
                String combinedCredentials = username + ":" + password;
                byte[] encodedCredentials = Base64.encodeBase64(combinedCredentials.getBytes());
                String credentials = new String(encodedCredentials);
                httpDelete.addHeader("Authorization", "Basic " + credentials);
            }
            if (headerList != null) {
                for (String header : headerList) {
                    String pair[] = header.split(":");
                    if (pair.length == 1) {
                        httpDelete.addHeader(pair[0], "");
                    } else {
                        httpDelete.addHeader(pair[0], pair[1]);
                    }
                }
            }
            response = client.execute(httpDelete);
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 202) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                output = result.toString();
            } else {
                output = String.valueOf(response.getStatusLine().getStatusCode());
            }
            if (log.isTraceEnabled()) {
                log.trace("Invoked DELETE " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());

        } finally {
            if (response != null) {
                response.close();
            }
            if (httpDelete != null) {
                httpDelete.releaseConnection();
            }
        }
        return output;
    }
}

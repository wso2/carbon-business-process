///*
// * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.wso2.carbon.bpmn.extensions;
//
//import org.apache.axiom.om.OMElement;
//import org.apache.axiom.om.util.AXIOMUtil;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.io.FileUtils;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.util.EntityUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.wso2.carbon.bpmn.core.BPMNConstants;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.URI;
//import java.nio.charset.Charset;
//import java.util.Arrays;
//import java.util.Iterator;
//import javax.xml.namespace.QName;
//import javax.xml.stream.XMLStreamException;
//
///**
// * Utility class for invoking HTTP endpoints.
// */
//public class RESTInvoker {
//
//    private static final Logger log = LoggerFactory.getLogger(RESTInvoker.class);
//
//    private final CloseableHttpClient client;
//
//    public RESTInvoker() throws IOException, XMLStreamException {
//
////        int maxTotal = 100;
////        int maxTotalPerRoute = 100;
////
////        String activitiConfigPath = org.wso2.carbon.kernel.utils.Utils.getCarbonConfigHome()
////                .resolve(BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME).toString();
////        File configFile = new File(activitiConfigPath);
////        String configContent = FileUtils.readFileToString(configFile);
////        OMElement configElement = AXIOMUtil.stringToOM(configContent);
////        Iterator beans = configElement.getChildrenWithName(
////                new QName("http://www.springframework.org/schema/beans", "bean"));
////        while (beans.hasNext()) {
////            OMElement bean = (OMElement) beans.next();
////            String beanId = bean.getAttributeValue(new QName(null, "id"));
////            if (beanId.equals(BPMNConstants.REST_CLIENT_CONFIG_ELEMENT)) {
////                Iterator beanProps = bean.getChildrenWithName(
////                        new QName("http://www.springframework.org/schema/beans", "property"));
////                while (beanProps.hasNext()) {
////                    OMElement beanProp = (OMElement) beanProps.next();
////                    if (beanProp.getAttributeValue(new QName(null, "name"))
////                            .equals(BPMNConstants.REST_CLIENT_MAX_TOTAL_CONNECTIONS)) {
////                        String value = beanProp.getAttributeValue(new QName(null, "value"));
////                        maxTotal = Integer.parseInt(value);
////                    } else if (beanProp.getAttributeValue(new QName(null, "name"))
////                            .equals(BPMNConstants.REST_CLIENT_MAX_CONNECTIONS_PER_ROUTE)) {
////                        String value = beanProp.getAttributeValue(new QName(null, "value"));
////                        maxTotalPerRoute = Integer.parseInt(value);
////                    }
////                }
////            }
////        }
////
////        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
////        cm.setDefaultMaxPerRoute(maxTotalPerRoute);
////        cm.setMaxTotal(maxTotal);
////        client = HttpClients.custom().setConnectionManager(cm).build();
////
////        if (log.isDebugEnabled()) {
////            log.debug("BPMN REST client initialized with maxTotalConnection = " + maxTotal +
////                    " and maxConnectionsPerRoute = " + maxTotalPerRoute);
////        }
//    }
//
//    public String invokeGET(URI uri, String headerList[], String username, String password)
//            throws Exception {
//
//        HttpGet httpGet = null;
//        CloseableHttpResponse response = null;
//        BufferedReader rd = null;
//        String output = "";
//        try {
//            httpGet = new HttpGet(uri);
//            if (username != null && password != null) {
//                String combinedCredentials = username + ":" + password;
//                byte[] encodedCredentials = Base64.encodeBase64(combinedCredentials.getBytes(Charset.defaultCharset
// ()));
//                httpGet.addHeader("Authorization", "Basic " + Arrays.toString(encodedCredentials));
//            }
//            if (headerList != null) {
//                for (String header : headerList) {
//                    String pair[] = header.split(":");
//                    if (pair.length == 1) {
//                        httpGet.addHeader(pair[0], "");
//                    } else {
//                        httpGet.addHeader(pair[0], pair[1]);
//                    }
//                }
//            }
//            response = client.execute(httpGet);
//            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset
// ()));
//            StringBuffer result = new StringBuffer();
//            String line = "";
//            while ((line = rd.readLine()) != null) {
//                result.append(line);
//            }
//            output = result.toString();
//            if (log.isTraceEnabled()) {
//                log.trace("Invoked GET " + uri.toString() + " - Response message: " + output);
//            }
//            EntityUtils.consume(response.getEntity());
//
//        } finally {
//            if (rd != null) {
//                rd.close();
//            }
//
//            if (response != null) {
//                response.close();
//            }
//
//            if (httpGet != null) {
//                httpGet.releaseConnection();
//            }
//        }
//        return output;
//    }
//
//    public String invokePOST(URI uri, String headerList[], String username, String password,
//                             String payload) throws Exception {
//
//        HttpPost httpPost = null;
//        CloseableHttpResponse response = null;
//        BufferedReader rd = null;
//        String output = "";
//        try {
//            httpPost = new HttpPost(uri);
//            httpPost.setEntity(new StringEntity(payload));
//            if (username != null && password != null) {
//                String combinedCredentials = username + ":" + password;
//                String encodedCredentials =
//                        new String(Base64.encodeBase64(combinedCredentials.getBytes(Charset.defaultCharset())),
//                                Charset.defaultCharset());
//                httpPost.addHeader("Authorization", "Basic " + encodedCredentials);
//            }
//            if (headerList != null) {
//                for (String header : headerList) {
//                    String pair[] = header.split(":");
//                    if (pair.length == 1) {
//                        httpPost.addHeader(pair[0], "");
//                    } else {
//                        httpPost.addHeader(pair[0], pair[1]);
//                    }
//                }
//            }
//            response = client.execute(httpPost);
//
//            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset
// ()));
//            StringBuffer result = new StringBuffer();
//            String line = "";
//            while ((line = rd.readLine()) != null) {
//                result.append(line);
//            }
//            output = result.toString();
//            if (log.isTraceEnabled()) {
//                log.trace("Invoked POST " + uri.toString() + " - Input payload: " + payload +
//                        " - Response message: " + output);
//            }
//            EntityUtils.consume(response.getEntity());
//
//        } finally {
//            if (rd != null) {
//                rd.close();
//            }
//
//            if (response != null) {
//                response.close();
//            }
//
//            if (httpPost != null) {
//                httpPost.releaseConnection();
//            }
//        }
//        return output;
//    }
//}

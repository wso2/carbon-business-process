/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.uuf.ui.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.uuf.ui.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Class to call BPMN backend
 */
public class BPMNExplorerServiceImpl implements BPMNExplorerService {
    private static final Logger log = LoggerFactory.getLogger(BPMNExplorerServiceImpl.class);

    private CloseableHttpClient client;
    private String baseUrl;

    /**
     * Initialize the HttpClient
     *
     * @param host
     * @param port
     */
    private void setHttpClient(String host, int port) {
        try {
            client = HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(
                            SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build()))
                    .build();
        } catch (NoSuchAlgorithmException | keyManagementException | KeyStoreException e) {
            log.error(Constants.DEFAULT_EX, e);
        }
        baseUrl = Constants.URL_SCHEMA + host + ":" + port;
    }

    /**
     * Return BPMN process details from deployments
     *
     * @param pagination
     * @param host
     * @param port
     * @param tenantId
     * @param username
     * @param password
     * @return JSONArray which contains process details for each BPMN deployment
     * @throws IOException
     */
    @Override
    public JSONArray getProcessDetails(String pagination, String host, int port, int tenantId, String username, String password)
            throws IOException {

        HttpGet httpGet = null;
        String line;
        JSONArray returnDataArray = new JSONArray();
        String cookie;
        int start = 0;

        setHttpClient(host, port);

        if ("".equals(pagination)) {
            start = 0;
        } else {
            int pageCount = Integer.parseInt(pagination.split("=")[1]);
            start = (pageCount - 1) * 10;
        }


        try (CloseableHttpResponse response = null;
             BufferedReader rd = null) {
            cookie = HTTPAuthenticate();
            httpGet = new HttpGet(
                    new URI(baseUrl + "/bpmn/repository/deployments?tenantId=" + tenantId + "&start=" + start));
            httpGet.addHeader(Constants.CONTENT_TYPE, Constants.JSON_CONTENT_TYPE);
            String authString = username + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpGet.addHeader(Constants.AUTHORIZATION, Constants.BASIC_AUTHORIZATION + new String(authEncBytes));

            response = client.execute(httpGet);
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            StringBuffer resultList = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                resultList.append(line);
            }
            JSONObject rsltListObj = new JSONObject(resultList.toString());

            // paginatePageCount for pagination
            int processCount = Integer.parseInt(rsltListObj.get("total").toString());
            int paginatePageCount = (int) Math.ceil((float) processCount / 10);

            JSONArray deploymentListData = rsltListObj.getJSONArray(Constants.DATA);
            String name;
            String deploymentId;
            if (processCount > 0) {
                for (int i = 0; i < deploymentListData.length(); i++) {
                    deploymentId = deploymentListData.getJSONObject(i).get(Constants.ID).toString();
                    name = deploymentListData.getJSONObject(i).get(Constants.NAME).toString();
                    JSONObject deploymentObj = new JSONObject();
                    deploymentObj.put(Constants.PACKAGE_NAME, name + "-" + deploymentId);
                    httpGet = new HttpGet(
                            new URI(baseUrl + "/bpmn/repository/process-definitions?deploymentId=" + deploymentId));
                    httpGet.addHeader(Constants.CONTENT_TYPE, Constants.JSON_CONTENT_TYPE);
                    String authString = username + ":" + password;
                    byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
                    httpGet.addHeader(Constants.AUTHORIZATION, Constants.BASIC_AUTHORIZATION + new String(authEncBytes));
                    response = client.execute(httpGet);
                    rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
                    StringBuffer result = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    JSONArray processArray = new JSONArray();
                    JSONArray dataArray = new JSONObject(result.toString()).getJSONArray(Constants.DATA);
                    //When one deployment has several processes
                    if (dataArray.length() >= 2) {
                        for (int j = 0; j < dataArray.length(); j++) {
                            JSONObject arrayJsonObj = dataArray.getJSONObject(j);
                            JSONObject newJsonObj = new JSONObject();
                            newJsonObj.put(Constants.PROCESS_NAME, arrayJsonObj.get(Constants.NAME));
                            newJsonObj.put(Constants.PROCESS_DEF_ID, arrayJsonObj.get(Constants.ID));
                            newJsonObj.put(Constants.VERSION, arrayJsonObj.get(Constants.VERSION));
                            newJsonObj.put(Constants.IMAGE_DATA,
                                    getBPMNProcessDiagram(arrayJsonObj.get(Constants.ID).toString(), cookie));
                            processArray.put(newJsonObj);
                        }
                        deploymentObj.put("multiProcesses", processArray);
                        returnDataArray.put(deploymentObj);

                    }
                    //When one deployment has only one process
                    else if (dataArray.length() == 1) {
                        JSONObject arrayJsonObj = dataArray.getJSONObject(0);
                        deploymentObj.put(Constants.PROCESS_NAME, arrayJsonObj.get(Constants.NAME));
                        deploymentObj.put(Constants.PROCESS_DEF_ID, arrayJsonObj.get(Constants.ID));
                        deploymentObj.put(Constants.VERSION, arrayJsonObj.get(Constants.VERSION));
                        deploymentObj.put(Constants.IMAGE_DATA,
                                getBPMNProcessDiagram(arrayJsonObj.get(Constants.ID).toString(), cookie));
                        JSONObject oneProcessObj = new JSONObject();
                        oneProcessObj.put("oneProcess", deploymentObj);
                        returnDataArray.put(oneProcessObj);
                    }
                }

                JSONArray pagiArray = new JSONArray();
                for (int i = 1; i <= paginatePageCount; i++) {
                    JSONObject pageObj = new JSONObject();
                    pageObj.put("pageNu", i);
                    pagiArray.put(pageObj);

                }
                JSONObject pagiObj = new JSONObject();
                pagiObj.put("paginationCount", pagiArray);
                returnDataArray.put(pagiObj);
            }

        } catch (JSONException | URISyntaxException e) {
            log.error(Constants.DEFAULT_EX, e);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return returnDataArray;
    }

    /**
     * Return session cookie from Authentication Admin Service
     */
    private String HTTPAuthenticate() throws IOException {
        HttpPost httpPost;
        String line;
        StringEntity stringEntity;
        String cookie = null;

        try (CloseableHttpResponse response = null;
             BufferedReader rd = null) {
            httpPost = new HttpPost(baseUrl + "/services/AuthenticationAdmin.AuthenticationAdminHttpsSoap11Endpoint/");
            httpPost.addHeader(Constants.SOAP_ACTION, Constants.SOAP_ACTION_LOGIN);
            httpPost.addHeader(Constants.CONTENT_TYPE, Constants.XML_CONTENT_TYPE);

            String payload =
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                            "xmlns:aut=\"http://authentication.services.core.carbon.wso2" +
                            ".org\"><soapenv:Header/><soapenv:Body><aut:login><aut:username>admin</aut:username" +
                            "><aut:password>admin</aut:password></aut:login></soapenv:Body></soapenv:Envelope>";

            stringEntity = new StringEntity(payload, Constants.UTF_8_ENCODING);
            stringEntity.setChunked(true);

            httpPost.setEntity(stringEntity);

            response = client.execute(httpPost);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            StringBuffer result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            int returnStart = result.indexOf("<ns:return>") + "<ns:return>".length();
            int returnEnd = result.indexOf("</ns:return>");
            String isLoginSuccess = result.substring(returnStart, returnEnd);

            if (isLoginSuccess.equals("true")) {
                //retrieve session cookie with BPS
                String str = response.getHeaders("Set-Cookie")[0].toString().split(";")[0];
                cookie = str.split(" ")[1];
            }
        } catch (ClientProtocolException e) {
            log.error(Constants.CLIENT_PROTOCOL_EX, e);
        }
        return cookie;
    }

    /**
     * Return BPMN process diagram data through BPMNDeploymentService
     *
     * @param processId
     * @param sessionId
     */
    private Object getBPMNProcessDiagram(String processId, String sessionId) throws IOException {
        HttpPost httpPost;
        String line;
        StringEntity stringEntity;
        String imageUrl = null;
        try (CloseableHttpResponse response = null;
             BufferedReader rd = null) {
            httpPost = new HttpPost(
                    baseUrl + "/services/BPMNDeploymentService.BPMNDeploymentServiceHttpsSoap11Endpoint/");
            httpPost.addHeader(Constants.COOKIE, sessionId);
            httpPost.addHeader(Constants.SOAP_ACTION, Constants.SOAP_ACTION_GET_PROCESS_DIAGRAM);
            httpPost.addHeader(Constants.CONTENT_TYPE, Constants.XML_CONTENT_TYPE);

            String imgPayload =
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                            "xmlns:ser=\"http://services.mgt.core.bpmn.carbon.wso2" +
                            ".org\"><soapenv:Header/><soapenv:Body><ser:getProcessDiagram><ser:processId>" +
                            processId + "</ser:processId></ser:getProcessDiagram></soapenv:Body></soapenv:Envelope>";
            stringEntity = new StringEntity(imgPayload, Constants.UTF_8_ENCODING);
            stringEntity.setChunked(true);

            httpPost.setEntity(stringEntity);
            response = client.execute(httpPost);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            StringBuffer imgResult = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                imgResult.append(line);
            }
            String imageData = imgResult.toString().split(":return")[1];
            imageUrl = "data:image/png;base64," + imageData.substring(1, imageData.length() - 4);

        } catch (ClientProtocolException e) {
            log.error(Constants.CLIENT_PROTOCOL_EX, e);
        }
        return imageUrl;
    }

    /**
     * Initialization of creating process instances
     *
     * @param processId
     * @param host
     * @param port
     * @param username
     * @param password
     * @return JSONObject which contains form data for each process definition
     * @throws JSONException
     * @throws IOException
     */
    @Override
    public JSONObject getFormData(String processId, String host, int port, String username, String password)
            throws JSONException, IOException {
        HttpGet httpGet = null;
        String line;
        JSONObject rsltObj;
        JSONObject rtrnObj = new JSONObject();

        setHttpClient(host, port);
        String processDefId = processId;

        try (CloseableHttpResponse response = null;
             BufferedReader rd = null) {
            httpGet = new HttpGet(baseUrl + "/bpmn/process-definition/" + processDefId + "/properties");
            httpGet.addHeader(Constants.CONTENT_TYPE, Constants.JSON_CONTENT_TYPE);
            String authString = username + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpGet.addHeader(Constants.AUTHORIZATION, Constants.BASIC_AUTHORIZATION + new String(authEncBytes));

            response = client.execute(httpGet);
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            StringBuffer rsltForm = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                rsltForm.append(line);
            }

            rsltObj = new JSONObject(rsltForm.toString());
            // When error occurs due to invalid data in the process creation
            if (!rsltObj.has(Constants.DATA) && rsltObj.has(Constants.ERROR_MSG)) {
                rtrnObj.put(Constants.ERROR_MESSAGE, rsltObj.get(Constants.ERROR_MSG));
            } else {
                JSONArray formData = new JSONObject(rsltForm.toString()).getJSONArray(Constants.DATA);
                // When form data is not required for process creation
                if (formData.length() <= 0) {
                    rtrnObj = createProcessInstance(processDefId, username, password);
                }
                // When it is required to fill a form to create process instance
                else {
                    rtrnObj.put("processDefId", processDefId);
                    rtrnObj.put("formData", formData);
                }
            }
        } catch (ClientProtocolException e) {
            log.error(Constants.CLIENT_PROTOCOL_EX, e);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return rtrnObj;
    }

    /**
     * Create process instance without form data
     *
     * @param processDefId
     * @param username
     * @param password
     * @throws JSONException
     * @throws IOException
     */
    private JSONObject createProcessInstance(String processDefId, String username, String password)
            throws JSONException, IOException {
        HttpPost httpPost = null;
        String line = "";
        StringEntity stringEntity;
        JSONObject rsltObj;
        JSONObject rtrnObj = new JSONObject();

        try (CloseableHttpResponse response = null;
             BufferedReader rd = null) {
            httpPost = new HttpPost(
                    baseUrl + "/bpmn/runtime/process-instances");
            httpPost.addHeader(Constants.CONTENT_TYPE, Constants.JSON_CONTENT_TYPE);
            String authString = username + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpPost.addHeader(Constants.AUTHORIZATION, Constants.BASIC_AUTHORIZATION + new String(authEncBytes));

            JSONObject bodyObj = new JSONObject();
            bodyObj.put(Constants.PROC_DEFINITION_ID, processDefId);
            stringEntity = new StringEntity(bodyObj.toString());
            httpPost.setEntity(stringEntity);
            response = client.execute(httpPost);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            StringBuffer result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            rsltObj = new JSONObject(result.toString());
            if (rsltObj.has(Constants.ID)) {
                JSONObject processInstanceObj = new JSONObject();
                processInstanceObj.put(Constants.PROCESS_DEF_ID, rsltObj.get(Constants.PROC_DEFINITION_ID));
                processInstanceObj.put(Constants.PROCESS_INSTANCE_ID, rsltObj.get(Constants.ID));
                rtrnObj.put(Constants.PROC_INSTANCE_DATA, processInstanceObj);
            } else if (rsltObj.has(Constants.ERROR_MSG)) {
                rtrnObj.put(Constants.ERROR_MESSAGE, rsltObj.get(Constants.ERROR_MSG));
            }
        } catch (ClientProtocolException e) {
            log.error(Constants.CLIENT_PROTOCOL_EX, e);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return rtrnObj;
    }

    /**
     * Create process instance using form data
     *
     * @param frmData
     * @param host
     * @param port
     * @param username
     * @param password
     * @return JSONObject which contains details of process instance
     * @throws JSONException
     * @throws IOException
     */
    @Override
    public JSONObject createProcessInstanceWithData(String frmData, String host, int port, String username, String password)
            throws JSONException, IOException {
        HttpPost httpPost = null;
        String line;
        StringEntity stringEntity;
        JSONObject rsltObj;
        JSONObject rtrnObj = new JSONObject();

        //Decode the query string for form data and process definition id
        String decodedStr = URLDecoder.decode(frmData, Constants.UTF_8_ENCODING);
        String splitFirst[] = decodedStr.split("&");
        JSONArray varArray = new JSONArray();
        for (int i = 0; i < (splitFirst.length - 1); i++) {
            String splitScnd[] = splitFirst[i].split("=");
            //If there are values for form fields
            if (splitScnd.length == 2) {
                JSONObject varObj = new JSONObject();
                varObj.put(Constants.NAME, splitScnd[0]);
                varObj.put("value", splitScnd[1]);
                varArray.put(varObj);
            }
        }

        try (CloseableHttpResponse response = null;
             BufferedReader rd = null) {
            httpPost = new HttpPost(
                    baseUrl + "/bpmn/runtime/process-instances");
            httpPost.addHeader(Constants.CONTENT_TYPE, Constants.JSON_CONTENT_TYPE);
            String authString = username + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpPost.addHeader(Constants.AUTHORIZATION, Constants.BASIC_AUTHORIZATION + new String(authEncBytes));

            JSONObject bodyObj = new JSONObject();
            bodyObj.put(Constants.PROC_DEFINITION_ID, splitFirst[splitFirst.length - 1].split("=")[1]);
            bodyObj.put("variables", varArray);
            stringEntity = new StringEntity(bodyObj.toString());
            httpPost.setEntity(stringEntity);
            response = client.execute(httpPost);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            StringBuffer result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            rsltObj = new JSONObject(result.toString());
            if (rsltObj.has(Constants.ID)) {
                JSONObject processInstanceObj = new JSONObject();
                processInstanceObj.put(Constants.PROCESS_DEF_ID, rsltObj.get(Constants.PROC_DEFINITION_ID));
                processInstanceObj.put(Constants.PROCESS_INSTANCE_ID, rsltObj.get(Constants.ID));
                rtrnObj.put(Constants.PROC_INSTANCE_DATA, processInstanceObj);
            } else if (rsltObj.has(Constants.ERROR_MSG)) {
                rtrnObj.put(Constants.ERROR_MESSAGE, rsltObj.get(Constants.ERROR_MSG));
            }
        } catch (ClientProtocolException e) {
            log.error(Constants.CLIENT_PROTOCOL_EX, e);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return rtrnObj;
    }
}



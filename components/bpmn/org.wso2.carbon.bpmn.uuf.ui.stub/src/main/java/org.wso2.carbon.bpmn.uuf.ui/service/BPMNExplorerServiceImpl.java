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

package org.wso2.carbon.bpmn.uuf.ui;

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
    private String userName;
    private String passWord;

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
        } catch (NoSuchAlgorithmException | eyManagementException | KeyStoreException e) {
            log.error("Exception : ", e);
        }
        baseUrl = "https://" + host + ":" + port;
        //Need to change this part after introducing user managemnet module
        userName = "admin";
        passWord = "admin";
    }

    /**
     * Return BPMN process details from deployments
     *
     * @param pagination
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    @Override
    public JSONArray getProcessDetails(String pagination, String host, int port, int tenantId) throws IOException {

        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        BufferedReader rd = null;
        String line;
        JSONArray returnDataArray = new JSONArray();
        String cookie;
        int start = 0;

        setHttpClient(host, port);

        if (pagination.equals("")) {
            start = 0;
        } else {
            int pageCount = Integer.parseInt(pagination.split("=")[1]);
            start = (pageCount - 1) * 10;
        }


        try {
            cookie = HTTPAuthenticate();
            httpGet = new HttpGet(
                    new URI(baseUrl + "/bpmn/repository/deployments?tenantId=" + tenantId + "&start=" + start));
            httpGet.addHeader("Content-type", "application/json");
            String authString = userName + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpGet.addHeader("Authorization", "Basic" + new String(authEncBytes));

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

            JSONArray deploymentListData = rsltListObj.getJSONArray("data");
            String name;
            String deploymentId;
            if (processCount > 0) {
                for (int i = 0; i < deploymentListData.length(); i++) {
                    deploymentId = deploymentListData.getJSONObject(i).get("id").toString();
                    name = deploymentListData.getJSONObject(i).get("name").toString();
                    JSONObject deploymentObj = new JSONObject();
                    deploymentObj.put("package-name", name + "-" + deploymentId);
                    httpGet = new HttpGet(
                            new URI(baseUrl + "/bpmn/repository/process-definitions?deploymentId=" + deploymentId));
                    httpGet.addHeader("Content-type", "application/json");
                    String authString = userName + ":" + password;
                    byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
                    httpGet.addHeader("Authorization", "Basic" + new String(authEncBytes));
                    response = client.execute(httpGet);
                    rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
                    StringBuffer result = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    JSONArray processArray = new JSONArray();
                    JSONArray dataArray = new JSONObject(result.toString()).getJSONArray("data");
                    //When one deployment has several processes
                    if (dataArray.length() >= 2) {
                        for (int j = 0; j < dataArray.length(); j++) {
                            JSONObject arrayJsonObj = dataArray.getJSONObject(j);
                            JSONObject newJsonObj = new JSONObject();
                            newJsonObj.put("process-name", arrayJsonObj.get("name"));
                            newJsonObj.put("process-def-id", arrayJsonObj.get("id"));
                            newJsonObj.put("version", arrayJsonObj.get("version"));
                            newJsonObj.put("image-data",
                                           getBPMNProcessDiagram(arrayJsonObj.get("id").toString(), cookie));
                            processArray.put(newJsonObj);
                        }
                        deploymentObj.put("multiProcesses", processArray);
                        returnDataArray.put(deploymentObj);

                    }
                    //When one deployment has only one process
                    else if (dataArray.length() == 1) {
                        JSONObject arrayJsonObj = dataArray.getJSONObject(0);
                        deploymentObj.put("process-name", arrayJsonObj.get("name"));
                        deploymentObj.put("process-def-id", arrayJsonObj.get("id"));
                        deploymentObj.put("version", arrayJsonObj.get("version"));
                        deploymentObj.put("image-data",
                                          getBPMNProcessDiagram(arrayJsonObj.get("id").toString(), cookie));
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

        } catch (IOException | JSONException | URISyntaxException e) {
            log.error("Exception : ", e);
        } finally {
            if (rd != null) {
                rd.close();
            }

            if (response != null) {
                response.close();
            }

            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return returnDataArray;
    }

    /**
     * Return session cookie from Authentication Admin Service
     */
    private String HTTPAuthenticate() {
        HttpPost httpPost;
        CloseableHttpResponse response;
        BufferedReader rd;
        String line;
        StringEntity stringEntity;
        String cookie = null;

        try {
            httpPost = new HttpPost(baseUrl + "/services/AuthenticationAdmin.AuthenticationAdminHttpsSoap11Endpoint/");
            httpPost.addHeader("SOAPAction", "urn:login");
            httpPost.addHeader("Content-Type", "text/xml");

            String payload =
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                            "xmlns:aut=\"http://authentication.services.core.carbon.wso2" +
                            ".org\"><soapenv:Header/><soapenv:Body><aut:login><aut:username>admin</aut:username" +
                            "><aut:password>admin</aut:password></aut:login></soapenv:Body></soapenv:Envelope>";

            stringEntity = new StringEntity(payload, "UTF-8");
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
        } catch (ClientProtocolException | IOException e) {
            log.error("Exception : ", e);
        }
        return cookie;
    }

    /**
     * Return BPMN process diagram data through BPMNDeploymentService
     *
     * @param processId
     * @param sessionId
     */
    private Object getBPMNProcessDiagram(String processId, String sessionId) {
        HttpPost httpPost;
        CloseableHttpResponse response;
        BufferedReader rd;
        String line;
        StringEntity stringEntity;
        String imageUrl = null;
        try {

            httpPost = new HttpPost(
                    baseUrl + "/services/BPMNDeploymentService.BPMNDeploymentServiceHttpsSoap11Endpoint/");
            httpPost.addHeader("COOKIE", sessionId);
            httpPost.addHeader("SOAPAction", "urn:getProcessDiagram");
            httpPost.addHeader("Content-Type", "text/xml");

            String imgPayload =
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                            "xmlns:ser=\"http://services.mgt.core.bpmn.carbon.wso2" +
                            ".org\"><soapenv:Header/><soapenv:Body><ser:getProcessDiagram><ser:processId>" +
                            processId + "</ser:processId></ser:getProcessDiagram></soapenv:Body></soapenv:Envelope>";
            stringEntity = new StringEntity(imgPayload, "UTF-8");
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

        } catch (ClientProtocolException | IOException e) {
            log.error("Exception : ", e);
        }
        return imageUrl;
    }

    /**
     * Initialization of creating process instances
     *
     * @param processId
     * @param host
     * @param port
     */
    @Override
    public JSONObject getFormData(String processId, String host, int port) throws JSONException, IOException {
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        BufferedReader rd = null;
        String line;
        JSONObject rsltObj;
        JSONObject rtrnObj = new JSONObject();

        setHttpClient(host, port);
        String processDefId = processId;

        try {
            httpGet = new HttpGet(baseUrl + "/bpmn/process-definition/" + processDefId + "/properties");
            httpGet.addHeader("Content-type", "application/json");
            String authString = userName + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpGet.addHeader("Authorization", "Basic" + new String(authEncBytes));

            response = client.execute(httpGet);
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            StringBuffer rsltForm = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                rsltForm.append(line);
            }

            rsltObj = new JSONObject(rsltForm.toString());
            // When error occurs due to invalid data in the process creation
            if (!rsltObj.has("data") && rsltObj.has("errorMessage")) {
                rtrnObj.put("error-message", rsltObj.get("errorMessage"));
            } else {
                JSONArray formData = new JSONObject(rsltForm.toString()).getJSONArray("data");
                // When form data is not required for process creation
                if (formData.length() <= 0) {
                    rtrnObj = createProcessInstance(processDefId);
                }
                // When it is required to fill a form to create process instance
                else {
                    rtrnObj.put("processDefId", processDefId);
                    rtrnObj.put("formData", formData);
                }
            }
        } catch (ClientProtocolException | IOException e) {
            log.error("Exception : ", e);
        } finally {
            if (rd != null) {
                rd.close();
            }

            if (response != null) {
                response.close();
            }

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
     */
    private JSONObject createProcessInstance(String processDefId) throws JSONException, IOException {
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        BufferedReader rd = null;
        String line = "";
        StringEntity stringEntity;
        JSONObject rsltObj;
        JSONObject rtrnObj = new JSONObject();

        try {

            httpPost = new HttpPost(
                    baseUrl + "/bpmn/runtime/process-instances");
            httpPost.addHeader("Content-Type", "application/json");
            String authString = userName + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpGet.addHeader("Authorization", "Basic" + new String(authEncBytes));

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("processDefinitionId", processDefId);
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
            if (rsltObj.has("id")) {
                JSONObject processInstanceObj = new JSONObject();
                processInstanceObj.put("process-def-id", rsltObj.get("processDefinitionId"));
                processInstanceObj.put("process-instance-id", rsltObj.get("id"));
                rtrnObj.put("processInstanceData", processInstanceObj);
            } else if (rsltObj.has("errorMessage")) {
                rtrnObj.put("error-message", rsltObj.get("errorMessage"));
            }
        } catch (ClientProtocolException | IOException e) {
            log.error("Exception : ", e);
        } finally {
            if (rd != null) {
                rd.close();
            }

            if (response != null) {
                response.close();
            }

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
     */
    @Override
    public JSONObject createProcessInstanceWithData(String frmData, String host, int port)
            throws JSONException, IOException {
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        BufferedReader rd = null;
        String line;
        StringEntity stringEntity;
        JSONObject rsltObj;
        JSONObject rtrnObj = new JSONObject();

        //Decode the query string for form data and process definition id
        String decodedStr = URLDecoder.decode(frmData, "UTF-8");
        String splitFirst[] = decodedStr.split("&");
        JSONArray varArray = new JSONArray();
        for (int i = 0; i < (splitFirst.length - 1); i++) {
            String splitScnd[] = splitFirst[i].split("=");
            //If there are values for form fields
            if (splitScnd.length == 2) {
                JSONObject varObj = new JSONObject();
                varObj.put("name", splitScnd[0]);
                varObj.put("value", splitScnd[1]);
                varArray.put(varObj);
            }
        }

        try {

            httpPost = new HttpPost(
                    baseUrl + "/bpmn/runtime/process-instances");
            httpPost.addHeader("Content-Type", "application/json");
            String authString = userName + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            httpGet.addHeader("Authorization", "Basic" + new String(authEncBytes));

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("processDefinitionId", splitFirst[splitFirst.length - 1].split("=")[1]);
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
            if (rsltObj.has("id")) {
                JSONObject processInstanceObj = new JSONObject();
                processInstanceObj.put("process-def-id", rsltObj.get("processDefinitionId"));
                processInstanceObj.put("process-instance-id", rsltObj.get("id"));
                rtrnObj.put("processInstanceData", processInstanceObj);
            } else if (rsltObj.has("errorMessage")) {
                rtrnObj.put("error-message", rsltObj.get("errorMessage"));
            }
        } catch (ClientProtocolException | IOException e) {
            log.error("Exception : ", e);
        } finally {
            if (rd != null) {
                rd.close();
            }

            if (response != null) {
                response.close();
            }

            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return rtrnObj;
    }
}



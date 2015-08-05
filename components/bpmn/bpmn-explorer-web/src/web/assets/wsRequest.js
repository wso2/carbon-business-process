/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Initialize HT server information
 * @param url back end url
 * @param sessionCookie session cookie
 */
function initHTServerInfo(url, sessionCookie) {
    this.URL = url;
    this.endPoint = this.URL + '/services/BPMNDeploymentService/';
    this.cookie = sessionCookie;
}

/*
 * Function to send http request to back-end server
 * 
 * @returns response payload
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function requestBPS(endPoint, soapAction, BPSSessionCookie, payload) {
    var BPSResponse;
    var httpClient = new XMLHttpRequest();
    httpClient.open('POST', endPoint, false);
    httpClient.setRequestHeader('COOKIE', BPSSessionCookie);
    httpClient.setRequestHeader('SOAPAction', soapAction);
    httpClient.setRequestHeader('Content-Type', 'text/xml');

    httpClient.send(payload);
    //BPSResponse = httpClient.responseText;
    BPSResponse = httpClient.responseText;
    if (httpClient.status == 401) { //session timed out

        return null;
    }
    else {
        return BPSResponse;
    }
}


/*
 * Function to make WS-HT simplequery request with basic limited parameters 
 * 
 * @returns response payload
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function getBPMNProcessDiagram(processId) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                        xmlns:ser="http://services.mgt.core.bpmn.carbon.wso2.org">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ser:getProcessDiagram>\
                             <ser:processId>' + processId + '</ser:processId>\
                          </ser:getProcessDiagram>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';

    var soapAction = 'urn:getProcessDiagram';
    var BPSResponse;
    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);
    if (BPSResponse == null) {
        return null;
    }
    else {
        return BPSResponse;
    }
}
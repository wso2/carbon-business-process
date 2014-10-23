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
    this.endPoint = this.URL + '/services/HumanTaskClientAPIAdmin/';
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
    BPSResponse = httpClient.responseText;

    return BPSResponse;
}


/*
 * Function to make WS-HT simplequery request with basic limited parameters 
 * 
 * @returns response payload
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function simpleQueryBasic(status, pageSize, pageNumber, queryCategory, queryOrder, queryOrderBy) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                    xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803"\
                                    xmlns:ns1="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:simpleQuery>\
                             <ns:simpleQueryInput>\
                                <ns1:status>' + status + '</ns1:status>\
                                <ns1:pageSize>' + pageSize + '</ns1:pageSize>\
                                <ns1:pageNumber>' + pageNumber + '</ns1:pageNumber>\
                                <ns1:simpleQueryCategory>' + queryCategory + '</ns1:simpleQueryCategory>\
                                <ns1:queryOrder>' + queryOrder + '</ns1:queryOrder>\
                                <ns1:queryOrderBy>' + queryOrderBy + '</ns1:queryOrderBy>\
                             </ns:simpleQueryInput>\
                          </ns:simpleQuery>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';

    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/simpleQuery';
    var BPSResponse;
    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make WS-HT simplequery request Advance parameters 
 * 
 * @returns  response payload
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function simpleQueryAdvance(status, pageSize, pageNumber, queryCategory, queryOrder, queryOrderBy, createdDate, taskName) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                            xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803"\
                            xmlns:ns1="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803">\
                        <soapenv:Body>\
                            <ns:simpleQuery>\
                                <ns:simpleQueryInput>\
                                    <ns1:status>' + status + '</ns1:status>\
                                    <ns1:createdDate>' + createdDate + '</ns1:createdDate>\
                                    <ns1:undatedDate></ns1:undatedDate>\
                                    <ns1:taskName>' + taskName + '</ns1:taskName>\
                                    <ns1:pageSize>' + pageSize + '</ns1:pageSize>\
                                    <ns1:pageNumber>' + pageNumber + '</ns1:pageNumber>\
                                    <ns1:simpleQueryCategory>' + queryCategory + '</ns1:simpleQueryCategory>\
                                    <ns1:queryOrder>' + queryOrder + '</ns1:queryOrder>\
                                    <ns1:queryOrderBy>' + queryOrderBy + '</ns1:queryOrderBy>\
                                </ns:simpleQueryInput>\
                            </ns:simpleQuery>\
                        </soapenv:Body>\
                    </soapenv:Envelope>';

    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/simpleQuery';
    //var endPoint = this.URL + '/services/HumanTaskClientAPIAdmin/';

    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}



/*
 * Function to make WS-HT loadTask request
 * 
 * return response payload
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function loadTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                    xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:loadTask>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:loadTask>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/loadTask';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make WS-HT getComments request
 * 
 * return response payload
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function getComments(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                    xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                                <soapenv:Body>\
                                <ns:getComments>\
                                 <ns:identifier>' + id + '</ns:identifier>\
                                </ns:getComments>\
                                </soapenv:Body>\
                            </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/getComments';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}


/**
 * function to claim task
 * @param id : ID of the task to claim
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */

function claimTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                    xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:claim>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:claim>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/claim';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make start tasks service request
 * @param id ID of the task to start progress
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function startTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                        xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:start>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:start>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/start';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}


/**
 * Function to make stop tasks service request
 * @param id ID of the task to stop progress
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function stopTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:stop>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:stop>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/stop';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;

}


/**
 * Function to make release tasks service request
 * @param id ID of the task to release
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function releaseTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                    xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:release>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:release>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/release';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make suspend tasks service request
 * @param id ID of the task to suspend
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function suspendTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                        xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:suspend>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:suspend>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/suspend';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}


/**
 * Function to make resume suspended tasks service request
 * @param id ID of the task to resume
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function resumeTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                    xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:resume>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:resume>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/resume';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make fail tasks service request
 * @param id ID of the task to fail
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function failTask(id) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                             xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803"\
                                             xmlns:ns1="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:fail>\
                             <ns:identifier>' + id + '</ns:identifier>\
                          </ns:fail>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/fail';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make add comment service request
 * @param id ID of the task to add comment
 * @param text text comment
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function addComment(id, text) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                                xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:addComment>\
                             <ns:identifier>' + id + '</ns:identifier>\
                             <ns:text>' + text + '</ns:text>\
                          </ns:addComment>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/addComment';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make delete comment service request
 * @param id ID of the task to delete comment
 * @param taskId - ID of task of relates to comment
 * @param commentId - comment id  
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function deleteComment(taskId, commentId) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                        xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:deleteComment>\
                             <ns:taskIdentifier>' + taskId + '</ns:taskIdentifier>\
                             <ns:commentIdentifier>' + commentId + '</ns:commentIdentifier>\
                          </ns:deleteComment>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/deleteComment';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

/**
 * Function to make assign task (delegate) service request
 * @param id - ID of the task to assign
 * @param userName - user name of the new asignee 
 * @returns response payload from HumanTaskClientAPIAdmin service
 * @throws {exception java.net.ConnectException} if connection error occurred
 */
function assignTask(id, userName) {
    var payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"\
                            xmlns:ns="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803"\
                            xmlns:ns1="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803">\
                       <soapenv:Header/>\
                       <soapenv:Body>\
                          <ns:delegate>\
                             <ns:identifier>' + id + '</ns:identifier>\
                             <ns:organizationalEntity>\
                                <ns1:user>' + userName + '</ns1:user>\
                             </ns:organizationalEntity>\
                          </ns:delegate>\
                       </soapenv:Body>\
                    </soapenv:Envelope>';
    var soapAction = 'http://docs.oasis-open.org/ns/bpel4people/ws-humantask/api/200803/delegate';
    var BPSResponse = null;

    BPSResponse = requestBPS(this.endPoint, soapAction, this.cookie, payload);

    return BPSResponse;
}

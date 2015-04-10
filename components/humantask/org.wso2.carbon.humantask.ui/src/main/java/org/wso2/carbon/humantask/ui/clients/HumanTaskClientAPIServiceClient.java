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

package org.wso2.carbon.humantask.ui.clients;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.*;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.*;
import org.wso2.carbon.humantask.ui.constants.HumanTaskUIConstants;
import org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil;

import javax.xml.stream.XMLStreamException;
import java.math.BigInteger;
import java.rmi.RemoteException;

/**
 * The client service class to call the back end taskOperationsService.
 */
public class HumanTaskClientAPIServiceClient {

    /**
     * Class logger
     */
    private static Log log = LogFactory.getLog(HumanTaskClientAPIServiceClient.class);

    /**
     * Task Operations stub.
     */
    private HumanTaskClientAPIAdminStub stub;

    /**
     * The class constructor.
     *
     * @param cookie           :
     * @param backendServerURL : The back end server URL.
     * @param configContext    : The axis configuration context.
     * @throws org.apache.axis2.AxisFault : If the client creation fails.
     */
    public HumanTaskClientAPIServiceClient(
            String cookie,
            String backendServerURL,
            ConfigurationContext configContext) throws AxisFault {
        String serviceURL = backendServerURL +
                HumanTaskUIConstants.SERVICE_NAMES.TASK_OPERATIONS_SERVICE;
        stub = new HumanTaskClientAPIAdminStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Lists the tasks matching the provided simple query object.
     *
     * @param queryInput : The simple query object with the filtering criteria.
     * @return : The result set
     * @throws java.rmi.RemoteException :
     * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
     *                                  :
     * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
     *                                  :
     */
    public TTaskSimpleQueryResultSet taskListQuery(TSimpleQueryInput queryInput)
            throws RemoteException, IllegalArgumentFault, IllegalStateFault {
        try {
            return stub.simpleQuery(queryInput);
        } catch (RemoteException e) {
            log.error("Error occurred while performing taskListQuery operation", e);
            throw e;
        } catch (IllegalStateFault illegalStateFault) {
            log.error("Error occurred while performing taskListQuery operation", illegalStateFault);
            throw illegalStateFault;
        } catch (IllegalArgumentFault illegalArgumentFault) {
            log.error("Error occurred while performing taskListQuery operation", illegalArgumentFault);
            throw illegalArgumentFault;
        }
    }

    /**
     * Load task data for the give task id.
     *
     * @param taskId :
     * @return :
     * @throws RemoteException    :
     * @throws IllegalAccessFault :
     */
    public TTaskAbstract loadTask(URI taskId) throws RemoteException, IllegalAccessFault {

        try {
            return stub.loadTask(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing loadTask operation", e);
            throw e;
        } catch (IllegalAccessFault illegalAccessFault) {
            log.error("Error occurred while performing loadTask operation", illegalAccessFault);
            throw illegalAccessFault;
        }
    }

    public boolean addAttachment(String taskID, String attachmentName, String contentType, String attachmentID) throws
            RemoteException,
            IllegalStateFault,
            IllegalOperationFault,
            IllegalArgumentFault,
            IllegalAccessFault, URI.MalformedURIException {
        String errorMsg = "Error occurred while performing addAttachment operation.";
        try {
            //TODO : "Some of the attributes(like accessType) defined in the Service WSDLs are ignored and nulls are " +
            //"passed from to the service call");
            return stub.addAttachment(new URI(taskID), attachmentName, "dummyAccessType", contentType, attachmentID);
        } catch (RemoteException e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errorMsg, e);
            throw e;
        } catch (URI.MalformedURIException e) {
            String errorMessage = errorMsg + " TaskId: \"" + taskID + "\"";
            log.error(errorMsg, e);
            throw e;
        }
    }

    public TAttachmentInfo[] getAttachmentInfos(URI taskId) throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
        String errorMsg = "Error occurred while performing getAttachments operation";
        try {
            return stub.getAttachmentInfos(taskId);
        } catch (RemoteException e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errorMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errorMsg, e);
            throw e;
        }

    }

    /**
     * Task complete operation.
     *
     * @param taskId  : The task id to be completed.
     * @param payLoad : The payload.
     * @throws RemoteException       :
     * @throws IllegalAccessFault    :
     * @throws IllegalArgumentFault  :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws XMLStreamException    :
     */
    public void complete(URI taskId, String payLoad)
            throws RemoteException, IllegalAccessFault, IllegalArgumentFault, IllegalStateFault,
            IllegalOperationFault, XMLStreamException {
        String errMsg = "Error occurred while performing complete operation";
        try {
            String decodedPayload = HumanTaskUIUtil.decodeHTML(payLoad);
            stub.complete(taskId, decodedPayload);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault illegalAccessFault) {
            log.error(errMsg, illegalAccessFault);
            throw illegalAccessFault;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Loads the task input.
     *
     * @param taskId : The id of the task/.
     * @return : The task input OMElement.
     * @throws RemoteException        :
     * @throws IllegalStateFault      :
     * @throws IllegalOperationFault:
     * @throws IllegalAccessFault:
     * @throws IllegalArgumentFault:
     * @throws javax.xml.stream.XMLStreamException
     *                                :
     */
    public OMElement loadTaskInput(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
            IllegalArgumentFault, XMLStreamException {
        String errMsg = "Error occurred while performing loadTaskInput operation";
        try {
            String input = (String) stub.getInput(taskId, null);
            return AXIOMUtil.stringToOM(input);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (XMLStreamException e) {
            log.error(errMsg, e);
            throw e;
        }
    }


    /**
     * Loads the task output.
     *
     * @param taskId : The id of the task/.
     * @return : The task input OMElement.
     * @throws RemoteException        :
     * @throws IllegalStateFault      :
     * @throws IllegalOperationFault:
     * @throws IllegalAccessFault:
     * @throws IllegalArgumentFault:
     * @throws javax.xml.stream.XMLStreamException
     *                                :
     */
    public OMElement loadTaskOutput(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
            IllegalArgumentFault, XMLStreamException {
        String errMsg = "Error occurred while performing loadTaskOutput operation";
        try {
            String output = (String) stub.getOutput(taskId, null);
            return AXIOMUtil.stringToOM(output);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (XMLStreamException e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    public void setTaskOutput(URI taskId, String payLoad)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
            IllegalArgumentFault, XMLStreamException {
        String errMsg = "Error occurred while performing setTaskOutput operation";
        try {
            String decodedPayload = HumanTaskUIUtil.decodeHTML(payLoad);
            stub.setOutput(taskId, new NCName("message"), decodedPayload);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Claim task operation.
     *
     * @param taskId : The ID of the task to be claimed.
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     * @throws IllegalStateFault     :
     * @throws RemoteException       :
     * @throws IllegalOperationFault :
     */
    public void claim(URI taskId) throws IllegalArgumentFault, IllegalAccessFault,
            IllegalStateFault, RemoteException, IllegalOperationFault {
        String errMsg = "Error occurred while performing claim operation";
        try {
            stub.claim(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Loads the task authorisation parameters for UI functionality.
     *
     * @param taskId : The task Id.
     * @return : The task authorisation parameters.
     * @throws RemoteException      :
     * @throws IllegalStateFault    :
     * @throws IllegalArgumentFault :
     */
    public TTaskAuthorisationParams getTaskParams(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalArgumentFault {

        String errMsg = "Error occurred while performing getTaskParams operation";
        try {
            return stub.loadAuthorisationParams(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    public void start(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        String errMsg = "Error occurred while performing start operation";
        try {
            stub.start(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Stop task.
     *
     * @param taskId : The task Id.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void stop(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        String errMsg = "Error occurred while performing stop operation";
        try {
            stub.stop(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Release task.
     *
     * @param taskId : The task id.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void release(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        String errMsg = "Error occurred while performing release operation";
        try {
            stub.release(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Gets the list of comments associated with a given task.
     *
     * @param taskId : The task id.
     * @return : The comments of the task.
     * @throws RemoteException       :
     * @throws IllegalStateFault:
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public TTaskEvents getTaskEvents(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            return stub.loadTaskEvents(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        }
    }

    /**
     * Gets the list of comments associated with a given task.
     *
     * @param taskId : The task id.
     * @return : The comments of the task.
     * @throws RemoteException       :
     * @throws IllegalStateFault:
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public TComment[] getComments(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        String errMsg = "Error occurred while performing get comments operation";
        try {
            return stub.getComments(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Add the given comment string to the task.
     *
     * @param taskId      : The id of the task.
     * @param commentText : The comment text.
     * @return : The id of the persisted comment.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public URI addComment(URI taskId, String commentText)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        String errMsg = "Error occurred while performing add comment operation";
        try {
            return stub.addComment(taskId, commentText);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Delete comment client operation.
     *
     * @param taskId    : The task which the comment belongs to.
     * @param commentId : The comment to be deleted.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void deleteComment(URI taskId, URI commentId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        String errMsg = "Error occurred while performing delete comment operation";
        try {
            stub.deleteComment(taskId, commentId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Suspend task operation.
     *
     * @param taskId : The task to be suspended.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void suspend(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        String errMsg = "Error occurred while performing suspend operation";
        try {
            stub.suspend(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Resume task operation.
     *
     * @param taskId : The task id.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void resume(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        String errMsg = "Error occurred while performing resume operation";
        try {
            stub.resume(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Returns an array of assignable users of a task.
     *
     * @param taskId : The task id.
     * @return : The assignable user array.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     */
    public TUser[] getTaskAssignableUsers(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault {
        String errMsg = "Error occurred while performing getTaskAssignableUsers operation";
        try {
            return stub.getAssignableUserList(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * Delegate task operation.
     *
     * @param taskId    : the task id of the task being delegated.
     * @param delegatee : The person to whom the task is being delegated.
     * @throws RemoteException              :
     * @throws IllegalStateFault            :
     * @throws IllegalArgumentFault         :
     * @throws IllegalOperationFault        :
     * @throws RecipientNotAllowedException :
     * @throws IllegalAccessFault           :
     */
    public void delegate(URI taskId, String delegatee)
            throws RemoteException, IllegalStateFault, IllegalArgumentFault, IllegalOperationFault,
            RecipientNotAllowedException, IllegalAccessFault {

        if (delegatee == null || "".equals(delegatee.trim())) {
            throw new IllegalArgumentException("Delegatee user name should not be empty.");
        }
        String errMsg = "Error occurred while performing delegate operation";

        TOrganizationalEntity delegateOrgEntity = new TOrganizationalEntity();
        TOrganizationalEntityChoice[] delegateArr = new TOrganizationalEntityChoice[1];
        TOrganizationalEntityChoice delegateeChoice = new TOrganizationalEntityChoice();
        TUser delegateeUser = new TUser();
        delegateeUser.setTUser(delegatee);
        delegateeChoice.setUser(delegateeUser);
        delegateArr[0] = delegateeChoice;
        delegateOrgEntity.setTOrganizationalEntityChoice(delegateArr);

        try {
            stub.delegate(taskId, delegateOrgEntity);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (RecipientNotAllowedException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }

    }

    /**
     * The skip operation.
     *
     * @param taskId : The task id.
     * @throws IllegalArgumentFault  :
     * @throws IllegalOperationFault :
     * @throws IllegalAccessFault    :
     * @throws IllegalStateFault     :
     * @throws RemoteException       :
     */
    public void skip(URI taskId)
            throws IllegalArgumentFault, IllegalOperationFault, IllegalAccessFault,
            IllegalStateFault, RemoteException {
        String errMsg = "Error occurred while performing skip operation";
        try {
            stub.skip(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }

    /**
     * The task fail operation.
     *
     * @param taskId : The id of the task to be failed.
     * @throws IllegalArgumentFault  :
     * @throws IllegalOperationFault :
     * @throws IllegalAccessFault    :
     * @throws IllegalStateFault     :
     * @throws RemoteException       :
     */
    public void fail(URI taskId)
            throws IllegalArgumentFault, IllegalOperationFault, IllegalAccessFault,
            IllegalStateFault, RemoteException {
        String errMsg = "Error occurred while performing fail operation";
        try {
            stub.fail(taskId, null);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }

    }

    /**
     * Task remove operation. Note: applicable for notifications only.
     *
     * @param taskId : The id of the task to be removed.
     * @throws IllegalArgumentFault  :
     * @throws IllegalOperationFault :
     * @throws IllegalAccessFault    :
     * @throws RemoteException       :
     */
    public void remove(URI taskId)
            throws IllegalArgumentFault, IllegalOperationFault, IllegalAccessFault,
            RemoteException {
        String errMsg = "Error occurred while performing resume operation";
        try {
            stub.remove(taskId);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        }

    }

    /**
     * Change priority client operation.
     *
     * @param taskId      : The task id.
     * @param priorityInt : The new priority value.
     * @throws IllegalArgumentFault  :
     * @throws IllegalOperationFault :
     * @throws IllegalAccessFault    :
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     */
    public void changePriority(URI taskId, int priorityInt)
            throws IllegalArgumentFault, IllegalOperationFault, IllegalAccessFault,
            RemoteException, IllegalStateFault {

        String errMsg = "Error occurred while performing change priority operation.";
        try {
            TPriority priority = new TPriority();
            priority.setTPriority(BigInteger.valueOf(priorityInt));
            stub.setPriority(taskId, priority);
        } catch (RemoteException e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error(errMsg, e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error(errMsg, e);
            throw e;
        }
    }
}

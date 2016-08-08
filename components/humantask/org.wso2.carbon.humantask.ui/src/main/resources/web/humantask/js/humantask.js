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

var HUMANTASK = {};

HUMANTASK.authParams = null;
HUMANTASK.taskDetails = null;
HUMANTASK.taskId = null;
HUMANTASK.taskClient = null;


HUMANTASK.ready = function(taskId, taskClient, isNotification) {
    HUMANTASK.taskId = taskId;
    HUMANTASK.taskClient = taskClient;
    HUMANTASK.loadTask(taskId, taskClient);
    HUMANTASK.loadTaskAuthParams(taskId, taskClient);
    if (isNotification == false) {
        HUMANTASK.loadComments(taskId, taskClient);
    }
    HUMANTASK.bindButtons();
};

HUMANTASK.loadTask = function(taskId, taskClient) {
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=taskDetails';
    $.getJSON(page,
              function(json) {
                  HUMANTASK.taskDetails = json;
                  //The post task details load actions should be called here.
                  HUMANTASK.fillTaskDetails();
                  HUMANTASK.showResponseFieldSet();
              });
};

HUMANTASK.loadTaskAuthParams = function(taskId, taskClient) {
    var authParams;
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=authParams';
    $.getJSON(page,
              function(json) {
                  HUMANTASK.authParams = json;
                  //The post task auth param load actions should be called here.
                  HUMANTASK.showHideActions();
              });
};

HUMANTASK.fillTaskDetails = function () {
    jQuery('#taskStatusTxt').text(HUMANTASK.taskDetails.taskStatus);
    jQuery('#taskPriorityTxt').text(HUMANTASK.taskDetails.taskPriority);
    jQuery('#taskCreatedOnDateTxt').text(HUMANTASK.taskDetails.taskCreatedOn);
    jQuery('#taskUpdatedOnDateTxt').text(HUMANTASK.taskDetails.taskUpdatedOn);
    jQuery('#taskTypeTxt').text(HUMANTASK.taskDetails.taskType);
    jQuery('#descriptionTxtDiv').text(HUMANTASK.taskDetails.taskPresentationDescription);
    jQuery('#taskOwnerTxt').text(HUMANTASK.taskDetails.taskOwner);

    if (HUMANTASK.taskDetails.taskCreatedBy != undefined && HUMANTASK.taskDetails.taskCreatedBy != null
            && HUMANTASK.taskDetails.taskCreatedBy.length > 0) {
        jQuery('#taskCreatedByTR').show();
        jQuery('#taskCreatedByTxt').text(HUMANTASK.taskDetails.taskCreatedBy);
    }
    jQuery('#taskSubjectTxt').text(HUMANTASK.taskDetails.taskPresentationSubject);

    if (HUMANTASK.taskDetails.taskPreviousStatus != undefined && HUMANTASK.taskDetails.taskPreviousStatus != null
            && HUMANTASK.taskDetails.taskPreviousStatus.length > 0) {
        jQuery('#taskPreviousStatusTR').show();
        jQuery('#taskPreviousStatusTxt').text(HUMANTASK.taskDetails.taskPreviousStatus);
    }

};

HUMANTASK.showHideActions = function() {

    if (HUMANTASK.authParams.authorisedToGetInput) {
        jQuery('#requestFieldSet').show();
    }

    if (HUMANTASK.authParams.authorisedToClaim) {
        jQuery('#claimLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToStart) {
        jQuery('#startLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToStop) {
        jQuery('#stopLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToRelease) {
        jQuery('#releaseLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToComment) {
        jQuery('#commentLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToDelegate) {
        jQuery('#delegateLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToSuspend) {
        jQuery('#suspendLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToResume) {
        jQuery('#resumeLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToRemove) {
        jQuery('#removeLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToSkip) {
        jQuery('#skipLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToFail) {
        jQuery('#failLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToComplete) {
        jQuery('#responseFormFieldSet').show();
    }

    if (HUMANTASK.authParams.authorisedToSetPriority) {
        jQuery('#changePriorityLinkLi').show();
    }

};

HUMANTASK.showResponseFieldSet = function() {
    if (HUMANTASK.taskDetails.taskStatus == 'COMPLETED') {
        jQuery('#responseFieldSet').show();
    }
};

HUMANTASK.loadComments = function(taskId, taskClient) {
sessionAwareFunction(function() {
    jQuery('#commentsTab').empty();
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=taskComments';
    $.getJSON(page,
              function(commentJson) {
                  HUMANTASK.populateComments(commentJson);
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.loadEvents = function(taskId, taskClient) {
sessionAwareFunction(function() {

    jQuery('#eventsTab').empty();
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=taskEvents';
    $.getJSON(page,
              function(eventJson) {
                  HUMANTASK.populateEvents(eventJson);
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.loadAttachments = function(taskId, taskClient) {
sessionAwareFunction(function() {
    jQuery('#attachmentsTab').empty();
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=taskAttachments';
    $.getJSON(page,
              function(attachmentsJson) {
                  HUMANTASK.populateAttachments(attachmentsJson, taskId);
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.populateComments = function (commentJSONMap) {
    $.each(commentJSONMap, function(commentId, commentJSON) {
        var commentDIV = HUMANTASK.createCommentDiv(commentJSON);
         jQuery('#commentsTab').append(commentDIV);
    });

};

HUMANTASK.populateEvents = function (eventJSONMap) {
    $.each(eventJSONMap, function(eventId, eventJSON) {
        var eventDiv = HUMANTASK.createEventDiv(eventJSON);
        jQuery('#eventsTab').append(eventDiv);
    });
};

HUMANTASK.populateAttachments = function (attachmentsJSONMap, taskId) {
    var attachmentTable = jQuery('<table class="styledLeft" id="taskAttachmentInfo"><thead>' +
                        '<tr>' +
                            '<th class="tvTableHeader">Name</th>' +
                            '<th class="tvTableHeader">ContentType</th>' +
                            '<th class="tvTableHeader">Link</th>' +
                        '</tr>' +
                        '</thead>' +
                        '<tbody id="taskAttachmentInfoBody"></tbody></table>');


    $.each(attachmentsJSONMap, function(attachmentId, attachmentJSON){
        var t_row = HUMANTASK.createAttachmentTableRow(attachmentJSON);
        jQuery('#taskAttachmentInfoBody',attachmentTable).append(t_row);
    });

    jQuery('#attachmentsTab').append(attachmentTable);
    var attachmentUploadForm = '<div id="attachmnetUploadForm">';

    attachmentUploadForm += '<form id="attachment_upload_form" method="post" name="attachmentUpload" action="../..' +
                            '/fileupload/attachment-mgt" enctype="multipart/form-data" target="_self">';
    attachmentUploadForm += '<input type="hidden" id="uRedirect" name="redirect" value="humantask/basic_task_view.jsp?taskId='+taskId+'"/>';
    attachmentUploadForm += '<input type="hidden" id="taskID" name="taskID" value=\"' + taskId + '\"/>';
    attachmentUploadForm += '<table><tbody><tr><td>File</td><td><input type="file" ' +
                            'id="fileToUploadID" name="fileToUpload"/></td></tr></tbody>' +
                            '<tfoot><tr><td><input name="attachmentUploadButton" class="button" type="button" ' +
                            'value="upload" onclick="HUMANTASK.uploadAttachment();"/></td>' +
                            '</tr></tfoot></table>';
    attachmentUploadForm += '</form>';
    attachmentUploadForm += '</div>';

    //removing to make HT UI read only to avoid security issues
    //jQuery('#attachmentsTab').append(attachmentUploadForm);
};

HUMANTASK.uploadAttachment= function() {
	sessionAwareFunction(function() {
      if( $('#fileToUploadID').val() =='' || $('#fileToUploadID').length ==0)
              {
              alert("Please select file to upload attachment.");
      		  return true ;
              }
    document.attachmentUpload.submit();
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.createCommentDiv = function(commentJSON) {
    var commentDiv = '<div class="commentBox">';
    commentDiv +=   '<a>' + commentJSON.commentAddedBy + '</a> added a comment - ' + commentJSON.commentAddedTime + HUMANTASK.createDeleteCommentLink(commentJSON) ;
    commentDiv +=   '<div class="commentContent">' + commentJSON.commentText + '</div>' ;
    commentDiv += '</div>';

    return commentDiv;
};

HUMANTASK.createAttachmentTableRow = function(attachmentJSON) {
    var attachmentsTableRow = '<tr>';
    attachmentsTableRow += '<td>' + attachmentJSON.attachmentName + '</td>';
    attachmentsTableRow += '<td>' + attachmentJSON.attachmentContentType + '</td>';
    attachmentsTableRow += '<td><a href=\"' + attachmentJSON.attachmentLink + '\">' + attachmentJSON.attachmentLink + '</a></td>';
    attachmentsTableRow += '</tr>';

    return attachmentsTableRow
};

HUMANTASK.createEventDiv = function(eventJSON) {
    //TODO construct this properly!!!
    var eventDiv = '<div class="commentBox">';
    eventDiv +=   '<b> User : </b> ' + eventJSON.eventInitiator + '<br>';
    eventDiv +=   '<b> Operation : </b> ' + eventJSON.eventType + '<br>';
    eventDiv +=   '<b> Time : </b> ' + eventJSON.eventTime + '<br>';


    if(eventJSON.oldState != eventJSON.newState) {
        eventDiv +=   '<b> Old State : </b> ' + eventJSON.oldState + '<br>';
        eventDiv +=   '<b> New State : </b> ' + eventJSON.newState + '<br>';
    }
    if(eventJSON.eventDetail) {
        eventDiv +=   '<b> Details : </b> ' + eventJSON.eventDetail + '<br>';
    }
    eventDiv += '</div>';

    return eventDiv;
};

HUMANTASK.createDeleteCommentLink = function(commentJSON) {
    return '<a onclick="HUMANTASK.deleteComment(' +  HUMANTASK.taskId +  ',' +  commentJSON.commentId + ')"> Delete</a>';
};

HUMANTASK.deleteComment = function (taskId, commentId) {
    sessionAwareFunction(function() {
if (confirm('Do you want to delete the comment?'))

    var deleteCommentURL = 'task-operations-ajaxprocessor.jsp?operation=deleteComment&taskClient=' +
                           HUMANTASK.taskClient + '&taskId=' + taskId + '&commentId=' + commentId;
    $.getJSON(deleteCommentURL,
              function(json) {
                  if (json.CommentDeleted == 'true') {
                      //location.reload(true);
                      HUMANTASK.loadComments(HUMANTASK.taskId, HUMANTASK.taskClient);
                      jQuery("#commentsTab").focus();
                  } else {
                      alert('Error occurred while deleting comment : ' + json.CommentDeleted);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.bindButtons = function() {
    jQuery('#claimLink').click(HUMANTASK.claimTask);
    jQuery('#stopLink').click(HUMANTASK.stopTask);
    jQuery('#startLink').click(HUMANTASK.startTask);
    jQuery('#releaseLink').click(HUMANTASK.releaseTask);
    jQuery('#suspendLink').click(HUMANTASK.suspendTask);
    jQuery('#resumeLink').click(HUMANTASK.resumeTask);
    jQuery('#failLink').click(HUMANTASK.failTask);
    jQuery('#skipLink').click(HUMANTASK.skipTask);
    jQuery('#removeLink').click(HUMANTASK.removeTask);
    jQuery('#addCommentButton').click(HUMANTASK.addComment);
    jQuery('#completeTaskButton').click(HUMANTASK.completeTask);
    jQuery('#saveTaskButton').click(HUMANTASK.saveTask);
    jQuery('#delegateButton').click(HUMANTASK.delegateTask);
    jQuery('#changePriorityButton').click(HUMANTASK.changePriority);

};


HUMANTASK.claimTask = function() {
    sessionAwareFunction(function() {
    var claimURL = 'task-operations-ajaxprocessor.jsp?operation=claim&taskClient=' +
                   HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(claimURL,
              function(json) {
                  if (json.TaskClaimed == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while claiming task : ' + json.TaskClaimed);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.startTask = function() {
    sessionAwareFunction(function() {
    var startURL = 'task-operations-ajaxprocessor.jsp?operation=start&taskClient=' +
                   HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(startURL,
              function(json) {
                  if (json.TaskStarted == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while starting task : ' + json.TaskStarted);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.stopTask = function() {
    sessionAwareFunction(function() {
    var stopURL = 'task-operations-ajaxprocessor.jsp?operation=stop&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(stopURL,
              function(json) {
                  if (json.TaskStopped == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while stopping task : ' + json.TaskStopped);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.releaseTask = function() {
    sessionAwareFunction(function() {
    var releaseURL = 'task-operations-ajaxprocessor.jsp?operation=release&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(releaseURL,
              function(json) {
                  if (json.TaskReleased == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskReleased);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.suspendTask = function () {
    sessionAwareFunction(function() {
    var suspendURL = 'task-operations-ajaxprocessor.jsp?operation=suspend&taskClient=' +
                     HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(suspendURL,
              function(json) {
                  if (json.TaskSuspended == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskSuspended);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.resumeTask = function() {
    sessionAwareFunction(function() {
    var resumeURL = 'task-operations-ajaxprocessor.jsp?operation=resume&taskClient=' +
                    HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(resumeURL,
              function(json) {
                  if (json.TaskResumed == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskResumed);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.failTask = function() {
    sessionAwareFunction(function() {
    var failURL = 'task-operations-ajaxprocessor.jsp?operation=fail&taskClient=' +
                    HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(failURL,
              function(json) {
                  if (json.TaskFailed == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while failing task : ' + json.TaskFailed);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.skipTask = function() {
    sessionAwareFunction(function() {
    var skipURL = HUMANTASK.getTaskOperationURL('skip');
    $.getJSON(skipURL,
              function(json) {
                  if (json.TaskSkipped == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while skipping task : ' + json.TaskSkipped);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.removeTask = function() {
    sessionAwareFunction(function() {
    var removeURL = 'task-operations-ajaxprocessor.jsp?operation=remove&taskClient=' +
                    HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(removeURL,
              function(json) {
                  if (json.TaskRemoved == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while removing task : ' + json.TaskRemoved);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.completeTask = function() {
    sessionAwareFunction(function() {
    var OUTPUT_XML = createTaskOutput();
    var completeURL = 'task-operations-ajaxprocessor.jsp?operation=complete&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&payLoad=' + OUTPUT_XML;
    $.getJSON(completeURL,
              function(json) {
                  if (json.TaskCompleted == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskCompleted);
                      return true;
                  }
               }).error(function(){
                      	location.reload(true);
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.saveTask = function() {
    sessionAwareFunction(function() {
        var OUTPUT_XML = createTaskOutput();
        var completeURL = 'task-operations-ajaxprocessor.jsp?operation=save&taskClient=' +
            HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&payLoad=' + OUTPUT_XML;
        $.getJSON(completeURL,
            function(json) {
                if (json.TaskSetOutput == 'true') {
                    location.reload(true);
                } else {
                    alert('Error occurred while saving task : ' + json.TaskSetOutput);
                    return true;
                }
            }).error(function(){
                location.reload(true);
            });
    }, "Session timed out. Please login again.");
    return false;
};

HUMANTASK.addComment = function() {
    sessionAwareFunction(function() {

    var commentText = jQuery('#commentTextAreaId').val();
    if( commentText=='' || commentText.length ==0){
    return true ;
    }
    var addCommentURL = 'task-operations-ajaxprocessor.jsp?operation=addComment&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&commentText=' + commentText;
    $.getJSON(addCommentURL,
              function(json) {
                  if (json.CommentAdded == 'true') {
                      jQuery('#commentSection').hide();
                      HUMANTASK.loadComments(HUMANTASK.taskId, HUMANTASK.taskClient);
                      // clear the comment text area.
                      $('#commentTextAreaId').val('');
                      //focus on the current comments.
                      jQuery("#commentsTab").focus();
                  } else {
                      alert('Error occurred while adding comment to task : ' + json.CommentAdded);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.delegateTask = function() {
    sessionAwareFunction(function() {
    var delegatee =  jQuery('#assignableUserList').val();
    var delegateURL = 'task-operations-ajaxprocessor.jsp?operation=delegate&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&delegatee=' + delegatee;
    $.getJSON(delegateURL,
              function(json) {
                  if (json.TaskDelegated == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while delageting task : ' + json.TaskDelegated);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};


HUMANTASK.changePriority = function() {
    sessionAwareFunction(function() {
    var priority = jQuery('#priorityList').val();
    var priorityURL = 'task-operations-ajaxprocessor.jsp?operation=changePriority&taskClient=' +
                      HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&priority=' + priority;
    $.getJSON(priorityURL,
              function(json) {
                  if (json.TaskPriorityChanged == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while changing task priority : ' + json.TaskPriorityChanged);
                      return true;
                  }
              });
	}, "Session timed out. Please login again.");
                                         return false;
};

HUMANTASK.handleTabSelection = function (tabType) {

    if (tabType == 'commentsTab') {
        $('#eventsTab').hide();
        $('#eventTabLink').removeClass('selected');

        $('#attachmentsTab').hide();
        $('#attachmentsTabLink').removeClass('selected');

        $('#commentsTab').show();
        $('#commentTabLink').addClass('selected');
        HUMANTASK.loadComments(HUMANTASK.taskId, HUMANTASK.taskClient);
    } else if (tabType == 'eventsTab') {
        $('#commentsTab').hide();
        $('#commentTabLink').removeClass('selected');

        $('#attachmentsTab').hide();
        $('#attachmentsTabLink').removeClass('selected');

        $('#eventsTab').show();
        $('#eventTabLink').addClass('selected');
        HUMANTASK.loadEvents(HUMANTASK.taskId, HUMANTASK.taskClient);
    } else if (tabType == 'attachmentsTab') {
        $('#commentsTab').hide();
        $('#commentTabLink').removeClass('selected');

        $('#eventsTab').hide();
        $('#eventTabLink').removeClass('selected');

        $('#attachmentsTab').show();
        $('#attachmentsTabLink').addClass('selected');
        HUMANTASK.loadAttachments(HUMANTASK.taskId, HUMANTASK.taskClient);
    }
};

/**
 *
 * @param tabId
 */
HUMANTASK.handleDelegateSelection = function (tabId) {
    toggleMe(tabId);
    HUMANTASK.fillAssignableUsersList();
};

/**
 *
 * @param tabId
 */
HUMANTASK.handleChangePrioritySelection = function (tabId) {
    toggleMe(tabId);
    HUMANTASK.populatePriorityValuesDropDown();
};

/**
 * Appends values to the priority list drop down.
 */
HUMANTASK.populatePriorityValuesDropDown = function () {
    $('#priorityList').empty();
    $('#priorityList').append($('<option>1 - Highest</option>').val('1'));
    $('#priorityList').append($('<option>2 </option>').val('2'));
    $('#priorityList').append($('<option>3  </option>').val('3'));
    $('#priorityList').append($('<option>4  </option>').val('4'));
    $('#priorityList').append($('<option>5  </option>').val('5'));
    $('#priorityList').append($('<option>6  </option>').val('6'));
    $('#priorityList').append($('<option>7  </option>').val('7'));
    $('#priorityList').append($('<option>8  </option>').val('8'));
    $('#priorityList').append($('<option>9  </option>').val('9'));
    $('#priorityList').append($('<option>10 - Lowest</option>').val('10'));
};


/**
 *
 */
HUMANTASK.fillAssignableUsersList = function () {

    // we need to do an ajax call only if the delegate section is visible.
    if ($('#delegateSection').is(":visible")) {
        var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + HUMANTASK.taskClient +
                   '&taskId=' + HUMANTASK.taskId + '&loadParam=assignableUsers';
        $.getJSON(page,
                  function(eventJson) {
                      HUMANTASK.populateAssignableUserDropDown(eventJson);
                  });
    }
};

/**
 * Appends values to the people list drop down.
 * @param eventJson  user list json.
 */
HUMANTASK.populateAssignableUserDropDown = function (eventJson) {
    $('#assignableUserList').empty();
    $.each(eventJson, function(index, userNameJSON) {
        $('#assignableUserList').append(
                $('<option></option>').val(userNameJSON.userName).html(userNameJSON.userName)
        );
    });
};

/**
 * Constructs the ajax url for the task operation.
 *
 * @param operationName : The task operation name.
 */
HUMANTASK.getTaskOperationURL = function(operationName) {
    return  'task-operations-ajaxprocessor.jsp?operation=' + operationName + '&taskClient=' +
                    HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
};



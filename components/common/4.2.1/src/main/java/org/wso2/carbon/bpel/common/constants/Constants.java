/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.common.constants;

/**
 * This class will manage constants that need to be maintained across components like bpel, human-task, b4p,
 * attachment-mgt etc.
 */
public class Constants {
    private Constants() {
    }

    /** Namespace for the attachmentID elements which are propagated B4P extension to human-task component */
    public static final String ATTACHMENT_ID_NAMESPACE = "http://wso2.org/bps/attachments";

    /** Namespace prefix for the attachmentID elements which are propagated B4P extension to human-task component */
    public static final String ATTACHMENT_ID_NAMESPACE_PREFIX = "attch";

    /** Local name for parent element for the attachmentID elements which are propagated B4P extension to human-task
     * component */
    public static final String ATTACHMENT_ID_PARENT_ELEMENT_NAME = "attachmentIDs";

    /** Local name for child elements for the attachmentID elements which are propagated B4P extension to human-task
     * component */
    public static final String ATTACHMENT_ID_CHILD_ELEMENT_NAME = "attachmentID";
}

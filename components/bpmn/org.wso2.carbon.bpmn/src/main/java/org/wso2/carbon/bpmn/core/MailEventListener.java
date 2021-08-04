/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.bpmn.core;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will listen to Activiti events and change the classloader before "Mail" tasks.
 * Previous class loader restored after the "Mail" task is completed.
 */
public class MailEventListener implements ActivitiEventListener {

    private static final Log log = LogFactory.getLog(MailEventListener.class);
    private static final String SERVICE_TASK = "serviceTask";
    private static final String MAP_SIZE_ENV_VARIABLE = "bps_bpmn_tasks_mail_classLoaderMaxMapSize";
    private static final String MAIL_BEHAVIOR_CLASS = "org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior";

    private ConcurrentHashMap<String, ClassLoader> classLoaderMap;
    private int MAX_MAP_SIZE = 1000;

    public MailEventListener() {
        classLoaderMap = new ConcurrentHashMap<>();
        String mapSize = System.getenv(MAP_SIZE_ENV_VARIABLE);
        if (mapSize != null) {
            try {
                int temp = Integer.parseInt(System.getenv(MAP_SIZE_ENV_VARIABLE));
                if (temp > 0) {
                    MAX_MAP_SIZE = temp;
                }
            } catch (NumberFormatException ex) {
                log.error("Invalid input for " + MAIL_BEHAVIOR_CLASS + " env variable", ex);
                MAX_MAP_SIZE = 1000;
            }
        }
    }

    @Override public void onEvent(ActivitiEvent activitiEvent) {

        if (activitiEvent instanceof ActivitiActivityEventImpl) {
            ActivitiActivityEventImpl activitiActivityEvent = (ActivitiActivityEventImpl) activitiEvent;
            if (activitiActivityEvent.getActivityType().equals(SERVICE_TASK) &&
                    activitiActivityEvent.getBehaviorClass().equals(MAIL_BEHAVIOR_CLASS)) {
                String key = activitiActivityEvent.getProcessInstanceId() +
                        activitiActivityEvent.getProcessDefinitionId();
                if (activitiActivityEvent.getType().equals(ActivitiEventType.ACTIVITY_STARTED)) {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    // Setting the class loader related to the mail task.
                    Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());
                    if (classLoaderMap.size() >= MAX_MAP_SIZE) {
                        log.error("Maximum map size reached in the MailEventListener. Clearing the class loader " +
                                "map. Increase the map size if required.");
                        classLoaderMap.clear();
                    }
                    classLoaderMap.put(key, classLoader);
                } else if (activitiActivityEvent.getType().equals(ActivitiEventType.ACTIVITY_COMPLETED)) {
                    if (classLoaderMap.containsKey(key)) {
                        Thread.currentThread().setContextClassLoader(classLoaderMap.remove(key));
                    } else {
                        log.error("MailEventListener Couldn't find a classloader to replace for this activity");
                    }
                }
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}

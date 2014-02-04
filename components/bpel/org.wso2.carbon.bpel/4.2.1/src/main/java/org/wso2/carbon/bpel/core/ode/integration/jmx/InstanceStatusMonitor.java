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

package org.wso2.carbon.bpel.core.ode.integration.jmx;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.Notification;

public class InstanceStatusMonitor extends NotificationBroadcasterSupport implements InstanceStatusMonitorMXBean {
    String lastFailedProcessInfo = "No new instance faliures";

    private static InstanceStatusMonitor instanceStatusMonitor=null;
    long sequenceNumber=0;

    public void setLastFailedProcessInfo(String info) {
        String oldStatus= lastFailedProcessInfo;
        this.lastFailedProcessInfo =info;

        Notification notification= new AttributeChangeNotification(this,
                sequenceNumber++, System.currentTimeMillis(),
                info, "Instance Status", "String",
                oldStatus, this.lastFailedProcessInfo);
        sendNotification(notification);
    }

    @Override
    public String getLastFailedProcessInfo() {
        return this.lastFailedProcessInfo;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[]{
                AttributeChangeNotification.ATTRIBUTE_CHANGE
        };
         String name = "notification on faliure";
        String description = "An instance has failed";
        MBeanNotificationInfo info =
                new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[]{info};
    }

    public static InstanceStatusMonitor getInstanceStatusMonitor(){
        if(instanceStatusMonitor==null){
            instanceStatusMonitor= new InstanceStatusMonitor();
            return instanceStatusMonitor;
        } else{
            return instanceStatusMonitor;
        }

    }
}

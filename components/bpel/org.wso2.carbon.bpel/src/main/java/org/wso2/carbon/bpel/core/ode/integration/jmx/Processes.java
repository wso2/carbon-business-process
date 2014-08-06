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


import org.apache.ode.bpel.iapi.ProcessState;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.bpel.core.ode.integration.store.MultiTenantProcessStore;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessStoreImpl;

import javax.xml.namespace.QName;


public class Processes implements ProcessesMXBean {

  private  MultiTenantProcessStore processStore;
    public String[] getProcessStates(){
        BPELServer bpelServer = BPELServiceComponent.getBPELServer();
        processStore= bpelServer.getMultiTenantProcessStore();
        int processCount=((ProcessStoreImpl) processStore).getProcesses().size();
        String[] processStates= new String[processCount];
        if(processCount!=0){
            for(int i=0;i<processCount;i++) {
                StringBuffer buffer= new StringBuffer();
                buffer.append(((ProcessStoreImpl) processStore).getProcesses().get(i).getLocalPart().toString());
                buffer.append(" State-");
                buffer.append(((ProcessStoreImpl) processStore).getProcessConfiguration(((ProcessStoreImpl) processStore).getProcesses().get(i)).getState().toString());
                processStates[i]=buffer.toString();
            }
        }
            return  processStates;
    }

    public String retireProcess(String processQnameLocalPart) {
        int processCount= ((ProcessStoreImpl) processStore).getProcesses().size();
        QName name=null ;
        if(processCount!=0){
             for(int i=0;i<processCount;i++){
                 if(processQnameLocalPart.equalsIgnoreCase(((ProcessStoreImpl) processStore).getProcesses().get(i).getLocalPart().toString())) {
                    name= ((ProcessStoreImpl) processStore).getProcesses().get(i);
                 }
             }
             if(name!=null){
                 ((ProcessStoreImpl) processStore).setState(name, ProcessState.RETIRED);
                 return "successfully retired "+processQnameLocalPart;
             } else{
                 return "no process with specified name";
             }
         }  else{
                return "No processess deployed";
         }
    }

    public String activateProcess(String processQnameLocalPart) {
        int processCount= ((ProcessStoreImpl) processStore).getProcesses().size();
        QName name=null ;
        if(processCount!=0){
            for(int i=0;i<processCount;i++){
                if(processQnameLocalPart.equalsIgnoreCase(((ProcessStoreImpl) processStore).getProcesses().get(i).getLocalPart().toString())) {
                    name= ((ProcessStoreImpl) processStore).getProcesses().get(i);
                }
            }
            if(name!=null){
                ((ProcessStoreImpl) processStore).setState(name, ProcessState.ACTIVE);
                return "successfully activated "+processQnameLocalPart;
            } else{
                return "no process with specified name";
            }
        }   else{
            return "no processess deployed";
        }
    }


    public String disableProcess(String processQnameLocalPart) {
        int processCount= ((ProcessStoreImpl) processStore).getProcesses().size();
        QName name=null ;
        if(processCount!=0){
            for(int i=0;i<processCount;i++){
                if(processQnameLocalPart.equalsIgnoreCase(((ProcessStoreImpl) processStore).getProcesses().get(i).getLocalPart().toString())) {
                    name= ((ProcessStoreImpl) processStore).getProcesses().get(i);
                }
            }
            if(name!=null){
                ((ProcessStoreImpl) processStore).setState(name, ProcessState.DISABLED);
                return "successfully disabled "+processQnameLocalPart;
            } else{
                return "no process with specified name";
            }
        }   else{
            return "no processess deployed";
        }
    }
}

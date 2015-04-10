/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.api.leantask;

import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.NCName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.LeanTaskDocument;
import org.wso2.carbon.humantask.client.api.leantask.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.leantask.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.leantask.IllegalStateFault;
import org.wso2.carbon.humantask.client.api.leantask.LeanTaskClientAPIAdminSkeletonInterface;
import org.wso2.carbon.humantask.client.api.leantask.humantask.TLeanTask;
import org.wso2.carbon.humantask.client.api.leantask.namespace.LeanTaskDefinitions_type0;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.LeanTaskDAO;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.LeanTask;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.core.store.LeanTaskConfiguration;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Callable;

import static org.wso2.carbon.humantask.core.utils.DOMUtils.domToString;

/**
 * The implementation of the WS Human Task- Lean Task API Operations.
 */
public class LeanTaskOperationsImpl extends AbstractAdmin implements LeanTaskClientAPIAdminSkeletonInterface {
    private static Log log = LogFactory.getLog(LeanTaskOperationsImpl.class);

    public void createLeanTaskAsync(Object inputMessage, TLeanTask taskDefinition, NCName taskName) throws IllegalArgumentFault, IllegalAccessFault {

    }

    /**
     * register lean task definition
     *
     * @param taskDefinition
     * @return
     * @throws IllegalStateFault
     * @throws IllegalAccessFault
     */
    public NCName registerLeanTaskDefinition(TLeanTask taskDefinition) throws IllegalStateFault, IllegalAccessFault {

        final String taskname = String.valueOf(taskDefinition.getName());
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        final Element element;
        final String md5sum;
        TransformerLeanTaskUtils leanTaskUtil;
        LeanTaskDocument document;
        final boolean versionEnabled;


        try {

            leanTaskUtil = new TransformerLeanTaskUtils();
            document = leanTaskUtil.transformLeanTask(taskDefinition);
            //task definition OMElement
            element = leanTaskUtil.e;

            //ToDO check md5sum calculation
            //calculate md5sum
            md5sum = calChecksum(domToString(element));


            //read human task config file and set versionEnabled
            HumanTaskServerConfiguration taskServerConfiguration = HumanTaskServiceComponent.getHumanTaskServer().getServerConfig();
            versionEnabled = taskServerConfiguration.isLeanTaskVersioningEnabled();

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            daoConn.registerLeanTaskDef(versionEnabled, tenantId, taskname, element, md5sum);

                            return null;
                        }
                    });


        } catch (ADBException e) {
            log.error("A binding error occurred " + e);

        } catch (XmlException e) {
            log.error("Xml passing error occurred " + e);

        } catch (Exception e) {
            log.error("An error occurred while registering the lean task " + e);
        }

        return taskDefinition.getName();
    }

    public LeanTaskDefinitions_type0 listLeanTaskDefinitions() throws IllegalAccessFault {
        return null;
    }


    public NCName unregisterLeanTaskDefinition(final NCName taskName2) throws IllegalArgumentFault, IllegalAccessFault {
        boolean registered = false;
        LeanTask task = null;// the task taken from the memory module

        //check whether given task is registered

        try {
            List<LeanTask> resultSet = (List<LeanTask>) HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();


                            return daoConn.checkTaskIsRegistered(taskName2);
                        }
                    });


            if (!resultSet.isEmpty()) {

                HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                        execTransaction(new Callable<Object>() {
                            public Object call() throws Exception {
                                HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                                HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                                daoConn.unregisterTask(taskName2);

                                return null;
                            }
                        });


            } else {
                throw new IllegalArgumentFault("Task is not registered");

            }
        } catch (Exception e) {
            log.error("An error occurred while unregistering the lean task " + e);
        }

        //update memory module

        return taskName2;
    }

    /**
     * @param inputMessage4
     * @param taskDefinition5
     * @param taskName6       --- without the version
     * @return
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public Object createLeanTask(Object inputMessage4, TLeanTask taskDefinition5, NCName taskName6) throws IllegalArgumentFault, IllegalAccessFault {

        final String taskName = taskName6.toString();
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        final HumanTaskStore humanTaskStore = HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager().getHumanTaskStore(tenantId);

        if (taskName6 != null && taskDefinition5 != null) {
            throw new IllegalArgumentFault("Both Task Name and Task Definition cannot be set.");
        } else if (taskName6 != null) {
            try {
                //check the humanTaskStore for the stored map
                //if present return correspond task definition
                if (humanTaskStore.getLeanTaskConfiguration(taskName) == null) {
                    try {
                        HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                                execTransaction(new Callable<Void>() {
                                    public Void call() throws Exception {
                                        HumanTaskEngine humanTaskEngine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                                        HumanTaskDAOConnection daoConnection = humanTaskEngine.getDaoConnectionFactory().getConnection();
                                        LeanTaskDAO matchingLeanTask = daoConnection.retrieveByTaskName(taskName, tenantId);
                                        if (matchingLeanTask != null) {
                                            humanTaskStore.setLeanTaskConfigurationMap(taskName, matchingLeanTask);
                                            //create the lean task using task definition
                                        } else {
                                            log.error("Cannot find a matching LeanTaskDAO");
                                        }
                                        return null;
                                    }
                                });
                    } catch (Exception e) {
                        log.error("Exception occured", e);
                    }
                }

                //if not present return null
                else {
                    LeanTaskConfiguration leanTaskConfiguration = humanTaskStore.getLeanTaskConfiguration(taskName);
                    //create the lean task using task definition
                    log.info("Test for create lean task" + leanTaskConfiguration.getMessageSchema().toString());
                }
            } catch (Exception e) {
            }
        }
        return inputMessage4;
    }

    private String calChecksum(String text) throws NoSuchAlgorithmException {

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(text.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString(16);
        //need to zero pad it to get the full 32 chars.
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }


        return hashtext;


    }
}

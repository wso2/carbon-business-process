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

package org.wso2.carbon.humantask.core.engine;

import org.wso2.carbon.bpel.common.WSDLAwareMessage;
import org.wso2.carbon.humantask.core.api.scheduler.Scheduler;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnectionFactory;
import org.wso2.carbon.humantask.core.dao.TaskCreationContext;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.event.processor.EventProcessor;
import org.wso2.carbon.humantask.core.engine.runtime.api.ExpressionLanguageRuntime;
import org.wso2.carbon.humantask.core.engine.runtime.xpath.XPathExpressionRuntime;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The human task engine. The responsibility of this class is to invoke task creation logic.
 */
public class HumanTaskEngine {
    /**
     * The DAO connection factory as an abstraction to the underlying persistence implementation
     */
    private HumanTaskDAOConnectionFactory daoConnectionFactory;

    /**
     * The people query evaluator
     */
    private PeopleQueryEvaluator peopleQueryEvaluator;

    /** The expression  */
    private Map<String, ExpressionLanguageRuntime> expressionLanguageRuntimeRegistry;

    /** */
    private EventProcessor eventProcessor;

    /**
     * Deadline scheduler
     */
    private Scheduler scheduler;

    public HumanTaskEngine() {
        initExpressionLanguageRuntimes();
    }

    private void initExpressionLanguageRuntimes() {
        expressionLanguageRuntimeRegistry = new HashMap<String, ExpressionLanguageRuntime>();
        expressionLanguageRuntimeRegistry.put(XPathExpressionRuntime.ns, new XPathExpressionRuntime());
    }

    // create task logic.
    private TaskDAO createTask(WSDLAwareMessage message,
                               HumanTaskBaseConfiguration taskConfiguration, int tenantId)
            throws HumanTaskException {

        TaskCreationContext creationContext = new TaskCreationContext();
        creationContext.setTaskConfiguration(taskConfiguration);
        creationContext.setTenantId(tenantId);
        creationContext.setMessageBodyParts(message.getBodyPartElements());
        creationContext.setMessageHeaderParts(message.getHeaderPartElements());
        creationContext.setPeopleQueryEvaluator(peopleQueryEvaluator);

        return getDaoConnectionFactory().getConnection().createTask(creationContext);
    }

    /**
     * The invoke method called when the {@link org.wso2.carbon.humantask.core.integration.AxisHumanTaskMessageReceiver}
     * is called for task creation .
     *
     * @param message : The wsdl message containing the task creation logic.
     * @return : The task ID.
     * @throws HumanTaskException : If the task creation fails.
     */
    public String invoke(final WSDLAwareMessage message, final HumanTaskBaseConfiguration taskConfiguration) throws Exception {
        TaskDAO task = scheduler.execTransaction(new Callable<TaskDAO>() {
            public TaskDAO call() throws Exception {
                HumanTaskStore taskStore = HumanTaskServiceComponent.getHumanTaskServer().
                        getTaskStoreManager().getHumanTaskStore(message.getTenantId());
//                return createTask(message, taskStore.getTaskConfiguration(message.getPortTypeName(),
//                        message.getOperationName()),
//                        message.getTenantId());
                 return createTask(message, taskConfiguration, message.getTenantId());
            }
        });

        return task.getId().toString();
    }


    /**
     * @return : The {@link HumanTaskDAOConnectionFactory}
     */
    public HumanTaskDAOConnectionFactory getDaoConnectionFactory() {
        return daoConnectionFactory;
    }

    /**
     * @param daoConnectionFactory : The DAO Connection Factory.
     */
    public void setDaoConnectionFactory(HumanTaskDAOConnectionFactory daoConnectionFactory) {
        this.daoConnectionFactory = daoConnectionFactory;
    }

    /**
     * @param pqe : The people query evaluator to set.
     */
    public void setPeopleQueryEvaluator(PeopleQueryEvaluator pqe) {
        this.peopleQueryEvaluator = pqe;
    }

    /**
     * @return : The people query evaluator for people evaluations.
     */
    public PeopleQueryEvaluator getPeopleQueryEvaluator() {
        return peopleQueryEvaluator;
    }

    /**
     * @param language : The required type of the expression language.
     * @return : The ExpressionLanguageRuntime object.
     */
    public ExpressionLanguageRuntime getExpressionLanguageRuntime(String language) {
        ExpressionLanguageRuntime epxLanguageRuntime = null;
        if (expressionLanguageRuntimeRegistry != null) {
            epxLanguageRuntime = expressionLanguageRuntimeRegistry.get(language);
        }
        return epxLanguageRuntime;
    }

    /**
     * @return : The scheduler:
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @param scheduler : The Scheduler object to set.
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     *
     * @param eventProcessor
     */
    public void setEventProcessor(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     *
     * @return
     */
    public EventProcessor getEventProcessor() {
        return this.eventProcessor;
    }

}

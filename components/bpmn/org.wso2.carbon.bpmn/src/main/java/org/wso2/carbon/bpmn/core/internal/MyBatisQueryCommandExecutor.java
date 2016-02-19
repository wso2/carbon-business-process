package org.wso2.carbon.bpmn.core.internal;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;

public class MyBatisQueryCommandExecutor {
    private MyBatisExtendedSessionFactory myBatisExtendedSessionFactory;

    public MyBatisQueryCommandExecutor(ProcessEngineConfigurationImpl processEngineConfiguration, String mappingResourceName) {
        myBatisExtendedSessionFactory = new MyBatisExtendedSessionFactory();
        myBatisExtendedSessionFactory.initFromProcessEngineConfiguration(processEngineConfiguration, mappingResourceName);
    }

    public <T> T executeQueryCommand(Command<T> command) {
        return myBatisExtendedSessionFactory.getCommandExecutorTxRequired().execute(command);
    }

}


/**
 * Helper to initialize a minimized process engine which does all the transaction and MyBatis mapping stuff for us
 * and can be used to execute queries.
 */


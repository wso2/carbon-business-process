package org.wso2.carbon.bpmn.core;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.integration.BPSGroupManagerFactory;
import org.wso2.carbon.bpmn.core.integration.BPSUserManagerFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ActivitiEngineBuilder {

    private static final Log log = LogFactory.getLog(ActivitiEngineBuilder.class);

    public ProcessEngine buildEngine() throws BPSException {
        ProcessEngine engine = null;
        try {
            String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
            String activitiConfigPath = carbonConfigDirPath + File.separator + BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
            File activitiConfigFile = new File(activitiConfigPath);
            ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.
                    createProcessEngineConfigurationFromInputStream(new FileInputStream(activitiConfigFile));

            // we have to build the process engine first to initialize session factories.
            engine = configuration.buildProcessEngine();

            configuration.getSessionFactories().put(UserIdentityManager.class, new BPSUserManagerFactory());
            configuration.getSessionFactories().put(GroupIdentityManager.class, new BPSGroupManagerFactory());
            // TODO: add debug logs

        } catch (FileNotFoundException e) {
            String msg = "Failed to create an Activiti engine.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
        return engine;
    }
}

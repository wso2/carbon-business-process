/*
 *
 *  * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wso2.carbon.bpmn.core.deployment;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smackx.DefaultMessageEventRequestListener;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.bpmn.core.Utils;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNDeployment;
import org.wso2.carbon.bpmn.core.mgt.services.BPMNDeploymentService;
import org.wso2.carbon.bpmn.extensions.jms.*;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.api.Resource;


import javax.jms.MessageListener;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Manages BPMN deployments of a tenant.
 */
public class TenantRepository {

    private static final Log log = LogFactory.getLog(TenantRepository.class);
    private Integer tenantId;
    private File repoFolder;

    private HashMap<String, JMSListener> messageListeners = new HashMap<>();
//	private ActivitiDAO activitiDAO;

    public TenantRepository(Integer tenantId) {
        this.tenantId = tenantId;
//		this.activitiDAO = new ActivitiDAO();
    }

    public File getRepoFolder() {
        return repoFolder;
    }

    public void setRepoFolder(File repoFolder) {
        this.repoFolder = repoFolder;
    }

    /**
     * Deploys a BPMN package in the Activiti engine. Each BPMN package has an entry in the registry.
     * Checksum of the latest version of the BPMN package is stored in this entry.
     * This checksum is used to determine whether a package is a new deployment
     * (or a new version of an existing package) or a redeployment of an existing package.
     * We have to ignor the later case. If a package is a new deployment, it is deployed in the Activiti engine.
     *
     * @param deploymentContext DeploymentContext
     * @return true, if artifact was deployed, false, if the artifact has not changed & hence not deployed
     * @throws DeploymentException if deployment fails
     */
//	public boolean deploy(BPMNDeploymentContext deploymentContext) throws DeploymentException {
//        ZipInputStream archiveStream = null;
//
//        try {
//
//            String deploymentName =
//                    FilenameUtils.getBaseName(deploymentContext.getBpmnArchive().getName());
//
//            // Compare the checksum of the BPMN archive with the currently available checksum in the registry
//            // to determine whether this is a new deployment.
//            String checksum = "";
//            try {
//                checksum = Utils.getMD5Checksum(deploymentContext.getBpmnArchive());
//            } catch (IOException e) {
//                log.error("Checksum genration failed for IO operation",e);
//            } catch (NoSuchAlgorithmException e) {
//                log.error("Checksum genration Algorithm not found",e);
//            }
//
//            DeploymentMetaDataModel deploymentMetaDataModel =
//                    activitiDAO.selectTenantAwareDeploymentModel(tenantId.toString(), deploymentName);
//
//            if (log.isDebugEnabled()) {
//                log.debug("deploymentName=" + deploymentName + " checksum=" + checksum);
//                log.debug("deploymentMetaDataModel=" + deploymentMetaDataModel.toString());
//            }
//
//            if (deploymentMetaDataModel != null) {
//                if (checksum.equalsIgnoreCase(deploymentMetaDataModel.getCheckSum())) {
//                    return false;
//                }
//            }
//
//            ProcessEngineImpl engine =
//                    (ProcessEngineImpl) BPMNServerHolder.getInstance().getEngine();
//
//            RepositoryService repositoryService = engine.getRepositoryService();
//            DeploymentBuilder deploymentBuilder =
//                    repositoryService.createDeployment().tenantId(tenantId.toString()).
//                            name(deploymentName);
//            try {
//                archiveStream =
//                        new ZipInputStream(new FileInputStream(deploymentContext.getBpmnArchive()));
//            } catch (FileNotFoundException e) {
//                String errMsg = "Archive stream not found for BPMN repsoitory";
//                throw new DeploymentException(errMsg, e);
//            }
//
//            deploymentBuilder.addZipInputStream(archiveStream);
//            Deployment deployment = deploymentBuilder.deploy();
//
//            if (deploymentMetaDataModel == null) {
//
//                deploymentMetaDataModel = new DeploymentMetaDataModel();
//                deploymentMetaDataModel.setPackageName(deploymentName);
//                deploymentMetaDataModel.setCheckSum(checksum);
//                deploymentMetaDataModel.setTenantID(tenantId.toString());
//                deploymentMetaDataModel.setId(deployment.getId());
//
//                //call for insertion
//                this.activitiDAO.insertDeploymentMetaDataModel(deploymentMetaDataModel);
//            } else {
//                //call for update
//                deploymentMetaDataModel.setCheckSum(checksum);
//                this.activitiDAO.updateDeploymentMetaDataModel(deploymentMetaDataModel);
//            }
//
//        } finally {
//            if (archiveStream != null) {
//                try {
//                    archiveStream.close();
//                } catch (IOException e) {
//                    log.error("Could not close archive stream", e);
//                }
//            }
//        }
//
//        return true;
//    }

    public void deploy(BPMNDeploymentContext deploymentContext) throws DeploymentException {
        ZipInputStream archiveStream = null;

        try {

            String deploymentName = FilenameUtils.getBaseName(deploymentContext.getBpmnArchive().getName());

            // Compare the checksum of the BPMN archive with the currently available checksum in the registry to determine whether this is a new deployment.
            String checksum = Utils.getMD5Checksum(deploymentContext.getBpmnArchive());
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH + BPMNConstants.REGISTRY_PATH_SEPARATOR + deploymentName;
            Resource deploymentEntry = null;
            if (tenantRegistry.resourceExists(deploymentRegistryPath)) {
                deploymentEntry = tenantRegistry.get(deploymentRegistryPath);
            } else {
                // This is a new deployment
                deploymentEntry = tenantRegistry.newCollection();
            }

            String latestChecksum = deploymentEntry.getProperty(BPMNConstants.LATEST_CHECKSUM_PROPERTY);
            if (latestChecksum != null && checksum.equals(latestChecksum)) {
                // This is a server restart
                return;
            }
            deploymentEntry.setProperty(BPMNConstants.LATEST_CHECKSUM_PROPERTY, checksum);
            tenantRegistry.put(deploymentRegistryPath, deploymentEntry);

            // Deploy the package in the Activiti engine
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().tenantId(tenantId.toString()).name(deploymentName);
            archiveStream = new ZipInputStream(new FileInputStream(deploymentContext.getBpmnArchive()));

            deploymentBuilder.addZipInputStream(archiveStream);
            deploymentBuilder.deploy();

            /**
             * **************************** Code for JMSStartTask ***************************************
             */

            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(deploymentContext.getBpmnArchive()));

            List<ProcessDefinition> list = getDeployedProcessDefinitions();

            ZipEntry entry = zipInputStream.getNextEntry();
            String configFile = new String();
            int read = 0;
            int count = 0;
            String processID = null;

            while(entry != null) {
                byte[] bytesIn = new byte[4096];
                while ((read = zipInputStream.read(bytesIn)) != -1) {
                    configFile = configFile.concat(new String(Arrays.copyOf(bytesIn, read)));
                }

                processID = list.get(count++).getDeploymentId();

                Hashtable<String, String> paramList = readBPMNArchive(configFile);
                if(!paramList.isEmpty()){
                    JMSListener listener = new JMSListener(paramList.get(JMSConstants.JMS_PROVIDER), paramList);
                    messageListeners.put(processID, listener);
                }
                configFile = new String();
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }

            /**
             * **************************** End of code for JMSStartTask ***************************************
             */
        } catch (Exception e) {
            String errorMessage = "Failed to deploy the archive: " + deploymentContext.getBpmnArchive().getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            if (archiveStream != null) {
                try {
                    archiveStream.close();
                } catch (IOException e) {
                    log.error("Could not close archive stream", e);
                }
            }
        }
    }

    /**
     * Undeploys a BPMN package.
     * This may be called by the BPMN deployer, when a BPMN package is deleted from the deployment folder or by admin services
     *
     * @param deploymentName package name to be undeployed
     * @param force          forceful deletion of package
     */

//	public void undeploy(String deploymentName, boolean force) {
//
//        DeploymentMetaDataModel deploymentMetaDataModel;
//        SqlSession sqlSession = null;
//        try {
//            // Remove the deployment from the tenant's registry
//            deploymentMetaDataModel = activitiDAO
//                    .selectTenantAwareDeploymentModel(tenantId.toString(), deploymentName);
//
//            if ((deploymentMetaDataModel == null) && !force) {
//                String msg = "Deployment: " + deploymentName + " does not exist.";
//                log.warn(msg);
//                return;
//            }
//
//            ProcessEngineImpl engine = (ProcessEngineImpl) BPMNServerHolder.getInstance().getEngine();
//
//            DbSqlSessionFactory dbSqlSessionFactory =
//                    (DbSqlSessionFactory) engine.getProcessEngineConfiguration().
//                            getSessionFactories().get(DbSqlSession.class);
//
//            SqlSessionFactory sqlSessionFactory = dbSqlSessionFactory.getSqlSessionFactory();
//            sqlSession = sqlSessionFactory.openSession();
//            DeploymentMapper deploymentMapper = sqlSession.getMapper(DeploymentMapper.class);
//            int rowCount = deploymentMapper.deleteDeploymentMetaData(deploymentMetaDataModel);
//
//            if (log.isDebugEnabled()) {
//                log.debug("Total row count deleted=" + rowCount);
//            }
//
//            // Remove the deployment archive from the tenant's deployment folder
//            File deploymentArchive = new File(repoFolder, deploymentName + ".bar");
//            FileUtils.deleteQuietly(deploymentArchive);
//
//            // Delete all versions of this package from the Activiti engine.
//            RepositoryService repositoryService = engine.getRepositoryService();
//            List<Deployment> deployments = repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString()).
//                                           deploymentName(deploymentName).list();
//            for (Deployment deployment : deployments) {
//                repositoryService.deleteDeployment(deployment.getId());
//            }
//
//            //commit metadata
//            sqlSession.commit();
//        } finally {
//            if (sqlSession != null) {
//                sqlSession.close();
//            }
//        }
//
//	}

    public void undeploy(String deploymentName, boolean force) throws BPSFault {

        try {
            // Remove the deployment from the tenant's registry
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH + BPMNConstants.REGISTRY_PATH_SEPARATOR + deploymentName;
            if (!tenantRegistry.resourceExists(deploymentRegistryPath) && !force) {
                String msg = "Deployment: " + deploymentName + " does not exist.";
                log.warn(msg);
                return;
            }
            tenantRegistry.delete(deploymentRegistryPath);

            // Remove the deployment archive from the tenant's deployment folder
            File deploymentArchive = new File(repoFolder, deploymentName + ".bar");
            FileUtils.deleteQuietly(deploymentArchive);

            // Delete all versions of this package from the Activiti engine.
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();
            List<Deployment> deployments =
                    repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString()).deploymentName(deploymentName).list();
            for (Deployment deployment : deployments) {

                JMSListener listener = messageListeners.get(deployment.getId());
                if(listener != null){
                    messageListeners.remove(deployment.getId());
                    //stop the listener from listening to the destination...

                    log.info("Listener for process with process ID " + deployment.getId() + " stopped listening...");
                }
                repositoryService.deleteDeployment(deployment.getId(), true);
            }

        } catch (RegistryException e) {
            String msg = "Failed to undeploy BPMN deployment: " + deploymentName + " for tenant: " + tenantId;
            log.error(msg, e);
            throw new BPSFault(msg, e);
        }
    }

    public List<Deployment> getDeployments() /*throws BPSFault*/ {

        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        List<Deployment> tenantDeployments = engine.getRepositoryService().createDeploymentQuery().
                deploymentTenantId(tenantId.toString()).list();

        return tenantDeployments;
    }

    public List<ProcessDefinition> getDeployedProcessDefinitions() throws BPSFault {

        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        return engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId.toString()).list();
    }

    /**
     * Information about BPMN deployments are recorded in 3 places:
     * Activiti database, Registry and the file system (deployment folder).
     * If information about a particular deployment is not recorded in all these 3 places, BPS may not work correctly.
     * Therefore, this method checks whether deployments are recorded in all these places and undeploys packages, if
     * they are missing in few places in an inconsistent way.
     * <p/>
     * As there are 3 places, there are 8 ways a package can be placed. These cases are handled as follows:
     * (1) Whenever a package is not in the deployment folder, it is undeploye (this covers 4 combinations).
     * (2) If a package is in all 3 places, it is a proper deployment and it is left untouched.
     * (3) If a package is only in the deployment folder, it is a new deployment. This will be handled by the deployer.
     * (4) If a package is in the deployment folder AND it is in either registry or Activiti DB (but not both), then it is an inconsistent deployment. This will be undeployed.
     *
     *
     * */
//    public void fixDeployments() {
//
//        // get all deployments in the deployment folder
//        List<String> fileArchiveNames = new ArrayList<String>();
//        File[] fileDeployments = repoFolder.listFiles();
//        if (fileDeployments != null) {
//            for (File fileDeployment : fileDeployments) {
//                String deploymentName = FilenameUtils.getBaseName(fileDeployment.getName());
//                fileArchiveNames.add(deploymentName);
//            }
//        } else {
//            log.error("File deployments returned null for tenant" + tenantId);
//        }
//
//
//        // get all deployments in the Activiti DB
//        List<String> activitiDeploymentNames = new ArrayList<String>();
//        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
//        RepositoryService repositoryService = engine.getRepositoryService();
//        List<Deployment> tenantDeployments =
//                repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString())
//                        .list();
//        for (Deployment deployment : tenantDeployments) {
//            String deploymentName = deployment.getName();
//            activitiDeploymentNames.add(deploymentName);
//        }
//
//        // get all deployments in the registry
//        List<String> metaDataDeploymentNames = new ArrayList<String>();
//        List<DeploymentMetaDataModel> deploymentMetaDataModelList =
//                activitiDAO.selectAllDeploymentModel();
//
//        int deploymentMetaDataModelListSize = deploymentMetaDataModelList.size();
//
//        for (int i = 0; i < deploymentMetaDataModelListSize; i++) {
//            DeploymentMetaDataModel deploymentMetaDataModel =
//                    deploymentMetaDataModelList.get(i);
//
//            if (deploymentMetaDataModel != null) {
//                String deploymentMetadataName = deploymentMetaDataModel.getPackageName();
//                metaDataDeploymentNames.add(deploymentMetadataName);
//            }
//        }
//
//        // construct the union of all deployments
//        Set<String> allDeploymentNames = new HashSet<String>();
//        allDeploymentNames.addAll(fileArchiveNames);
//        allDeploymentNames.addAll(activitiDeploymentNames);
//        allDeploymentNames.addAll(metaDataDeploymentNames);
//
//        for (String deploymentName : allDeploymentNames) {
//
//            if (!(fileArchiveNames.contains(deploymentName))) {
//                if (log.isDebugEnabled()) {
//                    log.debug(deploymentName +
//                            " has been removed from the deployment folder. Undeploying the package...");
//                }
//                undeploy(deploymentName, true);
//            } else {
//                if (activitiDeploymentNames.contains(deploymentName) &&
//                        !metaDataDeploymentNames.contains(deploymentName)) {
//                    if (log.isDebugEnabled()) {
//                        log.debug(deploymentName +
//                                " is missing in the registry. Undeploying the package to avoid inconsistencies...");
//                    }
//                    undeploy(deploymentName, true);
//                }
//
//                if (!activitiDeploymentNames.contains(deploymentName) &&
//                        metaDataDeploymentNames.contains(deploymentName)) {
//                    if (log.isDebugEnabled()) {
//                        log.debug(deploymentName +
//                                " is missing in the BPS database. Undeploying the package to avoid inconsistencies...");
//                    }
//                    undeploy(deploymentName, true);
//                }
//            }
//        }
//    }
    public void fixDeployments() throws BPSFault {

        // get all deployments in the deployment folder
        List<String> fileArchiveNames = new ArrayList<String>();
        File[] fileDeployments = repoFolder.listFiles();
        for (File fileDeployment : fileDeployments) {
            String deploymentName = FilenameUtils.getBaseName(fileDeployment.getName());
            fileArchiveNames.add(deploymentName);
        }

        // get all deployments in the Activiti DB
        List<String> activitiDeploymentNames = new ArrayList<String>();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RepositoryService repositoryService = engine.getRepositoryService();

        /**
         * ************************* Code for the JMSStartTask *********************************
         */

        //Create a DataHolder instance to read the default jms config information from activiti.xml file.
        JMSDataHolder dataHolder = JMSDataHolder.getInstance();

        JMSConnectionFactoryManager factoryManager = JMSConnectionFactoryManager.getInstance();

        //get the default parameters the data holder object has.
        HashMap<String, Hashtable<String, String>>  jmsProperties = dataHolder.getJmsProperties();
        Set<String> keys = jmsProperties.keySet();
        Iterator<String> propKeys = keys.iterator();
        String propKey;
        while(propKeys.hasNext()){
            propKey = propKeys.next();
            Hashtable<String, String> tempMap = jmsProperties.get(propKey);
            factoryManager.initializeConnectionFactories(propKey, tempMap);
        }

        BPMNDeploymentService deploymentService = new BPMNDeploymentService();
        BPMNDeployment deployments[] = deploymentService.getDeployments();

        BufferedReader reader = null;
        StringBuilder stringBuilder = null;
        String line;
        Hashtable<String, String> paramList = null;

        for (int i = 0; i < deployments.length; i++) {
            List<String> names = repositoryService.getDeploymentResourceNames(deployments[i].getDeploymentId());
            for (int j = 0; j < names.size(); j++) {
                if (names.get(j).endsWith(".xml")) {
                    InputStream fileStream = repositoryService.getResourceAsStream(deployments[i].getDeploymentId(),
                            names.get(j));
                    stringBuilder = new StringBuilder();
                    try {
                        reader = new BufferedReader(new InputStreamReader(fileStream));
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        String configFile = stringBuilder.toString();

                        paramList = readBPMNArchive(configFile);

                        if(!paramList.isEmpty()){
                            JMSListener listener = new JMSListener(paramList.get(JMSConstants.JMS_PROVIDER), paramList);
                            messageListeners.put(deployments[i].getDeploymentId(), listener);
                        }

                    } catch (IOException e) {
                        log.error(e.getMessage());
                    } catch (BPMNJMSException e) {
                        log.error(e.getMessage());
                    } catch (XMLStreamException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }

        /**
         * ************************* End of code for the JMSStartTask *********************************
         */

        List<Deployment> tenantDeployments = repositoryService.createDeploymentQuery().
                deploymentTenantId(tenantId.toString()).list();
        for (Deployment deployment : tenantDeployments) {
            String deploymentName = deployment.getName();
            activitiDeploymentNames.add(deploymentName);
        }

        // get all deployments in the registry
        List<String> registryDeploymentNames = new ArrayList<String>();
        try {
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH;
            if (tenantRegistry.resourceExists(deploymentRegistryPath)) {
                Collection registryDeployments = (Collection) tenantRegistry.get(deploymentRegistryPath);
                String[] deploymentPaths = registryDeployments.getChildren();
                for (String deploymentPath : deploymentPaths) {
                    String deploymentName = deploymentPath.substring(deploymentPath.lastIndexOf("/") + 1,
                            deploymentPath.length());
                    registryDeploymentNames.add(deploymentName);
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to obtain BPMN deployments from the Registry.";
            log.error(msg, e);
            throw new BPSFault(msg, e);
        }

        // construct the union of all deployments
        Set<String> allDeploymentNames = new HashSet<String>();
        allDeploymentNames.addAll(fileArchiveNames);
        allDeploymentNames.addAll(activitiDeploymentNames);
        allDeploymentNames.addAll(registryDeploymentNames);

        for (String deploymentName : allDeploymentNames) {
            try {
                if (!(fileArchiveNames.contains(deploymentName))) {
                    if (log.isDebugEnabled()) {
                        log.debug(deploymentName + " has been removed from the deployment folder. " +
                                "Undeploying the package...");
                    }
                    undeploy(deploymentName, true);
                } else {
                    if (activitiDeploymentNames.contains(deploymentName) && !registryDeploymentNames.
                            contains(deploymentName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(deploymentName + " is missing in the registry. Undeploying the " +
                                    "package to avoid inconsistencies...");
                        }
                        undeploy(deploymentName, true);
                    }

                    if (!activitiDeploymentNames.contains(deploymentName) && registryDeploymentNames.
                            contains(deploymentName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(deploymentName + " is missing in the BPS database. Undeploying the " +
                                    "package to avoid inconsistencies...");
                        }
                        undeploy(deploymentName, true);
                    }
                }
            } catch (BPSFault e) {
                String msg = "Failed undeploy inconsistent deployment: " + deploymentName;
                log.error(msg, e);
                throw new BPSFault(msg, e);
            }
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws BPMNJMSException
     * @throws XMLStreamException
     */

    private Hashtable<String, String> readBPMNArchive(String file) throws BPMNJMSException, XMLStreamException {
        Hashtable<String, String> paramList = new Hashtable<>();
        String jmsProviderID = null;
        String destinationName = null;
        String destinationType = null;
        String username = null;
        String password = null;
        String cacheLevel = null;
        String outputMappingsString = null;
        String onError = null;
        String contentType = null;
        String sessionTransacted = null;
        String sessionAck = null;
        String cacheUserTrans = null;
        String initReconDuration = null;
        String reconProgFact = null;
        String maxReconDuration = null;

        OMElement configElement = AXIOMUtil.stringToOM(file);

        Iterator processes = configElement.getChildrenWithName(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL",
                "process"));

        while (processes.hasNext()) {

            OMElement process = (OMElement) processes.next();
            String processId = process.getAttributeValue(new QName(null, "id"));
            if (processId.equals("startProcess")) {
                Iterator startEvent = process.getChildrenWithName
                        (new QName("http://www.omg.org/spec/BPMN/20100524/MODEL", "startEvent"));

                while (startEvent.hasNext()) {
                    OMElement startElement = (OMElement) startEvent.next();

                    Iterator extensionElements = startElement.getChildrenWithName
                            (new QName("http://www.omg.org/spec/BPMN/20100524/MODEL", "extensionElements"));
                    while (extensionElements.hasNext()) {
                        OMElement exeElements = (OMElement) extensionElements.next();
                        Iterator listeners = exeElements.getChildrenWithName
                                (new QName("http://activiti.org/bpmn", "executionListener"));

                        if(listeners.hasNext()){
                            OMElement execListener = (OMElement)listeners.next();
                            String className = execListener.getAttributeValue(new QName(null, "class"));
                            if(JMSConstants.JMS_START_TASK.equals(className)){
                                Iterator children = exeElements.getChildrenWithNamespaceURI("https://www.wso2.com");

                                while (children.hasNext()) {
                                    OMElement child = (OMElement) children.next();

                                    switch (child.getLocalName()){
                                        case JMSConstants.JMS_PROVIDER:
                                            jmsProviderID = child.getText();
                                            break;

                                        case JMSConstants.JMS_DESTINATION_TYPE:
                                            destinationType = child.getText();
                                            break;

                                        case JMSConstants.JMS_DESTINATION_NAME:
                                            destinationName = child.getText();
                                            break;

                                        case JMSConstants.JMS_CONNECTION_USERNAME:
                                            username = child.getText();
                                            break;

                                        case JMSConstants.JMS_CONNECTION_PASSWORD:
                                            password = child.getText();
                                            break;

                                        case JMSConstants.JMS_OUTPUT_MAPPINGS:
                                            outputMappingsString = child.getText();
                                            break;

                                        case JMSConstants.JMS_ON_ERROR:
                                            onError = child.getText();
                                            break;

                                        case JMSConstants.JMS_CONTENT_TYPE:
                                            contentType = child.getText();
                                            break;

                                        case JMSConstants.JMS_SESSION_TRANSACTED:
                                            sessionTransacted = child.getText();
                                            break;

                                        case JMSConstants.JMS_SESSION_ACK:
                                            sessionAck = child.getText();
                                            break;

                                        case JMSConstants.JMS_CACHE_LEVEL:
                                            cacheLevel = child.getText();

                                        case JMSConstants.JMS_USER_TRANSACTION:
                                            cacheUserTrans = child.getText();
                                            break;

                                        case JMSConstants.JMS_INIT_RECONN_DURATION:
                                            initReconDuration = child.getText();
                                            break;

                                        case JMSConstants.JMS_RECON_PROGRESS_FACTORY:
                                            reconProgFact = child.getText();
                                            break;

                                        case JMSConstants.JMS_MAX_RECON_DURATION:
                                            maxReconDuration = child.getText();
                                            break;
                                    }
                                }

                                HashMap<String, Hashtable<String, String>> map = JMSDataHolder.getInstance().getJmsProperties();
                                Hashtable<String, String> params;

                                if(jmsProviderID == null){
                                    String providerNotFoundErrorMsg = "JMS Provider ID is not provided. " +
                                            "jmsProviderID must be provided.";
                                    throw new BPMNJMSException(providerNotFoundErrorMsg);
                                }else{
                                   paramList.put(JMSConstants.JMS_PROVIDER, jmsProviderID);
                                }
                                if(destinationType == null){
                                    String destTypeNotFoundErrorMsg = "JMS Destination Type is not provided. " +
                                            "destinationType must be provided.";
                                    throw new BPMNJMSException(destTypeNotFoundErrorMsg);
                                }else{
                                    paramList.put(JMSConstants.PARAM_DESTINATION_TYPE, destinationType);
                                    if(JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)){
                                        params = map.get(JMSConstants.JMS_QUEUE_CONNECTION_FACTORY);
                                    }else{
                                        params = map.get(JMSConstants.JMS_TOPIC_CONNECTION_FACTORY);
                                    }
                                }
                                if(destinationName == null){
                                    String destNameNotFoundErrorMsg = "JMS Destination Name is not provided. " +
                                            "destination must be provided.";
                                    throw new BPMNJMSException(destNameNotFoundErrorMsg);
                                }else{
                                    paramList.put(JMSConstants.PARAM_DESTINATION, destinationName);
                                }

                                if(username != null){
                                    paramList.put(JMSConstants.PARAM_USERNAME, username);
                                }

                                if(password != null){
                                    paramList.put(JMSConstants.PARAM_PASSWORD, password);
                                }

                                if(cacheLevel != null){
                                    paramList.put(JMSConstants.PARAM_CACHE_LEVEL, cacheLevel);
                                }

                                if(outputMappingsString != null){
                                    paramList.put(JMSConstants.PARAM_OUTPUT_MAPPINGS, outputMappingsString);
                                }

                                if(onError != null){
                                    paramList.put(JMSConstants.PARAM_ON_ERROR, onError);
                                }
                                if(contentType != null){
                                    paramList.put(JMSConstants.PARAM_CONTENT_TYPE, contentType);
                                }if(sessionAck != null){
                                    paramList.put(JMSConstants.PARAM_SESSION_ACK, sessionAck);
                                }else{
                                    paramList.put(JMSConstants.PARAM_SESSION_ACK, params.get
                                            (JMSConstants.PARAM_SESSION_ACK));
                                }if(sessionTransacted != null){
                                    paramList.put(JMSConstants.PARAM_SESSION_TRANSACTED, sessionTransacted);
                                }else{
                                    paramList.put(JMSConstants.PARAM_SESSION_TRANSACTED,
                                            params.get(JMSConstants.PARAM_SESSION_TRANSACTED));
                                }if(cacheUserTrans != null){
                                    paramList.put(JMSConstants.PARAM_CACHE_USER_TRX, cacheUserTrans);
                                }else{
                                    paramList.put(JMSConstants.PARAM_CACHE_USER_TRX,
                                            params.get(JMSConstants.PARAM_CACHE_USER_TRX));
                                }if(initReconDuration != null){
                                    paramList.put(JMSConstants.PARAM_INIT_RECON_DURATION, initReconDuration);
                                }else{
                                    paramList.put(JMSConstants.PARAM_INIT_RECON_DURATION,
                                            params.get(JMSConstants.PARAM_INIT_RECON_DURATION));
                                }if(reconProgFact != null){
                                    paramList.put(JMSConstants.PARAM_RECON_PROGRESS_FACT, reconProgFact);
                                }else{
                                    paramList.put(JMSConstants.PARAM_RECON_PROGRESS_FACT,
                                            params.get(JMSConstants.PARAM_RECON_PROGRESS_FACT));
                                }if(maxReconDuration != null){
                                    paramList.put(JMSConstants.PARAM_MAX_RECON_DURATION, maxReconDuration);
                                }else{
                                    paramList.put(JMSConstants.PARAM_MAX_RECON_DURATION,
                                            params.get(JMSConstants.PARAM_MAX_RECON_DURATION));
                                }
                            }
                        }
                    }
                }
            }
        }
        return paramList;
    }
}



package org.wso2.carbon.humantask.core.store;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.humantask.LeanTaskDocument;
import org.wso2.carbon.humantask.TDeadlines;
import org.wso2.carbon.humantask.TPresentationElements;
import org.wso2.carbon.humantask.TPriorityExpr;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.utils.HumanTaskNamespaceContext;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yasima on 3/18/15.
 */
public class LeanTaskconfig extends LeanTaskConfiguration {

    private long id;
    private long version;
    private LeanTaskDocument leanTaskObject;
    private String defaultExpressionLanguage = HumanTaskConstants.WSHT_EXP_LANG_XPATH20;
    private HumanTaskNamespaceContext namespaceContext = new HumanTaskNamespaceContext();
    private String leanTaskArtifactName;
    private boolean isLeantask;
    private AxisConfiguration tenantAxisConf;
    private String packageName;
    private File leanTaskDefinitionFile;
    private List<EndpointConfiguration> endpointConfigs = new ArrayList<EndpointConfiguration>();
    private TaskPackageStatus packageStatus = TaskPackageStatus.ACTIVE;
    private boolean isErroneous = false;
    private String deploymentError = "NONE";


    public LeanTaskconfig(LeanTaskDocument leanTaskObject,
                          String leanTaskArtifactName,
                          boolean isLeantask
    ){

        this.leanTaskObject = leanTaskObject;
        this.leanTaskArtifactName = leanTaskArtifactName;
        this.isLeantask = isLeantask;
        this.packageStatus = TaskPackageStatus.ACTIVE;


    }

    @Override
    public QName getPortType() {
        return null;
    }

    @Override
    public String getOperation() {
        return null;
    }

    @Override
    public TPresentationElements getPresentationElements() {
        return null;
    }

    @Override
    public QName getName() {
        return null;
    }

    @Override
    public QName getServiceName() {
        return null;
    }

    @Override
    public String getPortName() {
        return null;
    }

    @Override
    public TPriorityExpr getPriorityExpression() {
        return null;
    }

    @Override
    public TDeadlines getDeadlines() {
        return null;
    }

    @Override
    public ConfigurationType getConfigurationType() {
        return null;
    }

    @Override
    public QName getDefinitionName() {
        return null;
    }
}

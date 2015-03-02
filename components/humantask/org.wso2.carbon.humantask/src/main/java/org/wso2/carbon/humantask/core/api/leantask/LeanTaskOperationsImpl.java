package org.wso2.carbon.humantask.core.api.leantask;

import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.NCName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.LeanTaskDocument;
import org.wso2.carbon.humantask.client.api.leantask.*;
import org.wso2.carbon.humantask.client.api.leantask.humantask.TLeanTask;
import org.wso2.carbon.humantask.client.api.leantask.namespace.LeanTaskDefinitions_type0;
import org.wso2.carbon.humantask.core.store.LeanTaskBaseConfguration;

import javax.management.modelmbean.XMLParseException;

/**
 * Created with IntelliJ IDEA.
 * User: suba
 * Date: 2/24/15
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class LeanTaskOperationsImpl extends AbstractAdmin implements LeanTaskServiceSkeletonInterface {

    private static Log log = LogFactory.getLog(LeanTaskOperationsImpl.class);

    public NCName registerLeanTaskDefinition(TLeanTask taskDefinition0)
            throws IllegalStateFault, IllegalAccessFault {

        TransformerLeanTaskUtils leanTaskUtil = new TransformerLeanTaskUtils();
        try {
            LeanTaskDocument document = leanTaskUtil.transformLeanTask(taskDefinition0);
        } catch (ADBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // LeanTaskBaseConfguration leanTaskConfig = new LeanTaskBaseConfguration() ;
        // leanTaskConfig.createLeantaskConfig(document);


        return null;
    }

    public LeanTaskDefinitions_type0 listLeanTaskDefinitions() throws IllegalAccessFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void createLeanTaskAsync(Object inputMessage, TLeanTask taskDefinition, NCName taskName) throws IllegalArgumentFault, IllegalAccessFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NCName unregisterLeanTaskDefinition(NCName taskName2)
            throws IllegalArgumentFault, IllegalAccessFault {
        return taskName2;
    }

    public Object createLeanTask(Object inputMessage4, TLeanTask taskDefinition5, NCName taskName6)
            throws IllegalArgumentFault, IllegalAccessFault {
        return inputMessage4;
    }


}












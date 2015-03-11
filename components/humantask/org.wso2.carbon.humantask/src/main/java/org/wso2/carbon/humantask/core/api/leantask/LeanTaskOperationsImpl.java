package org.wso2.carbon.humantask.core.api.leantask;

import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.NCName;
import org.apache.xmlbeans.XmlException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.leantask.client.api.*;
import org.wso2.carbon.humantask.leantask.client.api.humantask.TLeanTask;
import org.wso2.carbon.humantask.leantask.client.api.namespace.LeanTaskDefinitions_type0;

/**
 * Created with IntelliJ IDEA.
 * User: suba
 * Date: 3/4/15
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class LeanTaskOperationsImpl extends AbstractAdmin implements LeanTaskClientAPIAdminSkeletonInterface {

    public void createLeanTaskAsync(Object inputMessage, TLeanTask taskDefinition, NCName taskName) throws IllegalArgumentFault, IllegalAccessFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NCName registerLeanTaskDefinition(TLeanTask taskDefinition) throws IllegalStateFault, IllegalAccessFault {
        return taskDefinition.getName();
    }

    public LeanTaskDefinitions_type0 listLeanTaskDefinitions() throws IllegalAccessFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public NCName unregisterLeanTaskDefinition(NCName taskName2) throws IllegalArgumentFault, IllegalAccessFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object createLeanTask(Object inputMessage4, TLeanTask taskDefinition5, NCName taskName6) throws IllegalArgumentFault, IllegalAccessFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}

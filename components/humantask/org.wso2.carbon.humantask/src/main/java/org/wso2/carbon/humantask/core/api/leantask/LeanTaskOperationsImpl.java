package org.wso2.carbon.humantask.core.api.leantask;

import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.NCName;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.client.api.leantask.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.leantask.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.leantask.IllegalStateFault;
import org.wso2.carbon.humantask.client.api.leantask.LeanTaskClientAPIAdminSkeletonInterface;
import org.wso2.carbon.humantask.client.api.leantask.humantask.TLeanTask;
import org.wso2.carbon.humantask.client.api.leantask.namespace.LeanTaskDefinitions_type0;
import org.wso2.carbon.humantask.LeanTaskDocument;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import java.util.concurrent.Callable;


/**
 * Created with IntelliJ IDEA.
 * User: suba
 * Date: 3/4/15
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class LeanTaskOperationsImpl extends AbstractAdmin implements LeanTaskClientAPIAdminSkeletonInterface{

    public void createLeanTaskAsync(Object inputMessage, TLeanTask taskDefinition, NCName taskName) throws IllegalArgumentFault, IllegalAccessFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NCName registerLeanTaskDefinition(TLeanTask taskDefinition) throws IllegalStateFault, IllegalAccessFault {

        final String name= String.valueOf(taskDefinition.getName());
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();


        try {
            TransformerLeanTaskUtils leanTaskUtil = new TransformerLeanTaskUtils();
            LeanTaskDocument document = leanTaskUtil.transformLeanTask(taskDefinition);
            final Element e=leanTaskUtil.e;

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            daoConn.createLeanTaskDef(tenantId,name,e);
                            return null;
                        }
                    });

        } catch (ADBException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
            } catch (Exception e) {
        e.printStackTrace();
    }

        return taskDefinition.getName();
    }

    public LeanTaskDefinitions_type0 listLeanTaskDefinitions() throws IllegalAccessFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public NCName unregisterLeanTaskDefinition(NCName taskName2) throws IllegalArgumentFault, IllegalAccessFault {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object createLeanTask(Object inputMessage4, TLeanTask taskDefinition5, NCName taskName6) throws IllegalArgumentFault, IllegalAccessFault {


        if(!taskName6.equals(null)&&taskDefinition5.equals(null)){


        }


        return inputMessage4;
    }

}

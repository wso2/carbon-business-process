package org.wso2.carbon.humantask.core.api.leantask;

import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.NCName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.TDeadlines;
import org.wso2.carbon.humantask.TPresentationElements;
import org.wso2.carbon.humantask.TPriorityExpr;
import org.wso2.carbon.humantask.client.api.leantask.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.leantask.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.leantask.IllegalStateFault;
import org.wso2.carbon.humantask.client.api.leantask.LeanTaskClientAPIAdminSkeletonInterface;
import org.wso2.carbon.humantask.client.api.leantask.humantask.TLeanTask;
import org.wso2.carbon.humantask.client.api.leantask.namespace.LeanTaskDefinitions_type0;
import org.wso2.carbon.humantask.LeanTaskDocument;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.LeanTaskCreationContext;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.HumanTaskDAOConnectionImpl;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.LeanTask;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.core.store.LeanTaskConfiguration;
import org.wso2.carbon.humantask.core.store.LeanTaskconfig;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.humantask.core.utils.HumanTaskStoreUtils;


public class LeanTaskOperationsImpl extends AbstractAdmin implements LeanTaskClientAPIAdminSkeletonInterface{

    private static Log log = LogFactory.getLog(LeanTaskOperationsImpl.class);

    public static final String PROPERTY_FILE_NAME="config.properties";
    public static final String key_versioningRequired="versioningRequired";
    public static final String key_dbConnection="dbConnection";
    public static final String key_mysqlUserName="mysqlUserName";
    public static final String key_mysqlPass="mysqlPass";
    String dbConnection;
    String versioningRequired ;
    String mysqlUserName ;
    String mysqlPass;
    boolean isVersionRequired;

    public void createLeanTaskAsync(Object inputMessage, TLeanTask taskDefinition, NCName taskName) throws IllegalArgumentFault, IllegalAccessFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NCName registerLeanTaskDefinition(TLeanTask taskDefinition) throws IllegalStateFault, IllegalAccessFault {

        final String taskname= String.valueOf(taskDefinition.getName());
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        boolean registered=false;
        final Element element;
        final String md5sum;
        LeanTask task = null;// the task taken from the memory module
        TransformerLeanTaskUtils leanTaskUtil;
        LeanTaskDocument document;
        int version;



        try {
            loadProperties();
            leanTaskUtil = new TransformerLeanTaskUtils();
            document = leanTaskUtil.transformLeanTask(taskDefinition);
            element = leanTaskUtil.e;

            //calculate md5sum
            System.out.print(String.valueOf(taskDefinition.getName()));
            md5sum = calChecksum(String.valueOf(taskDefinition.getName()));


            //first need to check the memory module to see whether the given task is registered before.
            //get the latest task def
            if (registered) {

                //need to check the task status

                if (task.getStatus().equals(TaskPackageStatus.ACTIVE)) {


                    if(isVersionRequired){
                    //if it is then md5sum should be checked.
                    //if the checksums are same ignore the operation
                    //else task def should be given a new version number and retire the prevailing task def.
                    if (!task.getmd5sum().equals(md5sum)) {

                        version = persistLeanTask(tenantId, taskname, element, md5sum);
                        //add the lean task to the memory module


                        task.setTaskStatus(TaskPackageStatus.RETIRED);


                        Connection con = getdbConnection();

                        PreparedStatement update = con.prepareStatement
                                ("update HT_LEANTASK set STATUS='" + task.getStatus() + "' where LEANTASK_VERSION='" + task.getVersion() + "'");
                        update.executeUpdate();

                        //update the memory module
                        con.close();
                    }
                        else{

                        throw new IllegalStateFault("Task is already registered.");
                    }


                }


            } else if (task.getStatus().equals(TaskPackageStatus.RETIRED)) {
                    if (task.getmd5sum().equals(calChecksum(taskDefinition.toString()))) {
                        // set the task status to active (in memory module)
                        task.setTaskStatus(TaskPackageStatus.ACTIVE);

                        Connection con = getdbConnection();
                        PreparedStatement update = con.prepareStatement
                                ("update HT_LEANTASK set STATUS='" + task.getStatus() + "' where LEANTASK_VERSION='" + task.getVersion() + "'");
                        update.executeUpdate();
                        con.close();

                    } else {


                        persistLeanTask(tenantId, taskname, element, md5sum);
                        //add task to the memory module

                    }

                }


            } else {

                persistLeanTask(tenantId, taskname, element, md5sum);
                //add task to the memory module


            }

        }catch (ADBException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
            } catch (Exception e) {
        e.printStackTrace();
    }

        return taskDefinition.getName();
    }



    public LeanTaskDefinitions_type0 listLeanTaskDefinitions() throws IllegalAccessFault {

        //list active lean task definitions

        //from memory module?

        return null;
    }


    public NCName unregisterLeanTaskDefinition(NCName taskName2) throws IllegalArgumentFault, IllegalAccessFault {

        boolean registered=false;
        LeanTask task = null;// the task taken from the memory module

        //check whether given task is registered
        //check memory module

        if(registered&& task.getStatus().equals(TaskPackageStatus.ACTIVE)){

            task.setTaskStatus(TaskPackageStatus.RETIRED);
            Connection con = null;
            try {
                con = getdbConnection();
                PreparedStatement update = con.prepareStatement
                        ("update HT_LEANTASK set STATUS='"+ task.getStatus()+"' where LEANTASK_VERSION='"+task.getVersion()+"'");
                update.executeUpdate();
                con.close();

                //should move any instances of lean tasks of this task definition to “Error” state
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        else if(registered&& task.getStatus().equals(TaskPackageStatus.RETIRED)){




        }

        else if(!registered){


                throw new IllegalArgumentFault("Task is not registered");


        }
        //update memory module

        return taskName2;
    }

    public Object createLeanTask(Object inputMessage4, TLeanTask taskDefinition5, NCName taskName6) throws IllegalArgumentFault, IllegalAccessFault {


         // check whether both task name and task definition are set
        //if so throw illegalArgumentFault
       /* if (!taskName6.equals(null) && !taskDefinition5.equals(null)) {
            throw new IllegalAccessFault("Both Task Name and Task Definition cannot be set.");
        }

        //if task name is set then should check whether a lean task definition is registered in that name
        else if (!taskName6.equals(null)) {


        }

        // if task definition is set then it should be used to create the len task
        else if (!taskDefinition5.equals(null)) {*/

            try {
                 registerLeanTaskDefinition(taskDefinition5);
                final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

                TransformerLeanTaskUtils leanTaskUtil = new TransformerLeanTaskUtils();
                LeanTaskDocument document = leanTaskUtil.transformLeanTask(taskDefinition5);

                LeanTaskconfig taskConfiguration=new LeanTaskconfig(document,String.valueOf(taskDefinition5.getName()),true) ;
                final LeanTaskCreationContext  creationContext = new LeanTaskCreationContext();
                creationContext.setTaskConfiguration(taskConfiguration);
                creationContext.setTenantId(tenantId);
                creationContext.addMessageBodyPart("input", (Element) inputMessage4);


                HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                        execTransaction(new Callable<Object>() {
                            public Object call() throws Exception {
                HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                HumanTaskDAOConnection daoConnection = engine.getDaoConnectionFactory().getConnection();
                daoConnection.createLeanTask(creationContext);

                                return null;
                            }
                        });


            } catch (IllegalStateFault illegalStateFault) {
                illegalStateFault.printStackTrace();
            } catch (XmlException e) {
                e.printStackTrace();
            } catch (ADBException e) {
                e.printStackTrace();
            } catch (HumanTaskException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        // }
        return inputMessage4;
    }

    private void loadProperties() throws IOException {

        try {

            Properties properties = new Properties();
            InputStream inputStream = (InputStream) getClass().getClassLoader()
                    .getResourceAsStream(LeanTaskOperationsImpl.PROPERTY_FILE_NAME);

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '"
                        + LeanTaskOperationsImpl.PROPERTY_FILE_NAME
                        + "' not found in the classpath");
            }

             versioningRequired = properties.getProperty(LeanTaskOperationsImpl.key_versioningRequired);
             dbConnection=properties.getProperty(LeanTaskOperationsImpl.key_dbConnection);
             mysqlUserName = properties.getProperty(LeanTaskOperationsImpl.key_mysqlUserName);
             mysqlPass = properties.getProperty(LeanTaskOperationsImpl.key_mysqlPass);
             isVersionRequired= Boolean.parseBoolean(versioningRequired);

            log.info("Properties loaded successfully!");
        } catch (Exception e) {
            log.error(e);
        }

    }


    private String calChecksum(String text) throws NoSuchAlgorithmException {

        
        //text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<taskDefinition actualOwnerRequired=\"yes\" name=\"leanTask\" xmlns=\"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/leantask/api/200803\"><messageSchema xmlns=\"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/200803\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"tMessageSchema\"><messageField name=\"taskDef\" type=\"s27:string\" xmlns:s27=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"tMessageField\"><messageDisplay xml:lang=\"en-us\" xsi:type=\"tMessageDisplay\"/></messageField></messageSchema><presentationElements xmlns=\"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/200803\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"tPresentationElements\"><name xml:lang=\"en-us\" xsi:type=\"tText\"/><presentationParameters xsi:type=\"tPresentationParameters\"><presentationParameter name=\"Task\" type=\"s28:string\" xmlns:s28=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"tPresentationParameter\"/></presentationParameters><subject xml:lang=\"en-us\" xsi:type=\"tText\"/><description contentType=\"mimeTypeString\" xml:lang=\"en-us\" xsi:type=\"tDescription\"/></presentationElements></taskDefinition>";
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

        System.out.println(hashtext);
            return hashtext;

        /*java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(text.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();

        byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return complete.digest();
        */

       // String md5sum = HumanTaskStoreUtils.getHex(digest);
        //return md5sum;

    }

    /**
     * persist the lean task
     * @param tenantId
     * @param taskname
     * @param element
     * @param md5sum
     * @return
     * @throws Exception
     */
    private int persistLeanTask(final int tenantId, final String taskname, final Element element, final String md5sum) throws Exception {


        int version = 0;

        HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<Object>() {
                    public Object call() throws Exception {
                        HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                        HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                        daoConn.createLeanTaskDef(tenantId, taskname, element, md5sum);

                        return null;
                    }
                });

        //read the version number from the database and set

        Connection con=getdbConnection();

        Statement stmt = con.createStatement();
        String query = "select LEANTASK_VERSION from HT_LEANTASK where LEANTASK_VERSION in(select max(LEANTASK_VERSION) from HT_LEANTASK where LEANTASK_NAME='"+taskname+"')";
        ResultSet rs =  stmt.executeQuery(query);
        while (rs.next()) {
             version = rs.getInt("LEANTASK_VERSION");

        }

        query="update HT_LEANTASK set LEANTASK_ID='"+taskname+"_"+version+"' where LEANTASK_VERSION="+version;
        stmt.executeUpdate(query);

            return  version;


    }

    /**
     * get the database connection
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private Connection getdbConnection() throws SQLException, ClassNotFoundException {


        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection
                (dbConnection,mysqlUserName,mysqlPass);

        return  con;


    }

}

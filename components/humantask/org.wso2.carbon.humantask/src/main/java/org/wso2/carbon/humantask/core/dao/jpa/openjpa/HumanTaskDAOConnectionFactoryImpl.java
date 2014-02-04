package org.wso2.carbon.humantask.core.dao.jpa.openjpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.Constants;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnectionFactoryJDBC;
import org.wso2.carbon.humantask.core.dao.jpa.JPAVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.*;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Implementation
 */
public class HumanTaskDAOConnectionFactoryImpl implements HumanTaskDAOConnectionFactoryJDBC {

    private static Log log = LogFactory.getLog(HumanTaskDAOConnectionFactoryImpl.class);

    private EntityManagerFactory entityManagerFactory;

    private DataSource dataSource;

    private TransactionManager tnxManager;

    private Map<String, Object> jpaPropertiesMap;

    private static ThreadLocal<HumanTaskDAOConnectionImpl> connections = new ThreadLocal<HumanTaskDAOConnectionImpl>();


    public HumanTaskDAOConnectionFactoryImpl() {

    }


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTransactionManager(TransactionManager tnxManager) {
        this.tnxManager = tnxManager;

    }

    public void setDAOConnectionFactoryProperties(Map<String, Object> propertiesMap) {
        this.jpaPropertiesMap = propertiesMap;
    }

    private boolean isInTransaction() throws SystemException{
       return tnxManager.getTransaction() != null && tnxManager.getTransaction().getStatus() == Status.STATUS_ACTIVE;
    }

    public HumanTaskDAOConnection getConnection() {
        if (connections.get() != null) {
            return connections.get();
        } else {
            try {
                if (this.isInTransaction()) {
                    HashMap propMap = new HashMap();
//                    propMap.put("openjpa.TransactionMode", "managed");
                    EntityManager em = entityManagerFactory.createEntityManager(propMap);
                    em.getTransaction().begin();
                    HumanTaskDAOConnectionImpl conn = createHumanTaskDAOConnection(em);
                    connections.set(conn);

                    tnxManager.getTransaction().registerSynchronization(new Synchronization() {

                        public void afterCompletion(int i) {
                            if (connections.get() != null) {
                                if (i == Status.STATUS_COMMITTED) {
                                    if(log.isDebugEnabled()) {
                                        log.debug(" Transaction is successfully committed");
                                    }
                                    connections.get().getEntityManager().getTransaction().commit();
                                    connections.get().getEntityManager().close();
                                    connections.set(null);
                                } else if (i == Status.STATUS_ROLLEDBACK) {
                                    if(log.isDebugEnabled()) {
                                        log.debug(" Transaction is successfully rolled back ");
                                    }
                                    connections.get().getEntityManager().getTransaction().rollback();
                                    connections.get().getEntityManager().close();
                                    connections.set(null);
                                }
                            }
                        }
                        public void beforeCompletion() {
                        }
                    });
                    return conn;
                }
            } catch (RollbackException e) {
                throw new RuntimeException("Could not register synchronizer!", e);
            } catch (SystemException e) {
                throw new RuntimeException("Could not register synchronizer!", e);
            }
            return null;

        }
    }

    protected HumanTaskDAOConnectionImpl createHumanTaskDAOConnection(EntityManager entityManager) {
        return new HumanTaskDAOConnectionImpl(entityManager);
    }

    public void init() {

        JPAVendorAdapter vendorAdapter = getJPAVendorAdapter();
        this.entityManagerFactory = Persistence.createEntityManagerFactory("HT-PU",
                vendorAdapter.getJpaPropertyMap(tnxManager));

    }


    /**
     * Returns the JPA Vendor adapter based on user preference
     * <p/>
     * Note: Currently we only support one JPA vendor(OpenJPA), so I have omitted vendor selection
     * logic.
     *
     * @return JPAVendorAdapter implementation
     */
    private JPAVendorAdapter getJPAVendorAdapter() {
        JPAVendorAdapter vendorAdapter = new OpenJPAVendorAdapter();

        vendorAdapter.setDataSource(dataSource);

        // TODO: Investigate whether this could be moved to upper layer. Directly put bool into prop map.
        Object generateDDL = jpaPropertiesMap.get(Constants.PROP_ENABLE_DDL_GENERATION);
        Object showSQL = jpaPropertiesMap.get(Constants.PROP_ENABLE_SQL_TRACING);

        if (generateDDL == null) {
            generateDDL = Boolean.FALSE.toString();
        }

        if (showSQL == null) {
            showSQL = Boolean.FALSE.toString();
        }

        vendorAdapter.setGenerateDdl((Boolean) generateDDL);
        vendorAdapter.setShowSql((Boolean) showSQL);

        return vendorAdapter;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void shutdown() {
        this.entityManagerFactory.close();
    }
}

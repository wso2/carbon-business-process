package org.wso2.carbon.bpel.core.tests;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.config.BPELServerConfiguration;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Tests related to BPS.xml and other configurations
 */
public class BPSConfigurationTest extends TestCase {

    private static final Log log = LogFactory.getLog(BPSConfigurationTest.class);

    /**
     * Test for checking bps.xml gets parsed correctly.
     */
    public void testBPSConfigurationFile() {
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, System.getProperty("user.dir") +
                                                                   "/src/test/resources/conf");
        BPELServerConfiguration BPELsc = new BPELServerConfiguration();

        System.out.println(System.getProperty("user.dir"));

        //Datasource Config Fields
        assertEquals(BPELServerConfiguration.DataSourceType.EXTERNAL, BPELsc.getDsType());
        assertEquals("bpsds", BPELsc.getDataSourceName());
        assertEquals("com.sun.jndi.rmi.registry.RegistryContextFactory",
                     BPELsc.getDataSourceJNDIRepoInitialContextFactory());
        assertEquals("rmi://localhost:2199", BPELsc.getDataSourceJNDIRepoProviderURL());

        //Process Dehydration Fields
        assertTrue(BPELsc.isProcessDehydrationEnabled());
        assertEquals(1, BPELsc.getProcessDehydraionMaxCount());
        assertEquals(2, BPELsc.getProcessDehydrationMaxAge());

        //TransactionFactory
        assertEquals("org.wso2.bps.SampleTransactionFactory", BPELsc.getTransactionFactoryClass());

        //Event Listeners
        assertEquals("org.wso2.bps.SampleEventListener", BPELsc.getEventListeners().get(0));

        //Mexinterceptors
        assertEquals("org.wso2.bps.SampleMexInterceptor", BPELsc.getMexInterceptors().get(0));

        //Extension Bundle Fields
        assertEquals("org.wso2.bps.SampleExtensionRuntime", BPELsc.getExtensionBundleRuntimes().get(0));
        assertEquals("org.wso2.bps.SampleCorrelationFilter", BPELsc.getExtensionCorrelationFilters().get(0));

        //OpenJPA Props
        assertEquals("false", BPELsc.getOpenJpaProperties().get(BPELConstants.OPENJPA_FLUSH_BEFORE_QUERIES));

        //MexTimeOutField
        assertEquals(120001, BPELsc.getMexTimeOut());
        //ExternalService Timeout field
        assertEquals(60001, BPELsc.getExternalServiceTimeOut());

        //HTTP Connection Manager Properties
        assertEquals(21, BPELsc.getMaxConnectionsPerHost());
        assertEquals(101, BPELsc.getMaxTotalConnections());

        //Debug Transactions Manager
        assertEquals(true, BPELsc.isDebugOnTransactionManager());

        //Sync with registry
        assertTrue(BPELsc.isSyncWithRegistry());

    }
}

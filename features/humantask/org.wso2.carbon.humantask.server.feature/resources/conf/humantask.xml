<HumanTaskServerConfig xmlns="http://wso2.org/humantask/schema/server/config">
    <PersistenceConfig>
        <DataSource>bpsds</DataSource>
        <JPAVendorAdaptor>org.wso2.carbon.humantask.core.dao.jpa.vendor.OpenJpaVendorAdapter</JPAVendorAdaptor>
        <GenerateDdl>true</GenerateDdl>
        <ShowSql>false</ShowSql>
        <!--JNDIInitialContextFactory>com.sun.jndi.rmi.registry.RegistryContextFactory</JNDIInitialContextFactory>
        <JNDIProviderUrl>rmi://localhost:2199</JNDIProviderUrl-->
        <DAOConnectionFactoryClass>org.wso2.carbon.humantask.core.dao.jpa.openjpa.HumanTaskDAOConnectionFactoryImpl</DAOConnectionFactoryClass>
    </PersistenceConfig>
    <PeopleQueryEvaluatorConfig>
        <PeopleQueryEvaluatorClass>org.wso2.carbon.humantask.core.integration.CarbonUserManagerBasedPeopleQueryEvaluator</PeopleQueryEvaluatorClass>
    </PeopleQueryEvaluatorConfig>
    <TransactionManagerConfig>
        <TransactionManagerClass>org.apache.ode.il.EmbeddedGeronimoFactory</TransactionManagerClass>
    </TransactionManagerConfig>
    <SchedulerConfig>
        <MaxThreadPoolSize>5</MaxThreadPoolSize>
    </SchedulerConfig>
    <!--TaskCleanupConfig>
        <cronExpression>0 0 0/4 * * ?</cronExpression>
        <statuses>COMPLETED,OBSOLETE</statuses>
    </TaskCleanupConfig-->
    <!--TaskEventListeners>
        <ClassName>ClassName</ClassName>
        <ClassName>ClassName</ClassName>
    </TaskEventListeners-->
    <!--UIRenderingEnabled>true</UIRenderingEnabled-->
</HumanTaskServerConfig>


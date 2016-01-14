bpmn-explorer
==============

BPMN Humantask Jaggery App UI

Configuring bpmn-Explorer for tenants
===============================================

1. Make a copy of  <BPS_HOME>/repository/deployment/server/jaggeryapps/bpmn-explorer directory
2. Update <COPY_DIRECTORY>/bpmn-explorer/config/config.json configuration file as follows:
        2.1. update bpsTenantDomain with tenant domain
            eg: if tenant domain id wso2.bps.com
                "bpsTenantDomain": "wso2.bps.com"
        2.2. update bpsTenantId with tenant id
                Tenant ID can be found in "Update Tenant" view in management console (Home > Configure > Multitenancy > View Tenants,
                then click Edit to view tenant information).
            eg: if tenant ID is "1"
                "bpsTenantId" : "1"

3. Deploy bpmn-explorer as jaggery app by following one of below methods
    3.1 Make compressed .zip file of the bpmn-explorer directory with name bpmn-explorer.zip
        Login the BPS management console as tenants admin credentials, and deploy zip file by navigating to Applications / Jaggery
        under Add submenu in Main menu.
    3.2 Copy above bpmn-explorer.zip to <BPS_HOME>/repository/tenants/<TENANT_ID>/jaggeryapps directory
    3.3 Copy bpmn-explorer directory to <BPS_HOME>/repository/tenants/<TENANT_ID>/jaggeryapps directory

4. Copy <BPS_HOME>/repository/deployment/server/webapps/bpmn.war file to <BPS_HOME>/repository/tenants/<TENANT_ID>/webapps directory

5. Then goto url https://<HOST>:<PORT>/t/<TENANT_DOMAIN>/jaggeryapps/bpmn-explorer/ using favourite web browser

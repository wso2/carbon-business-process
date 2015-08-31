WS-Humantask-Explorer
=====================

HumanTask-Explorer jaggery based web-app providing user interface for humans to integrate them to service oriented applications.

If WSO2 Business process server is deployed with port offset 0, WS-HumanTask-Explorer can be accessed by https://[hostIP]:9443/WS-Humantask-Explorer/login url.


Configuring WS-Humantask-Explorer for tenants
===============================================

1. Make a copy of  <BPS_HOME>/repository/deployment/server/jaggeryapps/humantask-explorer directory
2. Update <COPY_DIRECTORY>/humantask-explorer/config/config.json configuration file as follows:
        2.1. update bpsTenant with tenant domain
            eg: if tenant domain id wso2.bps.com
                "bpsTenant": "wso2.bps.com"

3. Deploy humantask-explorer as jaggery app by following one of below methods
    3.1 Make compressed .zip file of the humantask-explorer directory with name humantask-explorer.zip
        Login the BPS management console as tenants admin credentials, and deploy zip file by navigating to Applications / Jaggery
        under Add submenu in Main menu.
    3.2 Copy above humantask-explorer.zip to <BPS_HOME>/repository/tenants/<TENANT_ID>/jaggeryapps directory
    3.3 Copy humantask-explorer directory to <BPS_HOME>/repository/tenants/<TENANT_ID>/jaggeryapps directory

4. Then goto url https://<HOST>:<PORT>/t/<TENANT_DOMAIN>/jaggeryapps/humantask-explorer/ using favourite web browser

mvn clean install
rm -rf /home/natasha/Documents/GitRepos/product-bps/modules/distribution/target/wso2bps-3.5.1-SNAPSHOT/repository/deployment/server/webapps/stats-rest
rm /home/natasha/Documents/GitRepos/product-bps/modules/distribution/target/wso2bps-3.5.1-SNAPSHOT/repository/deployment/server/webapps/stats-rest.war
cp target/stats-rest.war /home/natasha/Documents/GitRepos/product-bps/modules/distribution/target/wso2bps-3.5.1-SNAPSHOT/repository/deployment/server/webapps

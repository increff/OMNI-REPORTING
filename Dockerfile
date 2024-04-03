FROM --platform=linux/amd64 eclipse-temurin:21.0.2_13-jre-alpine

WORKDIR /usr/local/

ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.38.0/elastic-apm-agent-1.38.0.jar /usr/local/

ENTRYPOINT [ "java", "-Xms1500m", "-Xmx1500m",  "-javaagent:/usr/local/elastic-apm-agent-1.38.0.jar", "-Delastic.apm.service_name=omni-reporting-docker-test", "-Delastic.apm.application_packages=*", "-Delastic.apm.server_url=http://10.180.0.51:8200", "-Delastic.apm.secret_token=elkserver-dev", "-Delastic.apm.environment=elk", "-Dspring.cloud.vault.scheme=https", "-Dspring.cloud.vault.host=vault.nextscm.com", "-Dspring.cloud.vault.application-name=omniReporting", "-Dspring.cloud.vault.port=443", "-Dspring.cloud.vault.token=hvs.Yt4JtVfjJJT0q71NdTxfZhDA", "-Dspring.profiles.active=dev", "-Dspring.config.import=vault://",  "-jar", "/usr/local/app.jar"]

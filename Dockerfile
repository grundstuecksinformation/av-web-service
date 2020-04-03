FROM adoptopenjdk/openjdk11:latest

USER root

RUN apt-get update && apt-get install -y --no-install-recommends libfontconfig1 curl && rm -rf /var/lib/apt/lists/*

WORKDIR /home/cadastre-web-service
COPY build/libs/cadastre-web-service-*.jar /home/cadastre-web-service/cadastre-web-service.jar
RUN cd /home/cadastre-web-service && \
    chown -R 1001:0 /home/cadastre-web-service && \
    chmod -R g+rw /home/cadastre-web-service && \
    ls -la /home/cadastre-web-service

USER 1001
EXPOSE 8080
CMD LOGGING_LEVEL_CH_EHI_OEREB=INFO java -XX:MaxRAMPercentage=80.0 -jar cadastre-web-service.jar \
  "--spring.datasource.url=${DBURL}" \
  "--spring.datasource.username=${DBUSR}" \
  "--spring.datasource.password=${DBPWD}" \
  "--cadastre.dbschema=${DBSCHEMA}" \
  "--spring.datasource.driver-class-name=org.postgresql.Driver"

HEALTHCHECK --interval=30s --timeout=30s --start-period=60s CMD curl http://localhost:8080/actuator/health

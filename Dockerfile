FROM adoptopenjdk:11.0.7_10-jdk-hotspot

RUN apt-get update && apt-get install -y --no-install-recommends libfontconfig1 curl ftp && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

WORKDIR /home/cadastrewebservice

ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /home/cadastrewebservice/app/lib
COPY ${DEPENDENCY}/META-INF /home/cadastrewebservice/app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /home/cadastrewebservice/app
RUN chown -R 1001:0 /home/cadastrewebservice && \
    chmod -R g=u /home/cadastrewebservice

USER 1001

ENTRYPOINT ["java","-XX:MaxRAMPercentage=80.0", "-noverify", "-XX:TieredStopAtLevel=1", "-cp","app:app/lib/*","ch.so.agi.cadastre.webservice.CadastreWebServiceApplication","--spring.datasource.url=${DBURL}","--spring.datasource.username=${DBUSR}","--spring.datasource.password=${DBPWD}","--cadastre.dbschema=${DBSCHEMA}","--spring.datasource.driver-class-name=org.postgresql.Driver"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl http://localhost:8080/actuator/health

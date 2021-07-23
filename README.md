# grundstuecksinformation-cadastre-web-service

## Developing

## Jakarta vs javax
`spring-omx` verwendet noch die alten javax-Pakete. Aus diesem Grund kann noch nicht auf Jakarta umgestellt werden (siehe auch: https://stackoverflow.com/questions/68254055/springboot-application-javax-to-jakarta-migration-question). Es erscheint immer die Fehlermeldung:

```
Caused by: java.lang.NoClassDefFoundError: javax/xml/bind/JAXBException
	at ch.so.agi.cadastre.webservice.WsConfig.createMarshaller(WsConfig.java:17) ~[main/:na]
```

build.gradle für Jakarta:
```
dependencies {
    ...
    implementation 'jakarta.xml.bind:jakarta.xml.bind-api:3.0.1'
    implementation 'org.glassfish.jaxb:jaxb-runtime:3.0.1'
    implementation(files(genJaxb.classesDir).builtBy(genJaxb))
    ....
    jaxb 'org.glassfish.jaxb:jaxb-xjc:3.0.1'
    jaxb 'jakarta.xml.bind:jakarta.xml.bind-api:3.0.1'
    jaxb 'org.glassfish.jaxb:jaxb-runtime:3.0.1'
    jaxb 'jakarta.activation:jakarta.activation-api:2.0.0'
    ...
```

bindings.xjb:
```
<jxb:bindings version="3.0" xmlns:jxb="https://jakarta.ee/xml/ns/jaxb" xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc" jxb:extensionBindingPrefixes="xjc" xmlns:xs="http://www.w3.org/2001/XMLSchema">
```


### Database

```
docker run --rm --name grundstuecksinformation-db -p 54321:5432 --hostname primary -e PG_DATABASE=grundstuecksinformation -e PG_LOCALE=de_CH.UTF-8 -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary -e PG_USER=admin -e PG_PASSWORD=admin -e PG_PRIMARY_USER=repl -e PG_PRIMARY_PASSWORD=repl -e PG_ROOT_PASSWORD=secret -e PG_WRITE_USER=gretl -e PG_WRITE_PASSWORD=gretl -e PG_READ_USER=ogc_server -e PG_READ_PASSWORD=ogc_server -v ~/pgdata-grundstuecksinformation:/pgdata:delegated sogis/oereb-db:latest
```

### Test requests

```
curl -X GET -H "Accept: application/xml" -H "Content-Type: application/xml" http://localhost:8080/getegrid/xml/?XY=2600564,1215478 > response.xml && xmllint --format response.xml
curl -X GET -H "Accept: application/xml" -H "Content-Type: application/xml" http://localhost:8080/getegrid/xml/?XY=2600466,1215406 > response.xml && xmllint --format response.xml
curl -X GET -H "Accept: application/xml" -H "Content-Type: application/xml" http://localhost:8080/getegrid/xml/SO0200002457/452/ > response.xml && xmllint --format response.xml
curl -X GET -H "Accept: application/xml" -H "Content-Type: application/xml" http://localhost:8080/extract/xml/geometry/CH955832730623 > response.xml && xmllint --format response.xml
```

### xml2pdf 

```
java -jar /Users/stefan/apps/SaxonHE9-9-1-7J/saxon9he.jar -s:src/test/data/CH310663327779.xml -xsl:src/main/resources/xml2pdf.xslt -o:/Users/stefan/tmp/CH310663327779.fo
/Users/stefan/apps/fop-2.4/fop/fop -fo /Users/stefan/tmp/CH310663327779.fo -pdf /Users/stefan/tmp/CH310663327779.pdf -c src/main/resources/fop.xconf
```


## Building

### Docker

```
docker build -t sogis/cadastre-web-service .
```

## Running
```
docker run -p8080:8080 -e DBURL=jdbc:postgresql://localhost:54321/grundstuecksinformation -e DBUSR=gretl -e DBPWD=gretl -e DBSCHEMA=live sogis/cadastre-web-service

docker run -p8080:8080 -e DBURL=jdbc:postgresql://host.docker.internal:54321/grundstuecksinformation -e DBUSR=gretl -e DBPWD=gretl -e DBSCHEMA=live sogis/cadastre-web-service
```

```
https://geo-t.so.ch/api/cadastre/getegrid/xml/?XY=2600564,1215478
https://geo-t.so.ch/api/cadastre/extract/xml/geometry/CH955832730623
https://geo-t.so.ch/api/cadastre/extract/pdf/geometry/CH955832730623
```

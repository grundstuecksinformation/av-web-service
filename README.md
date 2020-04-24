# grundstuecksinformation-cadastre-web-service

## Developing

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

```

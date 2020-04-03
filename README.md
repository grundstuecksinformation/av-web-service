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



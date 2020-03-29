# grundstuecksinformation-cadastre-web-service

```
curl -X GET -H "Accept: application/xml" -H "Content-Type: application/xml" http://localhost:8080/getegrid/xml/?XY=2600564,1215478 > response.xml && xmllint --format response.xml
curl -X GET -H "Accept: application/xml" -H "Content-Type: application/xml" http://localhost:8080/getegrid/xml/?XY=2600466,1215406 > response.xml && xmllint --format response.xml
```



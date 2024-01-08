# Full-Text-Search-Comparison Elasticsearch implementation

## Elasticseach

Setup Elasticsearch with docker
```
cd docker
docker-compose up
```
Shut Elasticsearch down with volume and orphan removal
```
docker-compose down -v --remove-orphans
```

## Running app

`./gradlew bootRun`
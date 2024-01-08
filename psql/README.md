# Full-Text-Search-Comparison PSQL implementation

## Postgres

Setup PostgreSQL database (and run liquibase migrations) with docker
```
cd docker
docker-compose up
```
Shut PostgreSQL down with volume and orphan removal
```
docker-compose down -v --remove-orphans
```

## Running app

`./gradlew bootRun`
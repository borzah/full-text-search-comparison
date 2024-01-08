# Full-text Search comaparison

This repo consists of three gradle projects, each of them containing implementation to perform full-text search with Elasticsearch or PostgreSQL.

* `elasticsearch` project has full-text search implementation with Elasticsearch
* `psql` project has full-text search implementation with PostgreSQL using tsvector
* `psql-jsonb` project is experimental full-text search implementation with PostgreSQL using jsonb column

## Running apps

### Elasticsearch project

Move to `elasticsearch` folder

`cd elasticsearch`

Run Elasticsearch in docker

`docker compose up`

Run Spring application with gralde wrapper

`./gradlew bootRun`

POST http://localhost:8200/api/v1/metadata is used to save metadata. Request body is described in `elasticsearch/src/main/resources/testdata/MetadataPostRequest.json`

POST http://localhost:8200/api/v1/metadata/file is used to save large amount of metadata which will come from `elasticsearch/src/main/resources/testdata/metadata.json`. You can add large amount of data in that file and save it.

GET http://localhost:8200/api/v1/metadata/search/studies?searchValue={search} is used to search for metadata

### PostgreSQL+tsvector project

Move to `psql` folder

`cd psql`

Run Elasticsearch in docker

`docker compose up`

Run Spring application with gralde wrapper

`./gradlew bootRun`

POST http://localhost:8500/api/postgres is used to save metadata. Request body is described in `psql/src/main/resources/testdata/MetadataPostRequest.json`

POST http://localhost:8500/api/postgres/file is used to save large amount of metadata which will come from `psql/src/main/resources/testdata/metadata.json`. You can add large amount of data in that file and save it.

GET http://localhost:8500/api/postgres?searchValue={search} is used to search for metadata

### PostgreSQL+jsonb project

Move to `psql-jsonb` folder

`cd psql-jsonb`

Run Elasticsearch in docker

`docker compose up`

Run Spring application with gralde wrapper

`./gradlew bootRun`

POST http://localhost:8600/api/postgres is used to save metadata. Request body is described in `psql-jsonb/src/main/resources/testdata/MetadataPostRequest.json`

POST http://localhost:8600/api/postgres/file is used to save large amount of metadata which will come from `psql-jsonb/src/main/resources/testdata/metadata.json`. You can add large amount of data in that file and save it.

GET http://localhost:8600/api/postgres?searchValue={search} is used to search for metadata
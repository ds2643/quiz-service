# Correlation One Quiz Service
This repository contains an implementation of a minimal back-end (database and HTTP server) that aims to partially recreate Correlation One's existing quiz service.

The project is implemented with Clojure and MySQL.

## Proposed Architecture and Design
Please see `docs/architecture.md` for notes on design and architecture.

## Installation
Using the service requires installation of docker-compose and leiningen (tool for managing Clojure projects). See the following resources for help:
- [`docker-compose`](https://docs.docker.com/compose/install/)
- [`leiningen`](https://leiningen.org/#install)

### Initializing the Database
The service uses a dockerized MySQL database. This service may be initialized with docker-compose.

Assuming docker-compose is properly installed, the database may be started as follows:
```
export DB_PORT=3306 # assign port associated with db
docker-compose up
```

Schema migration is managed using a Clojure library called [`ragtime`](https://github.com/weavejester/ragtime), which is integrated into the Clojure project definition.

To perform schema migration, run the following command from the root of the project directory:
```
lein migrate
```

Such schema migrations can be undone using the complementary command:
```
lein rollback
```

Gracefully shut down the service from the `db/` subdirectory.
```
docker-compose down
```

### Building the Server
Assuming `leiningen` is properly installed, the server may be built as follows:
```
lein do clean, deps, install
```
This command clears the existing target directory (if it exists), downloads associated dependencies to the user's m2 cache, and prepares the project to be run locally.

## Use
Use of the service requires that the dockerized mysql database is available over the configured URL (see Installation for details).

Run the server on localhost as such:
```
export API_PORT=3000
lein run
```
Port 3666 is used by default. However, the `API_PORT` environment variable may be used to select another port.

In the context above, the server is available at url: `http//localhost:3000/`.

### Communicating with the Service
Please see the api description document included in `docs/api.md`.

Please note sample `json` messages are included in the `samples/` directory at the root of this repository.

### Testing
A set of tests packaged in the Clojure projects behaviorally probe the server and associated session-management logic.

These tests may be run using `lein`:
```
lein test
```

Please note that running these commands effectively restarts the state of the database (rollback migration is performed).

## Issues and Limitations
The current implementation could be improved upon in several ways:
1. The user should get confirmation that a session as successfully been submitted. The current api gives confirmation of submission when the calling application requests to close the session, but a more robust solution might be beneficial. One such solution is to delegate this responsibility to a separate email/notification service.

2. The current implementation accommodates limited question and answer data. An improved implementation might serve assets (e.g., images) through a dedicated route. Question text could be embedded in a richer medium, like HTML or markdown that supports references to such assets. A preliminary solution for storing such assets might be AWS/S3, but options providing a nice story for versioning might be preferred.

# ReviewHub API

Spring Boot REST backend for a school tutoring-desk (*sportelli didattici*) system. Teachers open *sportelli* — tutoring sessions on a given subject, in a given room, across one or more dated time slots. Students browse available sessions and book individual slots. Admins manage users, rooms, subjects, and sessions.

> **School project archive (2024/2025).** Preserved as a record of the project, without active development or maintenance. Read the [implementation notes & known limitations](#implementation-notes--known-limitations) before reusing the code.

Built during my final year of high school. Its only deployment was at school: a Raspberry Pi inside the school network, its own DNS name, Apache with TLS, and a `systemd` service that had to survive a reboot without me.

**Frontend**: [anItalianGeek/reviewhub](https://github.com/anItalianGeek/reviewhub)

---

## Stack

- **Spring Boot** — Gradle plugin `3.3.0`, explicit `3.4.1` starters (`web`, `data-jpa`, `tomcat`, `security`), and `tomcat-embed-jasper` `11.0.2`
- **Java & Gradle** — Gradle Wrapper `8.8` (`build.gradle` does not pin a Java toolchain or `sourceCompatibility`; compilation verified with JDK 21)
- **Authentication & Serialization** — JWT (`jjwt` `0.12.3`), `gson` `2.11.0`, `commons-text` `1.11.0`
- **Persistence** — Spring Data JPA / Hibernate on MariaDB (`mariadb-java-client` `3.5.1`)

---

## Domain model

```
Person ──< Sportello >── Room
   │           │
   │           └──── Subject
   │           │
   │           └──< Slot (Giorno)
   │                 │
   └────< Booking >──┘
```

- **Person (`Persona`)** — identified by school email (`@chilesotti.it` in controller lookups). Roles (`UserIdentity`): `STUDENT`, `TEACHER`, `ADMIN` (plus `NONE` as a fallback).
- **Sportello (`Sportello`)** — a tutoring course/session tied to a room (`Aula`), a subject (`Materia`), and a responsible teacher (`docente_responsabile`).
- **Slot (`Giorno`)** — a dated time window (`data_inizio`, `data_fine`, `sportelloId`) within a sportello, tracking `num_iscritti` and `max_iscritti`.
- **Booking (`IscrizioneSportello`)** — links one `Persona` to one `Giorno` and `Sportello` via a composite key (`IscrizioneSportelloId`).
- **Room (`Aula`)** and **Subject (`Materia`)** — reference tables referenced by each sportello.

---

## How the implementation works

### Authentication & token lifecycle

- **Login (`POST /api/v1/users/login`)**: hashes the HTML-escaped password with SHA-256 (`SHA256Encryptor`) and checks it against `persona`. On match, `JwtUtil` issues an HMAC-SHA signed JWT (`sub` set to the user's email, random `jti`, expiration controlled by `jwt.expiration` — `1200` seconds / 20 minutes in the default configuration, `86400` seconds fallback in code) and returns it alongside the user's `UserIdentity` role.
- **Token refresh (`PUT /api/v1/users/refresh`)**: validates the existing `Authorization: Bearer {token}` header and returns a new JWT for the same subject.
- **Custom filter (`AuthFilter`)**: registered before `UsernamePasswordAuthenticationFilter` in `SecurityConfig`. `AuthFilter` lets `/api/v1/users/login`, `/api/v1/users/create`, `/api/v1/users/check`, `/api/v1/users/logout`, and `/api/v1/test` pass without a token, and requires a valid `Authorization: Bearer {token}` header on all other requests.

### Roles, caller identity & ownership

The application models three roles (`STUDENT`, `TEACHER`, `ADMIN`) and passes the acting username via an `author` query parameter (or path variable), to which controllers and `AuthFilter` append the school domain `@chilesotti.it`:

- **Intended domain rules**: `TEACHER` and `ADMIN` create sportelli; modifying or deleting a sportello belongs to its responsible teacher or an `ADMIN`; `ADMIN` manages rooms, subjects, and privileged user creation.
- **How checks are wired in code**:
  - `PersonaController` checks `author` in `POST /api/v1/users/create` so unauthenticated callers or non-admins can only create `STUDENT` accounts.
  - `SportelloController` checks the caller's role and responsible-teacher email on `GET /api/v1/sportello/{id}` to decide whether to return the full list of enrolled students or only the caller's own bookings.
  - `AuthFilter` also contains role and ownership branches (querying `persona` and `sportello` via `DataSource` using `request.getParameter("author")`), though see [Implementation notes & known limitations](#implementation-notes--known-limitations) for how path matching and identity binding behave in practice.

### Sportello creation & booking flow

- **Two-step creation (`POST /api/v1/sportello/create`)**: `SportelloController.creaSportello` detaches the incoming `giorni` list, saves the `Sportello` entity first to obtain its generated ID, and then persists each `Giorno` row linked to that sportello.
- **Subscribing (`POST /api/v1/sportello/subscribe/{id}`)**: `iscriviAlloSportello` looks up the target `Giorno`, executes `giornoRepository.aggiungiIscritto(giornoId)` (`UPDATE Giorno g SET g.num_iscritti = g.num_iscritti + 1 WHERE g.num_iscritti < g.max_iscritti AND g.id = :giornoId`), and saves an `IscrizioneSportello` record.
- **Unsubscribing (`POST /api/v1/sportello/unsubscribe/{id}` and `POST /api/v1/sportello/{id}/remove-subscription/{username}`)**: deletes the matching `IscrizioneSportello` entry and decrements `num_iscritti` on `Giorno` when `num_iscritti > 0`.

---

## API overview

Most controllers are mounted under `/api/v1`, with an additional `/test` controller at the root:

| Group | Base path | Endpoints |
|---|---|---|
| **Users** (`PersonaController`) | `/api/v1/users` | `POST /login` · `PUT /refresh` · `DELETE /logout` · `POST /create` · `PUT /modify/{user}` · `DELETE /remove/{user}` · `GET /all` · `GET /{username}` · `POST /check` · `GET /test` |
| **Sportelli** (`SportelloController`) | `/api/v1/sportello` | `GET /all` · `GET /available` · `GET /subscribed` · `GET /by/{author}` · `GET /{id}` · `POST /create` · `PUT /modify/{id}` · `DELETE /remove/{id}` · `POST /subscribe/{id}` · `POST /unsubscribe/{id}` · `POST /{id}/remove-subscription/{username}` · `GET /test` |
| **Rooms** (`AulaController`) | `/api/v1/aula` | `GET` · `POST` · `PUT` · `DELETE /{id}` |
| **Subjects** (`MateriaController`) | `/api/v1/materia` | `GET` · `POST` · `PUT` · `DELETE /{materia}` |
| **Test** (`TestController`) | `/test` | `GET` · `GET /no-async` · `POST` |

An endpoint with no suffix uses the base path as written. Paginated list endpoints accept `offset` (used as the zero-based page index in `PageRequest.of(offset, limit)`) and an optional `limit` (defaulting to `10` for sportelli, `20` for rooms/subjects, and `30` for users).

---

## Implementation notes & known limitations

Because this repository preserves the high-school code as built, several implementation details are important to know before reading or reusing it:

- **JWT subject vs. `author` parameter (`AuthFilter`)**: `AuthFilter` validates that the bearer token is signed and unexpired, but derives `username` and `role` from `request.getParameter("author")` (`username + "@chilesotti.it"`) without comparing `author` to the JWT `sub` claim. In addition, `SecurityConfig` configures `.requestMatchers(..., "/**").permitAll()` and relies on `AuthFilter` for path checks, while the role/ownership regexes inside `AuthFilter` match paths without the `/api/v1` prefix (e.g. `/sportello/...`, `/users/...`, and `^/materia/.+\n`). As a result, requests to `/api/v1/...` fall through to the filter's default branch after token validation rather than enforcing strict RBAC or ownership isolation.
- **Booking capacity, overlaps & async transactions (`SportelloController`)**: `giornoRepository.aggiungiIscritto(giornoId)` only increments `num_iscritti` when `num_iscritti < max_iscritti` and returns a `boolean`, but `iscriviAlloSportello` ignores that return value and still saves the `IscrizioneSportello` row. There is no server-side query checking time-slot overlaps for a student, and controller methods combine `@Async("requestHandler")` with `CompletableFuture.supplyAsync(...)`, so `@Transactional` annotations on controller methods do not wrap the inner async lambda in a single database transaction.
- **Password hashing (`SHA256Encryptor`)**: passwords are HTML-escaped (`StringEscapeUtils.escapeHtml4`) and hashed with unsalted SHA-256 rather than a password-hashing function such as BCrypt or Argon2.
- **Hardcoded school domain**: `PersonaController`, `SportelloController`, and `AuthFilter` append `@chilesotti.it` to username parameters when querying `persona` and `sportello`.
- **Automated tests**: no automated test sources are tracked (`./gradlew test` completes with `:test NO-SOURCE`), so build checks currently verify compilation only.
- **Repository configuration artifacts**: the tracked `src/main/resources/application.properties` and `src/main/resources/server.keystore` reflect the historical development setup. Never reuse tracked secrets, keystores, or credentials in any real environment.

---

## Running locally

### 1. Clone the repository

```bash
git clone https://github.com/anItalianGeek/reviewhub_restAPI.git
cd reviewhub_restAPI
```

### 2. Prepare MariaDB and local configuration

The service expects a reachable MariaDB database (named `reviewhub_db` in the default configuration). While `spring.jpa.hibernate.ddl-auto=update` creates or updates tables from the JPA entities (`persona`, `sportello`, `giorno`, `iscrizione_sportello`, `aula`, `materia`, `auth_token`), **this is not a turnkey one-command setup**:

- Useful workflows require pre-populated reference rows in `aula` and `materia`, plus an initial `ADMIN` / `TEACHER` record in `persona` (with an `@chilesotti.it` email to match controller lookups, since `POST /api/v1/users/create` only permits unauthenticated creation of `STUDENT` accounts).
- In `src/main/resources/application.properties`, `server.ssl.enabled=true` is enabled on port `8888`. For local testing without TLS, override `server.ssl.enabled=false` (or supply your own keystore) and provide your own database and JWT settings via external Spring configuration or command-line arguments rather than reusing the tracked configuration values.

Key Spring properties used by the application:

- `spring.datasource.url` (e.g. `jdbc:mariadb://localhost:3306/reviewhub_db`)
- `spring.datasource.username` / `spring.datasource.password`
- `spring.datasource.driver-class-name` (`org.mariadb.jdbc.Driver`)
- `spring.jpa.hibernate.ddl-auto` (`update`) and `spring.jpa.properties.hibernate.dialect` (`org.hibernate.dialect.MariaDBDialect`)
- `server.port` (`8888`), `server.address` (`0.0.0.0`), `server.ssl.enabled` (`true` by default; set to `false` for plain HTTP locally), `server.ssl.key-store`, `server.ssl.key-store-password`
- `jwt.secret` (HMAC-SHA signing key, at least 256 bits) and `jwt.expiration` (token lifetime in seconds, e.g. `1200`)

### 3. Build and run with the Gradle Wrapper

On Linux / macOS:

```bash
./gradlew test
./gradlew bootRun --args='--server.ssl.enabled=false'
```

On Windows (PowerShell / CMD):

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun --args="--server.ssl.enabled=false"
```

When started on port `8888`, the API listens on `https://localhost:8888` by default (or `http://localhost:8888` when `server.ssl.enabled=false`).

# Copilot Instructions — user-data-store

This file captures conventions used when working with GitHub Copilot in the `user-data-store` repository and the broader Wultra PowerAuth ecosystem.

---


## Architecture

Three-module Maven project (Spring Boot 4, Java 17, WAR packaging for the server):

- **`user-data-store-client-model`** — API DTOs, request/response records, validation annotations. Published to Maven Central.
- **`user-data-store-rest-client`** — `UserDataStoreRestClient`, a typed HTTP client built on `wultra-core rest-client-base`. Published to Maven Central.
- **`user-data-store-server`** — Spring Boot application. Not published (deploy/install skipped in its `pom.xml`). Context path: `/user-data-store`.

Layer order inside the server: `Controller → Service → Repository (Spring Data JPA)`. Converters translate between entities and DTOs.

The five main resource domains are **Documents**, **Photos**, **Attachments**, **UserClaims**, and **Claims** — each with its own controller, service, repository, and entity.

**Database**: Liquibase manages schema changes. Changelogs live in `docs/db/changelog/` (not under `src/`). PostgreSQL is the primary production database; Oracle and MSSQL are also supported. Tests run against H2 in-memory using `spring.jpa.hibernate.ddl-auto=create-drop` (Liquibase disabled).

**Encryption**: `EncryptionService` provides optional AES-256 at-rest encryption for sensitive fields. Each entity stores an `EncryptionMode` enum column (`NO_ENCRYPTION` or `AES_CBC`). The master key is configured via `user-data-store.db.master.encryption.key`.

**Security**: Supports `BASIC_HTTP` and `OAUTH2` auth, selected by `user-data-store.security.auth.type`. Roles are `ROLE_READ` and `ROLE_WRITE`.

---


## Build & Test


### Build

```bash
mvn clean package
```


### Run all tests

```bash
mvn test
```


### Run a single test class

```bash
mvn test -pl user-data-store-server -Dtest=DocumentControllerTest
```


### Run a single test method

```bash
mvn test -pl user-data-store-server -Dtest=DocumentControllerTest#testPost
```


### Dev run

Use the IntelliJ run configuration at `.run/UserDataStoreApplication.run.xml` with the `dev` Spring profile. Health endpoint: `http://localhost:8091/user-data-store/actuator/health`.

---


## Key Conventions


### Two test styles

- **`@WebMvcTest` + `@MockitoBean`** — controller slice tests (no DB). Import `WebSecurityConfiguration`. Use `@WithMockUser` for auth.
- **`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@ActiveProfiles("test")`** — full integration tests that exercise `UserDataStoreRestClient` against a live H2 database. Use `@TestInstance(PER_CLASS)` and `@BeforeAll` to configure the client with HTTP Basic `admin/admin`.


### API model records

All request/response types in `user-data-store-client-model` are Java **records** annotated with `@Builder @Jacksonized`. Always add `@NotBlank`, `@Size`, etc. from Jakarta Validation.

```java
@Builder
@Jacksonized
public record DocumentCreateRequest(
        @NotBlank @Size(max = 255) String userId,
        ...
) {}
```


### JPA entities

- Use `@Getter @Setter` (never `@Data`) to avoid unsafe `equals`/`hashCode`.
- Implement `equals`/`hashCode` manually using `ProxyUtils.getUserClass()` for Hibernate proxy safety, based on business-key fields (not `id`).
- Timestamps: `LocalDateTime`, column `timestamp_created` / `timestamp_last_updated`.
- `@Column(columnDefinition = "CLOB")` for large text fields (`documentData`, etc.).


### Liquibase changesets

New changesets go under `docs/db/changelog/changesets/user-data-store/`. After adding a changeset, regenerate the static SQL scripts:

```bash
# PostgreSQL
liquibase --changeLogFile=./docs/db/changelog/changesets/user-data-store/db.changelog-module.xml \
  --output-file=./docs/sql/postgresql/generated-postgresql-script.sql updateSQL --url=offline:postgresql
```

Repeat for `oracle` and `mssql`.


### Copyright header

Use `User Data Store` (not a different project name) in the copyright header:

```java
/*
 * User Data Store
 * Copyright (C) 2024 Wultra s.r.o.
 * ...
 */
```

---


## Changelog

`CHANGELOG.md` lives at the `/docs` folder. Update it as part of every PR — before creating the PR, not after merge.


### Format

Follows [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/):

```markdown
# Changelog


## X.Y.Z (TBA)


### Added

- New feature description [(#N)](https://github.com/wultra/user-data-store/issues/N)


### Changed

- Changed behaviour description [(#N)](...)


### Fixed

- Bug fix description [(#N)](...)


## 1.2.3 - 2025-03-01


### Added

- ...
```

**Change type subsections** (use only those that apply):
- `Added` — new features
- `Changed` — changes in existing functionality
- `Deprecated` — soon-to-be removed features
- `Removed` — removed features
- `Fixed` — bug fixes
- `Security` — security vulnerability fixes

**Rules:**
- Always add new entries under `## X.Y.Z (TBA)` (the unreleased section at the top).
- On release, rename `## X.Y.Z (TBA)` to `## x.y.z - YYYY-MM-DD` (ISO 8601 date).
- Each entry: `- <Description starting with verb> [(#N)](url)` — link to the issue, not the PR.
- Descriptions should be human-readable, not raw commit messages (e.g. "Fixed NPE when application list is empty" not "fix #811: add missing import").
- Skip the Changelog update only for changes with no user-visible impact (e.g. pure CI/tooling changes).

---


## Code Conventions


### Java / Spring Boot

- **Lombok**: always use `@Getter`, `@Setter`, `@Slf4j`, etc. — never write manual getters/setters or `private static final Logger logger = ...`.
- **Java 25 / Lombok**: root `pom.xml` must declare `annotationProcessorPaths` for Lombok in `maven-compiler-plugin` (required since JDK 23 changed annotation processing policy to `none`).
- **Copyright header**: use the Wultra copyright header in all files (Java, XML, logback configs, etc.):
  ```java
  /*
   * User Data Store
   * Copyright (C) <year> Wultra s.r.o.
   *
   * This program is free software: you can redistribute it and/or modify
   * it under the terms of the GNU Affero General Public License as published
   * by the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU Affero General Public License for more details.
   *
   * You should have received a copy of the GNU Affero General Public License
   * along with this program.  If not, see <http://www.gnu.org/licenses/>.
   */
  ```
  For XML files (including logback configs), use the XML comment equivalent:
  ```
  <!--
    ~ User Data Store
    ~ Copyright (C) <year> Wultra s.r.o.
    ~
    ~ This program is free software: you can redistribute it and/or modify
    ~ it under the terms of the GNU Affero General Public License as published
    ~ by the Free Software Foundation, either version 3 of the License, or
    ~ (at your option) any later version.
    ~
    ~ This program is distributed in the hope that it will be useful,
    ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
    ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    ~ GNU Affero General Public License for more details.
    ~
    ~ You should have received a copy of the GNU Affero General Public License
    ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
    -->
  ```
- **`@author` tag**: always add `@author <Full Name>, <email>@wultra.com` to class-level Javadoc in both main and test classes.


### Markdown

- Always format tables.
- Add two empty lines above headers and one below.


### Logging

- Use `StructuredArguments.kv()` from `logstash-logback-encoder` for structured key-value pairs.
- Dev/test logback configs: use `logging-support` module from `java-core` with `%msg%sa%n` pattern (no literal space before `%sa` — the converter prepends its own leading space).
- Production configs: use `LogstashEncoder` for JSON output.

---


## RTK — Token-Optimized CLI

**rtk** is a CLI proxy that filters and compresses command outputs, saving 60-90% tokens.

### Rule

Always prefix shell commands with `rtk`:

```bash
# Instead of:              Use:
git status                 rtk git status
git log -10                rtk git log -10
cargo test                 rtk cargo test
docker ps                  rtk docker ps
kubectl get pods           rtk kubectl pods
```

### Meta commands (use directly)

```bash
rtk gain              # Token savings dashboard
rtk gain --history    # Per-command savings history
rtk discover          # Find missed rtk opportunities
rtk proxy <cmd>       # Run raw (no filtering) but track usage
```

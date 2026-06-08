# Changelog


## 1.6.0 (TBA)


### Added

- Enable filtering in GET documents API only for certain attributes. [(423)](https://github.com/wultra/user-data-store/issues/423)


### Changed

- Upgraded Docker base image to `ibm-semeru-runtimes:open-jdk-25.0.3.0-jre-noble` (OpenJDK 25). [(430)](https://github.com/wultra/user-data-store/issues/430)
- Used structured logging — action, state, and contextual IDs (userId, documentId, etc.) are now emitted as separate top-level JSON fields. [(416)](https://github.com/wultra/user-data-store/issues/416)
- Migrated to Spring Boot 4 and Jackson 3. [(417)](https://github.com/wultra/user-data-store/issues/417)

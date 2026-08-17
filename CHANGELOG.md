# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.6.0] - 2026-07-21

### Added

- Added JSON structured logging foundation [(#393)](https://github.com/wultra/user-data-store/issues/393)
- Added Artifact Signing documentation [(#407)](https://github.com/wultra/user-data-store/pull/407)
- Enabled filtering in the GET documents API by document type and attributes [(#423)](https://github.com/wultra/user-data-store/issues/423)

### Changed

- Improved structured logging with action, state, and contextual identifiers [(#416)](https://github.com/wultra/user-data-store/issues/416)
- Migrated to Spring Boot 4 and Jackson 3 [(#417)](https://github.com/wultra/user-data-store/issues/417)
- Updated Spring Boot to 4.0.7 [(#441)](https://github.com/wultra/user-data-store/issues/441)
- Upgraded Docker base image to OpenJDK 25 [(#430)](https://github.com/wultra/user-data-store/issues/430)
- Swapped Docker images to use Wultra base images for JRE and Liquibase [(#443)](https://github.com/wultra/user-data-store/issues/443)

### Fixed

- Registered Lombok annotation processor [(#419)](https://github.com/wultra/user-data-store/issues/419)

[unreleased]: https://github.com/wultra/user-data-store/compare/1.6.0...HEAD
[1.6.0]: https://github.com/wultra/user-data-store/compare/1.5.0...1.6.0

# System Requirements

The HW requirements for User Data Store are low, and current commodity hardware and cloud environments will be able to run the software. If in doubt, use the following HW configuration.

## Java Runtime Requirements

The following Java runtime versions are supported:

| Java Version | Support          |
|--------------|------------------|
| Java 21      | LTS, recommended |
| Java 17      | LTS, supported   |

The User Data Store may run on other Java versions, however we do not perform extensive testing with non-LTS releases.

## Hardware Requirements of Application

### Minimal
- CPU 1×core, 2.0GHz
- 1GB free RAM
- 5GB free disk space

### Recommended
- CPU 2×core, 2.0GHz
- 2GB free RAM
- 10GB free disk space

For HA setup, run multiple instances with the same configuration behind a load balancer.

## Database Platforms

User Data Store can run on PostgreSQL, Oracle, and MS SQL Server databases.

| Database Platform | Minimal Supported Version |
|-------------------|---------------------------|
| PostgreSQL        | 15                        |
| MS SQL Server     | SQL Server 2019           |
| Oracle DB         | 19c                       |

## Hardware Requirements of Database Platform

We recommend following configuration for database environment:
- CPU 2×core, 2.0GHz
- 8GB minimum RAM
- 50GB free disk space

## Read Next

Next chapters:
- [Deploying User Data Store](./Deploying-User-Data-Store.md)
- [Database Setup](Database-Setup.md)

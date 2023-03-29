# Deploying User Data Store

This chapter explains how to deploy User Data Store.


## Downloading User Data Store

You can download the latest `user-data-store.war` from the [User Data Store releases page](https://github.com/wultra/user-data-store/releases).


## Configuring User Data Store

Configuration parameter `user-data-store.db.master.encryption.key` is a master DB encryption key to derive server private keys for the encryption of sensitive data in the database.
It is an Advanced Encryption Standard (AES) key and recommended length is 256 bits.
An empty value means no encryption, which is not recommended.


## Setting Up REST Service Credentials

<!-- begin box info -->
The RESTful interface is secured using Basic HTTP Authentication.
<!-- end -->

The credentials are stored in the `uds_users` table.
To create the password hash, call `new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256").encode("password");`
There are two roles, that you may assign to the user in the table `uds_authorities`:
- `ROLE_READ`
- `ROLE_WRITE`


## Deploying User Data Store

Right now, the only standalone war is supported running from console using the `java -jar` command.
The deployed application is accessible on `http://localhost:8080/user-data-store/`.

## Supported Java Runtime Versions

The following Java runtime versions are supported:
- Java 17 (LTS release)

The User Data Store may run on other Java versions, however we do not perform extensive testing with non-LTS releases.

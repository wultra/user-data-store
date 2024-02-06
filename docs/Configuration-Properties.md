# Configuration Properties

The User Data Store uses the following public configuration properties:

## Database Configuration

| Property                                                       | Default   | Note                                                |
|----------------------------------------------------------------|-----------|-----------------------------------------------------|
| `spring.datasource.url`                                        | `_empty_` | Database JDBC URL                                   |
| `spring.datasource.username`                                   | `_empty_` | Database JDBC username                              |
| `spring.datasource.password`                                   | `_empty_` | Database JDBC password                              |
| `spring.datasource.driver-class-name`                          | `_empty_` | Datasource JDBC class name                          | 
| `spring.jpa.hibernate.ddl-auto`                                | `none`    | Configuration of automatic database schema creation | 
| `spring.jpa.properties.hibernate.connection.characterEncoding` | `_empty_` | Character encoding                                  |
| `spring.jpa.properties.hibernate.connection.useUnicode`        | `_empty_` | Character encoding - Unicode support                |


## User Data Store Configuration

| Property                                   | Default   | Note                                                                                                                                                                                                                         |
|--------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `user-data-store.db.master.encryption.key` | `_empty_` | Master DB encryption key (AES-256, key length of 32 bytes, base64 encoded) to derive server private keys for the encryption of sensitive data in the database. An empty value means no encryption, which is not recommended. |  

# Monitoring and Observability

| Property                                  | Default | Note                                                                                                                                                                       |
|-------------------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `management.tracing.sampling.probability` | `1.0`   | Specifies the proportion of requests that are sampled for tracing. A value of 1.0 means that 100% of requests are sampled, while a value of 0 effectively disables tracing |
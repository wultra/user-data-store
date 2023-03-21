# Configuration Properties

The User Data Store uses the following public configuration properties:

## Database Configuration

| Property                                                       | Default   | Note                                                |
|----------------------------------------------------------------|-----------|-----------------------------------------------------|
| `spring.datasource.url`                                        | `_empty_` | Database JDBC URL                                   |
| `spring.datasource.username`                                   | `_empty_` | Database JDBC username                              |
| `spring.datasource.password`                                   | `_empty_` | Database JDBC password                              |
| `spring.datasource.driver-class-name`                          | `_empty_` | Datasource JDBC class name                          | 
| `spring.jpa.database-platform`                                 | `_empty_` | Database dialect                                    | 
| `spring.jpa.hibernate.ddl-auto`                                | `none`    | Configuration of automatic database schema creation | 
| `spring.jpa.properties.hibernate.connection.characterEncoding` | `_empty_` | Character encoding                                  |
| `spring.jpa.properties.hibernate.connection.useUnicode`        | `_empty_` | Character encoding - Unicode support                |


## User Data Store Configuration

| Property                                   | Default   | Note                                                                                                                                  |
|--------------------------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------|
| `user-data-store.db.master.encryption.key` | `_empty_` | Master DB encryption key for decryption of server private key in database. Empty value means no encryption, which is not recommended. | 

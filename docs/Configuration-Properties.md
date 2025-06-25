# Configuration Properties

The User Data Store uses the following public configuration properties:

## Database Configuration

| Property                                                       | Default   | Note                                                |
|----------------------------------------------------------------|-----------|-----------------------------------------------------|
| `spring.datasource.url`                                        | `_empty_` | Database JDBC URL                                   |
| `spring.datasource.username`                                   | `_empty_` | Database JDBC username                              |
| `spring.datasource.password`                                   | `_empty_` | Database JDBC password                              |
| `spring.jpa.hibernate.ddl-auto`                                | `none`    | Configuration of automatic database schema creation | 
| `spring.jpa.properties.hibernate.connection.characterEncoding` | `_empty_` | Character encoding                                  |
| `spring.jpa.properties.hibernate.connection.useUnicode`        | `_empty_` | Character encoding - Unicode support                |


## User Data Store Configuration

| Property                                   | Default   | Note                                                                                                                                                                                                                         |
|--------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `user-data-store.db.master.encryption.key` | `_empty_` | Master DB encryption key (AES-256, key length of 32 bytes, base64 encoded) to derive server private keys for the encryption of sensitive data in the database. An empty value means no encryption, which is not recommended. |  


## OAuth2.x / OpenID Connect (OIDC)

Instead of basic authentication, you may use OAuth or OpenID Connect (OIDC).
User Data Store accepts only JWT, not opaque tokens.
Please note that User Data Store works with two roles: `READ`, and `WRITE`.
So users have to contain the claim `roles` with a required value in the used token.
The name of the role is case-sensitive.
(Mind that the role name without the `ROLE_` prefix is used, unlike the basic authentication where the authority is inserted in the database table `uds_authorities`.)
The claim name may be changed in the configuration `user-data-store.security.auth.oauth2.roles-claim=roles`.

| Property                                                | Default Value | Description                                                                                                                            |
|---------------------------------------------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `user-data-store.security.auth.type`                    | `BASIC_HTTP`  | `BASIC_HTTP` for basic HTTP authentication or `OAUTH2` for OpenID Connect. If OAUTH enabled, the properties bellow must be configured. |
| `user-data-store.security.auth.oauth2.roles-claim`      | `roles`       | A name of the claim in the JWT that contains the user roles.                                                                           |
| `spring.security.oauth2.resource-server.jwt.issuer-uri` |               | URL of the provider, e.g. `https://sts.windows.net/example/`                                                                           |
| `spring.security.oauth2.resource-server.jwt.audiences`  |               | A comma-separated list of allowed `aud` JWT claim values to be validated.                                                              |

See the [Spring Security documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html#oauth2-client-log-users-in) and [OpenID Connect UserInfo endpoint](https://connect2id.com/products/server/docs/api/userinfo) for details.


## Monitoring and Observability

| Property                                  | Default | Note                                                                                                                                                                        |
|-------------------------------------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `management.tracing.sampling.probability` | `1.0`   | Specifies the proportion of requests that are sampled for tracing. A value of 1.0 means that 100% of requests are sampled, while a value of 0 effectively disables tracing. |
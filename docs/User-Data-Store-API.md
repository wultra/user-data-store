# User Data Store API
<!-- template api -->

User Data Store provides a RESTful API which is used for interacting with the service.

The generated REST API documentation in deployed User Data Store:

```
http[s]://[host]:[port]/user-data-store/swagger-ui/index.html
```

Following endpoints are published in User Data Store RESTful API:

<!-- begin remove -->
## Methods

### Document API

- [GET /documents](#fetch-documents) - Fetch documents
- [POST /admin/documents](#create-a-document) - Create a document
- [PUT /admin/documents/{documentId}](#update-a-document) - Update a document
- [DELETE /admin/documents](#delete-documents) - Delete documents

### Photo API

- [GET /photos](#fetch-photos) - Fetch photos
- [POST /admin/photos](#create-a-photo) - Create a photo
- [PUT /admin/photo/{photoId}](#update-a-photo) - Update a photo
- [DELETE /admin/photos](#delete-photos) - Delete photos

### Attachment API

- [GET /attachments](#fetch-attachments) - Fetch attachments
- [POST /admin/attachments](#create-an-attachment) - Create an attachment
- [PUT /admin/attachments/{attachmentId}](#update-an-attachment) - Update an attachment
- [DELETE /admin/attachments](#delete-photos) - Delete attachments

### Claims API

- [GET /claims](#fetch-claims) - Fetch claims
- [POST /admin/claims](#create-claims) - Create claims
- [PUT /admin/claims](#update-claims) - Update claims
- [DELETE /admin/claims](#delete-claims) - Delete claims

### User Claims API (Deprecated)

<!-- begin box warning -->
This REST API is deprecated, see Swagger for usage information.
<!-- end -->

- `GET` `/private/user-claims` - Fetch user claims
- `POST` `/public/user-claims` - Create or update user claims
- `DELETE` `/public/user-claims` - Delete user claims

<!-- end -->

### Error Handling

User Data Store uses following format for error response body, accompanied by an appropriate HTTP status code. Besides the HTTP error codes that application server may return regardless of server application (such as 404 when resource is not found or 503 when server is down), following status codes may be returned:

| Status  | HTTP Code | Description                                                 |
|---------|-----------|-------------------------------------------------------------|
| OK      | 200       | No issue                                                    |
| ERROR   | 400       | Issue with a request format, or issue of the business logic |
| ERROR   | 401       | Unauthorized                                                |
| ERROR   | 409       | Request could not be processed on the server                |

All error responses that are produced by the User Data Store have the following body:

```json

{
  "status": "ERROR",
  "responseObject": {
    "code": "ERROR_GENERIC",
    "message": "An error message"
  }
}
```

- `status` - `OK`, `ERROR`
- `code` - `ERROR_GENERIC`
- `message` - Message that describes certain error.

### Authentication

Access to the REST API is authenticated. See chapter [Setting Up REST Service Credentials](Deploying-User-Data-Store.md#setting-up-rest-service-credentials) for configuring access to the REST API.

Use HTTP Basic authentication to access the REST API, for example:

`Authorization: Basic YWRtaW46YWRtaW4=`

The username and password is a Base-64 encoded string in the format `username:password`.

The following paths require a ROLE_WRITE authority:
- `/admin/**` - supported REST API for User Data Store
- `/public/**` - **deprecated** REST API for user claims

The remainder of the REST API paths require a ROLE_READ authority.

<!-- begin api GET /documents -->
### Fetch Documents

Fetch documents for a user.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/documents</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                                                         |
|----------------------------------------------------------|----------|---------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of fetched documents.                  |
| `documentId`                                             | `String` | Optional document identifier to allow fetching a specific document. |

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "documents": [
      {
        "id": "e6eea62b-274b-4c6a-81a8-5bbc75811863",
        "userId": "user1",
        "documentType": "profile",
        "dataType": "claims",
        "documentDataId": null,
        "externalId": null,
        "documentData": "...",
        "attributes": {},
        "timestampCreated": "2024-06-20T14:45:51.568024",
        "timestampLastUpdated": null
      }
    ]
  }
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: 'e6eea62b-274b-4c6a-81a8-5bbc75811863'"
  }
}
```
<!-- end -->

<!-- begin api POST /admin/documents -->
### Create a Document

Create a document with optional photos and attachments.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/documents</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

```json
{
  "requestObject": {
    "userId": "user1",
    "documentType": "profile",
    "dataType": "claims",
    "documentDataId": null,
    "externalId": null,
    "documentData": "...",
    "attributes": {
       "attribute1": "value1"
    },
    "photos": [
      {
        "photoType": "person",
        "photoData": "...",
        "externalId": null
      }
    ],
    "attachments": [
      {
        "attachmentType": "binary_base64",
        "attachmentData": "...",
        "externalId": null
      }
    ]
  }
}
```

##### Request Params

| Parameter                                                      | Type                                    | Description                                                                                                                     |
|----------------------------------------------------------------|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span>       | `String`                                | User identifier of document owner.                                                                                              |
| `documentType`<span class="required" title="Required">*</span> | `String`                                | One of `profile`, `personal_id`, `passport`, `drivers_license`, `payment_card`, `loyalty`, `photo`.                             |
| `dataType`<span class="required" title="Required">*</span>     | `String`                                | One of `claims`, `jwt`, `vc`, `image_base64`, `binary_base64`, `url`.                                                           |
| `documentDataId`                                               | `String`                                | Optional identifier of the data stored in the document (e.g. ID card number).                                                   |
| `externalId`                                                   | `String`                                | Optional external identifier of the document (e.g. identifier used in the bank system).                                         |
| `documentData`<span class="required" title="Required">*</span> | `String`                                | Document data. Use `{}` for empty data.                                                                                         |
| `attributes`<span class="required" title="Required">*</span>   | `Map<String, Object>`                   | Attributes containing metadata for the document. Use `{}` for empty attributes.                                                 |
| `photos`                                                       | `List<EmbeddedPhotoCreateRequest>`      | Optional list of photos which are created together with the document. See [POST /admin/photos](#create-a-photo).                |
| `attachments`                                                  | `List<EmbeddedAttachmentCreateRequest>` | Optional list of attachments which are created together with the document. See [POST /admin/attachments](#create-an-attachment) |

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "541d5681-245e-48ab-ad4d-3da8a363c923",
    "documentDataId": null,
    "photos": [
      {
        "id": "e6eea62b-274b-4c6a-81a8-5bbc75811863"
      }
    ],
    "attachments": [
      {
        "id": "fd8dbaf3-af64-43c7-b439-24ca79025b20"
      }
    ]
  }
}
```
<!-- end -->

<!-- begin api PUT /admin/documents/{documentId} -->
### Update a Document

Update a document.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/documents/{documentId}</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Path Params

| Param                                                        | Type     | Description          |
|--------------------------------------------------------------|----------|----------------------|
| `documentId`<span class="required" title="Required">*</span> | `String` | Document identifier. |


```json
{
  "requestObject": {
    "userId": "user1",
    "documentType": "profile",
    "dataType": "claims",
    "documentDataId": null,
    "externalId": null,
    "documentData": "...",
    "attributes": {
       "attribute1": "value1"
    }
  }
}
```

##### Request Params

| Parameter                                                      | Type                                    | Description                                                                                                                     |
|----------------------------------------------------------------|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span>       | `String`                                | User identifier of document owner.                                                                                              |
| `documentType`<span class="required" title="Required">*</span> | `String`                                | One of `profile`, `personal_id`, `passport`, `drivers_license`, `payment_card`, `loyalty`, `photo`.                             |
| `dataType`<span class="required" title="Required">*</span>     | `String`                                | One of `claims`, `jwt`, `vc`, `image_base64`, `binary_base64`, `url`.                                                           |
| `documentDataId`                                               | `String`                                | Optional identifier of the data stored in the document (e.g. ID card number).                                                   |
| `externalId`                                                   | `String`                                | Optional external identifier of the document (e.g. identifier used in the bank system).                                         |
| `documentData`<span class="required" title="Required">*</span> | `String`                                | Document data. Use `{}` for empty data.                                                                                         |
| `attributes`<span class="required" title="Required">*</span>   | `Map<String, Object>`                   | Attributes containing metadata for the document. Use `{}` for empty attributes.                                                 |

#### Response 200

```json
{
  "status": "OK"
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: '8ab06a8d-b850-4259-9756-52ed44514b1'"
  }
}
```
<!-- end -->

<!-- begin api DELETE /admin/documents -->
### Delete Documents

Delete documents.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/documents</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                                                         |
|----------------------------------------------------------|----------|---------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of documents to be deleted.            |
| `documentId`                                             | `String` | Optional document identifier to allow deleting a specific document. |

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api GET /photos -->
### Fetch photos

Fetch photos for a user.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/photos</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                        | Type     | Description                                            |
|--------------------------------------------------------------|----------|--------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span>     | `String` | User identifier of the owner of fetched photos.        |
| `documentId`<span class="required" title="Required">*</span> | `String` | Document identifier of document related to the photos. |

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "photos": [
      {
        "id": "c55b1970-a336-49d4-8067-7aa32d64eebe",
        "userId": null,
        "documentId": "49c6e850-900e-4d90-bdc8-d9bb47e44384",
        "externalId": null,
        "photoType": "person",
        "photoData": "...",
        "timestampCreated": "2024-06-20T16:06:39.313191",
        "timestampLastUpdated": null
      }
    ]
  }
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: '8ab06a8d-b850-4259-9756-52ed44514b1'"
  }
}
```
<!-- end -->

<!-- begin api POST /admin/photos -->
### Create a Photo

Create a photo.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/photos</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

```json
{
  "requestObject": {
    "userId": "user1",
    "documentId": "c55b1970-a336-49d4-8067-7aa32d64eebe",
    "photoType": "person",
    "photoData": "...",
    "externalId": null
  }
}
```

##### Request Params

| Parameter                                                    | Type     | Description                                                                           |
|--------------------------------------------------------------|----------|---------------------------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span>     | `String` | User identifier of document owner.                                                    |
| `documentId`<span class="required" title="Required">*</span> | `String` | Identifier of the related document.                                                   |
| `photoType`<span class="required" title="Required">*</span>  | `String` | One of `person`, `document_front_side`, `document_back_side`, `person_with_document`. |
| `photoData`<span class="required" title="Required">*</span>  | `String` | Base-64 encoded photo data.                                                           |
| `externalId`                                                 | `String` | Optional external identifier of the photo (e.g. identifier used in the bank system).  |

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "e42c8432-6971-419d-9a23-1c4042d91e24",
    "documentId": "c55b1970-a336-49d4-8067-7aa32d64eebe"
  }
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: '49c6e850-900e-4d90-bdc8-d9bb47e44384'"
  }
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "User reference not valid, ID: 'user1'"
  }
}
```
<!-- end -->

<!-- begin api PUT /admin/photos/{photoId} -->
### Update a Photo

Update a photo.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/photos/{photoId}</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Path Params

| Param     | Type     | Description       |
|-----------|----------|-------------------|
| `photoId` | `String` | Photo identifier. |

```json
{
  "requestObject": {
    "photoType": "person",
    "photoData": "...",
    "externalId": null
  }
}
```

##### Request Params

| Parameter                                                   | Type     | Description                                                                           |
|-------------------------------------------------------------|----------|---------------------------------------------------------------------------------------|
| `photoType`<span class="required" title="Required">*</span> | `String` | One of `person`, `document_front_side`, `document_back_side`, `person_with_document`. |
| `photoData`<span class="required" title="Required">*</span> | `String` | Base-64 encoded photo data.                                                           |
| `externalId`                                                | `String` | Optional external identifier of the photo (e.g. identifier used in the bank system).  |

#### Response 200

```json
{
  "status": "OK"
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Photo not found, ID: 'e42c8432-6971-419d-9a23-1c4042d91e24'"
  }
}
```
<!-- end -->

<!-- begin api DELETE /admin/photos -->
### Delete Photos

Delete photos.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/photos</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                                                                    |
|----------------------------------------------------------|----------|--------------------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of deleted photos.                                |
| `documentId`                                             | `String` | Optional document identifier to allow deleting photos for a specific document. |

#### Response 200

```json
{
  "status": "OK"
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: '49c6e850-900e-4d90-bdc8-d9bb47e44384'"
  }
}
```
<!-- end -->

<!-- begin api GET /attachments -->
### Fetch attachments

Fetch attachments for a user.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/attachments</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                        | Type     | Description                                                 |
|--------------------------------------------------------------|----------|-------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span>     | `String` | User identifier of the owner of fetched attachments.        |
| `documentId`<span class="required" title="Required">*</span> | `String` | Document identifier of document related to the attachments. |

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "attachments": [
      {
        "id": "c55b1970-a336-49d4-8067-7aa32d64eebe",
        "userId": null,
        "documentId": "49c6e850-900e-4d90-bdc8-d9bb47e44384",
        "externalId": null,
        "attachmentType": "binary_base64",
        "attachmentData": "...",
        "timestampCreated": "2024-06-20T16:06:39.313191",
        "timestampLastUpdated": null
      }
    ]
  }
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: '8ab06a8d-b850-4259-9756-52ed44514b1'"
  }
}
```
<!-- end -->

<!-- begin api POST /admin/attachments -->
### Create an Attachment

Create an attachment.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/attachments</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

```json
{
  "requestObject": {
    "userId": "user1",
    "documentId": "c55b1970-a336-49d4-8067-7aa32d64eebe",
    "attachmentType": "binary_base64",
    "attachmentData": "...",
    "externalId": null
  }
}
```

##### Request Params

| Parameter                                                        | Type     | Description                                                                               |
|------------------------------------------------------------------|----------|-------------------------------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span>         | `String` | User identifier of document owner.                                                        |
| `documentId`<span class="required" title="Required">*</span>     | `String` | Identifier of the related document.                                                       |
| `attachmentType`<span class="required" title="Required">*</span> | `String` | One of `text`, `image_base64`, `binary_base64`.                                           |
| `attachmentData`<span class="required" title="Required">*</span> | `String` | Base-64 encoded attachment data.                                                          |
| `externalId`                                                     | `String` | Optional external identifier of the attachment (e.g. identifier used in the bank system). |

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "e42c8432-6971-419d-9a23-1c4042d91e24",
    "documentId": "c55b1970-a336-49d4-8067-7aa32d64eebe"
  }
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: '49c6e850-900e-4d90-bdc8-d9bb47e44384'"
  }
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "User reference not valid, ID: 'user1'"
  }
}
```
<!-- end -->

<!-- begin api PUT /admin/attachments/{attachmentId} -->
### Update an Attachment

Update an attachment.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/attachments/{attachmentId}</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Path Params

| Param          | Type     | Description            |
|----------------|----------|------------------------|
| `attachmentId` | `String` | Attachment identifier. |

```json
{
  "requestObject": {
    "attachmentType": "binary_base64",
    "attachmentData": "...",
    "externalId": null
  }
}
```

##### Request Params

| Parameter                                                        | Type     | Description                                                                               |
|------------------------------------------------------------------|----------|-------------------------------------------------------------------------------------------|
| `attachmentType`<span class="required" title="Required">*</span> | `String` | One of `text`, `image_base64`, `binary_base64`.                                           |
| `attachmentData`<span class="required" title="Required">*</span> | `String` | Base-64 encoded attachment data.                                                          |
| `externalId`                                                     | `String` | Optional external identifier of the attachment (e.g. identifier used in the bank system). |

#### Response 200

```json
{
  "status": "OK"
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Attachment not found, ID: '7ae0eef7-d266-4662-9c20-749e42f69f1b'"
  }
}
```
<!-- end -->

<!-- begin api DELETE /admin/attachments -->
### Delete Attachments

Delete attachments.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/attachments</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                                                                         |
|----------------------------------------------------------|----------|-------------------------------------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of deleted attachments.                                |
| `documentId`                                             | `String` | Optional document identifier to allow deleting attachments for a specific document. |

#### Response 200

```json
{
  "status": "OK"
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Document not found, ID: '49c6e850-900e-4d90-bdc8-d9bb47e44384'"
  }
}
```
<!-- end -->

<!-- begin api GET /claims -->
### Fetch Claims

Fetch claims for a user.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/claims</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                                               |
|----------------------------------------------------------|----------|-----------------------------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of fetched claims.           |
| `claim`                                                  | `String` | Optional claim name for fetching individual claim values. |

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "claim1": "value1"
  }
}
```
<!-- end -->

<!-- begin api POST /admin/claims -->
### Create Claims

Create a claim.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/claims</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                             |
|----------------------------------------------------------|----------|-----------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of claims. |

```json
{
  "claim1": "value1"
}
```

#### Response 200

```json
{
  "status": "OK"
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "ALREADY_EXISTS",
    "message": "Claims for user 'user1' already exist"
  }
}
```
<!-- end -->

<!-- begin api PUT /admin/claims -->
### Update Claims

Create a claim.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/claims</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                             |
|----------------------------------------------------------|----------|-----------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of claims. |

```json
{
  "claim1": "value2"
}
```

#### Response 200

```json
{
  "status": "OK"
}
```

#### Response 400

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "NOT_FOUND",
    "message": "Claims for user 'user1' do not exist"
  }
}
```
<!-- end -->

<!-- begin api DELETE /admin/claims -->
### Delete Claims

Delete claims.

<!-- begin remove -->

<table>
    <tr>
        <td>Method</td>
        <td><code>DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td><code>/admin/claims</code></td>
    </tr>
</table>
<!-- end -->

#### Request

- Headers:
    - `Authorization: Basic ...`

##### Query Params

| Param                                                    | Type     | Description                                         |
|----------------------------------------------------------|----------|-----------------------------------------------------|
| `userId`<span class="required" title="Required">*</span> | `String` | User identifier of the owner of deleted claims.     |
| `claim`                                                  | `String` | Optional claim name for deleting individual claims. |

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->
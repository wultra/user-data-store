# User Data Store Documentation

The User Data Store component provides backed functionality for following use cases:
- Storing digital user documents: ID cards, passports, driver's licenses, payment cards, loyalty cards, etc.
- Storing information about user, for example name, address, various government identifiers, etc.
- Storing photos related to the user: both sides of the card, photos from the KYC process, etc.
- Storing any attachments related to user documents, such as digital contracts or other files. 

The data is stored securely in the database, encrypted with row-based encryption of individual records. See the chapter [Deploying User Data Store](./Deploying-User-Data-Store.md) for information about setting up encryption and how to protect the REST API access.

The component provides a REST API which allows managing user data in the database. The following REST APIs are available:
- **Document API** at `/documents` - allows managing user documents
- **Photo API** at `/photos` - allows managing photos
- **Attachment API** at `/attachments` - allows managing attachments
- **Claims API** at `/claims` - allows managing user claims

For detailed REST API documentation, see [User Data Store REST API](./User-Data-Store-API.md).

## Deployment Tutorials

- [Configuration Properties](./Configuration-Properties.md)
- [Database Structure](./Database-Structure.md)
- [Deploying User Data Store](./Deploying-User-Data-Store.md)
- [User Data Store REST API](./User-Data-Store-API.md)
- [Migration Instructions](./Migration-Instructions.md)

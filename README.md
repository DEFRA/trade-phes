# Defra: Trade: PHES
## Plant Health Export Service

Mono-repo for Defra's Plant Health Export Service.

## Package structure

[plants-backend-adapter-service](plants-backend-adapter-service/)
> Deals with the communications between Java, Dynamics & the Trade Platform. It also converts requests to OData queries and back again, mapping to models as relevant

[plants-file-storage-service](plants-file-storage-service/)
> Handles file uploads & downloads, takes a file from the Node tier, hands it off for anti-virus scanning and deals with Azure Blob to store the file if valid

## Licence

THIS INFORMATION IS LICENSED UNDER THE CONDITIONS OF THE OPEN GOVERNMENT LICENCE found at:

<http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3>

The following attribution statement MUST be cited in your products and applications when using this information.

> Contains public sector information licensed under the Open Government license v3

### About the licence

The Open Government Licence (OGL) was developed by the Controller of Her Majesty's Stationery Office (HMSO) to enable information providers in the public sector to license the use and re-use of their information under a common open licence.

It is designed to encourage use and re-use of information freely and flexibly, with only a few conditions.

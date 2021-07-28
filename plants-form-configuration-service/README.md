# Form Configuration Service

## Introduction

The Form Configuration Service provides access to the form configurations.

## Pre-Requisites

### Environment Variables

The following environment variables must be set before the service can be run.
(DB values will be determined by the instance you are connecting to)

- REFERENCE_DATA_SERVICE_VERSION (this value is read during deployment in ops-pipeline)
- REFERENCE_DATA_SERVICE_HOST
- REFERENCE_DATA_SERVICE_SCHEME (e.g. http)
- REFERENCE_DATA_SERVICE_PORT
- REFERENCE_DATA_SERVICE_DB_HOST
- REFERENCE_DATA_SERVICE_DB_PORT
- REFERENCE_DATA_SERVICE_DB_NAME
- REFERENCE_DATA_SERVICE_DB_USER
- REFERENCE_DATA_SERVICE_DB_PASSWORD

### Dependencies

In order to run the service you will need the following dependencies

- JDK v1.8
- Maven v3

### Data Dependencies

In order to run the service, the reference data database schema and source data needs to have been initialised.

Run the installation steps in the sub-projects:

- `database`
- `data-management`

## How to run

The service can be installed and run with:

```
./go
```

- Once running, you can browse: `http://localhost:<PORT>/`
- Check the Swagger documentation for details of REST endpoints and operations.

### Intellij setup

- See related instructions in `notification-service` for setting up Intellij
- Ensure that all environment variables are set correctly within the Intellij 'Run/Debug configuration'

### Browsing Swagger documentation

- Refer to the related instructions in `notification-service`
- Use the `ReferenceDataSwaggerV1.yaml` file

## How to test

### Unit Tests

Unit tests can be run with:

```
mvn clean test
```

The coverage report can be created with:

```
mvn clean test jacoco:report
```

The coverage report can then be viewed by opening the `target/site/jacoco/index.html` file in your browser.

### Integration Tests

See the 'integration' project for further details on how to run integration tests.

## How to create Docker images

Please refer to [TracesX documentation](https://traces-x-docs.herokuapp.com/index.html#local-development-environment).

## Directory Structure

The directory structure is laid out according to standard Maven project structure:

### Source files

- `src/main/java` - Java source files
- `src/test/java` - Java unit test files

### Source packages

###### `uk.gov.defra.plants.formconfiguration`

The root package for the Application.  Includes the Dropwizard Application and Configuration implementation.

###### `uk.gov.defra.plants.formconfiguration.dao`

This package defines an interface for create / read / update operations on reference data.  Operations are defined using JDBI annotations.  Operations must be annotated with a `Mapper` definition to specify how data is mapped to model object. 

###### `uk.gov.defra.plants.formconfiguration.dao.mapper`

This package defines the mappers used in the data operations specified in the `dao` package.

###### `uk.gov.defra.plants.formconfiguration.health`

This package contains implementations of Dropwizard [health checks](http://metrics.dropwizard.io/4.0.0/getting-started.html#health-checks), to report on the status of dependent services.

Two checks are defined:
- Database health check
- Application health check

###### `uk.gov.defra.plants.formconfiguration.logging`

This package contains the Application Insights appender factory, used to allow logging via Logback and Application Insights

###### `uk.gov.defra.plants.formconfiguration.model`

This package contains model objects which represent reference data. 

###### `uk.gov.defra.plants.formconfiguration.resource`

This package contains classes which define the resources available as URI endpoints.  

###### `uk.gov.defra.plants.formconfiguration.security`

This package defines security constraints for the service.  At present, basic authentication is required in order to access the service.  A [dropwizard authenticator](http://www.dropwizard.io/4.0.0/docs/manual/auth.html) is defined to handle basic authentication.

###### `uk.gov.defra.plants.formconfiguration.service`

This package defines the service layer which currently bridges the REST endpoints and the database layer.  

### Packaged files

- `target` - Compiled assets are written to this directory.

## Logging

Logback logging configuration is defined in `config.yml`.

A custom appender is created for use with Application Insights (the Azure centralised logging platform).
A custom factory is defined in `uk.gov.defra.plants.formconfiguration.logging.ApplicationInsightsAppenderFactory.java` to return an instance of `ApplicationInsightsAppender`.

## Contributing

To make DB/API changes we need to make the following changes to this code:

- Add/modify the database as necessary.
- Make changes necessary in `src/main/java/uk/gov/defra/exports/formconfiguration/model` to correspond to the DB.
- Alter the service (if required) and DAO according the changes made to the DB models.
- Alter the resource in `src/main/java/uk/gov/defra/exports/formconfiguration/resource` (if required).
- Alter the DB mapper appropriately.

We can add a service by performing the following tasks:

- Add the service to `src/main/java/uk/gov/defra/exports/formconfiguration/service`.
- Add the resource to `src/main/java/uk/gov/defra/exports/formconfiguration/resource`.

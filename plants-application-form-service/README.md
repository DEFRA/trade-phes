# Exports Application Form Service

## Introduction

The Application Form Service is a Java Dropwizard application which acts as a service layer between the database and the front-end application.

The Application Form service is responsible for transactional operations relating to 'notifications' which pass between various actors using the front-end Traces-X application.

More information about the Traces-X application is available [here](https://traces-x-docs.herokuapp.com/index.html).

## Pre-Requisites

### Environment Variables

The following environment variables must be set before the service can be run.
(DB values will be determined by the instance you are connecting to)

- APPLICATION_FORM_SERVICE_DB_USER
- APPLICATION_FORM_SERVICE_DB_PASSWORD
- APPLICATION_FORM_SERVICE_DB_HOST
- APPLICATION_FORM_SERVICE_DB_PORT
- APPLICATION_FORM_SERVICE_DB_NAME
- APPLICATION_FORM_SERVICE_SCHEME (e.g. http)
- APPLICATION_FORM_SERVICE_HOST (e.g. localhost)
- APPLICATION_FORM_SERVICE_PORT (e.g. 4560)
- APPLICATION_FORM_SERVICE_PASSWORD
- APPLICATION_FORM_SERVICE_DB_MIN_SIZE
- APPLICATION_FORM_SERVICE_DB_MAX_SIZE

### Dependencies

In order to run the service you will need the following dependencies

- JDK v1.8
- Maven v3

## How to run

The service can be installed and run with:

```
./go
```

### Intellij setup

- Right-click on the `ExportsApplication` file
- Select 'Run'  (this will fail with an error)
- Select Run / Edit configurations
- Add 'Program arguments': `server config.yml`
- Set environment variables
  - `APPLICATION_FORM_SERVICE_DB_USER`
  - `APPLICATION_FORM_SERVICE_DB_PASSWORD`
  - `APPLICATION_FORM_SERVICE_DB_HOST`
  - `APPLICATION_FORM_SERVICE_DB_PORT`
  - `APPLICATION_FORM_SERVICE_DB_NAME`
  - `APPLICATION_FORM_SERVICE_PORT`
  - `APPLICATION_FORM_SERVICE_PASSWORD`
  - `APPLICATION_FORM_SERVICE_DB_MIN_SIZE`
  - `APPLICATION_FORM_SERVICE_DB_MAX_SIZE`
- Click OK
- You should now be able to run / debug the project

### Browsing Swagger documentation

- Run Docker image: `docker run -d -p 80:8080 swaggerapi/swagger-editor`
- Browse `http://localhost`
- Open the Swagger file (from within Swagger web UI)
  - File / Import File
  - Browse to `ApplicationFormSwaggerV1.yaml` and click OK
- The Swagger API should now be presented

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

###### `uk.gov.defra.plants`

The root package for the Application.  Includes the Dropwizard Application and Configuration implementation.

###### `uk.gov.defra.plants.application.representation`

Contains Java objects which can be used to map to and from JSON representations.

###### `uk.gov.defra.plants.application.representation.enumeration`

This package contains enums which define string constants used in various stages during the application form logic.  These constants are also set during JSON serialization / deserialization.

For example: 'Temperature' : ('Ambient', 'Chilled', 'Frozen')

###### `uk.gov.defra.plants.application.dao`

This package defines an interface for create / read / update operations on application form data.  Operations are defined using JDBI annotations.  Operations must be annotated with a `Mapper` definition to specify how data is mapped to model object.

###### `uk.gov.defra.plants.application.dao.mapper`

This package defines the mappers used in the data operations specified in the `dao` package.

###### `uk.gov.defra.plants.application.health`

This package contains implementations of Dropwizard [health checks](http://metrics.dropwizard.io/4.0.0/getting-started.html#health-checks), to report on the status of dependent services.

Two checks are defined:
- Database health check
- Application health check

###### `uk.gov.defra.plants.application.logging`

This package contains the Application Insights appender factory, used to allow logging via Logback and Application Insights

###### `uk.gov.defra.plants.application.model`

This package contains model objects which represent application form data.

###### `uk.gov.defra.plants.application.resource`

This package contains classes which define the resources available as URI endpoints.  Specifically:

- '/admin'
- '/applicationForm'
- '/ping'

###### `uk.gov.defra.plants.application.resource.mapper`

This package defines a mapper transformation class, which can map a JSON response object into a model object (and vice versa).

###### `uk.gov.defra.plants.application.security`

This package defines security constraints for the service.  At present, basic authentication is required in order to access the service.  A [dropwizard authenticator](http://www.dropwizard.io/0.7.1/docs/manual/auth.html) is defined to handle basic authentication.

###### `uk.gov.defra.plants.application.service`

This package defines the service layer which currently bridges the REST endpoints and the database layer.  The service offers basic create / read / update logic.

### Packaged files

- `target` - Compiled assets are written to this directory.

## Logging

Logback logging configuration is defined in `config.yml`.

A custom appender is created for use with Application Insights (the Azure centralised logging platform).
A custom factory is defined in `uk.gov.defra.plants.applicationform.logger.ApplicationInsightsAppenderFactory.java` to return an instance of `ApplicationInsightsAppender`.

## Contributing

To make DB/API changes we need to make the following changes to this code:

- Add/modify the database as necessary.
- Make changes necessary in `src/main/java/uk/gov/defra/exports/application/model` to correspond to the DB.
- Alter the service (if required) and DAO according the changes made to the DB models.
- Alter the resource in `src/main/java/uk/gov/defra/exports/application/resource` (if required).
- Alter the response mapper and DB mapper appropriately.

We can add a service by performing the following tasks:

- Add the service to `src/main/java/uk/gov/defra/exports/application/service`.
- Add the resource to `src/main/java/uk/gov/defra/exports/application/resource`.
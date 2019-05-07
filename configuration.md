# Configuration Guide

Like Health APIs Data Query, Community Care Eligibility runs in Docker containers in AWS and are configured via properties files in AWS S3 
buckets. Application properties, certificates, and the corresponding Kong configuration are copied from
the S3 bucket into containers during the start up process. 

Docker containers must be bootstrapped with environment variables that enable access
to the S3 buckets.

## S3
Buckets are organized as follows:

```
S3
 ├ ${app-name}/application.properties
 ├ ${app-name}/<any>.jks
 ├ ${app-name}/<any>-truststore.jks
 ├ ..
 └ ${app-name}-kong/kong.yml
```

- Application names can be anything. This will be configured per container using the `AWS_APP_NAME`
  environment variable.
- Each application has has an `application.properties` which contain Spring Boot configuration.
- `system_certs` for Community Care Eligibility are currently co-located with the application.properties file (rather than referencing the `system_certs` bucket due to referencing  Urgent Care for the Queen Elizabeth component, this may not be the desired end state) and may contain any number or keystore and truststore files.

##### On container start
- Based on the configured application name, the `application.properties` is copied to `/opt/va/` 
  to be loaded by the Spring Boot application.
- Files and directories are recursively copied to `/opt/va/certs`.
  When configuring application properties, note the path will be `/opt/va/certs/<any>.jks`


## Docker Configuration

The following environment variables need to be set:

```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_DEFAULT_REGION
AWS_BUCKET_NAME
AWS_CONFIG_FOLDER_CCE
AWS_CONFIG_FOLDER_KONG
```

## Application Properties

Applications _must_ define the following properties. 

> Standard Spring Boot configuration properties as well as other application-specific advanced
> configuration properties are available.
> See `src/main/resources/application.properties` in each application. 

### Community Care Eligibility
```
bing-maps.api-key .......................Bing Maps API     
va-facilities.api-key ...................VA Facilities API
va-facilities.url .......................VA Facilities API URL
community-care.max-drive-time ...........Max drive time (minutes) criteria for eligibility
community-care.max-wait .................Max wait time (days) for eligibility

# Server SSL
server.ssl.key-store ....................Path to keystore, e.g. /opt/va/certs/<any>.jks
server.ssl.key-alias ....................Alias for key in keystore
server.ssl.key-store-password ...........Password for the keystore
server.ssl.trust-store ..................Path to truststore
server.ssl.trust-store-password .........Password for the truststore
server.ssl.enabled ......................<true|false> If enabled, mutual TLS is configured 

# Client SSL
ssl.key-store ...........................Path to keystore to use as a client
ssl.key-store-password ..................Password for the keystore
ssl.client-key-password .................Password for the client key
ssl.use-trust-store .....................<true|false> If enabled, mutual TLS is configured 
ssl.trust-store .........................Path to truststore
ssl.trust-store-password ................Password for the truststore

#E&E Request Properties
ee.endpoint.url .........................E&E Endpoint URL
ee.header.username ......................E&E Username
ee.header.password ......................E&E Password
ee.request.name .........................E&E Request Name
ee.truststore.path ......................E&E truststore path
ee.truststore.password ..................E&E truststore password


```
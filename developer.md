# Local Development

This application requires instances of Facilities API and E&E. Becuase E&E is not accessible outside the VA Intranet,
[Mock-EE](https://github.com/department-of-veterans-affairs/health-apis-mock-eligibility-and-enrollment)
is recommended for local development.

Refer to [health-apis-parent](https://github.com/department-of-veterans-affairs/health-apis-parent)
for basic environment setup. (Java, Maven, Docker, etc.)

Build all of the modules:

`mvn clean install`

A `secrets.conf` file is required for generating local configuration. For example:

```
#!/bin/bash
export VA_FACILITIES_API_KEY='xxx'
export VA_FACILITIES_URL='https://sandbox-api.va.gov/services/va_facilities/'
```

Generate configuration for local development:

`src/scripts/make-configs.sh`

Verify application properties for local development:

`less community-care-eligibility/config/application-dev.properties`

To launch the application:

`java -Dspring.profiles.active=dev -jar community-care-eligibility/target/community-care-eligibility-${VERSION}.jar`

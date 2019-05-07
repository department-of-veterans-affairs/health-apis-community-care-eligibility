# health-apis-community-care-eligibility

This API is a [Spring Boot](https://spring.io/projects/spring-boot) microservice 
that computes overall community-care eligibility by combining eligibility information 
from the Eligibility and Enrollment System (E&E) with wait- and drive-time access 
standards. Average wait times are provided by Facilities API, while average drive 
times are computed by Bing Maps.

----

## Building
- [Java Development Kit](https://openjdk.java.net/) 8
- [Maven](http://maven.apache.org/) 3.5
- [PlantUml](http://plantuml.com/) for diagrams
- Recommended [IntelliJ](https://www.jetbrains.com/idea/)
  or [Eclipse](https://www.eclipse.org/downloads/packages/installer)
  with the following plugins
  - [Lombok](https://projectlombok.org/)
  - [Google Java Format](https://github.com/google/google-java-format)
- [git-secrets](https://github.com/awslabs/git-secrets)

#### Maven
- Formats Java, XML, and JSON files
  (See the [Style Guide](https://google.github.io/styleguide/javaguide.html))
- Enforces unit test code coverage
- Performs [Checkstyle](http://checkstyle.sourceforge.net/) analysis using Google rules
- Performs [SpotBugs](https://spotbugs.github.io/) analysis
  with [Find Security Bugs](http://find-sec-bugs.github.io/) extensions
- Enforces Git branch naming conventions to support Jira integration

The above build steps can be skipped for use with IDE launch support by disabling the
_standard_ profile, e.g. `mvn -P'!standard' package`

#### git-secrets
git-secrets must be installed and configured to scan for AWS entries and the patterns in
[.git-secrets-patterns](.git-secrets-patterns). Exclusions are managed in
[.gitallowed](.gitallowed).
The [init-git-secrets.sh](src/scripts/init-git-secrets.sh) script can be used to simply set up.

> ###### !!  Mac users
> If using [Homebrew](https://brew.sh/), use `brew install --HEAD git-secrets` as decribed
> by [this post](https://github.com/awslabs/git-secrets/issues/65#issuecomment-416382565) to
> avoid issues committing multiple files.

----

## Tools
`src/scripts` provides tools to support development activities.

> !!  Review each script before running to ensure you understand exactly what it does.

- `init-git-secrets.sh`
  Initializes your clone of this repository to work with git secrets.

----

## Running

The spring application requires an
[external configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
for environment specific information, such as other service URLs. In production or
production-like environments, configuration is stored in AWS S3 buckets. In local developer
environments, configuration can be `config/` directories that are not maintained in Git. See
a teammate for connection details to developr resources.

See the [configuration guide](configuration.md) for configuring applications in AWS.

The following properties control the wait- and drive-time limits for primary vs. 
specialty care. By default, the configuration is 30-minute drive and 28-day wait for 
primary care, and 60-minute drive with a 20-day wait for specialty care.

```
community-care.max-drive-time-min-primary=30
community-care.max-wait-days-primary=28
community-care.max-drive-time-min-specialty=60
community-care.max-wait-days-specialty=20
```

This API supports a single search query that accepts a patient ICN, the patient's home address, 
their desired medical service type, and whether or not they are an established patient.

The medical service type must be one of:
* Audiology
* Cardiology
* Dermatology
* EmergencyCare
* Gastroenterology
* Gynecology
* MentalHealthCare
* Ophthalmology
* Optometry
* Orthopedic
* PrimaryCare
* UrgentCare
* Urology
* WomensHealth

For the search, the API combines data from three sources:
1. Patient eligibility information from E&E
2. All medical facilities in the state, from Facilities API
3. Drive times from the patient address to the medical facilities, from Bing Maps

The above data is combined to make an *objective* determination of community-care-eligibility. 
The response includes a description of the E&E eligibility codes and the IDs of any VA health 
facilities that satisfy the access standards.

Sample request:

```
https://foo.com/community-care/v0/eligibility/api/search?patient=0123456789V012345&street=742%20Evergreen%20Terrace&city=Springfield&state=KY&zip=12345&serviceType=primarycare&establishedPatient=false
```

Sample response:

```
{
  "patientRequest": {
    "patientIcn": "0123456789V012345",
    "patientAddress": {
      "street": "742 Evergeen Terrace",
      "city": "Springfield",
      "state": "KY",
      "zip": "12345"
    },
    "patientCoordinates": {
      "latitude": 12.34,
      "longitude": 56.78
    },
    "serviceType": "PrimaryCare",
    "establishedPatient": false
  },
  "communityCareEligibility": {
    "eligible": false,
    "description": "Urgent Care, Access-Standards",
    "facilities": [
      "vha_12345XY"
    ]
  },
  "facilities": [{
      "id": "vha_12345XY",
      "name": "Springfield VA Clinic",
      "address": {
        "street": "2900 South Street",
        "city": "Springfield",
        "state": "KY",
        "zip": "12345"
      },
      "coordinates": {
        "latitude": 90.12,
        "longitude": 34.56
      },
      "phoneNumber": "000-555-0321",
      "waitDays": {
        "newPatient": 19,
        "establishedPatient": 2
      },
      "driveMinutes": 25
    }, {
      "id": "vha_67890ZZ",
      "name": "Shelbyville VA Clinic",
      "address": {
        "street": "372 Main Street",
        "city": "Shelbyville",
        "state": "KY",
        "zip": "54321"
      },
      "coordinates": {
        "latitude": 78.90,
        "longitude": 12.34
      },
      "phoneNumber": "000-555-0123",
      "waitDays": {
        "newPatient": 14,
        "establishedPatient": 1
      },
      "driveMinutes": 90
    }
  ]
}
```

# health-apis-community-care-eligibility

This API is a [Spring Boot](https://spring.io/projects/spring-boot) microservice
that computes *objective* overall community-care eligibility by combining eligibility codes
from the Eligibility and Enrollment System (E&E) with wait- and drive-time access
standards described [here](https://www.va.gov/opa/pressrel/pressrelease.cfm?id=5187).
Average wait times are provided by Facilities API.
Average drive times are computed by Bing Maps.

![applications](src/plantuml/apps.png)

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
The configuration also includes properties for the wait- and drive-time access standards,
which default to the values described [here](https://www.va.gov/opa/pressrel/pressrelease.cfm?id=5187).

This API supports a search query that accepts a patient ICN, the patient's home address,
a medical service type, and whether or not the patient is established.

The medical service type is one of:
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

The API combines data from three sources:
1. Patient eligibility information from E&E.
2. All medical facilities in the state, from Facilities API.
3. Drive times from the patient address to the medical facilities, from Bing Maps.

This data is used to compute an *objective* determination of community-care-eligibility.
The response includes a description of the E&E eligibility codes and the IDs of any VA health
facilities that satisfy the access standards.

Sample request:

```
https://foo.com/community-care/v0/eligibility/api/search?patient=011235813V213455&street=742%20Evergreen%20Terrace&city=Springfield&state=KY&zip=89144&serviceType=primarycare&establishedPatient=false
```

Sample response:

```
{
  "patientRequest": {
    "patientIcn": "011235813V213455",
    "patientAddress": {
      "street": "742 Evergeen Terrace",
      "city": "Springfield",
      "state": "KY",
      "zip": "89144"
    },
    "patientCoordinates": {
      "latitude": 233.377,
      "longitude": 610.987
    },
    "serviceType": "PrimaryCare",
    "establishedPatient": false,
    "timeStamp": "2019-05-09T13:17:58.250Z"
  },
  "communityCareEligibility": {
    "eligible": true,
    "eligibilityCodes":[
      {
         "description": "Hardship",
         "code": "H"
      },
      {
         "description": "Urgent Care",
         "code": "U"
      }
    ],
    "facilities": [
      "vha_1597XY"
    ]
  },
  "facilities": [{
      "id": "vha_1597XY",
      "name": "Springfield VA Clinic",
      "address": {
        "street": "2584 South Street",
        "city": "Springfield",
        "state": "KY",
        "zip": "10946"
      },
      "coordinates": {
        "latitude": 41.81,
        "longitude": 67.65
      },
      "phoneNumber": "177-112-8657 x",
      "waitDays": {
        "newPatient": 19,
        "establishedPatient": 2
      },
      "driveMinutes": 25
    }, {
      "id": "vha_46368ZZ",
      "name": "Shelbyville VA Clinic",
      "address": {
        "street": "121393 Main Street",
        "city": "Shelbyville",
        "state": "KY",
        "zip": "75025"
      },
      "coordinates": {
        "latitude": 196.418,
        "longitude": 317.811
      },
      "phoneNumber": "1-422-983-2040",
      "waitDays": {
        "newPatient": 14,
        "establishedPatient": 1
      },
      "driveMinutes": 90
    }
  ]
}
```

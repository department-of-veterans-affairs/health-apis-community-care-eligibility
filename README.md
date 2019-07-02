# health-apis-community-care-eligibility

This API is a [Spring Boot](https://spring.io/projects/spring-boot) microservice
that computes **objective** overall community-care eligibility by combining eligibility codes
from the Eligibility and Enrollment System (E&E) with drive-time access
standards.

![applications](src/plantuml/apps.png)

Average drive times are also provided by Facilities API.
(Integration with VIA is in-progress.)

For more information about the end-to-end flow of information and interactions between systems/APIs, see the [sequence diagram](sequence-diagram.md).

For more information about the deployment architecture in different environments,
see the [architecture diagrams](architecture.md).

For details about building and running this application, see the [developer guide](developer.md).

----

The API supports a search query that accepts a patient ICN
and the patient's desired medical service type.

The medical service type is one of:
* Audiology
* Nutrition
* Optometry
* Podiatry
* PrimaryCare

The API combines data from two sources:
1. Patient eligibility information, from E&E.
2. VA health facilities in the state, from Facilities API.

This data is used to compute an overall determination of community-care-eligibility
based on the **objective** criteria of the MISSION Act. The six criteria are described
[here](https://www.va.gov/COMMUNITYCARE/docs/pubfiles/factsheets/VA-FS_CC-Eligibility.pdf).
The objective criteria of the MISSION Act are:
1. Service unavailable
2. Residence in a state without a full-service VA medical facility
3. 40-mile legacy/grandfathered from the Choice program
4. Access standards

The other two criteria, *best medical interest* and *quality standards*, are subjective
criteria outside the scope of this API. Because this API does not include subjective criteria,
its eligibility decisions **are not final**. A user-facing message
based on the result of this API should stress that the patient is *probably* eligible or
*probably not* eligible, and that no decision is final until they have consulted VA staff
and scheduled their appointment.

The response includes a description of the E&E eligibility codes and the IDs of any VA health
facilities that satisfy the access standards.

Sample request:

```
https://foo.com/community-care/v0/eligibility/search?patient=011235813V213455&serviceType=primarycare
```

Sample response:

```
{
  "patientRequest" : {
    "patientIcn" : "011235813V213455",
    "patientAddress" : {
      "street" : "742 Evergeen Terrace",
      "city" : "Springfield",
      "state" : "KY",
      "zip" : "89144"
    },
    "serviceType" : "PrimaryCare",
    "timestamp" : "2019-05-09T13:17:58.250Z"
  },
  "eligibilityCodes" : [
    {
      "description" : "Basic",
      "code" : "B"
    }
  ],
  "grandfathered" : false,
  "noFullServiceVaMedicalFacility" : false,
  "nearbyFacilities" : [
    {
      "id" : "vha_1597XY",
      "name" : "Springfield VA Clinic",
      "physicalAddress" : {
        "street" : "2584 South Street",
        "city" : "Springfield",
        "state" : "KY",
        "zip" : "10946"
      },
      "coordinates" : {
        "latitude" : 41.81,
        "longitude" : 67.65
      },
      "phoneNumber" : "177-112-8657 x",
      "website" : "https://www.va.gov",
    },
    {
      "id" : "vha_46368ZZ",
      "name" : "Shelbyville VA Clinic",
      "physicalAddress" : {
        "street" : "121393 Main Street",
        "city" : "Shelbyville",
        "state" : "KY",
        "zip" : "75025"
      },
      "coordinates" : {
        "latitude" : 196.418,
        "longitude" : 317.811
      },
      "phoneNumber" : "1-422-983-2040",
      "website" : "https://www.va.gov",
    }
  ],
  "eligible" : false
}
```

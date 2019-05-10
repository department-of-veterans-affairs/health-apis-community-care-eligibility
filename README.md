# health-apis-community-care-eligibility

This API is a [Spring Boot](https://spring.io/projects/spring-boot) microservice
that computes *objective* overall community-care eligibility by combining eligibility codes
from the Eligibility and Enrollment System (E&E) with wait- and drive-time access
standards described [here](https://www.va.gov/opa/pressrel/pressrelease.cfm?id=5187).
Average wait times are provided by Facilities API.
Average drive times are computed by Bing Maps.

![applications](src/plantuml/apps.png)

Bing Maps is expected to be removed in the near future, when average drive times become
available in the Facilities API.

For details about building and running this application, see the [developer guide](developer.md).

----

The API supports a search query that accepts a patient ICN, the patient's home address,
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
  "patientRequest" : {
    "patientIcn" : "011235813V213455",
    "patientAddress" : {
      "street" : "742 Evergeen Terrace",
      "city" : "Springfield",
      "state" : "KY",
      "zip" : "89144"
    },
    "patientCoordinates" : {
      "latitude" : 233.377,
      "longitude" : 610.987
    },
    "serviceType" : "PrimaryCare",
    "establishedPatient" : false,
    "timestamp" : "2019-05-09T13:17:58.250Z"
  },
  "communityCareEligibility" : {
    "eligible" : true,
    "eligibilityCode" : [
      {
        "description" : "Hardship",
        "code" : "H"
      },
      {
        "description" : "Urgent Care",
        "code" : "U"
      }
    ],
    "facilities" : [
      "vha_1597XY"
    ]
  },
  "facilities" : [
    {
      "id" : "vha_1597XY",
      "name" : "Springfield VA Clinic",
      "address" : {
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
      "waitDays" : {
        "newPatient" : 19,
        "establishedPatient" : 2
      },
      "driveMinutes" : 25
    },
    {
      "id" : "vha_46368ZZ",
      "name" : "Shelbyville VA Clinic",
      "address" : {
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
      "waitDays" : {
        "newPatient" : 14,
        "establishedPatient" : 1
      },
      "driveMinutes" : 90
    }
  ]
}
```

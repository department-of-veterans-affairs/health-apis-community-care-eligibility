# health-apis-community-care-eligibility

This API is a [Spring Boot](https://spring.io/projects/spring-boot) microservice
that computes **objective** overall community-care eligibility by combining eligibility codes
from the Eligibility and Enrollment System (E&E) with wait- and drive-time access
standards.
(Average historical wait times are provided by Facilities API. Average drive times are also provided by Facilities API. )

![applications](src/plantuml/apps.png)

Historical wait times from the Facilities API are expected to be replaced
with live wait times from the VistA Integration Adapter (VIA).

For more information about the deployment architecture in different environments,
see the [architecture diagrams](architecture.md).

For details about building and running this application, see the [developer guide](developer.md).

----

The API supports a search query that accepts a patient ICN, the patient's home address,
a medical service type, and whether or not the patient is established.

The medical service type is one of:
* Audiology
* Nutrition
* Optometry
* Podiatry
* PrimaryCare

The API combines data from two sources:
1. Patient eligibility information, from E&E.
2. Medical facilities in the state, from Facilities API.

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
*probably not* eligible.

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
      }
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
      }
    }
  ]
}
```

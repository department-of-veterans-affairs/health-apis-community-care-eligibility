package gov.va.api.health.communitycareeligibility.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.CommunityCareEligibility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.EligibilityCode;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.PatientRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerCommunityCareEligibilityResponse {
  static final CommunityCareEligibilityResponse
      SWAGGER_EXAMPLE_COMMUNITY_CARE_ELIGIBILITY_RESPONSE =
          CommunityCareEligibilityResponse.builder()
              .patientRequest(
                  PatientRequest.builder()
                      .patientIcn("011235813V213455")
                      .patientAddress(
                          Address.builder()
                              .street("742 Evergeen Terrace")
                              .city("Springfield")
                              .state("KY")
                              .zip("89144")
                              .build())
                      .serviceType("PrimaryCare")
                      .timestamp("2019-05-09T13:17:58.250Z")
                      .build())
              .communityCareEligibility(
                  CommunityCareEligibility.builder()
                      .eligible(true)
                      .eligibilityCode(
                          asList(
                              EligibilityCode.builder().description("Hardship").code("H").build(),
                              EligibilityCode.builder()
                                  .description("Urgent Care")
                                  .code("U")
                                  .build()))
                      .facilities(asList("vha_1597XY"))
                      .build())
              .facilities(
                  asList(
                      Facility.builder()
                          .id("vha_1597XY")
                          .name("Springfield VA Clinic")
                          .address(
                              Address.builder()
                                  .street("2584 South Street")
                                  .city("Springfield")
                                  .state("KY")
                                  .zip("10946")
                                  .build())
                          .coordinates(
                              Coordinates.builder().latitude(41.81).longitude(67.65).build())
                          .phoneNumber("177-112-8657 x")
                          .waitDays(19)
                          .build(),
                      Facility.builder()
                          .id("vha_46368ZZ")
                          .name("Shelbyville VA Clinic")
                          .address(
                              Address.builder()
                                  .street("121393 Main Street")
                                  .city("Shelbyville")
                                  .state("KY")
                                  .zip("75025")
                                  .build())
                          .coordinates(
                              Coordinates.builder().latitude(196.418).longitude(317.811).build())
                          .phoneNumber("1-422-983-2040")
                          .waitDays(14)
                          .build()))
              .build();
}

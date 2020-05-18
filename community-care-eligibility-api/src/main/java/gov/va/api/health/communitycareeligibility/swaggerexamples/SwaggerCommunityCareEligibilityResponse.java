package gov.va.api.health.communitycareeligibility.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import java.math.BigDecimal;

public class SwaggerCommunityCareEligibilityResponse {

  /** Swagger CommunityCareEligibilityResponse Example. */
  public static CommunityCareEligibilityResponse communityCareEligibilityResponse() {
    return CommunityCareEligibilityResponse.builder()
        .patientRequest(
            CommunityCareEligibilityResponse.PatientRequest.builder()
                .patientIcn("011235813V213455")
                .serviceType("PrimaryCare")
                .timestamp("2019-05-09T13:17:58.250Z")
                .build())
        .patientAddress(
            CommunityCareEligibilityResponse.Address.builder()
                .country("USA")
                .street("742 Evergeen Terrace")
                .city("Springfield")
                .state("KY")
                .zip("89144")
                .build())
        .patientCoordinates(
            CommunityCareEligibilityResponse.Coordinates.builder()
                .latitude(new BigDecimal("40.758541"))
                .longitude(new BigDecimal("-73.982132"))
                .build())
        .eligibilityCodes(
            asList(
                CommunityCareEligibilityResponse.EligibilityCode.builder()
                    .description("Basic")
                    .code("B")
                    .build()))
        .grandfathered(false)
        .noFullServiceVaMedicalFacility(false)
        .nearbyFacilities(
            asList(
                CommunityCareEligibilityResponse.Facility.builder()
                    .id("vha_1597XY")
                    .name("Springfield VA Clinic")
                    .physicalAddress(
                        CommunityCareEligibilityResponse.Address.builder()
                            .street("2584 South Street")
                            .city("Springfield")
                            .state("KY")
                            .zip("10946")
                            .build())
                    .coordinates(
                        CommunityCareEligibilityResponse.Coordinates.builder()
                            .latitude(new BigDecimal("41.81"))
                            .longitude(new BigDecimal("67.65"))
                            .build())
                    .phoneNumber("177-112-8657 x")
                    .website("https://www.va.gov")
                    .driveMinutes(
                        CommunityCareEligibilityResponse.DriveMinutes.builder()
                            .min(5)
                            .max(15)
                            .build())
                    .build(),
                CommunityCareEligibilityResponse.Facility.builder()
                    .id("vha_46368ZZ")
                    .name("Shelbyville VA Clinic")
                    .physicalAddress(
                        CommunityCareEligibilityResponse.Address.builder()
                            .street("121393 Main Street")
                            .city("Shelbyville")
                            .state("KY")
                            .zip("75025")
                            .build())
                    .coordinates(
                        CommunityCareEligibilityResponse.Coordinates.builder()
                            .latitude(new BigDecimal("196.418"))
                            .longitude(new BigDecimal("317.811"))
                            .build())
                    .driveMinutes(
                        CommunityCareEligibilityResponse.DriveMinutes.builder()
                            .min(15)
                            .max(25)
                            .build())
                    .active(true)
                    .mobile(false)
                    .phoneNumber("1-422-983-2040")
                    .website("https://www.va.gov")
                    .build()))
        .eligible(false)
        .build();
  }
}

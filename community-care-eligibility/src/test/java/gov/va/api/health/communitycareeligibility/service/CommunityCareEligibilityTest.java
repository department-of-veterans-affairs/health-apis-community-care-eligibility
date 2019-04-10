package gov.va.api.health.communitycareeligibility.service;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import lombok.SneakyThrows;
import org.junit.Test;

public class CommunityCareEligibilityTest {
  @Test
  @SneakyThrows
  public void happyPath() {
    Address patientAddress =
        Address.builder().street("66 Main St").city("Melbourne").state("FL").zip("12345").build();

    AccessToCareClient accessToCare = mock(AccessToCareClient.class);
    when(accessToCare.facilities(eq(patientAddress), eq("PrimaryCare")))
        .thenReturn(
            singletonList(
                AccessToCareFacility.builder()
                    .facilityId("FAC123")
                    .name("some facility")
                    .address("911 derp st")
                    .city("Palm Bay")
                    .state("fl")
                    .zip("75319")
                    .phone("867-5309")
                    .latitude(100)
                    .longitude(200)
                    .estWaitTime(10)
                    .newWaitTime(1)
                    .build()));

    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.driveTimeMinutes(eq(patientAddress), any(Facility.class))).thenReturn(15);

    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .accessToCare(accessToCare)
            .bingMaps(bingMaps)
            .maxDriveTime(1)
            .maxWait(1)
            .build();

    CommunityCareEligibilityResponse result =
        controller.search(
            patientAddress.street(),
            patientAddress.city(),
            patientAddress.state(),
            patientAddress.zip(),
            "PrimaryCare");

    System.out.println(
        JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result));
  }
}

package gov.va.api.health.communitycareeligibility.service;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.WaitDays;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resource;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resources;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.Test;

public final class CommunityCareEligibilityTest {
  @Test
  @SneakyThrows
  public void empty() {
    AccessToCareClient accessToCare = mock(AccessToCareClient.class);
    when(accessToCare.facilities(any(Address.class), any(String.class)))
        .thenReturn(singletonList(AccessToCareFacility.builder().build()));

    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.routes(any(Address.class), any(Facility.class)))
        .thenReturn(BingResponse.builder().build());

    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .accessToCare(accessToCare)
            .bingMaps(bingMaps)
            .maxDriveTime(1)
            .maxWait(1)
            .build();

    CommunityCareEligibilityResponse result =
        controller.search(" 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare");
    assertThat(result)
        .isEqualTo(CommunityCareEligibilityResponse.builder().communityCareEligible(true).build());
  }

  @Test
  @SneakyThrows
  public void happyPath() {
    AccessToCareClient accessToCare = mock(AccessToCareClient.class);
    when(accessToCare.facilities(
            eq(
                Address.builder()
                    .street("66 Main St")
                    .city("Melbourne")
                    .state("fl")
                    .zip("12345")
                    .build()),
            eq("primarycare")))
        .thenReturn(
            singletonList(
                AccessToCareFacility.builder()
                    .facilityId(" FAC123 ")
                    .name(" some facility ")
                    .address(" 911 derp st ")
                    .city(" Palm Bay ")
                    .state(" fl ")
                    .zip(" 75319 ")
                    .phone(" 867-5309 ")
                    .latitude(100.0)
                    .longitude(200.0)
                    .estWaitTime(10.0)
                    .newWaitTime(1.0)
                    .build()));

    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.routes(
            eq(
                Address.builder()
                    .street("66 Main St")
                    .city("Melbourne")
                    .state("fl")
                    .zip("12345")
                    .build()),
            any(Facility.class)))
        .thenReturn(
            BingResponse.builder()
                .resourceSets(
                    singletonList(
                        Resources.builder()
                            .resources(
                                singletonList(
                                    Resource.builder()
                                        .travelDuration((int) TimeUnit.MINUTES.toSeconds(30))
                                        .travelDurationTraffic((int) TimeUnit.MINUTES.toSeconds(45))
                                        .build()))
                            .build()))
                .build());

    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .accessToCare(accessToCare)
            .bingMaps(bingMaps)
            .maxDriveTime(1)
            .maxWait(1)
            .build();

    CommunityCareEligibilityResponse result =
        controller.search(" 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare");
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .communityCareEligible(true)
                .facilities(
                    singletonList(
                        Facility.builder()
                            .id("FAC123")
                            .name("some facility")
                            .address(
                                Address.builder()
                                    .street("911 derp st")
                                    .city("Palm Bay")
                                    .state("FL")
                                    .zip("75319")
                                    .build())
                            .coordinates(
                                Coordinates.builder().latitude(100.0).longitude(200.0).build())
                            .phoneNumber("867-5309")
                            .waitDays(
                                WaitDays.builder().newPatient(1).establishedPatient(10).build())
                            .driveMinutes(30)
                            .build()))
                .build());
  }
}

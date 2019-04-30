package gov.va.api.health.communitycareeligibility.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.WaitDays;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resource;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resources;
import gov.va.med.esr.webservices.jaxws.schemas.CommunityCareEligibilityInfo;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummary;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityCollection;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.DatatypeFactory;
import lombok.SneakyThrows;
import org.junit.Test;

public final class CommunityCareEligibilityTest {
  @Test
  @SneakyThrows
  public void disjointWaitTimeAndDriveTime() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    // when(eeClient.requestEligibility("1008679665V880686")).thenReturn(new
    // GetEESummaryResponse());

    Coordinates nearCoordinates = Coordinates.builder().latitude(1D).longitude(2D).build();
    Coordinates farCoordinates = Coordinates.builder().latitude(3D).longitude(4D).build();

    Coordinates patientCoordinates = Coordinates.builder().latitude(200D).longitude(100D).build();
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("fl")
                .zip("12345")
                .street("66 Main St")
                .build()))
        .thenReturn(patientCoordinates);
    when(bingMaps.routes(eq(patientCoordinates), eq(nearCoordinates)))
        .thenReturn(
            BingResponse.builder()
                .resourceSets(
                    singletonList(
                        Resources.builder()
                            .resources(
                                singletonList(
                                    Resource.builder()
                                        .travelDuration((int) TimeUnit.MINUTES.toSeconds(5))
                                        .build()))
                            .build()))
                .build());
    when(bingMaps.routes(eq(patientCoordinates), eq(farCoordinates)))
        .thenReturn(
            BingResponse.builder()
                .resourceSets(
                    singletonList(
                        Resources.builder()
                            .resources(
                                singletonList(
                                    Resource.builder()
                                        .travelDuration((int) TimeUnit.HOURS.toSeconds(1))
                                        .build()))
                            .build()))
                .build());

    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities(patientCoordinates, "primarycare"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    asList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("nearFac")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(1D)
                                    .longg(2D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .established(100)
                                                        .neww(100)
                                                        .service("primarycare")
                                                        .build()))
                                            .build())
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("near st")
                                                    .state("fl")
                                                    .build())
                                            .build())
                                    .build())
                            .build(),
                        VaFacilitiesResponse.Facility.builder()
                            .id("farFac")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(3D)
                                    .longg(4D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .established(0)
                                                        .neww(0)
                                                        .service("primarycare")
                                                        .build()))
                                            .build())
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("far st")
                                                    .state("fl")
                                                    .build())
                                            .build())
                                    .build())
                            .build()))
                .build());

    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .maxDriveTime(10)
            .maxWait(5)
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .eeClient(eeClient)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search(
            " 66 Main St",
            "Melbourne  ",
            " fl",
            " 12345 ",
            "primarycare",
            "1008679665V880686",
            false);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .facilities(
                asList(
                    Facility.builder()
                        .id("nearFac")
                        .address(Address.builder().street("near st").state("FL").build())
                        .coordinates(nearCoordinates)
                        .waitDays(
                            WaitDays.builder().newPatient(100).establishedPatient(100).build())
                        .driveMinutes(5)
                        .build(),
                    Facility.builder()
                        .id("farFac")
                        .address(Address.builder().street("far st").state("FL").build())
                        .coordinates(farCoordinates)
                        .waitDays(WaitDays.builder().newPatient(0).establishedPatient(0).build())
                        .driveMinutes(60)
                        .build()))
            .communityCareEligibility(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(true)
                    .description("Access-Standards")
                    .build())
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void empty() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility(any(String.class))).thenReturn(new GetEESummaryResponse());

    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.routes(any(Coordinates.class), any(Coordinates.class)))
        .thenReturn(BingResponse.builder().build());

    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities(any(Coordinates.class), any(String.class)))
        .thenReturn(VaFacilitiesResponse.builder().build());

    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .eeClient(eeClient)
            .maxDriveTime(1)
            .maxWait(1)
            .build();

    CommunityCareEligibilityResponse result =
        controller.search(
            " 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare", "123", true);
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .communityCareEligibility(
                    CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                        .eligible(true)
                        .description("Access-Standards")
                        .build())
                .facilities(Collections.emptyList())
                .build());
  }

  @Test
  @SneakyThrows
  public void happyPath() {
    GregorianCalendar gCal = new GregorianCalendar();
    gCal.setTime(Date.from(Instant.parse("2019-03-27T14:37:48Z")));
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("1008679665V880686"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .communityCareEligibilityInfo(
                            CommunityCareEligibilityInfo.builder()
                                .eligibilities(
                                    VceEligibilityCollection.builder()
                                        .eligibility(
                                            singletonList(
                                                VceEligibilityInfo.builder()
                                                    .vceCode("H")
                                                    .vceDescription("Hardship")
                                                    .vceEffectiveDate(
                                                        DatatypeFactory.newInstance()
                                                            .newXMLGregorianCalendar(gCal))
                                                    .build()))
                                        .build())
                                .build())
                        .build())
                .build());

    Coordinates testCoordinates = Coordinates.builder().latitude(200.00).longitude(100.00).build();
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("fl")
                .zip("12345")
                .street("66 Main St")
                .build()))
        .thenReturn(testCoordinates);
    when(bingMaps.routes(eq(testCoordinates), any(Coordinates.class)))
        .thenReturn(
            BingResponse.builder()
                .resourceSets(
                    singletonList(
                        Resources.builder()
                            .resources(
                                singletonList(
                                    Resource.builder()
                                        .travelDuration((int) TimeUnit.MINUTES.toSeconds(30))
                                        .build()))
                            .build()))
                .build());

    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities(testCoordinates, "primarycare"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id(" FAC123 ")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(200.00)
                                    .longg(100.00)
                                    .name(" some facility ")
                                    .phone(
                                        VaFacilitiesResponse.Phone.builder()
                                            .main(" 867-5309 ")
                                            .build())
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .established(10)
                                                        .neww(1)
                                                        .service("primarycare")
                                                        .build()))
                                            .build())
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1(" 911 derp st ")
                                                    .city(" Palm Bay ")
                                                    .state(" FL ")
                                                    .zip(" 75319 ")
                                                    .build())
                                            .build())
                                    .build())
                            .build()))
                .build());

    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .maxDriveTime(1)
            .maxWait(1)
            .eeClient(eeClient)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search(
            " 66 Main St",
            "Melbourne  ",
            " fl",
            " 12345 ",
            "primarycare",
            "1008679665V880686",
            true);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
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
                        .coordinates(testCoordinates)
                        .phoneNumber("867-5309")
                        .waitDays(WaitDays.builder().newPatient(1).establishedPatient(10).build())
                        .driveMinutes(30)
                        .build()))
            .communityCareEligibility(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(true)
                    .description("Hardship")
                    .build())
            .build();
    assertThat(actual).isEqualTo(expected);
  }
}

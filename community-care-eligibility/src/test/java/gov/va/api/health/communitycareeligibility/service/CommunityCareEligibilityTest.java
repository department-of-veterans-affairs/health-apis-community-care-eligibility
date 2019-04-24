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
import gov.va.med.esr.webservices.jaxws.schemas.CommunityCareEligibilityInfo;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummary;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityCollection;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public final class CommunityCareEligibilityTest {

  @Test
  @SneakyThrows
  public void empty() {

    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility(any(String.class))).thenReturn(new GetEESummaryResponse());

    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.routes(any(Coordinates.class), any(Facility.class)))
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
        controller.search(" 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare");
    assertThat(result)
        .isEqualTo(CommunityCareEligibilityResponse.builder().communityCareEligible(null).build());
  }

  @Test
  @SneakyThrows
  public void happyPath() {
    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date date = isoFormat.parse("2019-03-27T14:37:48");

    GregorianCalendar gCal = new GregorianCalendar();
    gCal.setTime(date);

    XMLGregorianCalendar xmlGregorianCalendar =
        DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);

    VceEligibilityInfo info = new VceEligibilityInfo();
    info.setVceCode("H");
    info.setVceDescription("Hardship");
    info.setVceEffectiveDate(xmlGregorianCalendar);

    VceEligibilityCollection vceEligibilityCollection = new VceEligibilityCollection();
    vceEligibilityCollection.getEligibility().add(info);

    CommunityCareEligibilityInfo communityCareEligibilityInfo = new CommunityCareEligibilityInfo();
    communityCareEligibilityInfo.setEligibilities(vceEligibilityCollection);

    EeSummary summary = new EeSummary();
    summary.setCommunityCareEligibilityInfo(communityCareEligibilityInfo);

    GetEESummaryResponse getEESummaryResponse = new GetEESummaryResponse();
    getEESummaryResponse.setSummary(summary);

    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("1008679665V880686")).thenReturn(getEESummaryResponse);

    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("fl")
                .zip("12345")
                .street("66 Main St")
                .build()))
        .thenReturn(Coordinates.builder().latitude(200.00).longitude(100.00).build());
    when(bingMaps.routes(
            eq(Coordinates.builder().longitude(100.00).latitude(200.00).build()),
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
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities(
            Coordinates.builder().longitude(100.00).latitude(200.00).build(), "primarycare"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.VaFacility.builder()
                            .id(" FAC123 ")
                            .attributes(
                                VaFacilitiesResponse.VaFacilityAttributes.builder()
                                    .lat(200.00)
                                    .longg(100.00)
                                    .name(" some facility ")
                                    .phone(
                                        VaFacilitiesResponse.VaFacilityAttributes.Phone.builder()
                                            .main(" 867-5309 ")
                                            .build())
                                    .waitTimes(
                                        VaFacilitiesResponse.VaFacilityAttributes.WaitTimes
                                            .builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.VaFacilityAttributes
                                                        .WaitTime.builder()
                                                        .established(10)
                                                        .neww(1)
                                                        .service("primarycare")
                                                        .build()))
                                            .build())
                                    .address(
                                        VaFacilitiesResponse.VaFacilityAttributes.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.VaFacilityAttributes
                                                    .PhysicalAddress.builder()
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

    CommunityCareEligibilityResponse result =
        controller.search(" 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare");
    assertThat(result)
        .isEqualTo(
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
                            .coordinates(
                                Coordinates.builder().latitude(200.0).longitude(100.0).build())
                            .phoneNumber("867-5309")
                            .waitDays(
                                WaitDays.builder().newPatient(1).establishedPatient(10).build())
                            .driveMinutes(30)
                            .build()))
                .communityCareEligibilities(
                    singletonList(
                        CommunityCareEligibilityResponse.CommunityCareEligibilities.builder()
                            .description("Hardship")
                            .code("H")
                            .effectiveDate("2019-03-27T14:37:48Z")
                            .build()))
                .build());
  }
}

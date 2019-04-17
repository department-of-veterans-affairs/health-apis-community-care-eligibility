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

    AccessToCareClient accessToCare = mock(AccessToCareClient.class);
    when(accessToCare.facilities(
            Address.builder()
                .street("66 Main St")
                .city("Melbourne")
                .state("fl")
                .zip("12345")
                .build(),
            "primarycare"))
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
                                Coordinates.builder().latitude(100.0).longitude(200.0).build())
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

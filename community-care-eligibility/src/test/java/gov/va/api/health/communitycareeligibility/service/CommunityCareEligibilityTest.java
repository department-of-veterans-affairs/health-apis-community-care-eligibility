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
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public final class CommunityCareEligibilityTest {

  @SneakyThrows
  private static XMLGregorianCalendar parseXmlGregorianCalendar(String timestamp) {
    GregorianCalendar gCal = new GregorianCalendar();
    gCal.setTime(Date.from(Instant.parse(timestamp)));
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
  }

  @Test
  @SneakyThrows
  public void controllerNullChecks() {
    assertThat(CommunityCareEligibilityV1ApiController.waitDays(null, true)).isNull();
    assertThat(CommunityCareEligibilityV1ApiController.waitDays(Facility.builder().build(), true))
        .isNull();
  }

  @Test
  @SneakyThrows
  public void disjointWaitTimeAndDriveTime() {
    Coordinates nearCoordinates = Coordinates.builder().latitude(1D).longitude(2D).build();
    Coordinates farCoordinates = Coordinates.builder().latitude(3D).longitude(4D).build();
    Coordinates patientCoordinates = Coordinates.builder().latitude(200D).longitude(100D).build();
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
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
    when(facilitiesClient.facilities("FL"))
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
            .maxDriveTimePrimary(10)
            .maxWaitPrimary(5)
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search(
            "123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare", false);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                (CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientCoordinates(patientCoordinates)
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345")
                            .street("66 Main St")
                            .build())
                    .timeStamp(actual.patientRequest().timeStamp())
                    .patientIcn("123")
                    .establishedPatient(false)
                    .serviceType("PrimaryCare")
                    .build()))
            .communityCareEligibilities(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(true)
                    .build())
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
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void empty() {
    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .bingMaps(mock(BingMapsClient.class))
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .build();
    CommunityCareEligibilityResponse result =
        controller.search("123", "66 Main St", "Melbourne", "fl", "12345 ", "primarycare", false);
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientAddress(
                            Address.builder()
                                .state("FL")
                                .city("Melbourne")
                                .zip("12345")
                                .street("66 Main St")
                                .build())
                        .patientIcn("123")
                        .timeStamp(result.patientRequest().timeStamp())
                        .establishedPatient(false)
                        .serviceType("PrimaryCare")
                        .build())
                .communityCareEligibilities(
                    CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                        .eligible(true)
                        .build())
                .facilities(Collections.emptyList())
                .build());
  }

  @Test
  @SneakyThrows
  public void facilityTransformerNullChecks() {
    assertThat(FacilityTransformer.builder().serviceType("primarycare").build().toFacility(null))
        .isNull();
    VaFacilitiesResponse.Facility facility = VaFacilitiesResponse.Facility.builder().build();
    Facility mapped =
        FacilityTransformer.builder().serviceType("primarycare").build().toFacility(facility);
    // top level attributes is null
    assertThat(mapped).isEqualTo(Facility.builder().build());
    facility =
        VaFacilitiesResponse.Facility.builder()
            .attributes(VaFacilitiesResponse.Attributes.builder().build())
            .build();
    mapped = FacilityTransformer.builder().serviceType("primarycare").build().toFacility(facility);
    // attribute is not null, but everything beyond it is
    assertThat(mapped).isEqualTo(Facility.builder().build());
    facility =
        VaFacilitiesResponse.Facility.builder()
            .attributes(
                VaFacilitiesResponse.Attributes.builder()
                    .address(VaFacilitiesResponse.Address.builder().build())
                    .build())
            .build();
    mapped = FacilityTransformer.builder().serviceType("primarycare").build().toFacility(facility);
    // Address is not null, but physical Address is
    assertThat(mapped).isEqualTo(Facility.builder().build());
    facility =
        VaFacilitiesResponse.Facility.builder()
            .attributes(
                VaFacilitiesResponse.Attributes.builder()
                    .address(
                        VaFacilitiesResponse.Address.builder()
                            .physical(VaFacilitiesResponse.PhysicalAddress.builder().build())
                            .build())
                    .build())
            .build();
    mapped = FacilityTransformer.builder().serviceType("primarycare").build().toFacility(facility);
    // Physical address exists, but all attributes are null
    assertThat(mapped).isEqualTo(Facility.builder().build());
  }

  @Test
  @SneakyThrows
  public void happyPath() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123"))
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
                                                        parseXmlGregorianCalendar(
                                                            "2019-03-27T14:37:48Z"))
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
                .state("FL")
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
    when(facilitiesClient.facilities("FL"))
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
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .eeClient(eeClient)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search(
            "123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare", false);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                (CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientCoordinates(testCoordinates)
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345")
                            .street("66 Main St")
                            .build())
                    .timeStamp(actual.patientRequest().timeStamp())
                    .patientIcn("123")
                    .establishedPatient(false)
                    .serviceType("PrimaryCare")
                    .build()))
            .communityCareEligibilities(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(true)
                    .eligibilityCodes(
                        Collections.singletonList(
                            CommunityCareEligibilityResponse.EligibilityCodes.builder()
                                .description("Hardship")
                                .code("H")
                                .build()))
                    .build())
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
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void mentalHealth() {
    Coordinates patientCoordinates = Coordinates.builder().latitude(1D).longitude(2D).build();
    Coordinates facilityCoordinates = Coordinates.builder().latitude(200D).longitude(100D).build();
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
                .zip("12345")
                .street("66 Main St")
                .build()))
        .thenReturn(patientCoordinates);
    when(bingMaps.routes(eq(patientCoordinates), eq(facilityCoordinates)))
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
    when(facilitiesClient.facilities("FL"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(200D)
                                    .longg(100D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .established(1)
                                                        .neww(10)
                                                        .service("MentalHealth")
                                                        .build()))
                                            .build())
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("911 derp st")
                                                    .state("FL")
                                                    .build())
                                            .build())
                                    .build())
                            .build()))
                .build());
    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .maxDriveTimePrimary(60)
            .maxWaitPrimary(2)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search(
            "123", "66 Main St", "Melbourne", "fl", "12345", "MentalHealthCare", true);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientIcn("123")
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345")
                            .street("66 Main St")
                            .build())
                    .timeStamp(actual.patientRequest().timeStamp())
                    .patientCoordinates(patientCoordinates)
                    .serviceType("MentalHealthCare")
                    .establishedPatient(true)
                    .build())
            .communityCareEligibilities(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(false)
                    .facilities(singletonList("FAC123"))
                    .build())
            .facilities(
                singletonList(
                    Facility.builder()
                        .id("FAC123")
                        .address(Address.builder().street("911 derp st").state("FL").build())
                        .coordinates(facilityCoordinates)
                        .waitDays(WaitDays.builder().newPatient(10).establishedPatient(1).build())
                        .driveMinutes(30)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void setDriveMinutesNullChecks() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123")).thenReturn(GetEESummaryResponse.builder().build());
    Coordinates testNullResourceSets =
        Coordinates.builder().latitude(200.00).longitude(100.00).build();
    Coordinates testNullRoutes = Coordinates.builder().latitude(220.00).longitude(100.00).build();
    Coordinates testNullResources =
        Coordinates.builder().latitude(240.00).longitude(100.00).build();
    Coordinates testNullTravelDuration =
        Coordinates.builder().latitude(260.00).longitude(100.00).build();
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
                .zip("12345")
                .street("Null Routes")
                .build()))
        .thenReturn(testNullRoutes);
    when(bingMaps.routes(eq(testNullRoutes), any(Coordinates.class))).thenReturn(null);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
                .zip("12345")
                .street("Routes But Null Resource Set")
                .build()))
        .thenReturn(testNullResourceSets);
    when(bingMaps.routes(eq(testNullResourceSets), any(Coordinates.class)))
        .thenReturn(BingResponse.builder().build());
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
                .zip("12345")
                .street("Resource Set But Null Resources")
                .build()))
        .thenReturn(testNullResources);
    when(bingMaps.routes(eq(testNullResources), any(Coordinates.class)))
        .thenReturn(
            BingResponse.builder()
                .resourceSets(Collections.singletonList(Resources.builder().build()))
                .build());
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
                .zip("12345")
                .street("Resources But Null Travel Duration")
                .build()))
        .thenReturn(testNullTravelDuration);
    when(bingMaps.routes(eq(testNullTravelDuration), any(Coordinates.class)))
        .thenReturn(
            BingResponse.builder()
                .resourceSets(
                    Collections.singletonList(
                        Resources.builder()
                            .resources(Collections.singletonList(Resource.builder().build()))
                            .build()))
                .build());
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities("FL"))
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
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .eeClient(eeClient)
            .build();
    assertThat(
            controller
                .search("123", "Null Routes", "Melbourne  ", " fl", " 12345 ", "primarycare", false)
                .facilities()
                .get(0)
                .driveMinutes())
        .isNull();
    assertThat(
            controller
                .search(
                    "123",
                    "Routes But Null Resource Set",
                    "Melbourne  ",
                    " fl",
                    " 12345 ",
                    "primarycare",
                    false)
                .facilities()
                .get(0)
                .driveMinutes())
        .isNull();
    assertThat(
            controller
                .search(
                    "123",
                    "Resource Set But Null Resources",
                    "Melbourne  ",
                    " fl",
                    " 12345 ",
                    "primarycare",
                    false)
                .facilities()
                .get(0)
                .driveMinutes())
        .isNull();
    assertThat(
            controller
                .search(
                    "123",
                    "Resources But Null Travel Duration",
                    "Melbourne  ",
                    " fl",
                    " 12345 ",
                    "primarycare",
                    false)
                .facilities()
                .get(0)
                .driveMinutes())
        .isNull();
  }

  @Test
  @SneakyThrows
  public void testNotYetEligibleDate() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123"))
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
                                                        parseXmlGregorianCalendar(
                                                            "2099-03-27T14:37:48Z"))
                                                    .build()))
                                        .build())
                                .build())
                        .build())
                .build());
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.routes(any(Coordinates.class), any(Coordinates.class)))
        .thenReturn(BingResponse.builder().build());
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities(any(String.class)))
        .thenReturn(VaFacilitiesResponse.builder().build());
    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .eeClient(eeClient)
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .build();
    CommunityCareEligibilityResponse result =
        controller.search(
            "123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare", false);
    assertThat(result.facilities().isEmpty());
  }

  @SneakyThrows
  @Test(expected = Exceptions.UnknownServiceTypeException.class)
  public void unknownServiceType() {
    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .bingMaps(mock(BingMapsClient.class))
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .build();
    controller.search("123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "Dentistry", false);
  }

  @Test
  @SneakyThrows
  public void urgentCare() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123"))
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
                                                    .vceCode("U")
                                                    .vceDescription("Urgent Care")
                                                    .vceEffectiveDate(
                                                        parseXmlGregorianCalendar(
                                                            "2019-03-27T14:37:48Z"))
                                                    .build()))
                                        .build())
                                .build())
                        .build())
                .build());
    Coordinates patientCoordinates = Coordinates.builder().latitude(1D).longitude(2D).build();
    Coordinates facilityCoordinates = Coordinates.builder().latitude(200D).longitude(100D).build();
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
                .zip("12345")
                .street("66 Main St")
                .build()))
        .thenReturn(patientCoordinates);
    when(bingMaps.routes(eq(patientCoordinates), eq(facilityCoordinates)))
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
    when(facilitiesClient.facilities("FL"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(200D)
                                    .longg(100D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .established(1)
                                                        .neww(10)
                                                        .service("UrgentCare")
                                                        .build()))
                                            .build())
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("911 derp st")
                                                    .state("FL")
                                                    .build())
                                            .build())
                                    .build())
                            .build()))
                .build());
    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .eeClient(eeClient)
            .maxDriveTimePrimary(60)
            .maxWaitPrimary(2)
            .maxDriveTimeSpecialty(60)
            .maxWaitSpecialty(2)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("123", "66 Main St", "Melbourne", "fl", "12345", "urgentcare", true);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientIcn("123")
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345")
                            .street("66 Main St")
                            .build())
                    .timeStamp(actual.patientRequest().timeStamp())
                    .patientCoordinates(patientCoordinates)
                    .serviceType("UrgentCare")
                    .establishedPatient(true)
                    .build())
            .communityCareEligibilities(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(true)
                    .eligibilityCodes(
                        Collections.singletonList(
                            CommunityCareEligibilityResponse.EligibilityCodes.builder()
                                .code("U")
                                .description("Urgent Care")
                                .build()))
                    .facilities(singletonList("FAC123"))
                    .build())
            .facilities(
                singletonList(
                    Facility.builder()
                        .id("FAC123")
                        .address(Address.builder().street("911 derp st").state("FL").build())
                        .coordinates(facilityCoordinates)
                        .waitDays(WaitDays.builder().newPatient(10).establishedPatient(1).build())
                        .driveMinutes(30)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void xIsIneligible() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123"))
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
                                                    .vceCode("X")
                                                    .vceDescription("Ineligible")
                                                    .vceEffectiveDate(
                                                        parseXmlGregorianCalendar(
                                                            "2019-03-27T14:37:48Z"))
                                                    .build()))
                                        .build())
                                .build())
                        .build())
                .build());
    Coordinates patientCoordinates = Coordinates.builder().latitude(1D).longitude(2D).build();
    Coordinates facilityCoordinates = Coordinates.builder().latitude(200D).longitude(100D).build();
    BingMapsClient bingMaps = mock(BingMapsClient.class);
    when(bingMaps.coordinates(
            Address.builder()
                .city("Melbourne")
                .state("FL")
                .zip("12345")
                .street("66 Main St")
                .build()))
        .thenReturn(patientCoordinates);
    when(bingMaps.routes(eq(patientCoordinates), eq(facilityCoordinates)))
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
    when(facilitiesClient.facilities("FL"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(200D)
                                    .longg(100D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .established(1)
                                                        .neww(10)
                                                        .service("dermatology")
                                                        .build()))
                                            .build())
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("911 derp st")
                                                    .state("FL")
                                                    .build())
                                            .build())
                                    .build())
                            .build()))
                .build());
    CommunityCareEligibilityV1ApiController controller =
        CommunityCareEligibilityV1ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .bingMaps(bingMaps)
            .eeClient(eeClient)
            .maxDriveTimePrimary(60)
            .maxWaitPrimary(2)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("123", "66 Main St", "Melbourne", "fl", "12345", "dermatology", true);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientIcn("123")
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345")
                            .street("66 Main St")
                            .build())
                    .patientCoordinates(patientCoordinates)
                    .timeStamp(actual.patientRequest().timeStamp())
                    .serviceType("Dermatology")
                    .establishedPatient(true)
                    .build())
            .communityCareEligibilities(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(false)
                    .eligibilityCodes(
                        Collections.singletonList(
                            CommunityCareEligibilityResponse.EligibilityCodes.builder()
                                .code("X")
                                .description("Ineligible")
                                .build()))
                    .build())
            .facilities(
                singletonList(
                    Facility.builder()
                        .id("FAC123")
                        .address(Address.builder().street("911 derp st").state("FL").build())
                        .coordinates(facilityCoordinates)
                        .waitDays(WaitDays.builder().newPatient(10).establishedPatient(1).build())
                        .driveMinutes(30)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }
}

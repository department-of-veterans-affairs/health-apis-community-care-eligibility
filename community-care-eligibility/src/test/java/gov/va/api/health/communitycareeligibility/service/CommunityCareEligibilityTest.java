package gov.va.api.health.communitycareeligibility.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.med.esr.webservices.jaxws.schemas.CommunityCareEligibilityInfo;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummary;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityCollection;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
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
  public void audiology() {
    Coordinates facilityCoordinates = Coordinates.builder().latitude(200D).longitude(100D).build();
    Address patientAddress =
        Address.builder().city("Melbourne").state("FL").zip("12345").street("66 Main St").build();
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);

    when(facilitiesClient.nearby(patientAddress, 60, "Audiology")).thenReturn(asList("FAC123"));
    when(facilitiesClient.facilities("FL", "Audiology"))
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
                                                        .neww(1)
                                                        .service("Audiology")
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

    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .maxDriveTimePrimary(60)
            .maxDriveTimeSpecialty(60)
            .maxWaitPrimary(2)
            .maxWaitSpecialty(2)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("123", "66 Main St", "Melbourne", "fl", "12345", "Audiology");
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
                    .timestamp(actual.patientRequest().timestamp())
                    .serviceType("Audiology")
                    .build())
            .communityCareEligibility(
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
                        .waitDays(1)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void disjointWaitTimeAndDriveTime() {
    Coordinates nearCoordinates = Coordinates.builder().latitude(1D).longitude(2D).build();
    Coordinates farCoordinates = Coordinates.builder().latitude(3D).longitude(4D).build();
    Address patientAddress =
        Address.builder().city("Melbourne").state("FL").zip("12345").street("66 Main St").build();
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);

    when(facilitiesClient.facilities("FL", "PrimaryCare"))
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
    when(facilitiesClient.nearby(patientAddress, 10, "PrimaryCare")).thenReturn(asList("nearFac"));
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .maxDriveTimePrimary(10)
            .maxWaitPrimary(5)
            .facilitiesClient(facilitiesClient)
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare");
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                (CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345")
                            .street("66 Main St")
                            .build())
                    .timestamp(actual.patientRequest().timestamp())
                    .patientIcn("123")
                    .serviceType("PrimaryCare")
                    .build()))
            .communityCareEligibility(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(true)
                    .build())
            .facilities(
                asList(
                    Facility.builder()
                        .id("nearFac")
                        .address(Address.builder().street("near st").state("FL").build())
                        .coordinates(nearCoordinates)
                        .waitDays(100)
                        .build(),
                    Facility.builder()
                        .id("farFac")
                        .address(Address.builder().street("far st").state("FL").build())
                        .coordinates(farCoordinates)
                        .waitDays(0)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void empty() {
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .build();
    CommunityCareEligibilityResponse result =
        controller.search("123", "66 Main St", "Melbourne", "fl", "12345 ", "primarycare");
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
                        .timestamp(result.patientRequest().timestamp())
                        .serviceType("PrimaryCare")
                        .build())
                .communityCareEligibility(
                    CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                        .eligible(true)
                        .build())
                .facilities(Collections.emptyList())
                .build());
  }

  @Test
  @SneakyThrows
  public void facilityTransformerNullChecks() {
    FacilityTransformer transformer = FacilityTransformer.builder().serviceType("xyz").build();

    // facility is null
    assertThat(transformer.toFacility(null)).isNull();

    // top level attributes is null
    assertThat(transformer.toFacility(VaFacilitiesResponse.Facility.builder().build()))
        .isEqualTo(Facility.builder().build());

    // empty attributes
    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder()
                    .attributes(VaFacilitiesResponse.Attributes.builder().build())
                    .build()))
        .isEqualTo(Facility.builder().build());

    // empty address
    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder()
                    .attributes(
                        VaFacilitiesResponse.Attributes.builder()
                            .address(VaFacilitiesResponse.Address.builder().build())
                            .build())
                    .build()))
        .isEqualTo(Facility.builder().build());

    // empty physical address
    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder()
                    .attributes(
                        VaFacilitiesResponse.Attributes.builder()
                            .address(
                                VaFacilitiesResponse.Address.builder()
                                    .physical(
                                        VaFacilitiesResponse.PhysicalAddress.builder().build())
                                    .build())
                            .build())
                    .build()))
        .isEqualTo(Facility.builder().build());
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
    Address patientAddress =
        Address.builder().city("Melbourne").state("FL").zip("12345").street("66 Main St").build();

    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities("FL", "PrimaryCare"))
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
    when(facilitiesClient.nearby(patientAddress, 1, "PrimaryCare")).thenReturn(emptyList());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .eeClient(eeClient)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare");
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                (CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345")
                            .street("66 Main St")
                            .build())
                    .timestamp(actual.patientRequest().timestamp())
                    .patientIcn("123")
                    .serviceType("PrimaryCare")
                    .build()))
            .communityCareEligibility(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(true)
                    .eligibilityCode(
                        Collections.singletonList(
                            CommunityCareEligibilityResponse.EligibilityCode.builder()
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
                        .waitDays(1)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
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
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities(any(String.class), any(String.class)))
        .thenReturn(VaFacilitiesResponse.builder().build());
    when(facilitiesClient.nearby(any(Address.class), any(int.class), any(String.class)))
        .thenReturn(emptyList());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(eeClient)
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .build();
    CommunityCareEligibilityResponse result =
        controller.search("123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "primarycare");
    assertThat(result.facilities().isEmpty());
  }

  @SneakyThrows
  @Test(expected = Exceptions.UnknownServiceTypeException.class)
  public void unknownServiceType() {
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .build();
    controller.search("123", " 66 Main St", "Melbourne  ", " fl", " 12345 ", "Dentistry");
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
    Coordinates facilityCoordinates = Coordinates.builder().latitude(200D).longitude(100D).build();
    Address patientAddress =
        Address.builder().city("Melbourne").state("FL").zip("12345").street("66 Main St").build();
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilities("FL", "Optometry"))
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
                                                        .neww(1)
                                                        .service("optometry")
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
    when(facilitiesClient.nearby(patientAddress, 60, "Optometry")).thenReturn(emptyList());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(eeClient)
            .maxDriveTimePrimary(60)
            .maxWaitPrimary(2)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("123", "66 Main St", "Melbourne", "fl", "12345", "optometry");
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
                    .timestamp(actual.patientRequest().timestamp())
                    .serviceType("Optometry")
                    .build())
            .communityCareEligibility(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(false)
                    .eligibilityCode(
                        Collections.singletonList(
                            CommunityCareEligibilityResponse.EligibilityCode.builder()
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
                        .waitDays(1)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }
}

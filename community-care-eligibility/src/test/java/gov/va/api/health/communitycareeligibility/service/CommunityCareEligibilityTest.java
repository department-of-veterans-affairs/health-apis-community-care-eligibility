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
import gov.va.med.esr.webservices.jaxws.schemas.AddressCollection;
import gov.va.med.esr.webservices.jaxws.schemas.AddressInfo;
import gov.va.med.esr.webservices.jaxws.schemas.CommunityCareEligibilityInfo;
import gov.va.med.esr.webservices.jaxws.schemas.ContactInfo;
import gov.va.med.esr.webservices.jaxws.schemas.DemographicInfo;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummary;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityCollection;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    when(facilitiesClient.nearbyFacilities(patientAddress, 60, "Audiology"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(200D)
                                    .lng(100D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .waitDays(
                                                            VaFacilitiesResponse.WaitDays.builder()
                                                                .neww(1.0)
                                                                .build())
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
    EligibilityAndEnrollmentClient client = mock(EligibilityAndEnrollmentClient.class);
    when(client.requestEligibility("123"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    new ArrayList<AddressInfo>(
                                                        Arrays.asList(
                                                            AddressInfo.builder()
                                                                .addressTypeCode("Residential")
                                                                .state("FL")
                                                                .city("Melbourne")
                                                                .line1("66 Main St")
                                                                .line2("")
                                                                .line3("")
                                                                .zipCode("12345")
                                                                .build())))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(client)
            .maxDriveTimePrimary(60)
            .maxDriveTimeSpecialty(60)
            .maxWaitPrimary(2)
            .maxWaitSpecialty(2)
            .build();
    CommunityCareEligibilityResponse actual = controller.search("123", "Audiology");
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
            .eligible(false)
            .eligibilityCodes(emptyList())
            .grandfathered(false)
            .noFullServiceVaMedicalFacility(false)
            .accessStandardsFacilities(singletonList("FAC123"))
            .nearbyFacilities(
                singletonList(
                    Facility.builder()
                        .id("FAC123")
                        .physicalAddress(
                            Address.builder().street("911 derp st").state("FL").build())
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

    when(facilitiesClient.nearbyFacilities(patientAddress, 10, "PrimaryCare"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    asList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("nearFac")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(1D)
                                    .lng(2D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .waitDays(
                                                            VaFacilitiesResponse.WaitDays.builder()
                                                                .neww(100.0)
                                                                .build())
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
                            .build()))
                .build());
    EligibilityAndEnrollmentClient client = mock(EligibilityAndEnrollmentClient.class);
    when(client.requestEligibility("123"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    new ArrayList<AddressInfo>(
                                                        Arrays.asList(
                                                            AddressInfo.builder()
                                                                .addressTypeCode("Residential")
                                                                .state("FL")
                                                                .city("Melbourne")
                                                                .line1("66 Main St")
                                                                .line2("")
                                                                .line3("")
                                                                .zipCode("12345")
                                                                .build())))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .maxDriveTimePrimary(10)
            .maxWaitPrimary(5)
            .facilitiesClient(facilitiesClient)
            .eeClient(client)
            .build();

    CommunityCareEligibilityResponse actual = controller.search("123", "primarycare");
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
            .eligibilityCodes(emptyList())
            .grandfathered(false)
            .noFullServiceVaMedicalFacility(false)
            .eligible(true)
            .accessStandardsFacilities(emptyList())
            .nearbyFacilities(
                asList(
                    Facility.builder()
                        .id("nearFac")
                        .physicalAddress(Address.builder().street("near st").state("FL").build())
                        .coordinates(nearCoordinates)
                        .waitDays(100)
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void empty() {
    EligibilityAndEnrollmentClient client = mock(EligibilityAndEnrollmentClient.class);
    when(client.requestEligibility("123"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    new ArrayList<AddressInfo>(
                                                        Arrays.asList(
                                                            AddressInfo.builder()
                                                                .addressTypeCode("Residential")
                                                                .state("FL")
                                                                .city("Melbourne")
                                                                .line1("66 Main St")
                                                                .line2("")
                                                                .line3("")
                                                                .zipCode("12345")
                                                                .build())))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(client)
            .build();
    CommunityCareEligibilityResponse result = controller.search("123", "primarycare");
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
                .eligibilityCodes(emptyList())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .eligible(true)
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
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    new ArrayList<AddressInfo>(
                                                        Arrays.asList(
                                                            AddressInfo.builder()
                                                                .addressTypeCode("Residential")
                                                                .state("FL")
                                                                .city("Melbourne")
                                                                .line1("66 Main St")
                                                                .line2("")
                                                                .line3("Apt. 602")
                                                                .zipCode("12345")
                                                                .zipPlus4("0104")
                                                                .build())))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    Address patientAddress =
        Address.builder()
            .city("Melbourne")
            .state("FL")
            .zip("12345-0104")
            .street("66 Main St Apt. 602")
            .build();

    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.nearbyFacilities(patientAddress, 1, "PrimaryCare"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id(" FAC123 ")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(200.00)
                                    .lng(100.00)
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
                                                        .waitDays(
                                                            VaFacilitiesResponse.WaitDays.builder()
                                                                .neww(1.0)
                                                                .build())
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
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .eeClient(eeClient)
            .build();
    CommunityCareEligibilityResponse actual = controller.search("123", "primarycare");
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                (CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientAddress(
                        Address.builder()
                            .state("FL")
                            .city("Melbourne")
                            .zip("12345-0104")
                            .street("66 Main St Apt. 602")
                            .build())
                    .timestamp(actual.patientRequest().timestamp())
                    .patientIcn("123")
                    .serviceType("PrimaryCare")
                    .build()))
            .grandfathered(false)
            .noFullServiceVaMedicalFacility(false)
            .eligible(true)
            .eligibilityCodes(
                Collections.singletonList(
                    CommunityCareEligibilityResponse.EligibilityCode.builder()
                        .description("Hardship")
                        .code("H")
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
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    new ArrayList<AddressInfo>(
                                                        Arrays.asList(
                                                            AddressInfo.builder()
                                                                .addressTypeCode("Residential")
                                                                .state("FL")
                                                                .city("Melbourne")
                                                                .line1("66 Main St")
                                                                .line2("")
                                                                .line3("")
                                                                .zipCode("12345")
                                                                .build())))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.nearbyFacilities(any(Address.class), any(int.class), any(String.class)))
        .thenReturn(VaFacilitiesResponse.builder().build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(eeClient)
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .build();
    CommunityCareEligibilityResponse result = controller.search("123", "primarycare");
    assertThat(result.nearbyFacilities().isEmpty());
  }

  @SneakyThrows
  @Test(expected = Exceptions.UnknownServiceTypeException.class)
  public void unknownServiceType() {
    EligibilityAndEnrollmentClient client = mock(EligibilityAndEnrollmentClient.class);
    when(client.requestEligibility("123"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    new ArrayList<AddressInfo>(
                                                        Arrays.asList(
                                                            AddressInfo.builder()
                                                                .addressTypeCode("Residential")
                                                                .state("FL")
                                                                .city("Melbourne")
                                                                .line1("66 Main St")
                                                                .line2("")
                                                                .line3("")
                                                                .zipCode("12345")
                                                                .build())))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(client)
            .maxDriveTimePrimary(1)
            .maxWaitPrimary(1)
            .build();
    controller.search("123", "Dentistry");
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
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    new ArrayList<AddressInfo>(
                                                        Arrays.asList(
                                                            AddressInfo.builder()
                                                                .addressTypeCode("Residential")
                                                                .state("FL")
                                                                .city("Melbourne")
                                                                .line1("66 Main St")
                                                                .line2("")
                                                                .line3("")
                                                                .zipCode("12345")
                                                                .build())))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    Address patientAddress =
        Address.builder().city("Melbourne").state("FL").zip("12345").street("66 Main St").build();
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.nearbyFacilities(patientAddress, 60, "Optometry"))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .lat(200D)
                                    .lng(100D)
                                    .waitTimes(
                                        VaFacilitiesResponse.WaitTimes.builder()
                                            .health(
                                                singletonList(
                                                    VaFacilitiesResponse.WaitTime.builder()
                                                        .waitDays(
                                                            VaFacilitiesResponse.WaitDays.builder()
                                                                .neww(1.0)
                                                                .build())
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
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(eeClient)
            .maxDriveTimePrimary(60)
            .maxWaitPrimary(2)
            .build();
    CommunityCareEligibilityResponse actual = controller.search("123", "optometry");
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .grandfathered(false)
            .noFullServiceVaMedicalFacility(false)
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
            .eligible(false)
            .eligibilityCodes(
                Collections.singletonList(
                    CommunityCareEligibilityResponse.EligibilityCode.builder()
                        .description("Ineligible")
                        .code("X")
                        .build()))
            .build();

    assertThat(actual).isEqualTo(expected);
  }
}

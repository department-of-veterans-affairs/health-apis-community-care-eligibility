package gov.va.api.health.communitycareeligibility.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.PcmmResponse;
import gov.va.med.esr.webservices.jaxws.schemas.AddressCollection;
import gov.va.med.esr.webservices.jaxws.schemas.AddressInfo;
import gov.va.med.esr.webservices.jaxws.schemas.CommunityCareEligibilityInfo;
import gov.va.med.esr.webservices.jaxws.schemas.ContactInfo;
import gov.va.med.esr.webservices.jaxws.schemas.DemographicInfo;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummary;
import gov.va.med.esr.webservices.jaxws.schemas.GeocodingInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityCollection;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public final class CommunityCareEligibilityTest {
  @SneakyThrows
  private static XMLGregorianCalendar parseXmlGregorianCalendar(String timestamp) {
    GregorianCalendar gCal = new GregorianCalendar();
    gCal.setTime(Date.from(Instant.parse(timestamp)));
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
  }

  @Test
  public void audiology() {
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.nearbyFacilities(
            Coordinates.builder()
                .latitude(new BigDecimal("28.112506"))
                .longitude(new BigDecimal("-80.7000423"))
                .build(),
            60,
            "Audiology"))
        .thenReturn(
            VaNearbyFacilitiesResponse.builder()
                .data(
                    asList(
                        VaNearbyFacilitiesResponse.Facility.builder().id("FAC456").build(),
                        VaNearbyFacilitiesResponse.Facility.builder().id("FAC123").build()))
                .build());
    when(facilitiesClient.facilitiesByIds(asList("FAC456", "FAC123")))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    asList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .mobile(true)
                                    .active("A")
                                    .lat(new BigDecimal("200"))
                                    .lng(new BigDecimal("100"))
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("911 fac st")
                                                    .state("FL")
                                                    .build())
                                            .build())
                                    .build())
                            .build(),
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC456")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .mobile(true)
                                    .active("A")
                                    .lat(new BigDecimal("100"))
                                    .lng(new BigDecimal("300"))
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1(" 123 who cares drive ")
                                                    .address2("   ")
                                                    .address3(" PO Box 321 ")
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
                                                    asList(
                                                        AddressInfo.builder()
                                                            .addressTypeCode("Residential")
                                                            .state(" fL")
                                                            .city("Melbourne ")
                                                            .line1(" 66 pat St ")
                                                            .line2("   ")
                                                            .line3("   Apt 777    ")
                                                            .postalCode(" 12345 ")
                                                            .zipPlus4(" 6789 ")
                                                            .build()))
                                                .build())
                                        .build())
                                .build())
                        .communityCareEligibilityInfo(
                            CommunityCareEligibilityInfo.builder()
                                .geocodingInfo(
                                    GeocodingInfo.builder()
                                        .addressLatitude(new BigDecimal("28.112506"))
                                        .addressLongitude(new BigDecimal("-80.7000423"))
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
            .build();
    CommunityCareEligibilityResponse actual = controller.search("", "123", "Audiology", null);
    CommunityCareEligibilityResponse expected =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                CommunityCareEligibilityResponse.PatientRequest.builder()
                    .patientIcn("123")
                    .timestamp(actual.patientRequest().timestamp())
                    .serviceType("Audiology")
                    .build())
            .patientAddress(
                Address.builder()
                    .state("FL")
                    .city("Melbourne")
                    .zip("12345-6789")
                    .street("66 pat St Apt 777")
                    .build())
            .patientCoordinates(
                Coordinates.builder()
                    .latitude(new BigDecimal("28.112506"))
                    .longitude(new BigDecimal("-80.7000423"))
                    .build())
            .eligible(false)
            .eligibilityCodes(emptyList())
            .grandfathered(false)
            .noFullServiceVaMedicalFacility(false)
            .nearbyFacilities(
                asList(
                    Facility.builder()
                        .mobile(true)
                        .active(true)
                        .id("FAC123")
                        .physicalAddress(Address.builder().street("911 fac st").state("FL").build())
                        .coordinates(
                            Coordinates.builder()
                                .latitude(new BigDecimal("200"))
                                .longitude(new BigDecimal("100"))
                                .build())
                        .build(),
                    Facility.builder()
                        .mobile(true)
                        .active(true)
                        .id("FAC456")
                        .physicalAddress(
                            Address.builder()
                                .street("123 who cares drive PO Box 321")
                                .state("FL")
                                .build())
                        .coordinates(
                            Coordinates.builder()
                                .latitude(new BigDecimal("100"))
                                .longitude(new BigDecimal("300"))
                                .build())
                        .build()))
            .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.successful)
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void facilityTransformerNullChecks() {
    FacilityTransformer transformer = FacilityTransformer.builder().build();
    assertThat(transformer.toFacility(null, null)).isNull();
    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder().build(),
                VaNearbyFacilitiesResponse.Facility.builder().build()))
        .isEqualTo(Facility.builder().mobile(false).active(false).build());
    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder()
                    .attributes(VaFacilitiesResponse.Attributes.builder().build())
                    .build(),
                VaNearbyFacilitiesResponse.Facility.builder().build()))
        .isEqualTo(Facility.builder().mobile(false).active(false).build());
    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder()
                    .attributes(
                        VaFacilitiesResponse.Attributes.builder()
                            .address(VaFacilitiesResponse.Address.builder().build())
                            .build())
                    .build(),
                VaNearbyFacilitiesResponse.Facility.builder().build()))
        .isEqualTo(Facility.builder().mobile(false).active(false).build());
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
                    .build(),
                VaNearbyFacilitiesResponse.Facility.builder().build()))
        .isEqualTo(Facility.builder().mobile(false).active(false).build());
  }

  @Test
  public void hardship() {
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
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .maxDriveTimePrimary(1)
            .eeClient(eeClient)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("session-id", "123", "cardiology", null);
    assertThat(actual)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .timestamp(actual.patientRequest().timestamp())
                        .patientIcn("123")
                        .serviceType("Cardiology")
                        .build())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .eligible(true)
                .eligibilityCodes(
                    Collections.singletonList(
                        CommunityCareEligibilityResponse.EligibilityCode.builder()
                            .description("Hardship")
                            .code("H")
                            .build()))
                .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.successful)
                .build());
  }

  @Test
  public void incompleteGeocodingInfo() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .communityCareEligibilityInfo(
                            CommunityCareEligibilityInfo.builder()
                                .geocodingInfo(
                                    GeocodingInfo.builder()
                                        .addressLatitude(BigDecimal.ZERO)
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityResponse result =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(eeClient)
            .build()
            .search("", "123", "cardiology", null);
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientIcn("123")
                        .serviceType("Cardiology")
                        .timestamp(result.patientRequest().timestamp())
                        .build())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .processingStatus(
                    CommunityCareEligibilityResponse.ProcessingStatus.geocoding_incomplete)
                .build());
  }

  @Test
  public void invalidExtendedDriveMin() {
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .maxDriveTimeSpecialty(30)
            .build();
    assertThrows(
        Exceptions.InvalidExtendedDriveMin.class,
        () -> controller.search("", "123", "cardiology", 20));
  }

  @Test
  public void missingGeocodingInfo() {
    EligibilityAndEnrollmentClient client = mock(EligibilityAndEnrollmentClient.class);
    when(client.requestEligibility("123")).thenReturn(GetEESummaryResponse.builder().build());
    CommunityCareEligibilityResponse result =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(client)
            .build()
            .search("", "123", "cardiology", null);
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientIcn("123")
                        .serviceType("Cardiology")
                        .timestamp(result.patientRequest().timestamp())
                        .build())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .processingStatus(
                    CommunityCareEligibilityResponse.ProcessingStatus.geocoding_not_available)
                .build());
  }

  @Test
  public void noFacilities() {
    EligibilityAndEnrollmentClient client = mock(EligibilityAndEnrollmentClient.class);
    when(client.requestEligibility("123"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .communityCareEligibilityInfo(
                            CommunityCareEligibilityInfo.builder()
                                .geocodingInfo(
                                    GeocodingInfo.builder()
                                        .addressLatitude(BigDecimal.ZERO)
                                        .addressLongitude(BigDecimal.ONE)
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityResponse result =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(client)
            .build()
            .search("", "123", "cardiology", null);
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientIcn("123")
                        .timestamp(result.patientRequest().timestamp())
                        .serviceType("Cardiology")
                        .build())
                .patientCoordinates(
                    Coordinates.builder()
                        .latitude(BigDecimal.ZERO)
                        .longitude(BigDecimal.ONE)
                        .build())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .eligible(true)
                .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.successful)
                .build());
  }

  @Test
  public void noFacilityWithinDefaultRangeButPopulateWithExtendedRange() {
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.nearbyFacilities(
            Coordinates.builder()
                .latitude(new BigDecimal("28.112506"))
                .longitude(new BigDecimal("-80.7000423"))
                .build(),
            60,
            "Audiology"))
        .thenReturn(VaNearbyFacilitiesResponse.builder().build());
    when(facilitiesClient.nearbyFacilities(
            Coordinates.builder()
                .latitude(new BigDecimal("28.112506"))
                .longitude(new BigDecimal("-80.7000423"))
                .build(),
            90,
            "Audiology"))
        .thenReturn(
            VaNearbyFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaNearbyFacilitiesResponse.Facility.builder().id("FAC123").build()))
                .build());
    when(facilitiesClient.facilitiesByIds(asList("FAC123")))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    singletonList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .mobile(true)
                                    .active("A")
                                    .lat(new BigDecimal("200"))
                                    .lng(new BigDecimal("100"))
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("911 fac st")
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
                        .communityCareEligibilityInfo(
                            CommunityCareEligibilityInfo.builder()
                                .geocodingInfo(
                                    GeocodingInfo.builder()
                                        .addressLatitude(new BigDecimal("28.112506"))
                                        .addressLongitude(new BigDecimal("-80.7000423"))
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
            .build();
    CommunityCareEligibilityResponse actual = controller.search("", "123", "Audiology", 90);
    assertThat(actual)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientIcn("123")
                        .serviceType("Audiology")
                        .extendedDriveMin(90)
                        .timestamp(actual.patientRequest().timestamp())
                        .build())
                .patientCoordinates(
                    Coordinates.builder()
                        .latitude(new BigDecimal("28.112506"))
                        .longitude(new BigDecimal("-80.7000423"))
                        .build())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .nearbyFacilities(
                    singletonList(
                        Facility.builder()
                            .mobile(true)
                            .active(true)
                            .id("FAC123")
                            .physicalAddress(
                                Address.builder().street("911 fac st").state("FL").build())
                            .coordinates(
                                Coordinates.builder()
                                    .latitude(new BigDecimal("200"))
                                    .longitude(new BigDecimal("100"))
                                    .build())
                            .build()))
                .eligible(true)
                .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.successful)
                .build());
  }

  @Test
  public void notYetEligibleDate() {
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
                                .geocodingInfo(
                                    GeocodingInfo.builder()
                                        .addressLatitude(BigDecimal.ZERO)
                                        .addressLongitude(BigDecimal.ONE)
                                        .build())
                                .build())
                        .build())
                .build());
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.nearbyFacilities(
            any(Coordinates.class), any(int.class), any(String.class)))
        .thenReturn(VaNearbyFacilitiesResponse.builder().build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(eeClient)
            .maxDriveTimePrimary(1)
            .build();
    CommunityCareEligibilityResponse result = controller.search("", "123", "cardiology", null);
    assertThat(result.nearbyFacilities().isEmpty());
  }

  @Test
  public void outdatedGeocodingInfo() {
    System.out.println(Instant.now());
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123"))
        .thenReturn(
            GetEESummaryResponse.builder()
                .summary(
                    EeSummary.builder()
                        .communityCareEligibilityInfo(
                            CommunityCareEligibilityInfo.builder()
                                .geocodingInfo(
                                    GeocodingInfo.builder()
                                        .addressLatitude(BigDecimal.ZERO)
                                        .addressLongitude(BigDecimal.ZERO)
                                        .geocodeDate(
                                            parseXmlGregorianCalendar("2019-09-26T17:17:17Z"))
                                        .build())
                                .build())
                        .demographics(
                            DemographicInfo.builder()
                                .contactInfo(
                                    ContactInfo.builder()
                                        .addresses(
                                            AddressCollection.builder()
                                                .address(
                                                    asList(
                                                        AddressInfo.builder()
                                                            .addressTypeCode("Residential")
                                                            .addressChangeDateTime(
                                                                parseXmlGregorianCalendar(
                                                                    "2019-09-26T18:18:18Z"))
                                                            .build()))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityResponse result =
        CommunityCareEligibilityV0ApiController.builder()
            .eeClient(eeClient)
            .build()
            .search("", "123", "cardiology", null);
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientIcn("123")
                        .serviceType("Cardiology")
                        .timestamp(result.patientRequest().timestamp())
                        .build())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .patientCoordinates(
                    Coordinates.builder()
                        .longitude(BigDecimal.ZERO)
                        .latitude(BigDecimal.ZERO)
                        .build())
                .processingStatus(
                    CommunityCareEligibilityResponse.ProcessingStatus.geocoding_out_of_date)
                .build());
  }

  @Test
  public void primaryCare() {
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    PcmmClient pcmmClient = mock(PcmmClient.class);
    when(facilitiesClient.nearbyFacilities(
            Coordinates.builder()
                .latitude(new BigDecimal("28.112506"))
                .longitude(new BigDecimal("-80.7000423"))
                .build(),
            60,
            "PrimaryCare"))
        .thenReturn(
            VaNearbyFacilitiesResponse.builder()
                .data(
                    asList(
                        VaNearbyFacilitiesResponse.Facility.builder().id("FAC456").build(),
                        VaNearbyFacilitiesResponse.Facility.builder().id("FAC123").build()))
                .build());
    when(facilitiesClient.facilitiesByIds(asList("FAC456", "FAC123")))
        .thenReturn(
            VaFacilitiesResponse.builder()
                .data(
                    asList(
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC123")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .mobile(true)
                                    .active("A")
                                    .lat(new BigDecimal("200"))
                                    .lng(new BigDecimal("100"))
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1("911 fac st")
                                                    .state("FL")
                                                    .build())
                                            .build())
                                    .build())
                            .build(),
                        VaFacilitiesResponse.Facility.builder()
                            .id("FAC456")
                            .attributes(
                                VaFacilitiesResponse.Attributes.builder()
                                    .mobile(true)
                                    .active("A")
                                    .lat(new BigDecimal("100"))
                                    .lng(new BigDecimal("300"))
                                    .address(
                                        VaFacilitiesResponse.Address.builder()
                                            .physical(
                                                VaFacilitiesResponse.PhysicalAddress.builder()
                                                    .address1(" 123 who cares drive ")
                                                    .address2("   ")
                                                    .address3(" PO Box 321 ")
                                                    .state("FL")
                                                    .build())
                                            .build())
                                    .build())
                            .build()))
                .build());
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility("123"))
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
                                                    asList(
                                                        AddressInfo.builder()
                                                            .addressTypeCode("Residential")
                                                            .state(" fL")
                                                            .city("Melbourne ")
                                                            .line1(" 66 pat St ")
                                                            .line2("   ")
                                                            .line3("   Apt 777    ")
                                                            .postalCode(" 12345 ")
                                                            .zipPlus4(" 6789 ")
                                                            .build()))
                                                .build())
                                        .build())
                                .build())
                        .communityCareEligibilityInfo(
                            CommunityCareEligibilityInfo.builder()
                                .geocodingInfo(
                                    GeocodingInfo.builder()
                                        .addressLatitude(new BigDecimal("28.112506"))
                                        .addressLongitude(new BigDecimal("-80.7000423"))
                                        .build())
                                .build())
                        .build())
                .build());
    when(pcmmClient.pactStatusByIcn("123"))
        .thenReturn(
            PcmmResponse.builder()
                .patientAssignmentsAtStations(
                    List.of(
                        PcmmResponse.PatientAssignmentsAtStation.builder()
                            .primaryCareAssignments(
                                List.of(
                                    PcmmResponse.PrimaryCareAssignment.builder()
                                        .assignmentStatus(
                                            PcmmResponse.PrimaryCareAssignment.PactStatus.Pending)
                                        .build(),
                                    PcmmResponse.PrimaryCareAssignment.builder()
                                        .assignmentStatus(
                                            PcmmResponse.PrimaryCareAssignment.PactStatus.Active)
                                        .build()))
                            .build(),
                        PcmmResponse.PatientAssignmentsAtStation.builder()
                            .primaryCareAssignments(
                                List.of(
                                    PcmmResponse.PrimaryCareAssignment.builder()
                                        .assignmentStatus(
                                            PcmmResponse.PrimaryCareAssignment.PactStatus.None)
                                        .build(),
                                    PcmmResponse.PrimaryCareAssignment.builder()
                                        .assignmentStatus(
                                            PcmmResponse.PrimaryCareAssignment.PactStatus.Pending)
                                        .build()))
                            .build()))
                .build());

    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(eeClient)
            .pcmmClient(pcmmClient)
            .maxDriveTimePrimary(60)
            .maxDriveTimeSpecialty(60)
            .build();
    CommunityCareEligibilityResponse actual =
        controller.search("session-id", "123", "primarycare", null);
    assertThat(actual)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .timestamp(actual.patientRequest().timestamp())
                        .patientIcn("123")
                        .serviceType("PrimaryCare")
                        .build())
                .patientAddress(
                    Address.builder()
                        .state("FL")
                        .city("Melbourne")
                        .zip("12345-6789")
                        .street("66 pat St Apt 777")
                        .build())
                .patientCoordinates(
                    Coordinates.builder()
                        .latitude(new BigDecimal("28.112506"))
                        .longitude(new BigDecimal("-80.7000423"))
                        .build())
                .nearbyFacilities(
                    asList(
                        Facility.builder()
                            .mobile(true)
                            .active(true)
                            .id("FAC123")
                            .physicalAddress(
                                Address.builder().street("911 fac st").state("FL").build())
                            .coordinates(
                                Coordinates.builder()
                                    .latitude(new BigDecimal("200"))
                                    .longitude(new BigDecimal("100"))
                                    .build())
                            .build(),
                        Facility.builder()
                            .mobile(true)
                            .active(true)
                            .id("FAC456")
                            .physicalAddress(
                                Address.builder()
                                    .street("123 who cares drive PO Box 321")
                                    .state("FL")
                                    .build())
                            .coordinates(
                                Coordinates.builder()
                                    .latitude(new BigDecimal("100"))
                                    .longitude(new BigDecimal("300"))
                                    .build())
                            .build()))
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .eligible(false)
                .eligibilityCodes(emptyList())
                .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.successful)
                .pactStatus(PcmmResponse.PrimaryCareAssignment.PactStatus.Active)
                .build());
  }

  @Test
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
                                                    asList(
                                                        AddressInfo.builder()
                                                            .addressTypeCode("Residential")
                                                            .state("FL")
                                                            .city("Melbourne")
                                                            .line1("66 Main St")
                                                            .line2("")
                                                            .line3("")
                                                            .zipcode("12345")
                                                            .build()))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(client)
            .maxDriveTimePrimary(30)
            .maxDriveTimeSpecialty(30)
            .build();
    assertThrows(
        Exceptions.UnknownServiceTypeException.class,
        () -> controller.search("", "123", "Dentistry", 20));
  }

  @Test
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
                                                    .build()))
                                        .build())
                                .build())
                        .build())
                .build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(eeClient)
            .maxDriveTimePrimary(60)
            .build();
    CommunityCareEligibilityResponse actual = controller.search("", "123", "optometry", null);
    assertThat(actual)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientIcn("123")
                        .serviceType("Optometry")
                        .timestamp(actual.patientRequest().timestamp())
                        .build())
                .eligible(false)
                .eligibilityCodes(
                    Collections.singletonList(
                        CommunityCareEligibilityResponse.EligibilityCode.builder()
                            .description("Ineligible")
                            .code("X")
                            .build()))
                .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.successful)
                .build());
  }
}

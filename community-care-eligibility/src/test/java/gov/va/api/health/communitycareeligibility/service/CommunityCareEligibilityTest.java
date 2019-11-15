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
import gov.va.api.health.queenelizabeth.ee.QueenElizabethService;
import gov.va.api.health.queenelizabeth.ee.exceptions.PersonNotFound;
import gov.va.api.health.queenelizabeth.ee.exceptions.RequestFailed;
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
                                                    .address1("123 who cares drive")
                                                    .state("FL")
                                                    .build())
                                            .build())
                                    .build())
                            .build()))
                .build());
    QueenElizabethService client = mock(QueenElizabethService.class);
    when(client.getEeSummary("123"))
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
                                                            .line3("   ")
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
                    .street("66 pat St")
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
                            Address.builder().street("123 who cares drive").state("FL").build())
                        .coordinates(
                            Coordinates.builder()
                                .latitude(new BigDecimal("100"))
                                .longitude(new BigDecimal("300"))
                                .build())
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  /** Test condition when QueenElizabethService has unknown fault condition. */
  @Test(expected = Exceptions.EeUnavailableException.class)
  public void eeUnknownFault() {
    QueenElizabethService client = mock(QueenElizabethService.class);
    when(client.getEeSummary("123")).thenThrow(new RequestFailed(null));
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(client)
            .maxDriveTimePrimary(1)
            .build();
    controller.search("", "123", "optometry", null);
  }

  /** Test condition when QueenElizabethService does not find person. */
  @Test(expected = Exceptions.UnknownPatientIcnException.class)
  public void eeUnknownPatient() {
    QueenElizabethService client = mock(QueenElizabethService.class);
    when(client.getEeSummary("123")).thenThrow(new PersonNotFound(null));
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(client)
            .maxDriveTimePrimary(1)
            .build();
    controller.search("", "123", "optometry", null);
  }

  @Test
  public void facilityTransformerNullChecks() {
    FacilityTransformer transformer = FacilityTransformer.builder().serviceType("xyz").build();
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
    QueenElizabethService eeClient = mock(QueenElizabethService.class);
    when(eeClient.getEeSummary("123"))
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
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .eligible(true)
                .eligibilityCodes(
                    Collections.singletonList(
                        CommunityCareEligibilityResponse.EligibilityCode.builder()
                            .description("Hardship")
                            .code("H")
                            .build()))
                .build());
  }

  @Test(expected = Exceptions.MissingGeocodingInfoException.class)
  public void incompleteGeocodingInfo() {
    QueenElizabethService eeClient = mock(QueenElizabethService.class);
    when(eeClient.getEeSummary("123"))
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
    CommunityCareEligibilityV0ApiController.builder()
        .eeClient(eeClient)
        .build()
        .search("", "123", "PrimaryCare", null);
  }

  @Test(expected = Exceptions.InvalidExtendedDriveMin.class)
  public void invalidExtendedDriveMin() {
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(mock(FacilitiesClient.class))
            .eeClient(mock(QueenElizabethService.class))
            .maxDriveTimePrimary(30)
            .build();
    controller.search("", "123", "primarycare", 20);
  }

  @Test(expected = Exceptions.MissingGeocodingInfoException.class)
  public void missingGeocodingInfo() {
    CommunityCareEligibilityV0ApiController.builder()
        .eeClient(mock(QueenElizabethService.class))
        .build()
        .search("", "123", "PrimaryCare", null);
  }

  @Test
  public void noFacilities() {
    QueenElizabethService client = mock(QueenElizabethService.class);
    when(client.getEeSummary("123"))
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
            .search("", "123", "primarycare", null);
    assertThat(result)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .patientIcn("123")
                        .timestamp(result.patientRequest().timestamp())
                        .serviceType("PrimaryCare")
                        .build())
                .patientCoordinates(
                    Coordinates.builder()
                        .latitude(BigDecimal.ZERO)
                        .longitude(BigDecimal.ONE)
                        .build())
                .grandfathered(false)
                .noFullServiceVaMedicalFacility(false)
                .eligible(true)
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

    QueenElizabethService client = mock(QueenElizabethService.class);
    when(client.getEeSummary("123"))
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
                .build());
  }

  @Test
  public void noService() {
    QueenElizabethService eeClient = mock(QueenElizabethService.class);
    when(eeClient.getEeSummary("123"))
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
    CommunityCareEligibilityResponse actual =
        CommunityCareEligibilityV0ApiController.builder()
            .eeClient(eeClient)
            .build()
            .search(null, "123", "", null);
    assertThat(actual)
        .isEqualTo(
            CommunityCareEligibilityResponse.builder()
                .patientRequest(
                    CommunityCareEligibilityResponse.PatientRequest.builder()
                        .timestamp(actual.patientRequest().timestamp())
                        .patientIcn("123")
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
                .build());
  }

  @Test
  public void notYetEligibleDate() {
    QueenElizabethService eeClient = mock(QueenElizabethService.class);
    when(eeClient.getEeSummary("123"))
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
    CommunityCareEligibilityResponse result = controller.search("", "123", "primarycare", null);
    assertThat(result.nearbyFacilities().isEmpty());
  }

  @Test(expected = Exceptions.OutdatedGeocodingInfoException.class)
  public void outdatedGeocodingInfo() {
    System.out.println(Instant.now());
    QueenElizabethService eeClient = mock(QueenElizabethService.class);
    when(eeClient.getEeSummary("123"))
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
    CommunityCareEligibilityV0ApiController.builder()
        .eeClient(eeClient)
        .build()
        .search("", "123", "PrimaryCare", null);
  }

  @Test(expected = Exceptions.UnknownServiceTypeException.class)
  public void unknownServiceType() {
    QueenElizabethService client = mock(QueenElizabethService.class);
    when(client.getEeSummary("123"))
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
    controller.search("", "123", "Dentistry", 20);
  }

  @Test
  public void xIsIneligible() {
    QueenElizabethService eeClient = mock(QueenElizabethService.class);
    when(eeClient.getEeSummary("123"))
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
                .build());
  }
}

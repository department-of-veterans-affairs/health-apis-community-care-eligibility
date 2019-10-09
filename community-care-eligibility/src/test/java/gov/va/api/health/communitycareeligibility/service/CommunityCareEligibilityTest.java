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
  @SneakyThrows
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
    CommunityCareEligibilityResponse actual = controller.search("", "123", "Audiology", 90);
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
                singletonList(
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
                        .build()))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void facilityTransformerNullChecks() {
    FacilityTransformer transformer = FacilityTransformer.builder().serviceType("xyz").build();

    assertThat(transformer.toFacility(null)).isNull();

    assertThat(transformer.toFacility(VaFacilitiesResponse.Facility.builder().build()))
        .isEqualTo(Facility.builder().mobile(false).active(false).build());

    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder()
                    .attributes(VaFacilitiesResponse.Attributes.builder().build())
                    .build()))
        .isEqualTo(Facility.builder().mobile(false).active(false).build());

    assertThat(
            transformer.toFacility(
                VaFacilitiesResponse.Facility.builder()
                    .attributes(
                        VaFacilitiesResponse.Attributes.builder()
                            .address(VaFacilitiesResponse.Address.builder().build())
                            .build())
                    .build()))
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
                    .build()))
        .isEqualTo(Facility.builder().mobile(false).active(false).build());
  }

  @Test
  @SneakyThrows
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
        controller.search("session-id", "123", "primarycare", 0);
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

    CommunityCareEligibilityV0ApiController.builder()
        .eeClient(eeClient)
        .build()
        .search("", "123", "PrimaryCare", 0);
  }

  @Test(expected = Exceptions.MissingGeocodingInfoException.class)
  public void missingGeocodingInfo() {
    CommunityCareEligibilityV0ApiController.builder()
        .eeClient(mock(EligibilityAndEnrollmentClient.class))
        .build()
        .search("", "123", "PrimaryCare", 0);
  }

  @Test
  @SneakyThrows
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
            .search("", "123", "primarycare", 0);
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
  @SneakyThrows
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
        .thenReturn(VaFacilitiesResponse.builder().build());
    CommunityCareEligibilityV0ApiController controller =
        CommunityCareEligibilityV0ApiController.builder()
            .facilitiesClient(facilitiesClient)
            .eeClient(eeClient)
            .maxDriveTimePrimary(1)
            .build();
    CommunityCareEligibilityResponse result = controller.search("", "123", "primarycare", 0);
    assertThat(result.nearbyFacilities().isEmpty());
  }

  @Test(expected = Exceptions.OutdatedGeocodingInfoException.class)
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

    CommunityCareEligibilityV0ApiController.builder()
        .eeClient(eeClient)
        .build()
        .search("", "123", "PrimaryCare", 0);
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
            .maxDriveTimePrimary(1)
            .build();
    controller.search("", "123", "Dentistry", 0);
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
    CommunityCareEligibilityResponse actual = controller.search("", "123", "optometry", 0);
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

package gov.va.api.health.communitycareeligibility.service;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.StringUtils.upperCase;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.EligibilityCode;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.PatientRequest;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityService;
import gov.va.med.esr.webservices.jaxws.schemas.AddressInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GeocodingInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v0/eligibility", produces = "application/json")
public class CommunityCareEligibilityV0ApiController implements CommunityCareEligibilityService {
  private static final Map<String, String> SERVICES_MAP = initServicesMap();

  private int maxDriveMinsPrimary;

  private int maxDriveMinsSpecialty;

  private EligibilityAndEnrollmentClient eeClient;

  private FacilitiesClient facilitiesClient;

  /** Autowired constructor. */
  @Builder
  public CommunityCareEligibilityV0ApiController(
      @Value("${community-care.max-drive-time-min-primary}") int maxDriveTimePrimary,
      @Value("${community-care.max-drive-time-min-specialty}") int maxDriveTimeSpecialty,
      @Autowired EligibilityAndEnrollmentClient eeClient,
      @Autowired FacilitiesClient facilitiesClient) {
    this.maxDriveMinsPrimary = maxDriveTimePrimary;
    this.maxDriveMinsSpecialty = maxDriveTimeSpecialty;
    this.eeClient = eeClient;
    this.facilitiesClient = facilitiesClient;
  }

  private static List<VceEligibilityInfo> eligibilityInfos(GetEESummaryResponse response) {
    return response == null
            || response.getSummary() == null
            || response.getSummary().getCommunityCareEligibilityInfo() == null
            || response.getSummary().getCommunityCareEligibilityInfo().getEligibilities() == null
        ? Collections.emptyList()
        : response
            .getSummary()
            .getCommunityCareEligibilityInfo()
            .getEligibilities()
            .getEligibility();
  }

  private static Optional<GeocodingInfo> geocodingInfo(GetEESummaryResponse eeResponse) {
    if (eeResponse == null
        || eeResponse.getSummary() == null
        || eeResponse.getSummary().getCommunityCareEligibilityInfo() == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(
        eeResponse.getSummary().getCommunityCareEligibilityInfo().getGeocodingInfo());
  }

  private static Map<String, String> initServicesMap() {
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (String service :
        asList(
            "Audiology",
            "Cardiology",
            "Dermatology",
            "Gastroenterology",
            "Gynecology",
            "MentalHealthCare",
            "Ophthalmology",
            "Optometry",
            "Orthopedics",
            "PrimaryCare",
            "Urology",
            "WomensHealth")) {
      map.put(service, service);
    }
    return map;
  }

  private static Optional<AddressInfo> residentialAddress(GetEESummaryResponse eeResponse) {
    if (eeResponse == null
        || eeResponse.getSummary() == null
        || eeResponse.getSummary().getDemographics() == null
        || eeResponse.getSummary().getDemographics().getContactInfo() == null
        || eeResponse.getSummary().getDemographics().getContactInfo().getAddresses() == null) {
      return Optional.empty();
    }

    return eeResponse
        .getSummary()
        .getDemographics()
        .getContactInfo()
        .getAddresses()
        .getAddress()
        .stream()
        .filter(a -> "Residential".equalsIgnoreCase(a.getAddressTypeCode()))
        .findFirst();
  }

  private static String stripNewlines(String str) {
    return str.replaceAll("[\r\n]", "");
  }

  private static Address toAddress(Optional<AddressInfo> eeAddress) {
    if (eeAddress.isEmpty()) {
      return null;
    }
    AddressInfo addressInfo = eeAddress.get();

    String zip = trimToNull(addressInfo.getZipCode());
    if (zip == null) {
      zip = trimToNull(addressInfo.getPostalCode());
    }
    if (zip == null) {
      zip = trimToNull(addressInfo.getZipcode());
    }
    String zipPlus4 = trimToNull(addressInfo.getZipPlus4());
    if (zip != null && zipPlus4 != null) {
      zip = zip + "-" + zipPlus4;
    }

    return Address.builder()
        .city(trimToNull(addressInfo.getCity()))
        .state(upperCase(trimToNull(addressInfo.getState()), Locale.US))
        .street(
            trimToNull(
                trimToEmpty(addressInfo.getLine1())
                    + " "
                    + trimToEmpty(addressInfo.getLine2())
                    + " "
                    + trimToEmpty(addressInfo.getLine3())))
        .zip(trimToNull(zip))
        .build();
  }

  private static Coordinates toCoordinates(String patientIcn, GeocodingInfo geocodingInfo) {
    BigDecimal lat = geocodingInfo.getAddressLatitude();
    BigDecimal lng = geocodingInfo.getAddressLongitude();
    if (lat == null || lng == null) {
      throw new Exceptions.MissingGeocodingInfoException(patientIcn);
    }
    return Coordinates.builder().latitude(lat).longitude(lng).build();
  }

  /** Compute community care eligibility. */
  @Override
  @SneakyThrows
  @GetMapping(value = "/search")
  public CommunityCareEligibilityResponse search(
      @RequestHeader(value = "X-VA-SESSIONID", defaultValue = "") String optSessionIdHeader,
      @NotBlank @RequestParam(value = "patient") String patientIcn,
      @NotBlank @RequestParam(value = "serviceType") String serviceType) {
    if (isNotBlank(optSessionIdHeader)) {
      // Strip newlines for Spotbugs
      log.info(
          "sessionId={}, patient={}, serviceType={}",
          stripNewlines(optSessionIdHeader),
          stripNewlines(patientIcn),
          stripNewlines(serviceType));
    }

    String mappedServiceType = SERVICES_MAP.get(serviceType.trim());
    if (mappedServiceType == null) {
      throw new Exceptions.UnknownServiceTypeException(serviceType);
    }

    return search(
        PatientRequest.builder()
            .patientIcn(patientIcn.trim())
            .serviceType(mappedServiceType)
            .timestamp(Instant.now().toString())
            .build());
  }

  private CommunityCareEligibilityResponse search(PatientRequest request) {
    GetEESummaryResponse eeResponse = eeClient.requestEligibility(request.patientIcn());
    Instant timestamp = Instant.parse(request.timestamp());
    List<EligibilityCode> eligibilityCodes =
        eligibilityInfos(eeResponse)
            .stream()
            .filter(Objects::nonNull)
            .map(
                vceEligibilityInfo ->
                    EligibilityAndEnrollmentTransformer.builder()
                        .eligibilityInfo(vceEligibilityInfo)
                        .timestamp(timestamp)
                        .build()
                        .toEligibility())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    CommunityCareEligibilityResponse.CommunityCareEligibilityResponseBuilder response =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(request)
            .eligibilityCodes(eligibilityCodes)
            .grandfathered(false)
            .noFullServiceVaMedicalFacility(false);

    List<String> codeStrings =
        eligibilityCodes.stream().map(c -> c.code()).collect(Collectors.toList());
    if (CollectionUtils.containsAny(codeStrings, asList("G", "N", "H", "X"))) {
      return response
          .eligible(!codeStrings.contains("X"))
          .grandfathered(codeStrings.contains("G"))
          .noFullServiceVaMedicalFacility(codeStrings.contains("N"))
          .build();
    }

    Optional<AddressInfo> eeAddress = residentialAddress(eeResponse);
    response.patientAddress(toAddress(eeAddress));

    Optional<GeocodingInfo> geocoding = geocodingInfo(eeResponse);
    if (geocoding.isEmpty()) {
      throw new Exceptions.MissingGeocodingInfoException(request.patientIcn());
    }
    Coordinates patientCoordinates = toCoordinates(request.patientIcn(), geocoding.get());
    response.patientCoordinates(patientCoordinates);

    XMLGregorianCalendar eeAddressChangeXgc =
        eeAddress.isPresent() ? eeAddress.get().getAddressChangeDateTime() : null;
    XMLGregorianCalendar geocodeXgc = geocoding.get().getGeocodeDate();
    if (eeAddressChangeXgc != null
        && geocodeXgc != null
        && geocodeXgc
            .toGregorianCalendar()
            .toInstant()
            .isBefore(eeAddressChangeXgc.toGregorianCalendar().toInstant())) {
      throw new Exceptions.OutdatedGeocodingInfoException(
          request.patientIcn(),
          geocodeXgc.toGregorianCalendar().toInstant(),
          eeAddressChangeXgc.toGregorianCalendar().toInstant());
    }

    String serviceType = request.serviceType();
    final int driveMins =
        equalsIgnoreCase(serviceType, "primarycare") ? maxDriveMinsPrimary : maxDriveMinsSpecialty;
    VaFacilitiesResponse nearbyResponse =
        facilitiesClient.nearbyFacilities(patientCoordinates, driveMins, serviceType);
    List<Facility> nearbyFacilities =
        nearbyResponse == null
            ? Collections.emptyList()
            : nearbyResponse
                .data()
                .stream()
                .map(
                    vaFacility ->
                        FacilityTransformer.builder()
                            .serviceType(serviceType)
                            .build()
                            .toFacility(vaFacility))
                .collect(Collectors.toList());
    response.nearbyFacilities(nearbyFacilities);

    response.eligible(nearbyFacilities.isEmpty());

    return response.build();
  }
}

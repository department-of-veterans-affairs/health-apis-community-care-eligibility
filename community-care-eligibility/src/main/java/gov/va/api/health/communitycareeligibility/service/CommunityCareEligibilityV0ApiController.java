package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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
import gov.va.med.esr.webservices.jaxws.schemas.AddressInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GeocodingInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class CommunityCareEligibilityV0ApiController {
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
        ? emptyList()
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
            "Nutrition",
            "Ophthalmology",
            "Optometry",
            "Orthopedics",
            "Podiatry",
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

    Address patientAddress =
        Address.builder()
            .country(trimToNull(addressInfo.getCountry()))
            .city(trimToNull(addressInfo.getCity()))
            .state(upperCase(trimToNull(addressInfo.getState()), Locale.US))
            .street(
                trimToNull(
                    Stream.of(
                            addressInfo.getLine1(), addressInfo.getLine2(), addressInfo.getLine3())
                        .map(StringUtils::trimToNull)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))))
            .zip(trimToNull(zip))
            .build();

    /* Dont return a value when all null. */
    if (allBlank(
        patientAddress.country(),
        patientAddress.city(),
        patientAddress.state(),
        patientAddress.street(),
        patientAddress.zip())) {
      return null;
    }

    return patientAddress;
  }

  private static Optional<Coordinates> toCoordinates(GeocodingInfo geocodingInfo) {
    BigDecimal lat = geocodingInfo.getAddressLatitude();
    BigDecimal lng = geocodingInfo.getAddressLongitude();
    if (lat == null || lng == null) {
      return Optional.empty();
    }
    return Optional.of(Coordinates.builder().latitude(lat).longitude(lng).build());
  }

  private int driveMins(String serviceType) {
    return equalsIgnoreCase(serviceType, "primarycare")
        ? maxDriveMinsPrimary
        : maxDriveMinsSpecialty;
  }

  private CommunityCareEligibilityResponse requestNearbyFacilityResults(
      PatientRequest request,
      CommunityCareEligibilityResponse.CommunityCareEligibilityResponseBuilder response,
      GetEESummaryResponse eeResponse,
      List<String> codeStrings) {

    if (CollectionUtils.containsAny(codeStrings, asList("G", "N", "H"))) {
      return response
          .grandfathered(codeStrings.contains("G"))
          .noFullServiceVaMedicalFacility(codeStrings.contains("N"))
          .build();
    }

    Optional<AddressInfo> eeAddress = residentialAddress(eeResponse);
    response.patientAddress(toAddress(eeAddress));

    Optional<GeocodingInfo> geocoding = geocodingInfo(eeResponse);
    if (geocoding.isEmpty()) {
      log.info("No geocoding information found for ICN: {}", request.patientIcn());

      return response
          .eligible(false)
          .processingStatus(
              CommunityCareEligibilityResponse.ProcessingStatus.geocoding_not_available)
          .build();
    }

    Optional<Coordinates> patientCoordinates = toCoordinates(geocoding.get());
    if (patientCoordinates.isEmpty()) {
      log.info(
          "Unable to determine coordinates from geocoding info found for ICN: {}",
          request.patientIcn());
      return response
          .eligible(false)
          .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.geocoding_incomplete)
          .build();
    }

    response.patientCoordinates(patientCoordinates.get());

    XMLGregorianCalendar eeAddressChangeXgc =
        eeAddress.isPresent() ? eeAddress.get().getAddressChangeDateTime() : null;
    XMLGregorianCalendar geocodeXgc = geocoding.get().getGeocodeDate();
    if (eeAddressChangeXgc != null
        && geocodeXgc != null
        && geocodeXgc
            .toGregorianCalendar()
            .toInstant()
            .isBefore(eeAddressChangeXgc.toGregorianCalendar().toInstant())) {
      log.info(
          "For patient ICN {}, geocoding information (updated {})"
              + " is out of date against residential address (updated {})",
          request.patientIcn(),
          geocodeXgc.toGregorianCalendar().toInstant(),
          eeAddressChangeXgc.toGregorianCalendar().toInstant());
      return response
          .eligible(false)
          .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.geocoding_out_of_date)
          .build();
    }

    List<Facility> nearbyFacilities =
        transformFacilitiesCalls(
            patientCoordinates.get(), driveMins(request.serviceType()), request.serviceType());
    response.nearbyFacilities(nearbyFacilities);

    if (!nearbyFacilities.isEmpty()) {
      response.eligible(false);
    }

    if (request.extendedDriveMin() != null) {
      List<Facility> extendedFacilities =
          transformFacilitiesCalls(
              patientCoordinates.get(), request.extendedDriveMin(), request.serviceType());
      response.nearbyFacilities(extendedFacilities);
    }

    return response.build();
  }

  private String requestPcmmResults() {
    return "PCMM Results Stub";
  }

  /** Compute community care eligibility. */
  @SneakyThrows
  @GetMapping(value = "/search")
  public CommunityCareEligibilityResponse search(
      @RequestHeader(value = "X-VA-SESSIONID", defaultValue = "") String optSessionIdHeader,
      @NotBlank @RequestParam(value = "patient") String patientIcn,
      @NotBlank @RequestParam(value = "serviceType") String serviceType,
      @Max(value = 90) @RequestParam(value = "extendedDriveMin", required = false)
          Integer extendedDriveMin) {
    if (isNotBlank(optSessionIdHeader)) {
      // Strip newlines for Spotbugs
      log.info(
          "sessionId={}, patient={}, serviceType={}",
          sanitize(optSessionIdHeader),
          sanitize(patientIcn),
          sanitize(serviceType));
    }

    String mappedServiceType = SERVICES_MAP.get(trimToEmpty(serviceType));
    if (mappedServiceType == null) {
      throw new Exceptions.UnknownServiceTypeException(serviceType);
    }

    if (extendedDriveMin != null && extendedDriveMin <= driveMins(mappedServiceType)) {
      throw new Exceptions.InvalidExtendedDriveMin(
          mappedServiceType, extendedDriveMin, driveMins(mappedServiceType));
    }

    return search(
        PatientRequest.builder()
            .patientIcn(patientIcn.trim())
            .serviceType(mappedServiceType)
            .extendedDriveMin(extendedDriveMin)
            .timestamp(Instant.now().toString())
            .build());
  }

  @SneakyThrows
  private CommunityCareEligibilityResponse search(PatientRequest request) {
    GetEESummaryResponse eeResponse = eeClient.requestEligibility(request.patientIcn());

    Instant timestamp = Instant.parse(request.timestamp());
    List<EligibilityCode> eligibilityCodes =
        eligibilityInfos(eeResponse).stream()
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
            .noFullServiceVaMedicalFacility(false)
            .processingStatus(CommunityCareEligibilityResponse.ProcessingStatus.successful);

    if (request.serviceType().equals(SERVICES_MAP.get("PrimaryCare"))) {
      return response.eligible(false).build();
    }

    List<String> codeStrings =
        eligibilityCodes.stream().map(EligibilityCode::code).collect(Collectors.toList());
    if (codeStrings.contains("X")) {
      return response
          .eligible(false)
          .grandfathered(codeStrings.contains("G"))
          .noFullServiceVaMedicalFacility(codeStrings.contains("N"))
          .build();
    }

    // Set default eligibility
    response.eligible(true);

    CompletableFuture<String> pcmmRequestFuture =
        CompletableFuture.supplyAsync(this::requestPcmmResults);

    CompletableFuture<CommunityCareEligibilityResponse> nearbyRequestFuture =
        CompletableFuture.supplyAsync(
            () -> requestNearbyFacilityResults(request, response, eeResponse, codeStrings));

    CompletableFuture<String> combinedPcmmAndNearbyResultsFuture =
        pcmmRequestFuture.thenCombine(
            nearbyRequestFuture,
            (pcmmRequestResult, nearbyRequestResult) ->
                "Stub: " + pcmmRequestResult + ":" + nearbyRequestResult);

    // Stub before actual results processing
    System.out.println(
        "Results of PCMM and Nearby Facilities calls: " + combinedPcmmAndNearbyResultsFuture.get());

    return response.build();
  }

  private List<Facility> transformFacilitiesCalls(
      Coordinates coordinates, int driveMins, String serviceType) {
    VaNearbyFacilitiesResponse nearbyResponse =
        facilitiesClient.nearbyFacilities(coordinates, driveMins, serviceType);
    if (nearbyResponse == null) {
      return emptyList();
    }

    VaFacilitiesResponse vaFacilitiesResponse =
        facilitiesClient.facilitiesByIds(
            nearbyResponse.data().stream()
                .filter(Objects::nonNull)
                .map(fac -> fac.id())
                .collect(Collectors.toList()));
    if (vaFacilitiesResponse == null) {
      return emptyList();
    }

    Map<String, VaNearbyFacilitiesResponse.Facility> nearbyFacilityMap =
        nearbyResponse.data().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(fac -> fac.id(), Function.identity()));

    return vaFacilitiesResponse.data().stream()
        .filter(Objects::nonNull)
        .map(
            vaFacility ->
                FacilityTransformer.builder()
                    .build()
                    .toFacility(vaFacility, nearbyFacilityMap.get(vaFacility.id())))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}

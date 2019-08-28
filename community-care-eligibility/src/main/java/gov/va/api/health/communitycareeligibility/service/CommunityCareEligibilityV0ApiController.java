package gov.va.api.health.communitycareeligibility.service;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityService;
import gov.va.med.esr.webservices.jaxws.schemas.AddressInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GeocodingInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/v0/eligibility", produces = "application/json")
public class CommunityCareEligibilityV0ApiController implements CommunityCareEligibilityService {
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
            || response
                    .getSummary()
                    .getCommunityCareEligibilityInfo()
                    .getEligibilities()
                    .getEligibility()
                == null
        ? Collections.emptyList()
        : response
            .getSummary()
            .getCommunityCareEligibilityInfo()
            .getEligibilities()
            .getEligibility();
  }

  private static Address patientAddress(String patientIcn, GetEESummaryResponse eeResponse) {
    if (eeResponse == null
        || eeResponse.getSummary() == null
        || eeResponse.getSummary().getDemographics() == null
        || eeResponse.getSummary().getDemographics().getContactInfo() == null
        || eeResponse.getSummary().getDemographics().getContactInfo().getAddresses() == null) {
      throw new Exceptions.MissingResidentialAddressException(patientIcn);
    }
    Optional<AddressInfo> maybeEeAddress =
        eeResponse
            .getSummary()
            .getDemographics()
            .getContactInfo()
            .getAddresses()
            .getAddress()
            .stream()
            .filter(a -> "Residential".equals(a.getAddressTypeCode()))
            .findFirst();
    if (!maybeEeAddress.isPresent()) {
      throw new Exceptions.MissingResidentialAddressException(patientIcn);
    }

    AddressInfo eeAddress = maybeEeAddress.get();

    String zip = trimToEmpty(eeAddress.getZipCode());
    if (zip.isEmpty()) {
      if (trimToEmpty(eeAddress.getPostalCode()).isEmpty()) {
        zip = trimToEmpty(eeAddress.getZipcode());
      } else {
        zip = trimToEmpty(eeAddress.getPostalCode());
      }
    }
    String zipPlus4 = trimToEmpty(eeAddress.getZipPlus4());
    if (!zip.isEmpty() && !zipPlus4.isEmpty()) {
      zip = zip + "-" + zipPlus4;
    }

    Address address =
        Address.builder()
            .city(trimToEmpty(eeAddress.getCity()))
            .state(trimToEmpty(eeAddress.getState()).toUpperCase())
            .street(
                trimToEmpty(
                    trimToEmpty(eeAddress.getLine1())
                        + " "
                        + trimToEmpty(eeAddress.getLine2())
                        + " "
                        + trimToEmpty(eeAddress.getLine3())))
            .zip(zip)
            .build();

    if (address.city().isEmpty()
        || address.state().isEmpty()
        || address.zip().isEmpty()
        || address.street().isEmpty()) {
      throw new Exceptions.IncompleteAddressException(patientIcn, address);
    }

    return address;
  }

  private static Coordinates patientCoordinates(GetEESummaryResponse eeResponse) {
    if (eeResponse == null
        || eeResponse.getSummary() == null
        || eeResponse.getSummary().getCommunityCareEligibilityInfo() == null) {
      return null;
    }

    GeocodingInfo geocoding =
        eeResponse.getSummary().getCommunityCareEligibilityInfo().getGeocodingInfo();
    if (geocoding == null) {
      return null;
    }

    BigDecimal lat = geocoding.getAddressLatitude();
    BigDecimal lng = geocoding.getAddressLongitude();
    if (lat == null && lng == null) {
      return null;
    }

    return Coordinates.builder().latitude(lat).longitude(lng).build();
  }

  private static Map<String, String> servicesMap() {
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (String service :
        Arrays.asList(
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

  /** Compute community care eligibility. */
  @Override
  @SneakyThrows
  @GetMapping(value = "/search")
  public CommunityCareEligibilityResponse search(
      @NotBlank @RequestParam(value = "patient") String patientIcn,
      @NotBlank @RequestParam(value = "serviceType") String serviceType) {
    String mappedServiceType = servicesMap().get(serviceType);
    if (mappedServiceType == null) {
      throw new Exceptions.UnknownServiceTypeException(serviceType);
    }

    Instant timestamp = Instant.now();
    GetEESummaryResponse eeResponse = eeClient.requestEligibility(patientIcn.trim());
    List<VceEligibilityInfo> vceEligibilityCollection = eligibilityInfos(eeResponse);
    List<CommunityCareEligibilityResponse.EligibilityCode> eligibilityCodes =
        vceEligibilityCollection
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

    List<String> codeString = new ArrayList<>();
    for (int i = 0; i < eligibilityCodes.size(); i++) {
      codeString.add(eligibilityCodes.get(i).code());
    }

    CommunityCareEligibilityResponse communityCareEligibilityResponse =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                CommunityCareEligibilityResponse.PatientRequest.builder()
                    .serviceType(mappedServiceType)
                    .patientIcn(patientIcn)
                    .timestamp(timestamp.toString())
                    .build())
            .eligibilityCodes(eligibilityCodes)
            .grandfathered(false)
            .noFullServiceVaMedicalFacility(false)
            .build();

    if (CollectionUtils.containsAny(codeString, Arrays.asList("G", "N", "H", "X"))) {
      return communityCareEligibilityResponse
          .eligible(!codeString.contains("X"))
          .grandfathered(codeString.contains("G"))
          .noFullServiceVaMedicalFacility(codeString.contains("N"));
    }

    Address patientAddress = patientAddress(patientIcn, eeResponse);
    communityCareEligibilityResponse
        .patientAddress(patientAddress)
        .patientCoordinates(patientCoordinates(eeResponse));

    boolean isPrimary = equalsIgnoreCase(mappedServiceType, "primarycare");
    final int driveMins = isPrimary ? maxDriveMinsPrimary : maxDriveMinsSpecialty;
    VaFacilitiesResponse nearbyResponse =
        facilitiesClient.nearbyFacilities(patientAddress, driveMins, mappedServiceType);
    List<Facility> nearbyFacilities =
        nearbyResponse == null
            ? Collections.emptyList()
            : nearbyResponse
                .data()
                .stream()
                .map(
                    vaFacility ->
                        FacilityTransformer.builder()
                            .serviceType(mappedServiceType)
                            .build()
                            .toFacility(vaFacility))
                .collect(Collectors.toList());
    communityCareEligibilityResponse.nearbyFacilities(nearbyFacilities);
    if (nearbyFacilities.isEmpty()) {
      return communityCareEligibilityResponse.eligible(true);
    }

    return communityCareEligibilityResponse.eligible(false);
  }
}

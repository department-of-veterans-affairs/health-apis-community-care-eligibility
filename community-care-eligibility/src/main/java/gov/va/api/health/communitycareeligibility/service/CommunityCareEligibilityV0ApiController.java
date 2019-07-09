package gov.va.api.health.communitycareeligibility.service;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityService;
import gov.va.med.esr.webservices.jaxws.schemas.AddressInfo;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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

  @SneakyThrows
  private static Address parsePatientAddress(AddressInfo addressInfo) {
    String zip = trimToEmpty(addressInfo.getZipCode());
    if (zip.isEmpty()) {
      if (trimToEmpty(addressInfo.getPostalCode()).isEmpty()) {
        zip = trimToEmpty(addressInfo.getZipcode());
      } else {
        zip = trimToEmpty(addressInfo.getPostalCode());
      }
    }
    String zipPlus4 = trimToEmpty(addressInfo.getZipPlus4());
    if (!zip.isEmpty() && !zipPlus4.isEmpty()) {
      zip = zip + "-" + zipPlus4;
    }
    Address patientAddress =
        Address.builder()
            .city(trimToEmpty(addressInfo.getCity()))
            .state(trimToEmpty(addressInfo.getState()).toUpperCase())
            .street(
                trimToEmpty(
                    trimToEmpty(addressInfo.getLine1())
                        + " "
                        + trimToEmpty(addressInfo.getLine2())
                        + " "
                        + trimToEmpty(addressInfo.getLine3())))
            .zip(zip)
            .build();
    if (patientAddress.city().isEmpty()
        || patientAddress.state().isEmpty()
        || patientAddress.zip().isEmpty()
        || patientAddress.street().isEmpty()) {
      throw new Exceptions.IncompleteAddressException(patientAddress);
    }
    return patientAddress;
  }

  @SneakyThrows
  private static Optional<AddressInfo> patientResidentialAddressInfo(
      GetEESummaryResponse response) {
    if (response == null
        || response.getSummary() == null
        || response.getSummary().getDemographics() == null
        || response.getSummary().getDemographics().getContactInfo() == null
        || response.getSummary().getDemographics().getContactInfo().getAddresses() == null) {
      return Optional.empty();
    }
    return response
        .getSummary()
        .getDemographics()
        .getContactInfo()
        .getAddresses()
        .getAddress()
        .stream()
        .filter(a -> "Residential".equals(a.getAddressTypeCode()))
        .findFirst();
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

  private List<VceEligibilityInfo> eligibilityInfos(GetEESummaryResponse response) {
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
    Optional<AddressInfo> eeAddress = patientResidentialAddressInfo(eeResponse);
    if (!eeAddress.isPresent()) {
      throw new Exceptions.MissingResidentialAddressException(patientIcn);
    }
    Address patientAddress = parsePatientAddress(eeAddress.get());
    communityCareEligibilityResponse.patientAddress(patientAddress);
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

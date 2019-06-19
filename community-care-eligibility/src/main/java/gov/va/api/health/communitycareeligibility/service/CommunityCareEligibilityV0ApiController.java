package gov.va.api.health.communitycareeligibility.service;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityService;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

  private int maxWaitDaysPrimary;

  private int maxDriveMinsSpecialty;

  private int maxWaitDaysSpecialty;

  private EligibilityAndEnrollmentClient eeClient;

  private FacilitiesClient facilitiesClient;

  /** Autowired constructor. */
  @Builder
  public CommunityCareEligibilityV0ApiController(
      @Value("${community-care.max-drive-time-min-primary}") int maxDriveTimePrimary,
      @Value("${community-care.max-wait-days-primary}") int maxWaitPrimary,
      @Value("${community-care.max-drive-time-min-specialty}") int maxDriveTimeSpecialty,
      @Value("${community-care.max-wait-days-specialty}") int maxWaitSpecialty,
      @Autowired EligibilityAndEnrollmentClient eeClient,
      @Autowired FacilitiesClient facilitiesClient) {
    this.maxDriveMinsPrimary = maxDriveTimePrimary;
    this.maxWaitDaysPrimary = maxWaitPrimary;
    this.maxDriveMinsSpecialty = maxDriveTimeSpecialty;
    this.maxWaitDaysSpecialty = maxWaitSpecialty;
    this.eeClient = eeClient;
    this.facilitiesClient = facilitiesClient;
  }

  private static Map<String, String> servicesMap() {
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (String service :
        Arrays.asList("Audiology", "Nutrition", "Optometry", "Podiatry", "PrimaryCare")) {
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

  @SneakyThrows
  private List<Facility> facilitiesMeetingWaitTimeStandards(
      List<Facility> facilities, boolean isPrimary) {
    int waitDays = isPrimary ? maxWaitDaysPrimary : maxWaitDaysSpecialty;
    return facilities
        .stream()
        .filter(facility -> facility.waitDays() != null && facility.waitDays() <= waitDays)
        .collect(Collectors.toList());
  }

  /** Compute community care eligibility. */
  @Override
  @SneakyThrows
  @GetMapping(value = "/search")
  public CommunityCareEligibilityResponse search(
      @NotBlank @RequestParam(value = "patient") String patientIcn,
      @NotBlank @RequestParam(value = "street") String street,
      @NotBlank @RequestParam(value = "city") String city,
      @NotBlank @RequestParam(value = "state") String state,
      @NotBlank @RequestParam(value = "zip") String zip,
      @NotBlank @RequestParam(value = "serviceType") String serviceType) {
    String mappedServiceType = servicesMap().get(serviceType);
    if (mappedServiceType == null) {
      throw new Exceptions.UnknownServiceTypeException(serviceType);
    }

    Instant timestamp = Instant.now();
    List<VceEligibilityInfo> vceEligibilityCollection =
        eligibilityInfos(eeClient.requestEligibility(patientIcn.trim()));
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

    Address patientAddress =
        Address.builder()
            .street(street.trim())
            .city(city.trim())
            .state(state.toUpperCase(Locale.US).trim())
            .zip(zip.trim())
            .build();
    CommunityCareEligibilityResponse communityCareEligibilityResponse =
        CommunityCareEligibilityResponse.builder()
            .patientRequest(
                CommunityCareEligibilityResponse.PatientRequest.builder()
                    .serviceType(mappedServiceType)
                    .patientIcn(patientIcn)
                    .patientAddress(patientAddress)
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

    boolean isPrimary = equalsIgnoreCase(mappedServiceType, "primarycare");
    final int driveMins = isPrimary ? maxDriveMinsPrimary : maxDriveMinsSpecialty;
    VaFacilitiesResponse facilityIdsWithinDriveTimeResponse =
        facilitiesClient.nearbyFacilities(patientAddress, driveMins, mappedServiceType);

    List<String> facilityIdsWithinDriveTime =
        facilityIdsWithinDriveTimeResponse == null
            ? Collections.emptyList()
            : facilityIdsWithinDriveTimeResponse
                .data()
                .stream()
                .map(facility -> StringUtils.trimToNull(facility.id()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    if (facilityIdsWithinDriveTime.isEmpty()) {
      return communityCareEligibilityResponse.eligible(true);
    }
    List<Facility> nearbyFacilities =
        facilityIdsWithinDriveTimeResponse == null
            ? Collections.emptyList()
            : facilityIdsWithinDriveTimeResponse
                .data()
                .stream()
                .map(
                    vaFacility ->
                        FacilityTransformer.builder()
                            .serviceType(mappedServiceType)
                            .build()
                            .toFacility(vaFacility))
                .collect(Collectors.toList());

    List<Facility> facilitiesMeetingAccessStandards =
        facilitiesMeetingWaitTimeStandards(nearbyFacilities, isPrimary);

    return communityCareEligibilityResponse
        .eligible(facilitiesMeetingAccessStandards.isEmpty())
        .nearbyFacilities(nearbyFacilities)
        .accessStandardsFacilities(
            facilitiesMeetingAccessStandards
                .stream()
                .map(accessStandardFacility -> accessStandardFacility.id())
                .collect(Collectors.toList()));
  }
}

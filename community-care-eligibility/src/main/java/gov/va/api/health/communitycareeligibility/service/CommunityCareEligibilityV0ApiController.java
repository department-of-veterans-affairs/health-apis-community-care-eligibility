package gov.va.api.health.communitycareeligibility.service;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resource;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resources;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping(
  value = {"/api"},
  produces = "application/json"
)
public class CommunityCareEligibilityV0ApiController {

  private int maxDriveMinsPrimary;

  private int maxWaitDaysPrimary;

  private int maxDriveMinsSpecialty;

  private int maxWaitDaysSpecialty;

  private BingMapsClient bingMaps;

  private EligibilityAndEnrollmentClient eeClient;

  private FacilitiesClient facilitiesClient;

  /** Autowired constructor. */
  @Builder
  public CommunityCareEligibilityV0ApiController(
      @Value("${community-care.max-drive-time-min-primary}") int maxDriveTimePrimary,
      @Value("${community-care.max-wait-days-primary}") int maxWaitPrimary,
      @Value("${community-care.max-drive-time-min-specialty}") int maxDriveTimeSpecialty,
      @Value("${community-care.max-wait-days-specialty}") int maxWaitSpecialty,
      @Autowired BingMapsClient bingMaps,
      @Autowired EligibilityAndEnrollmentClient eeClient,
      @Autowired FacilitiesClient facilitiesClient) {
    this.maxDriveMinsPrimary = maxDriveTimePrimary;
    this.maxWaitDaysPrimary = maxWaitPrimary;
    this.maxDriveMinsSpecialty = maxDriveTimeSpecialty;
    this.maxWaitDaysSpecialty = maxWaitSpecialty;
    this.bingMaps = bingMaps;
    this.eeClient = eeClient;
    this.facilitiesClient = facilitiesClient;
  }

  private static boolean hasServiceType(
      VaFacilitiesResponse.Facility vaFacility, String serviceType) {
    return vaFacility != null
        && vaFacility.attributes() != null
        && vaFacility.attributes().waitTimes() != null
        && vaFacility
            .attributes()
            .waitTimes()
            .health()
            .stream()
            .anyMatch(
                waitTime ->
                    waitTime != null
                        && waitTime.service() != null
                        && equalsIgnoreCase(serviceType, waitTime.service()));
  }

  private static Map<String, String> servicesMap() {
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (String service :
        Arrays.asList(
            "PrimaryCare",
            "MentalHealthCare",
            "UrgentCare",
            "EmergencyCare",
            "Audiology",
            "Cardiology",
            "Dermatology",
            "Gastroenterology",
            "Gynecology",
            "Ophthalmology",
            "Optometry",
            "Orthopedics",
            "Urology",
            "WomensHealth")) {
      map.put(service, service);
    }
    return map;
  }

  static Integer waitDays(Facility facility, boolean establishedPatient) {
    if (facility == null) {
      return null;
    }
    if (facility.waitDays() == null) {
      return null;
    }
    return establishedPatient
        ? facility.waitDays().establishedPatient()
        : facility.waitDays().newPatient();
  }

  private boolean eligbleByEligbilityAndEnrollmentResponse(
      List<String> eligibilityCodes, String serviceType) {
    if (eligibilityCodes.contains("X")) {
      return false;
    } else if ((eligibilityCodes.contains("G")
            || eligibilityCodes.contains("N")
            || eligibilityCodes.contains("H")
            || eligibilityCodes.contains("M")
            || eligibilityCodes.contains("WT")
            || eligibilityCodes.contains("MWT")
            || eligibilityCodes.contains("HWT"))
        && !serviceType.equalsIgnoreCase("urgentcare")) {
      return true;
    } else if (eligibilityCodes.contains("U") && serviceType.equalsIgnoreCase("urgentcare")) {
      return true;
    }
    return false;
  }

  @SneakyThrows
  private List<Facility> facilitiesMeetingAccessStandards(
      List<Facility> facilities, String serviceType, boolean establishedPatient) {
    boolean isPrimary =
        equalsIgnoreCase(serviceType, "primarycare")
            || equalsIgnoreCase(serviceType, "mentalhealth");
    int waitDays = isPrimary ? maxWaitDaysPrimary : maxWaitDaysSpecialty;
    int driveMins = isPrimary ? maxDriveMinsPrimary : maxDriveMinsSpecialty;
    return facilities
        .stream()
        .filter(
            facility ->
                waitDays(facility, establishedPatient) != null
                    && waitDays(facility, establishedPatient) <= waitDays
                    && facility.driveMinutes() != null
                    && facility.driveMinutes() <= driveMins)
        .collect(Collectors.toList());
  }

  private List<VceEligibilityInfo> processEligibilityAndEnrollmentResponse(
      GetEESummaryResponse response) {
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

  /** Search community care eligibility. */
  @SneakyThrows
  @GetMapping(value = "/search")
  public CommunityCareEligibilityResponse search(
      @NotBlank @RequestParam(value = "patient") String patientIcn,
      @NotBlank @RequestParam(value = "street") String street,
      @NotBlank @RequestParam(value = "city") String city,
      @NotBlank @RequestParam(value = "state") String state,
      @NotBlank @RequestParam(value = "zip") String zip,
      @NotBlank @RequestParam(value = "serviceType") String serviceType,
      @RequestParam(value = "establishedPatient") Boolean establishedPatient) {
    String mappedServiceType = servicesMap().get(serviceType);
    if (mappedServiceType == null) {
      throw new Exceptions.UnknownServiceTypeException(serviceType);
    }
    // For 'MentalHealthCare', use 'MentalHealth' for filtering
    final String filteringServiceType =
        equalsIgnoreCase(mappedServiceType, "MentalHealthCare")
            ? "MentalHealth"
            : mappedServiceType;
    Address patientAddress =
        Address.builder()
            .street(street.trim())
            .city(city.trim())
            .state(state.toUpperCase(Locale.US).trim())
            .zip(zip.trim())
            .build();
    VaFacilitiesResponse vaFacilitiesResponse = facilitiesClient.facilities(patientAddress.state());
    List<VaFacilitiesResponse.Facility> filteredByServiceType =
        vaFacilitiesResponse == null
            ? Collections.emptyList()
            : vaFacilitiesResponse
                .data()
                .stream()
                .filter(vaFacility -> hasServiceType(vaFacility, filteringServiceType))
                .collect(Collectors.toList());
    log.info(
        "VA facilities filtered by service type '{}' and state {}: {}",
        filteringServiceType,
        patientAddress.state(),
        filteredByServiceType.stream().map(facility -> facility.id()).collect(Collectors.toList()));
    List<Facility> facilities =
        filteredByServiceType
            .stream()
            .map(
                vaFacility ->
                    FacilityTransformer.builder()
                        .serviceType(filteringServiceType)
                        .build()
                        .toFacility(vaFacility))
            .collect(Collectors.toList());

    Coordinates patientCoordinates = bingMaps.coordinates(patientAddress);
    facilities.parallelStream().forEach(facility -> setDriveMinutes(patientCoordinates, facility));
    Collections.sort(facilities, Comparator.comparing(f -> f.driveMinutes()));

    Instant timestamp = Instant.now();
    List<VceEligibilityInfo> vceEligibilityCollection =
        processEligibilityAndEnrollmentResponse(eeClient.requestEligibility(patientIcn.trim()));
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

    boolean communityCareEligible =
        eligbleByEligbilityAndEnrollmentResponse(codeString, filteringServiceType);
    List<Facility> facilitiesMeetingAccessStandards =
        facilitiesMeetingAccessStandards(facilities, filteringServiceType, establishedPatient);
    if (!communityCareEligible && !codeString.contains("X")) {
      communityCareEligible = facilitiesMeetingAccessStandards.isEmpty();
    }

    return CommunityCareEligibilityResponse.builder()
        .patientRequest(
            CommunityCareEligibilityResponse.PatientRequest.builder()
                .patientCoordinates(patientCoordinates)
                .serviceType(mappedServiceType)
                .establishedPatient(establishedPatient)
                .patientIcn(patientIcn)
                .patientAddress(patientAddress)
                .timestamp(timestamp.toString())
                .build())
        .communityCareEligibility(
            CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                .eligible(communityCareEligible)
                .eligibilityCode(eligibilityCodes)
                .facilities(
                    facilitiesMeetingAccessStandards
                        .stream()
                        .map(facility -> facility.id())
                        .collect(Collectors.toList()))
                .build())
        .facilities(facilities)
        .build();
  }

  private void setDriveMinutes(Coordinates patientCoordinates, Facility facility) {
    BingResponse routes = bingMaps.routes(patientCoordinates, facility.coordinates());
    if (routes == null) {
      return;
    }
    if (routes.resourceSets().isEmpty()) {
      return;
    }
    Resources resources = routes.resourceSets().get(0);
    if (resources.resources().isEmpty()) {
      return;
    }
    Resource resource = resources.resources().get(0);
    if (resource.travelDuration() == null) {
      return;
    }
    facility.driveMinutes((int) TimeUnit.SECONDS.toMinutes(resource.travelDuration()));
  }
}

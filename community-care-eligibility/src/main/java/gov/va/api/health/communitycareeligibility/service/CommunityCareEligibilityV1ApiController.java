package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resource;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resources;
import gov.va.api.health.communitycareeligibility.service.VaFacilitiesResponse.Attributes;
import gov.va.api.health.communitycareeligibility.service.VaFacilitiesResponse.PhysicalAddress;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class CommunityCareEligibilityV1ApiController {
  private int maxDriveMinsPrimary;

  private int maxWaitDaysPrimary;

  private int maxDriveMinsSpecialty;

  private int maxWaitDaysSpecialty;

  private BingMapsClient bingMaps;

  private EligibilityAndEnrollmentClient eeClient;

  private FacilitiesClient facilitiesClient;

  /** Autowired constructor. */
  @Builder
  public CommunityCareEligibilityV1ApiController(
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

  private static String state(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility == null) {
      return null;
    }
    Attributes attributes = vaFacility.attributes();
    if (attributes == null) {
      return null;
    }
    VaFacilitiesResponse.Address address = attributes.address();
    if (address == null) {
      return null;
    }
    PhysicalAddress physical = address.physical();
    if (physical == null) {
      return null;
    }
    return StringUtils.trimToNull(physical.state());
  }

  private CommunityCareEligibilityResponse.CommunityCareEligibility communityCareEligibility(
      Boolean communityCareEligible, String communityCareDescription) {
    if (allBlank(communityCareDescription, communityCareEligible)) {
      return null;
    }
    if (communityCareDescription.length() == 0) {
      return null;
    }
    return CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
        .eligible(communityCareEligible)
        .description(communityCareDescription)
        .build();
  }

  private Boolean eligbleByEligbilityAndEnrollmentResponse(
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
  private boolean eligibleByAccessStandards(
      String serviceType, boolean establishedPatient, List<Facility> facilities) {
    boolean isPrimary =
        equalsIgnoreCase(serviceType, "primarycare")
            || equalsIgnoreCase(serviceType, "mentalhealth");
    int waitTime = isPrimary ? maxWaitDaysPrimary : maxWaitDaysSpecialty;
    int driveMins = isPrimary ? maxDriveMinsPrimary : maxDriveMinsSpecialty;
    return facilities
        .stream()
        .noneMatch(
            facility ->
                (establishedPatient
                        ? facility.waitDays().establishedPatient() < waitTime
                        : facility.waitDays().newPatient() < waitTime)
                    && facility.driveMinutes() != null
                    && facility.driveMinutes() < driveMins);
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
    GetEESummaryResponse response = eeClient.requestEligibility(patientIcn);
    List<VceEligibilityInfo> vceEligibilityCollection =
        processEligibilityAndEnrollmentResponse(response);
    List<String> eligibilityDescriptions = new ArrayList<>();
    List<String> eligibilityCodes = new ArrayList<>();
    Instant now = Instant.now();
    for (int i = 0; i < vceEligibilityCollection.size(); i++) {
      if (vceEligibilityCollection
          .get(i)
          .getVceEffectiveDate()
          .toGregorianCalendar()
          .toInstant()
          .isBefore(now)) {
        eligibilityDescriptions.add(vceEligibilityCollection.get(i).getVceDescription());
        eligibilityCodes.add(vceEligibilityCollection.get(i).getVceCode());
      }
    }

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
    Coordinates patientCoordinates = bingMaps.coordinates(patientAddress);
    VaFacilitiesResponse vaFacilitiesResponse = facilitiesClient.facilities(patientCoordinates);
    List<VaFacilitiesResponse.Facility> filteredByServiceTypeAndState =
        vaFacilitiesResponse == null
            ? Collections.emptyList()
            : vaFacilitiesResponse
                .data()
                .stream()
                .filter(vaFacility -> hasServiceType(vaFacility, filteringServiceType))
                .filter(vaFacility -> equalsIgnoreCase(state(vaFacility), patientAddress.state()))
                .collect(Collectors.toList());
    log.info(
        "VA facilities filtered by service type '{}' and state {}: {}",
        filteringServiceType,
        patientAddress.state(),
        filteredByServiceTypeAndState
            .stream()
            .map(facility -> facility.id())
            .collect(Collectors.toList()));
    List<Facility> facilities =
        filteredByServiceTypeAndState
            .stream()
            .map(
                vaFacility ->
                    FacilityTransformer.builder()
                        .serviceType(filteringServiceType)
                        .build()
                        .toFacility(vaFacility))
            .collect(Collectors.toList());
    facilities.parallelStream().forEach(facility -> setDriveMinutes(patientCoordinates, facility));
    Boolean communityCareEligible =
        eligbleByEligbilityAndEnrollmentResponse(eligibilityCodes, filteringServiceType);
    if (!communityCareEligible && !eligibilityCodes.contains("X")) {
      communityCareEligible =
          eligibleByAccessStandards(filteringServiceType, establishedPatient, facilities);
      eligibilityDescriptions.add("Access-Standards");
    }
    String communityCareDescriptions = String.join(", ", eligibilityDescriptions);
    CommunityCareEligibilityResponse.CommunityCareEligibility communityCareEligibility =
        communityCareEligibility(communityCareEligible, communityCareDescriptions);
    return CommunityCareEligibilityResponse.builder()
        .patientRequest(
            CommunityCareEligibilityResponse.PatientRequest.builder()
                .patientCoordinates(patientCoordinates)
                .serviceType(mappedServiceType)
                .establishedPatient(establishedPatient)
                .patientIcn(patientIcn)
                .patientAddress(patientAddress)
                .build())
        .communityCareEligibility(communityCareEligibility)
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

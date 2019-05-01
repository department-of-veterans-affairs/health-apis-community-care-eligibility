package gov.va.api.health.communitycareeligibility.service;

import static org.apache.commons.lang3.StringUtils.capitalize;
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
import java.util.Collections;
import java.util.List;
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

  private List<VceEligibilityInfo> processEligibilitAndEnrollmentResponse(
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
      @NotBlank @RequestParam(value = "patientIcn") String patientIcn,
      @NotBlank @RequestParam(value = "street") String street,
      @NotBlank @RequestParam(value = "city") String city,
      @NotBlank @RequestParam(value = "state") String state,
      @NotBlank @RequestParam(value = "zip") String zip,
      @NotBlank @RequestParam(value = "serviceType") String serviceType,
      @RequestParam(value = "establishedPatient") Boolean establishedPatient) {
    GetEESummaryResponse response = eeClient.requestEligibility(patientIcn);
    List<VceEligibilityInfo> vceEligibilityCollection =
        processEligibilitAndEnrollmentResponse(response);
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
    final String serviceRequestType =
        equalsIgnoreCase(serviceType, "MentalHealthCare") ? "MentalHealth" : serviceType;
    Address patientAddress =
        Address.builder()
            .street(street.trim())
            .city(city.trim())
            .state(state.trim())
            .zip(zip.trim())
            .build();
    Coordinates patientCoordinates = bingMaps.coordinates(patientAddress);
    VaFacilitiesResponse vaFacilitiesResponse =
        serviceRequestType.equalsIgnoreCase("urgentcare")
            ? null
            : facilitiesClient.facilities(patientCoordinates);
    List<VaFacilitiesResponse.Facility> filteredByServiceTypeAndState =
        vaFacilitiesResponse == null
            ? Collections.emptyList()
            : vaFacilitiesResponse
                .data()
                .stream()
                .filter(vaFacility -> hasServiceType(vaFacility, serviceRequestType))
                .filter(
                    vaFacility ->
                        equalsIgnoreCase(
                            vaFacility.attributes().address().physical().state().trim(),
                            patientAddress.state()))
                .collect(Collectors.toList());
    log.info(
        "va facilities filtered by service type {}: {}",
        serviceRequestType,
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
                        .serviceType(serviceRequestType)
                        .build()
                        .toFacility(vaFacility))
            .collect(Collectors.toList());
    facilities.parallelStream().forEach(facility -> setDriveMinutes(patientCoordinates, facility));
    Boolean communityCareEligible = false;
    if (eligibilityCodes.contains("X")) {
      communityCareEligible = false;
    } else if ((eligibilityCodes.contains("G")
            || eligibilityCodes.contains("N")
            || eligibilityCodes.contains("H")
            || eligibilityCodes.contains("M")
            || eligibilityCodes.contains("WT")
            || eligibilityCodes.contains("MWT")
            || eligibilityCodes.contains("HWT"))
        && !serviceRequestType.equalsIgnoreCase("urgentcare")) {
      communityCareEligible = true;
    } else if (eligibilityCodes.contains("U")
        && serviceRequestType.equalsIgnoreCase("urgentcare")) {
      communityCareEligible = true;
    } else {
      eligibilityDescriptions.add("Access-Standards");
      if (eligibleByAccessStandards(serviceRequestType, establishedPatient, facilities)) {
        communityCareEligible = true;
      }
    }
    String communityCareDescriptions = String.join(", ", eligibilityDescriptions);
    CommunityCareEligibilityResponse.CommunityCareEligibility communityCareEligibility =
        EligibilityAndEnrollmentTransformer.builder()
            .communityCareEligibility(
                CommunityCareEligibilityResponse.CommunityCareEligibility.builder()
                    .eligible(communityCareEligible)
                    .description(communityCareDescriptions)
                    .build())
            .build()
            .toCommunityCareEligibilities();
    return CommunityCareEligibilityResponse.builder()
        .patientRequest(
            CommunityCareEligibilityResponse.PatientRequest.builder()
                .patientCoordinates(patientCoordinates)
                .serviceType(capitalize(serviceType))
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

package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resource;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resources;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  private FacilitiesClient facilitiesClient;

  private BingMapsClient bingMaps;

  private EligibilityAndEnrollmentClient eeClient;

  private int maxDriveTime;

  private int maxWait;

  private int maxDriveTimeSpecialty;

  private int maxWaitSpecialty;

  /** Autowired constructor. */
  @Builder
  public CommunityCareEligibilityV1ApiController(
      @Value("${community-care.max-drive-time}") int maxDriveTime,
      @Value("${community-care.max-wait}") int maxWait,
      @Value("${community-care.max-drive-time-specialty}") int maxDriveTimeSpecialty,
      @Value("${community-care.max-wait-specialty}") int maxWaitSpecialty,
      @Autowired BingMapsClient bingMaps,
      @Autowired EligibilityAndEnrollmentClient eeClient,
      @Autowired FacilitiesClient facilitiesClient) {
    this.maxDriveTime = maxDriveTime;
    this.maxWait = maxWait;
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
                        && StringUtils.equalsIgnoreCase(serviceType, waitTime.service()));
  }

  @SneakyThrows
  private boolean computeEligibilityBasedOnDriveTime(List<Facility> facilities, String serviceType) {
      int driveTime = ((StringUtils.equalsIgnoreCase(serviceType, "primarycare")) || (StringUtils.equalsIgnoreCase(serviceType, "primarycare")) ? maxDriveTime : maxDriveTimeSpecialty);
      List<Facility> filtered =
        facilities
            .stream()
            .filter(
                facility ->
                    (facility.driveMinutes() != null && facility.driveMinutes() < driveTime))
            .collect(Collectors.toList());
    return filtered.isEmpty();
  }

  @SneakyThrows
  private boolean computeEligibilityBasedOnWaitTime(
      boolean establishedPatient, List<Facility> facilities, String serviceType) {

      int waitTime = ((StringUtils.equalsIgnoreCase(serviceType, "primarycare")) || (StringUtils.equalsIgnoreCase(serviceType, "primarycare")) ? maxWait : maxWaitSpecialty);
      List<Facility> filtered =
        facilities
            .stream()
            .filter(
                facility ->
                    (establishedPatient
                        ? (facility.waitDays().establishedPatient() < waitTime)
                        : (facility.waitDays().newPatient() < waitTime)))
            .collect(Collectors.toList());
    return filtered.isEmpty();
  }

  /** Search community care eligibility. */
  @SneakyThrows
  @GetMapping(value = "/search")
  public CommunityCareEligibilityResponse search(
      @NotBlank @RequestParam(value = "street") String street,
      @NotBlank @RequestParam(value = "city") String city,
      @NotBlank @RequestParam(value = "state") String state,
      @NotBlank @RequestParam(value = "zip") String zip,
      @NotBlank @RequestParam(value = "serviceType") String serviceType,
      @NotBlank @RequestParam(value = "patientICN") String patientIcn,
      @RequestParam(value = "establishedPatient") Boolean establishedPatient) {
    Address patientAddress =
        Address.builder()
            .street(street.trim())
            .city(city.trim())
            .state(state.trim())
            .zip(zip.trim())
            .build();
    Coordinates patientCoordinates = bingMaps.coordinates(patientAddress);
    GetEESummaryResponse response = eeClient.requestEligibility(patientIcn);
    List<VceEligibilityInfo> vceEligibilityCollection =
        response.getSummary() == null
            ? Collections.emptyList()
            : response
                .getSummary()
                .getCommunityCareEligibilityInfo()
                .getEligibilities()
                .getEligibility();
    List<String> eligibilityDescriptions = new ArrayList<String>();
    List<String> eligibilityCodes = new ArrayList<String>();
    for (int i = 0; i < vceEligibilityCollection.size(); i++) {
      eligibilityDescriptions.add(vceEligibilityCollection.get(i).getVceDescription());
      eligibilityCodes.add(vceEligibilityCollection.get(i).getVceCode());
    }
    VaFacilitiesResponse vaFacilitiesResponse =
        serviceType.equalsIgnoreCase("urgentcare")
            ? null
            : facilitiesClient.facilities(patientCoordinates, serviceType);
    List<VaFacilitiesResponse.Facility> filteredByServiceTypeAndState =
        vaFacilitiesResponse == null
            ? Collections.emptyList()
            : vaFacilitiesResponse
                .data()
                .stream()
                .filter(
                    vaFacility ->
                        hasServiceType(vaFacility, serviceType)
                            && (StringUtils.equalsIgnoreCase(
                                vaFacility.attributes().address().physical().state().trim(),
                                patientAddress.state())))
                .collect(Collectors.toList());
    log.info("va facilities filtered by service type {}: {}", serviceType, vaFacilitiesResponse);
    List<Facility> facilities =
        filteredByServiceTypeAndState
            .stream()
            .map(
                vaFacility ->
                    FacilityTransformer.builder()
                        .serviceType(serviceType)
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
        && !serviceType.equalsIgnoreCase("urgentcare")) {
      communityCareEligible = true;
    } else if (eligibilityCodes.contains("U") && serviceType.equalsIgnoreCase("urgentcare")) {
      communityCareEligible = true;
    } else {
      if (computeEligibilityBasedOnWaitTime(establishedPatient, facilities, serviceType)) {
        eligibilityDescriptions.add("Wait-Time");
        communityCareEligible = true;
      }
      if (computeEligibilityBasedOnDriveTime(facilities, serviceType)) {
        eligibilityDescriptions.add("Drive-Time");
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
        .communityCareEligibility(communityCareEligibility)
        .facilities(facilities)
        .build();
  }

  private void setDriveMinutes(Coordinates patientCoordinates, Facility facility) {
    BingResponse routes = bingMaps.routes(patientCoordinates, facility);
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

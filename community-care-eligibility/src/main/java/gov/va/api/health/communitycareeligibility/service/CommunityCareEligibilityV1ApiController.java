package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.CommunityCareEligibilities;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resource;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resources;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
  private AccessToCareClient accessToCare;

  private BingMapsClient bingMaps;

  private EligibilityAndEnrollmentClient eeClient;

  private int maxDriveTime;

  private int maxWait;

  /** Autowired constructor. */
  @Builder
  public CommunityCareEligibilityV1ApiController(
      @Value("${community-care.max-drive-time}") int maxDriveTime,
      @Value("${community-care.max-wait}") int maxWait,
      @Autowired AccessToCareClient accessToCare,
      @Autowired BingMapsClient bingMaps,
      // @Autowired EnrollmentEligibilityClient enrollmentEligibility
      @Autowired EligibilityAndEnrollmentClient eeClient) {
    this.maxDriveTime = maxDriveTime;
    this.maxWait = maxWait;
    this.accessToCare = accessToCare;
    this.bingMaps = bingMaps;
    this.eeClient = eeClient;
  }

  @SneakyThrows
  private boolean computeEligibility(
      Address patientAddress, boolean establishedPatient, List<Facility> facilities) {
    if (Arrays.asList("AK", "AZ", "IA", "NM", "MN", "ND", "OK", "SD", "UT")
        .stream()
        .anyMatch(patientAddress.state()::equalsIgnoreCase)) {
      // No VAMC locations in these states, automatically eligible
      return true;
    }

    // Filter facilities in same state, within a certain drive time and wait time
    List<Facility> filtered =
        facilities
            .stream()
            .filter(
                facility ->
                    (StringUtils.equalsIgnoreCase(
                            facility.address().state(), patientAddress.state())
                        && facility.driveMinutes() != null
                        && facility.driveMinutes() < maxDriveTime
                        && (establishedPatient
                            ? (facility.waitDays().establishedPatient() < maxWait)
                            : (facility.waitDays().newPatient() < maxWait))))
            .collect(Collectors.toList());

    // return false if NO facilities meet requirements
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
      @NotBlank @RequestParam(value = "serviceType") String serviceType) {

    Address patientAddress =
        Address.builder()
            .street(street.trim())
            .city(city.trim())
            .state(state.trim())
            .zip(zip.trim())
            .build();
    GetEESummaryResponse response = eeClient.requestEligibility("1008679665V880686");

    List<VceEligibilityInfo> vceEligibilityCollection =
        response.getSummary() == null
            ? Collections.emptyList()
            : response
                .getSummary()
                .getCommunityCareEligibilityInfo()
                .getEligibilities()
                .getEligibility();
    List<CommunityCareEligibilities> communityCareEligibilities =
        vceEligibilityCollection
            .stream()
            .filter(Objects::nonNull)
            .map(
                vceEligibilityInfo ->
                    EligibilityAndEnrollmentTransformer.builder()
                        .eligibilityInfo(vceEligibilityInfo)
                        .build()
                        .toCommunityCareEligibilities())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    List<AccessToCareFacility> accessToCareFacilities =
        accessToCare.facilities(patientAddress, serviceType);
    List<Facility> facilities =
        accessToCareFacilities
            .stream()
            .filter(Objects::nonNull)
            .map(
                accessToCareFacility ->
                    AccessToCareFacilityTransformer.builder()
                        .atcFacility(accessToCareFacility)
                        .build()
                        .toFacility())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    facilities.parallelStream().forEach(facility -> setDriveMinutes(patientAddress, facility));
    return CommunityCareEligibilityResponse.builder()
        .communityCareEligibilities(communityCareEligibilities)
        .facilities(facilities)
        .build();
  }

  private void setDriveMinutes(Address patientAddress, Facility facility) {
    BingResponse routes = bingMaps.routes(patientAddress, facility);
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

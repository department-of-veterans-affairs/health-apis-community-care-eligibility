package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resource;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Resources;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client.EnrollmentEligibilityClient;
import gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client.Query;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static gov.va.api.health.communitycareeligibility.service.Transformers.hasPayload;

@Validated
@RestController
@RequestMapping(
  value = {"/api"},
  produces = "application/json"
)
public class CommunityCareEligibilityV1ApiController {
  private AccessToCareClient accessToCare;

  private BingMapsClient bingMaps;

  private EnrollmentEligibilityClient enrollmentEligibility;

  private int maxDriveTime;

  private int maxWait;

  /** Autowired constructor. */
  @Builder
  public CommunityCareEligibilityV1ApiController(
      @Value("${community-care.max-drive-time}") int maxDriveTime,
      @Value("${community-care.max-wait}") int maxWait,
      @Autowired AccessToCareClient accessToCare,
      @Autowired BingMapsClient bingMaps,
      @Autowired EnrollmentEligibilityClient enrollmentEligibility) {

    this.maxDriveTime = maxDriveTime;
    this.maxWait = maxWait;
    this.accessToCare = accessToCare;
    this.bingMaps = bingMaps;
    this.enrollmentEligibility = enrollmentEligibility;
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
    boolean establishedPatient = true;
    Query<GetEESummaryResponse> query = Query.forType(GetEESummaryResponse.class).id("1008679665V880686").build();
    GetEESummaryResponse eeSummaryResponse = hasPayload(enrollmentEligibility.search(query));
    Address patientAddress =
        Address.builder()
            .street(street.trim())
            .city(city.trim())
            .state(state.trim())
            .zip(zip.trim())
            .build();
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
    boolean communityCareEligible =
        computeEligibility(patientAddress, establishedPatient, facilities);
    return CommunityCareEligibilityResponse.builder()
        .communityCareEligible(communityCareEligible)
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

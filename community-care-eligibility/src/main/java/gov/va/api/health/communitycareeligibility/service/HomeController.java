package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
  private AccessToCareClient accessToCare;

  private BingMapsClient bingMaps;

  private int maxDriveTime;

  private int maxWait;

  /** Autowired constructor. */
  public HomeController(
      @Value("${community-care.max-drive-time}") int maxDriveTime,
      @Value("${community-care.max-wait}") int maxWait,
      @Autowired AccessToCareClient accessToCare,
      @Autowired BingMapsClient bingMaps) {
    this.maxDriveTime = maxDriveTime;
    this.maxWait = maxWait;
    this.accessToCare = accessToCare;
    this.bingMaps = bingMaps;
  }

  @SneakyThrows
  private boolean checkIfEligibleForCommunityCare(
      Address patientAddress, boolean establishedPatient, List<Facility> facilities) {
    String[] automaticallyEligibleStates = {"AK", "AZ", "IA", "NM", "MN", "ND", "OK", "SD", "UT"};
    if (Arrays.stream(automaticallyEligibleStates)
        .anyMatch(patientAddress.state()::equalsIgnoreCase)) {
      // No VAMC locations in these states, automatically eligible
      return true;
    } else {
      List<Facility> facilitiesMeetingRequirements =
          facilities
              .stream()
              .filter(
                  facility ->
                      (facility.driveMinutes() < maxDriveTime
                          && facility.address().state().equals(patientAddress.state())
                          && (establishedPatient
                              ? (facility.waitDays().establishedPatient() < maxWait)
                              : (facility.waitDays().newPatient() < maxWait))))
              .collect(Collectors.toList());

      return facilitiesMeetingRequirements.size()
          == // return false if NO facilities meet requirements
          0;
    }
  }

  /** Search by address and service type. */
  @ResponseBody
  @SneakyThrows
  @GetMapping(value = {"/search"})
  public CommunityCareEligibilityResponse search(
      @RequestParam(value = "street") String street,
      @RequestParam(value = "city") String city,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "zip") String zip,
      @RequestParam(value = "serviceType") String serviceType,
      @RequestParam(value = "establishedPatient") boolean establishedPatient) {
    Address patientAddress =
        Address.builder().street(street).city(city).state(state).zip(zip).build();
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
            .collect(Collectors.toList());
    facilities
        .parallelStream()
        .forEach(
            facility -> facility.driveMinutes(bingMaps.driveTimeMinutes(patientAddress, facility)));
    boolean communityCareEligible =
        checkIfEligibleForCommunityCare(patientAddress, establishedPatient, facilities);
    return CommunityCareEligibilityResponse.builder()
        .communityCareEligible(communityCareEligible)
        .facilities(facilities)
        .build();
  }
}

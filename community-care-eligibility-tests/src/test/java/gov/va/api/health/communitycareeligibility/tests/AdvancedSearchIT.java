package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.Requestor.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.sentinel.Environment;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AdvancedSearchIT {
  @BeforeAll
  public static void assumeLocal() {
    assumeEnvironmentIn(Environment.LOCAL);
  }

  @Test
  void patientEligibleDueToCode() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013120787V412913", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(
            response.eligibilityCodes().stream()
                .filter(Objects::nonNull)
                .anyMatch(c -> "N".equals(c.code())))
        .isTrue();
    assertThat(response.eligible()).isTrue();
  }

  @Test
  void patientEligibleDueToHardship() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012845331V153043", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.eligible()).isTrue();
  }

  @Test
  void patientEligibleDueToNoNearbyFacilities() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012845943V900681", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.nearbyFacilities()).isEmpty();
    assertThat(response.eligible()).isTrue();
  }

  @Test
  void patientEligibleDueToStateWithNoVaMedicalCenters() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012845944V882130", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.noFullServiceVaMedicalFacility()).isTrue();
    assertThat(response.eligible()).isTrue();
  }

  @Test
  void patientIndeterminateDueToNoAddress() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013117618V394810", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.patientAddress()).isNull();
    assertThat(response.eligible()).isNull();
  }

  @Test
  void patientIndeterminateDueToNoGeoCoding() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013120801V413425", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.patientCoordinates()).isNull();
    assertThat(response.eligible()).isNull();
  }

  @Test
  void patientNotEligibleDueToCode() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013120835V054496", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(
            response.eligibilityCodes().stream()
                .filter(Objects::nonNull)
                .anyMatch(c -> "X".equals(c.code())))
        .isTrue();
    assertThat(response.eligible()).isFalse();
  }

  @Test
  void patientNotEligibleDueToNearbyFacility() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012853802V084487", "Cardiology");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.nearbyFacilities()).isNotEmpty();
    assertThat(response.eligible()).isFalse();
  }
}

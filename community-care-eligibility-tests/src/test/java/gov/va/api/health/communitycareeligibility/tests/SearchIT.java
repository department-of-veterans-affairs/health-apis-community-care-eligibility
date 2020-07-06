package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.Requestor.makeRequest;
import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.systemDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.sentinel.Environment;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class SearchIT {
  private void assumeStagingLabOrLab() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isNotEqualTo(Environment.LOCAL)
        .isNotEqualTo(Environment.STAGING)
        .isNotEqualTo(Environment.PROD);
  }

  @Test
  void audiology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Audiology");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void cardiology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Cardiology");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void dermatology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Dermatology");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void gastroenterology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Gastroenterology");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void gynecology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Gynecology");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void mentalHealthCare() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "MentalHealthCare");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void nutrition() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Nutrition");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void ophthalmology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Ophthalmology");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void optometry() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Optometry");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void orthopedics() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Orthopedics");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void patientEligibleDueToCode() {
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013120787V412913", "PrimaryCare");
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
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012845331V153043", "PrimaryCare");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.eligible()).isTrue();
  }

  @Test
  void patientEligibleDueToNoNearbyFacilities() {
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012845943V900681", "PrimaryCare");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.nearbyFacilities()).isEmpty();
    assertThat(response.eligible()).isTrue();
  }

  @Test
  void patientEligibleDueToStateWithNoVaMedicalCenters() {
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012845944V882130", "PrimaryCare");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.noFullServiceVaMedicalFacility()).isTrue();
    assertThat(response.eligible()).isTrue();
  }

  @Test
  void patientIndeterminateDueToNoAddress() {
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013117618V394810", "PrimaryCare");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.patientAddress()).isNull();
    assertThat(response.eligible()).isNull();
  }

  @Test
  void patientIndeterminateDueToNoGeoCoding() {
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013120801V413425", "PrimaryCare");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.patientCoordinates()).isNull();
    assertThat(response.eligible()).isNull();
  }

  @Test
  void patientNotEligibleDueToCode() {
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1013120835V054496", "PrimaryCare");
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
    assumeStagingLabOrLab();
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "1012853802V084487", "PrimaryCare");
    CommunityCareEligibilityResponse response =
        makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
    assertThat(response.nearbyFacilities()).isNotEmpty();
    assertThat(response.eligible()).isFalse();
  }

  @Test
  void podiatry() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Podiatry");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void primaryCare() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "PrimaryCare");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void urology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Urology");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void womensHealth() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "WomensHealth");
    makeRequest(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }
}

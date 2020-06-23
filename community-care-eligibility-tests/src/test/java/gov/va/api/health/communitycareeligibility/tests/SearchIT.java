package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.Requestor.makeRequest;
import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.systemDefinition;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import org.junit.jupiter.api.Test;

public class SearchIT {
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

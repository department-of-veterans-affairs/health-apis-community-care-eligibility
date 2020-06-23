package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.Requestor.search;
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
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void cardiology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Cardiology");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void dermatology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Dermatology");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void gastroenterology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Gastroenterology");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void gynecology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Gynecology");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void mentalHealthCare() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "MentalHealthCare");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void nutrition() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Nutrition");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void ophthalmology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Ophthalmology");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void optometry() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Optometry");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void orthopedics() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Orthopedics");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void podiatry() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Podiatry");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void primaryCare() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "PrimaryCare");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void urology() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "Urology");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void womensHealth() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "WomensHealth");
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }
}

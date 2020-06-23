package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.Requestor.search;
import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.systemDefinition;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.api.ErrorResponse;
import org.junit.jupiter.api.Test;

public class SearchValidationIT {
  @Test
  void extendedDriveMin() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s&extendedDriveMin=%s",
            systemDefinition().patient(), "PrimaryCare", 90);
    search(request, 200).expectValid(CommunityCareEligibilityResponse.class);
  }

  @Test
  void extendedDriveMin_high() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s&extendedDriveMin=%s",
            systemDefinition().patient(), "PrimaryCare", 100);
    search(request, 400).expectValid(ErrorResponse.BadRequest.class);
  }

  @Test
  void extendedDriveMin_low() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s&extendedDriveMin=%s",
            systemDefinition().patient(), "PrimaryCare", -1);
    search(request, 400).expectValid(ErrorResponse.BadRequest.class);
  }

  @Test
  void missingPatient() {
    String request = String.format("v0/eligibility/search?serviceType=%s", "PrimaryCare");
    search(request, 500).expectValid(ErrorResponse.InternalServerError.class);
  }

  @Test
  void missingServiceType() {
    String request =
        String.format("v0/eligibility/search?patient=%s", systemDefinition().patient());
    search(request, 500).expectValid(ErrorResponse.InternalServerError.class);
  }
}

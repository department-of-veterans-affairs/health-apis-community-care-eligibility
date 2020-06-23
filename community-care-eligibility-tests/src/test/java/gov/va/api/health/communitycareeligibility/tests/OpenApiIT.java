package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.cceClient;
import static gov.va.api.health.communitycareeligibility.tests.Requestor.makeRequest;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.http.Method;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

public class OpenApiIT {
  @Test
  void openApiJson() {
    makeRequest("v0/eligibility/openapi.json", 200);
  }

  @Test
  void openApiJson_root() {
    makeRequest("v0/eligibility/", 200);
  }
}

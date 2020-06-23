package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.cceClient;

import gov.va.api.health.sentinel.ExpectedResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class OpenApiIT {
  private static ExpectedResponse requestOpenApi(@NonNull String openApiPath, int expectedStatus) {
    log.info(
        "Expect {} is status code ({})",
        cceClient().service().apiPath() + openApiPath,
        expectedStatus);
    return cceClient().get(openApiPath).expect(expectedStatus);
  }

  @Test
  void openApiJson() {
    requestOpenApi("v0/eligibility/openapi.json", 200);
  }

  @Test
  void openApiJson_root() {
    requestOpenApi("v0/eligibility/", 200);
  }
}

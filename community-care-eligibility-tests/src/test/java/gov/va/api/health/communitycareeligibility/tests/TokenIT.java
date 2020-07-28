package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.Requestor.makeRequest;
import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static org.assertj.core.api.Assumptions.assumeThat;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class TokenIT {
  @BeforeAll
  static void assumeEnvironment() {
    // These tests require Kong
    String m = "Skipping TokenIT in " + Environment.get();
    assumeThat(Environment.get()).overridingErrorMessage(m).isNotEqualTo(Environment.LOCAL);
  }

  @Test
  void badToken() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "PrimaryCare");
    SystemDefinitions.ServiceDefinition svc = systemDefinition().cce();
    log.info("Expect {} with bad token is status code ({})", svc.apiPath() + request, 401);
    ExpectedResponse.of(
            RestAssured.given()
                .baseUri(svc.url())
                .port(svc.port())
                .relaxedHTTPSValidation()
                .header("Authorization", "Bearer BADTOKEN")
                .request(Method.GET, svc.urlWithApiPath() + request))
        .logAction(logAllWithTruncatedBody(2000))
        .expect(401);
  }

  @Test
  void notMe() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s", "5555555555555", "PrimaryCare");
    makeRequest(request, 403);
  }
}

package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.Requestor.makeRequest;
import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

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
  static void assumeNotLocal() {
    // These tests require Kong
    assumeEnvironmentNotIn(Environment.LOCAL);
  }

  @Test
  void badToken() {
    String request =
        String.format(
            "v0/eligibility/search?patient=%s&serviceType=%s",
            systemDefinition().patient(), "PrimaryCare");
    SystemDefinitions.Service svc = systemDefinition().cce();
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

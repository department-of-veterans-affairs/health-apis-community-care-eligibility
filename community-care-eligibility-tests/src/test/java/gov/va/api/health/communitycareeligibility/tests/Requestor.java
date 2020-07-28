package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
final class Requestor {
  static ExpectedResponse makeRequest(@NonNull String request, int expectedStatus) {
    SystemDefinitions.ServiceDefinition svc = systemDefinition().cce();
    log.info("Expect {} is status code ({})", svc.apiPath() + request, expectedStatus);
    return ExpectedResponse.of(
            RestAssured.given()
                .baseUri(svc.url())
                .port(svc.port())
                .relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + System.getProperty("access-token", "unset"))
                .request(Method.GET, svc.urlWithApiPath() + request))
        .logAction(logAllWithTruncatedBody(2000))
        .expect(expectedStatus);
  }
}

package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.cceClient;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.ServiceDefinition;
import io.restassured.http.Method;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
final class Requestor {
  static ExpectedResponse makeRequest(@NonNull String request, int expectedStatus) {
    ServiceDefinition svc = cceClient().service();
    log.info("Expect {} is status code ({})", svc.apiPath() + request, expectedStatus);
    return ExpectedResponse.of(
            svc.requestSpecification().request(Method.GET, svc.urlWithApiPath() + request))
        .logAction(logAllWithTruncatedBody(2000))
        .expect(expectedStatus);
  }
}

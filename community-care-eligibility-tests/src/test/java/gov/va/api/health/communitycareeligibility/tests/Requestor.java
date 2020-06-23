package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.cceClient;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.http.Method;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
final class Requestor {
  static ExpectedResponse makeRequest(@NonNull String request, int expectedStatus) {
    log.info(
        "Expect {} is status code ({})", cceClient().service().apiPath() + request, expectedStatus);
    return ExpectedResponse.of(
            cceClient()
                .service()
                .requestSpecification()
                .request(Method.GET, cceClient().service().urlWithApiPath() + request))
        .logAction(logAllWithTruncatedBody(2000))
        .expect(expectedStatus);
  }
}

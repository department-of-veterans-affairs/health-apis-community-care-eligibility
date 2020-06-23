package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.communitycareeligibility.tests.SystemDefinitions.cceClient;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.sentinel.ExpectedResponse;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
final class Requestor {
  static ExpectedResponse search(@NonNull String request, int expectedStatus) {
    log.info(
        "Expect {} is status code ({})", cceClient().service().apiPath() + request, expectedStatus);
    return cceClient().get(request).logAction(logAllWithTruncatedBody(2000)).expect(expectedStatus);
  }
}

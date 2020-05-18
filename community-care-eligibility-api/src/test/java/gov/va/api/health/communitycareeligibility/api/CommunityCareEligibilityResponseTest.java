package gov.va.api.health.communitycareeligibility.api;

import static gov.va.api.health.communitycareeligibility.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.communitycareeligibility.api.swaggerexamples.SwaggerCommunityCareEligibilityResponse;
import org.junit.jupiter.api.Test;

public class CommunityCareEligibilityResponseTest {

  @Test
  public void communityCareEligibilityResponse() {
    assertRoundTrip(SwaggerCommunityCareEligibilityResponse.communityCareEligibilityResponse());
  }
}

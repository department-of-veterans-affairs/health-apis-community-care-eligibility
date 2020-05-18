package gov.va.api.health.communitycareeligibility.api;

import static gov.va.api.health.communitycareeligibility.api.RoundTrip.assertRoundTrip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.va.api.health.communitycareeligibility.swaggerexamples.SwaggerErrorResponse;
import org.junit.jupiter.api.Test;

public class ErrorResponseTest {
  @Test
  public void applyException() {
    ErrorResponse.InternalServerError e = ErrorResponse.InternalServerError.builder().build();
    ErrorResponse.applyException(e, new RuntimeException("Failed"));
    assertEquals(e.type(), "RuntimeException");
    assertEquals(e.message(), "Failed");
  }

  @Test
  public void badRequest() {
    assertRoundTrip(SwaggerErrorResponse.badRequest());
  }

  @Test
  public void internalServerError() {
    assertRoundTrip(
        ErrorResponse.InternalServerError.builder()
            .timestamp(1557407878250L)
            .type("InternalServerErrorException")
            .message("Internal Server Error")
            .build());
  }

  @Test
  public void notFound() {
    assertRoundTrip(SwaggerErrorResponse.notFound());
  }

  @Test
  public void serviceUnavailable() {
    assertRoundTrip(
        ErrorResponse.ServiceUnavailable.builder()
            .timestamp(1557407878250L)
            .type("ServiceUnavailableException")
            .message("Service Unavailable")
            .build());
  }
}

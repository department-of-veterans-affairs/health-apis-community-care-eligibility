package gov.va.api.health.communitycareeligibility.swaggerexamples;

import gov.va.api.health.communitycareeligibility.api.ErrorResponse;

/** Swagger example for error responses. */
public class SwaggerErrorResponse {
  /** Swagger BadRequest Example. */
  public static ErrorResponse.BadRequest badRequest() {
    return ErrorResponse.BadRequest.builder()
        .timestamp(1557407878250L)
        .type("UnknownServiceTypeException")
        .message("Unknown service type: dentistry")
        .build();
  }

  /** Swagger BadRequest Example. */
  public static ErrorResponse.NotFound notFound() {
    return ErrorResponse.NotFound.builder()
        .timestamp(1557407878250L)
        .type("UnknownPatientIcnException")
        .message("Unknown patient ICN: 011235813V213455")
        .build();
  }
}

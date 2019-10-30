package gov.va.api.health.communitycareeligibility.api.swaggerexamples;

import gov.va.api.health.communitycareeligibility.api.ErrorResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerErrorResponse {
  public static ErrorResponse.BadRequest errorResponseBadRequest() {
    return ErrorResponse.BadRequest.builder()
        .timestamp(1557407878250L)
        .type("UnknownServiceTypeException")
        .message("Unknown service type: dentistry")
        .build();
  }

  public static ErrorResponse.NotFound errorResponseNotFound() {
    return ErrorResponse.NotFound.builder()
        .timestamp(1557407878250L)
        .type("UnknownPatientIcnException")
        .message("Unknown patient ICN: 011235813V213455")
        .build();
  }

  public static ErrorResponse.NotFound overrideErrorResponseNotFound() {
    return ErrorResponse.NotFound.builder()
        .timestamp(1557407878250L)
        .type("UnknownPatientIcnException")
        .message("[OVERRIDE] Unknown patient ICN: 011235813V213455")
        .build();
  }
}

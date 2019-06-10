package gov.va.api.health.communitycareeligibility.api.swaggerexamples;

import gov.va.api.health.communitycareeligibility.api.ErrorResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerErrorResponse {
  static final ErrorResponse.BadRequest SWAGGER_EXAMPLE_ERROR_RESPONSE_BAD_REQUEST =
      ErrorResponse.BadRequest.builder()
          .timestamp(1557407878250L)
          .type("UnknownServiceTypeException")
          .message("Unknown service type: dentistry")
          .build();

  static final ErrorResponse.NotFound SWAGGER_EXAMPLE_ERROR_RESPONSE_NOT_FOUND =
      ErrorResponse.NotFound.builder()
          .timestamp(1557407878250L)
          .type("UnknownPatientIcnException")
          .message("Unknown patient ICN: 011235813V213455")
          .build();
}

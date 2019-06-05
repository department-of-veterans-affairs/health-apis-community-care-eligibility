package gov.va.api.health.communitycareeligibility.api.swaggerexamples;

import gov.va.api.health.communitycareeligibility.api.ErrorResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerErrorResponse {
  static final ErrorResponse SWAGGER_EXAMPLE_ERROR_RESPONSE_BLANK_ADDRESS =
      ErrorResponse.builder()
          .timestamp(1557407878250L)
          .type("ConstraintViolationException")
          .message("search.state: must not be blank")
          .build();

  static final ErrorResponse SWAGGER_EXAMPLE_ERROR_RESPONSE_UNKNOWN_SERVICE =
      ErrorResponse.builder()
          .timestamp(1557407878250L)
          .type("UnknownServiceTypeException")
          .message("Unknown service type: dentistry")
          .build();

  static final ErrorResponse SWAGGER_EXAMPLE_ERROR_RESPONSE_UNKNOWN_PATIENT =
      ErrorResponse.builder()
          .timestamp(1557407878250L)
          .type("UnknownPatientIcnException")
          .message("Unknown patient ICN: 011235813V213455")
          .build();
}

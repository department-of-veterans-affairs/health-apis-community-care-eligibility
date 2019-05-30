package gov.va.api.health.communitycareeligibility.api.swaggerexamples;

import gov.va.api.health.communitycareeligibility.api.ErrorResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerErrorResponse {
  static final ErrorResponse SWAGGER_EXAMPLE_ERROR_RESPONSE =
      ErrorResponse.builder()
          .timestamp(1557407878250L)
          .type("EeUnavailableException")
          .message("E&E is not available: Connection refused")
          .build();
}

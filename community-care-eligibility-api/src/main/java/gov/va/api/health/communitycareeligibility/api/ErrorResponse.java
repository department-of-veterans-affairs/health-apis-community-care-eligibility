package gov.va.api.health.communitycareeligibility.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/** The error response is the payload returned to the caller should a failure occur. */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(
  fieldVisibility = JsonAutoDetect.Visibility.ANY,
  isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class ErrorResponse {
  long timestamp;

  String type;

  String message;

  /** Create a new error response based on the given exception. */
  public static <T extends ErrorResponse> void applyException(
      @NonNull T response, @NonNull Exception e) {
    // Avoid System currentTimeMillis() for Fortify
    response.timestamp(Instant.now().toEpochMilli());
    response.type(e.getClass().getSimpleName());
    response.message(e.getMessage());
  }

  @NoArgsConstructor
  @Schema(
    example =
        "${errorResponseBadRequest:gov.va.api.health.communitycareeligibility.api.swaggerexamples.SwaggerErrorResponse#errorResponseBadRequest}"
  )
  public static final class BadRequest extends ErrorResponse {
    @Builder
    public BadRequest(long timestamp, String type, String message) {
      super(timestamp, type, message);
    }
  }

  @NoArgsConstructor
  public static final class InternalServerError extends ErrorResponse {
    @Builder
    public InternalServerError(long timestamp, String type, String message) {
      super(timestamp, type, message);
    }
  }

  @NoArgsConstructor
  @Schema(example = "${errorResponseNotFound:to.be.Overridden#byConfiguration}")
  public static final class NotFound extends ErrorResponse {
    @Builder
    public NotFound(long timestamp, String type, String message) {
      super(timestamp, type, message);
    }
  }

  @NoArgsConstructor
  public static final class ServiceUnavailable extends ErrorResponse {
    @Builder
    public ServiceUnavailable(long timestamp, String type, String message) {
      super(timestamp, type, message);
    }
  }
}

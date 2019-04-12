package gov.va.api.health.communitycareeligibility.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/** Utility methods for transforming CDW results to Argonaut. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Transformers {
  /**
   * Return false if at least one value in the given list is a non-blank string, or a non-null
   * object.
   */
  public static boolean allBlank(Object... values) {
    for (Object v : values) {
      if (!isBlank(v)) {
        return false;
      }
    }
    return true;
  }

  /** Return true if the value is a blank string, or any other object that is null. */
  public static boolean isBlank(Object value) {
    if (value instanceof CharSequence) {
      return StringUtils.isBlank((CharSequence) value);
    }
    return value == null;
  }
  /** Throw a MissingPayload exception if the value is null. */
  public static <T> T hasPayload(T value) {
    if (value == null) {
      throw new MissingPayload();
    }
    return value;
  }

  /**
   * Indicates the EE payload is missing, but no errors were reported. This exception indicates
   * there is a bug in EE, Queen Elizabeth, or the Queen Elizabeth client.
   */
  static class MissingPayload extends TransformationException {
    MissingPayload() {
      super(
              "Payload is missing, but no errors reported by Queen Elizabeth."
                      + " This can occur when the resource is registered with the identity service"
                      + " but the database returns an empty search result.");
    }
  }

  /** Base exception for controller errors. */
  static class TransformationException extends RuntimeException {
    @SuppressWarnings("SameParameterValue")
    TransformationException(String message) {
      super(message);
    }
  }
}

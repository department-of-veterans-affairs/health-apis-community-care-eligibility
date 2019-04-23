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

  /**
   * Return true if the value is a blank string, or any other object that is null.
   */
  public static boolean isBlank(Object value) {
    if (value instanceof CharSequence) {
      return StringUtils.isBlank((CharSequence) value);
    }
    return value == null;
  }
}

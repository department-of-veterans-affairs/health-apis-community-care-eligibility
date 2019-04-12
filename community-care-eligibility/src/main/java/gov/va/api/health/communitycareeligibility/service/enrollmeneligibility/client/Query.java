package gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client;

import lombok.Builder;
import lombok.Value;

/**
 * Type safe model for searching Enrollment Eligibility.
 *
 * <pre>
 *   Query.forType(GetEESummaryResponse.class)
 *     .id(1010101010V666666)
 *     .build();
 * </pre>
 */
@Value
@Builder(toBuilder = true)
public class Query<T> {
  String id;

  Class<T> type;

  /** Start a builder chain to query for a given type. */
  public static <R> QueryBuilder<R> forType(Class<R> forType) {
    return Query.<R>builder().type(forType);
  }

  /**
   * Returns a Enrollment Eligibility formatted query string. Enrollment Eligibility only gets a
   * single query parameter, an id. example: ?id=1010101010V666666
   */
  public String toQueryString() {
    StringBuilder queryString = new StringBuilder();
    queryString.append("?id=").append(id);
    return queryString.toString();
  }
}

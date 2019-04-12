package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client.Query;

public interface EnrollmentEligibilityClient {
  /** Returns a deserialized result of the EnrollmentEligibility EE search. */
  <T> T search(Query<T> query);

  /** Generic Enrollment Eligibility exception. */
  class EnrollmentEligibilityServiceException extends RuntimeException {
    EnrollmentEligibilityServiceException(Query<?> query) {
      super(query.toQueryString());
    }
  }

  /** Request Malformed. example: Missing required id parameter. */
  class BadRequest extends EnrollmentEligibilityServiceException {
    public BadRequest(Query<?> query) {
      super(query);
    }
  }

  /** Not found by Enrollment Eligibility in EE. */
  class NotFound extends EnrollmentEligibilityServiceException {
    public NotFound(Query<?> query) {
      super(query);
    }
  }

  /** Bad Things happened when performing the search. */
  class SearchFailed extends EnrollmentEligibilityServiceException {
    public SearchFailed(Query<?> query) {
      super(query);
    }
  }
}

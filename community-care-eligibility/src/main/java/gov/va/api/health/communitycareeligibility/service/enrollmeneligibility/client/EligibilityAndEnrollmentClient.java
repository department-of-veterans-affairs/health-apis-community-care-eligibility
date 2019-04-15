package gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client;

import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;

public interface EligibilityAndEnrollmentClient {
  GetEESummaryResponse requestEligibility(String id);
}

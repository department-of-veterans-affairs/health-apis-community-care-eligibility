package gov.va.api.health.communitycareeligibility.service;

import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;

/** EligibilityAndEnrollmentClient. */
public interface EligibilityAndEnrollmentClient {
  GetEESummaryResponse requestEligibility(String patientIcn);
}

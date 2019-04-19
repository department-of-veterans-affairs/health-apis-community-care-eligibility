package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;

public interface FacilityClient {
  VaFacilitiesResponse facilities(Coordinates patientAddress, String serviceType);
}

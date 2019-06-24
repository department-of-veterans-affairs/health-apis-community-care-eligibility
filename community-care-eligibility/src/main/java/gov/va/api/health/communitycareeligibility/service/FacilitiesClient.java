package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;

public interface FacilitiesClient {
  VaFacilitiesResponse nearbyFacilities(Address address, int driveMins, String serviceType);
}

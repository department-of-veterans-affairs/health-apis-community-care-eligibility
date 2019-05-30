package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;

public interface FacilitiesClient {
  VaFacilitiesResponse facilities(String state);

  VaNearbyFacilitiesResponse nearby(Address address, int driveMins);
}

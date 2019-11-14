package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;

public interface FacilitiesClient {

  VaFacilitiesResponse facilitiesByIds(String ids);

  VaNearbyFacilitiesResponse nearbyFacilities(
      Coordinates coordinates, int driveMins, String serviceType);
}

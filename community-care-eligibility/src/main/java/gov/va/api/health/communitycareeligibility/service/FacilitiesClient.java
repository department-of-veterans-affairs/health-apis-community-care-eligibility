package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import java.util.List;

public interface FacilitiesClient {

  VaFacilitiesResponse facilitiesByIds(List<String> ids);

  VaNearbyFacilitiesResponse nearbyFacilities(
      Coordinates coordinates, int driveMins, String serviceType);
}

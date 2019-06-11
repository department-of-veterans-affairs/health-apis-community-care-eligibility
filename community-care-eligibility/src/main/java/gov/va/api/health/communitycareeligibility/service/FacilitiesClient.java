package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import java.util.List;

public interface FacilitiesClient {
  VaFacilitiesResponse facilities(String state, String serviceType);

  List<String> nearby(Address address, int driveMins, String serviceType);
}

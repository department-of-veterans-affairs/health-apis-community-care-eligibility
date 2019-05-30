package gov.va.api.health.communitycareeligibility.service;

import java.util.List;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;

public interface FacilitiesClient {
  VaFacilitiesResponse facilities(String state);

  List<String> nearby(Address address, int driveMins);
}

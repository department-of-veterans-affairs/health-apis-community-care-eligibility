package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;

public interface BingMapsClient {
  Coordinates coordinates(Address address);

  BingResponse routes(Coordinates source, Coordinates destination);
}

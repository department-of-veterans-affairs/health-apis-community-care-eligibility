package gov.va.api.health.communitycareeligibility.service;

import java.util.List;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;

public interface AccessToCareClient {
  List<AccessToCareFacility> facilities(Address patientAddress, String serviceType);
}

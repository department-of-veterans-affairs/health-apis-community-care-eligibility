package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import java.util.List;

public interface AccessToCareClient {
  List<AccessToCareFacility> facilities(Address patientAddress, String serviceType);
}

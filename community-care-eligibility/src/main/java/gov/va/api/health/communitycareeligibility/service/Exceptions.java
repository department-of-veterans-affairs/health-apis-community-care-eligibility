package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import lombok.experimental.UtilityClass;

@UtilityClass
final class Exceptions {
  static final class EeUnavailableException extends RuntimeException {
    EeUnavailableException(Throwable cause) {
      super("E&E is not available: " + cause.getMessage(), cause);
    }
  }

  static final class FacilitiesUnavailableException extends RuntimeException {
    FacilitiesUnavailableException(Throwable cause) {
      super("Facilities API is not available: " + cause.getMessage(), cause);
    }
  }

  static final class IncompleteAddressException extends RuntimeException {
    IncompleteAddressException(String patientIcn, Address patientAddress) {
      super("Residential address for ICN " + patientIcn + " is incomplete: " + patientAddress);
    }
  }

  static final class MissingResidentialAddressException extends RuntimeException {
    MissingResidentialAddressException(String patientIcn) {
      super("No residential address found for ICN: " + patientIcn);
    }
  }

  static final class UnknownPatientIcnException extends RuntimeException {
    UnknownPatientIcnException(String patientIcn, Throwable cause) {
      super("Unknown patient ICN: " + patientIcn, cause);
    }
  }

  static final class UnknownServiceTypeException extends RuntimeException {
    UnknownServiceTypeException(String serviceType) {
      super("Unknown service type: " + serviceType);
    }
  }
}

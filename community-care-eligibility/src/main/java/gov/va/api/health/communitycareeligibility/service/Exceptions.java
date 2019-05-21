package gov.va.api.health.communitycareeligibility.service;

import lombok.experimental.UtilityClass;

@UtilityClass
final class Exceptions {
  static final class BingMapsUnavailableException extends RuntimeException {
    BingMapsUnavailableException(String message) {
      super("Bing Maps API is not available and is returning " + message);
    }
  }

  static final class EeUnavailableException extends RuntimeException {
    EeUnavailableException(Throwable cause) {
      super("E&E is not available", cause);
    }
  }

  static final class FacilitiesUnavailableException extends RuntimeException {
    FacilitiesUnavailableException(String message) {
      super("Facilities API is not available and is returning " + message);
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

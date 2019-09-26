package gov.va.api.health.communitycareeligibility.service;

import java.time.Instant;

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

  static final class MissingGeocodingInfoException extends RuntimeException {
    MissingGeocodingInfoException(String patientIcn) {
      super("No geocoding information found for ICN: " + patientIcn);
    }
  }

  static final class OutdatedGeocodingInfoException extends RuntimeException {
    OutdatedGeocodingInfoException(String patientIcn, Instant geocodeTime, Instant addressTime) {
      super(
          String.format(
              "For patient ICN %s, geocoding information (updated %s) is out of date against residential address (updated %s)",
              patientIcn, geocodeTime, addressTime));
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

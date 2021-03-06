package gov.va.api.health.communitycareeligibility.service;

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

  static final class InvalidExtendedDriveMin extends RuntimeException {
    InvalidExtendedDriveMin(String serviceType, int extendedDriveMins, int driveMins) {
      super(
          String.format(
              "Extended drive-radius (%s mins) does not exceed"
                  + " standard drive-radius (%s mins) for service-type '%s'",
              extendedDriveMins, driveMins, serviceType));
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

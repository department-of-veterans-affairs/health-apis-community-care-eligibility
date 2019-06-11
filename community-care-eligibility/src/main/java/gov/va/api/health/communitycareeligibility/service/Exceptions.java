package gov.va.api.health.communitycareeligibility.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Exceptions {
  public static final class EeUnavailableException extends RuntimeException {
    public EeUnavailableException(Throwable cause) {
      super("E&E is not available: " + cause.getMessage(), cause);
    }
  }

  public static final class FacilitiesUnavailableException extends RuntimeException {
    public FacilitiesUnavailableException(Throwable cause) {
      super("Facilities API is not available: " + cause.getMessage(), cause);
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

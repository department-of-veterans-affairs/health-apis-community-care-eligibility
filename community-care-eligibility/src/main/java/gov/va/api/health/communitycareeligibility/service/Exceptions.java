package gov.va.api.health.communitycareeligibility.service;

import lombok.experimental.UtilityClass;

@UtilityClass
final class Exceptions {
  static final class BingMapsUnavailableException extends RuntimeException {
    BingMapsUnavailableException() {
      super("Bing Maps API is not available");
    }
  }

  static final class FacilitiesUnavailableException extends RuntimeException {
    FacilitiesUnavailableException() {
      super("Facilities API is not available");
    }
  }

  static final class UnknownServiceTypeException extends RuntimeException {
    UnknownServiceTypeException(String serviceType) {
      super("Unknown service type: " + serviceType);
    }
  }
}

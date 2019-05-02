package gov.va.api.health.communitycareeligibility.service;

import lombok.experimental.UtilityClass;

@UtilityClass
final class Exceptions {
  static final class UnknownServiceTypeException extends RuntimeException {
    UnknownServiceTypeException(String serviceType) {
      super("Unknown service type: " + serviceType);
    }
  }
}

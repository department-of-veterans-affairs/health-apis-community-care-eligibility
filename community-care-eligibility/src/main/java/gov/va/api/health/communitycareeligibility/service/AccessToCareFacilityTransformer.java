package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.StringUtils.upperCase;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.WaitDays;
import java.util.Locale;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class AccessToCareFacilityTransformer {
  @NonNull private final AccessToCareFacility atcFacility;

  private Address address() {
    if (allBlank(
        atcFacility.address(), atcFacility.city(), atcFacility.state(), atcFacility.zip())) {
      return null;
    }
    return Address.builder()
        .street(trimToNull(atcFacility.address()))
        .city(trimToNull(atcFacility.city()))
        .state(trimToNull(upperCase(atcFacility.state(), Locale.US)))
        .zip(trimToNull(atcFacility.zip()))
        .build();
  }

  private Coordinates coordinates() {
    if (allBlank(atcFacility.latitude(), atcFacility.longitude())) {
      return null;
    }
    return Coordinates.builder()
        .latitude(atcFacility.latitude())
        .longitude(atcFacility.longitude())
        .build();
  }

  Facility toFacility() {
    String id = trimToNull(atcFacility.facilityId());
    String name = trimToNull(atcFacility.name());
    Address address = address();
    Coordinates coordinates = coordinates();
    String phoneNumber = trimToNull(atcFacility.phone());
    WaitDays waitDays = waitDays();
    if (allBlank(id, name, address, coordinates, phoneNumber, waitDays)) {
      return null;
    }
    return Facility.builder()
        .id(id)
        .name(name)
        .address(address)
        .coordinates(coordinates)
        .phoneNumber(phoneNumber)
        .waitDays(waitDays)
        .build();
  }

  private WaitDays waitDays() {
    if (allBlank(atcFacility.estWaitTime(), atcFacility.newWaitTime())) {
      return null;
    }
    return WaitDays.builder()
        .establishedPatient(atcFacility.estWaitTime().intValue())
        .newPatient(atcFacility.newWaitTime().intValue())
        .build();
  }
}

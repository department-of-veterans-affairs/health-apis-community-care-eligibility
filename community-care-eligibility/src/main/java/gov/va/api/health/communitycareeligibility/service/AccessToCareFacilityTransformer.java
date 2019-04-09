package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import gov.va.api.health.communitycareeligibility.api.Address;
import gov.va.api.health.communitycareeligibility.api.Coordinates;
import gov.va.api.health.communitycareeligibility.api.Facility;
import gov.va.api.health.communitycareeligibility.api.WaitDays;
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
        .state(trimToNull(atcFacility.state()))
        .zip(trimToNull(atcFacility.zip()))
        .build();
  }

  Facility toFacility() {
    return Facility.builder()
        .id(trimToNull(atcFacility.facilityId()))
        .name(trimToNull(atcFacility.name()))
        .address(address())
        .coordinates(
            Coordinates.builder()
                .latitude(atcFacility.latitude())
                .longitude(atcFacility.longitude())
                .build())
        .phoneNumber(trimToNull(atcFacility.phone()))
        .waitDays(
            WaitDays.builder()
                .establishedPatient((int) atcFacility.estWaitTime())
                .newPatient((int) atcFacility.newWaitTime())
                .build())
        .build();
  }
}

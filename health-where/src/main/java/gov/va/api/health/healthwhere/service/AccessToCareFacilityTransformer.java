package gov.va.api.health.healthwhere.service;

import static gov.va.api.health.healthwhere.service.Transformers.allBlank;

import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
public final class AccessToCareFacilityTransformer {

  @NonNull private final AccessToCareFacility atcFacility;

  private Address address() {
    if (allBlank(
        atcFacility.address(), atcFacility.city(), atcFacility.state(), atcFacility.zip())) {
      return null;
    }
    return Address.builder()
        .street(StringUtils.trimToNull(atcFacility.address()))
        .city(StringUtils.trimToNull(atcFacility.city()))
        .state(StringUtils.trimToNull(atcFacility.state()))
        .zip(StringUtils.trimToNull(atcFacility.zip()))
        .build();
  }

  public Facility toFacility() {
    return Facility.builder()
        .id(StringUtils.trimToNull(atcFacility.facilityID()))
        .name(StringUtils.trimToNull(atcFacility.name()))
        .address(address())
        .coordinates(
            Coordinates.builder()
                .latitude(atcFacility.latitude())
                .longitude(atcFacility.longitude())
                .build())
        .phoneNumber(StringUtils.trimToNull(atcFacility.phone()))
        .waitDays(
            WaitDays.builder()
                .establishedPatient((int) atcFacility.estWaitTime())
                .newPatient((int) atcFacility.newWaitTime())
                .build())
        .build();
  }
}

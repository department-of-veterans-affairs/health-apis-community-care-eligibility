package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.WaitDays;
import gov.va.api.health.communitycareeligibility.service.VaFacilitiesResponse.VaFacility;
import gov.va.api.health.communitycareeligibility.service.VaFacilityAttributes.PhysicalAddress;
import gov.va.api.health.communitycareeligibility.service.VaFacilityAttributes.WaitTime;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
public class FacilityTransformer {
  @NonNull private final String serviceType;

  private static Address address(VaFacility vaFacility) {
    VaFacilityAttributes attributes = vaFacility.attributes();
    if (attributes == null) {
      return null;
    }
    VaFacilityAttributes.Address address = attributes.address();
    if (address == null) {
      return null;
    }
    PhysicalAddress physical = address.physical();
    if (physical == null) {
      return null;
    }
    String street =
        StringUtils.trimToEmpty(physical.address1())
            + " "
            + StringUtils.trimToEmpty(physical.address2())
            + " "
            + StringUtils.trimToEmpty(physical.address3());
    return Address.builder()
        .street(StringUtils.trimToNull(street))
        .city(StringUtils.trimToNull(physical.city()))
        .state(StringUtils.trimToNull(physical.state()))
        .zip(StringUtils.trimToNull(physical.zip()))
        .build();
  }

  private static Coordinates coordinates(VaFacility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    return Coordinates.builder()
        .latitude(vaFacility.attributes().lat())
        .longitude(vaFacility.attributes().longg())
        .build();
  }

  private static String name(VaFacility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    return StringUtils.trimToNull(vaFacility.attributes().name());
  }

  private static String phoneNumber(VaFacility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (vaFacility.attributes().phone() == null) {
      return null;
    }
    return StringUtils.trimToNull(vaFacility.attributes().phone().main());
  }

  /** Check for Facility. */
  public Facility toFacility(VaFacility vaFacility) {
    if (vaFacility == null) {
      return null;
    }
    return Facility.builder()
        .id(StringUtils.trimToNull(vaFacility.id()))
        .name(name(vaFacility))
        .address(address(vaFacility))
        .coordinates(coordinates(vaFacility))
        .phoneNumber(phoneNumber(vaFacility))
        .waitDays(waitDays(vaFacility))
        .build();
  }

  private WaitDays waitDays(VaFacility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (vaFacility.attributes().waitTimes() == null) {
      return null;
    }
    Optional<WaitTime> optWaitTime =
        vaFacility
            .attributes()
            .waitTimes()
            .health()
            .stream()
            .filter(
                waitTime ->
                    waitTime != null
                        && StringUtils.equalsIgnoreCase(waitTime.service(), serviceType))
            .findFirst();
    if (!optWaitTime.isPresent()) {
      return null;
    }
    return WaitDays.builder()
        .newPatient(optWaitTime.get().neww())
        .establishedPatient(optWaitTime.get().established())
        .build();
  }
}

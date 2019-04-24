package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.WaitDays;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
public class FacilityTransformer {
  @NonNull private final String serviceType;

  private static Address address(
      VaFacilitiesResponse.Facility vaFacility) {
    VaFacilitiesResponse.Attributes attributes = vaFacility.attributes();
    if (attributes == null) {
      return null;
    }
    VaFacilitiesResponse.Address address = attributes.address();
    if (address == null) {
      return null;
    }
    VaFacilitiesResponse.PhysicalAddress physical = address.physical();
    if (physical == null) {
      return null;
    }
    String street =
        StringUtils.trimToEmpty(physical.address1())
            + " "
            + StringUtils.trimToEmpty(physical.address2())
            + " "
            + StringUtils.trimToEmpty(physical.address3());
    if (allBlank(street, physical.city(), physical.state(), physical.zip())) {
      return null;
    }
    return Address.builder()
        .street(StringUtils.trimToNull(street))
        .city(StringUtils.trimToNull(physical.city()))
        .state(StringUtils.trimToNull(physical.state()))
        .zip(StringUtils.trimToNull(physical.zip()))
        .build();
  }

  private static Coordinates coordinates(
      VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (allBlank(vaFacility.attributes().lat(), vaFacility.attributes().longg())) {
      return null;
    }
    return Coordinates.builder()
        .latitude(vaFacility.attributes().lat())
        .longitude(vaFacility.attributes().longg())
        .build();
  }

  private static String name(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    return StringUtils.trimToNull(vaFacility.attributes().name());
  }

  private static String phoneNumber(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (vaFacility.attributes().phone() == null) {
      return null;
    }
    return StringUtils.trimToNull(vaFacility.attributes().phone().main());
  }

  /** Check for Facility. */
  public Facility toFacility(
      VaFacilitiesResponse.Facility vaFacility) {
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

  private WaitDays waitDays(
      VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (vaFacility.attributes().waitTimes() == null) {
      return null;
    }
    Optional<VaFacilitiesResponse.WaitTime> optWaitTime =
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

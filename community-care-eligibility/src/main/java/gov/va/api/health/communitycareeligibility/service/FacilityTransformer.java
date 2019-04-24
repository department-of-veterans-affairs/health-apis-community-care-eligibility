package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
public class FacilityTransformer {
  @NonNull private final String serviceType;

  private static CommunityCareEligibilityResponse.Address address(
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
    return CommunityCareEligibilityResponse.Address.builder()
        .street(StringUtils.trimToNull(street))
        .city(StringUtils.trimToNull(physical.city()))
        .state(StringUtils.trimToNull(physical.state()))
        .zip(StringUtils.trimToNull(physical.zip()))
        .build();
  }

  private static CommunityCareEligibilityResponse.Coordinates coordinates(
      VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (allBlank(vaFacility.attributes().lat(), vaFacility.attributes().longg())) {
      return null;
    }
    return CommunityCareEligibilityResponse.Coordinates.builder()
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
  public CommunityCareEligibilityResponse.Facility toFacility(
      VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility == null) {
      return null;
    }
    return CommunityCareEligibilityResponse.Facility.builder()
        .id(StringUtils.trimToNull(vaFacility.id()))
        .name(name(vaFacility))
        .address(address(vaFacility))
        .coordinates(coordinates(vaFacility))
        .phoneNumber(phoneNumber(vaFacility))
        .waitDays(waitDays(vaFacility))
        .build();
  }

  private CommunityCareEligibilityResponse.WaitDays waitDays(
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
    return CommunityCareEligibilityResponse.WaitDays.builder()
        .newPatient(optWaitTime.get().neww())
        .establishedPatient(optWaitTime.get().established())
        .build();
  }
}

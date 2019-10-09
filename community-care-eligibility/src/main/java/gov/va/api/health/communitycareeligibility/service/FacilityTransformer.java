package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import java.util.Locale;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
public class FacilityTransformer {

  @NonNull private final String serviceType;

  private static Boolean active(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return false;
    }
    return equalsIgnoreCase(trimToNull(vaFacility.attributes().active()), "A");
  }

  private static Address address(VaFacilitiesResponse.Facility vaFacility) {
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
        .street(trimToNull(street))
        .city(trimToNull(physical.city()))
        .state(trimToNull(StringUtils.upperCase(physical.state(), Locale.US)))
        .zip(trimToNull(physical.zip()))
        .build();
  }

  private static Coordinates coordinates(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (allBlank(vaFacility.attributes().lat(), vaFacility.attributes().lng())) {
      return null;
    }
    return Coordinates.builder()
        .latitude(vaFacility.attributes().lat())
        .longitude(vaFacility.attributes().lng())
        .build();
  }

  private static Boolean mobile(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null || vaFacility.attributes().mobile() == null) {
      return false;
    }
    return vaFacility.attributes().mobile();
  }

  private static String name(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    return trimToNull(vaFacility.attributes().name());
  }

  private static String phoneNumber(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    if (vaFacility.attributes().phone() == null) {
      return null;
    }
    return trimToNull(vaFacility.attributes().phone().main());
  }

  private static String website(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility.attributes() == null) {
      return null;
    }
    return trimToNull(vaFacility.attributes().website());
  }

  /** Check for Facility. */
  public Facility toFacility(VaFacilitiesResponse.Facility vaFacility) {
    if (vaFacility == null) {
      return null;
    }
    return Facility.builder()
        .id(trimToNull(vaFacility.id()))
        .name(name(vaFacility))
        .physicalAddress(address(vaFacility))
        .coordinates(coordinates(vaFacility))
        .phoneNumber(phoneNumber(vaFacility))
        .website(website(vaFacility))
        .mobile(mobile(vaFacility))
        .active(active(vaFacility))
        .build();
  }
}

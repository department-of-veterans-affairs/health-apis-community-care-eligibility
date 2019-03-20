package gov.va.api.health.healthwhere.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VaFacilityPhysicalAddress {
  private String zip;

  private String city;

  private String state;

  private String address_1;

  private String address_2;

  private String address_3;
}

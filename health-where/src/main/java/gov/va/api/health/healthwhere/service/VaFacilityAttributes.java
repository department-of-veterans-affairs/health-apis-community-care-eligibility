package gov.va.api.health.healthwhere.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VaFacilityAttributes {
  private String name;

  private VaFacilityAddress address;

  private VaFacilityPhone phone;

  private VaFacilityServices services;

  private VaFacilityServicesWaitTimes wait_times;
}

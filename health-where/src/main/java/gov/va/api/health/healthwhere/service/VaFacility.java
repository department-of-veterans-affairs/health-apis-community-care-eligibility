package gov.va.api.health.healthwhere.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VaFacility {
  private String id;

  private VaFacilityAttributes attributes;
}

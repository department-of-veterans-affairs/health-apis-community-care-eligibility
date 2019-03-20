package gov.va.api.health.healthwhere.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VaFacilitiesResponse {
  private List<VaFacility> data;

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class VaFacility {
    private String id;

    private VaFacilityAttributes attributes;
  }
}

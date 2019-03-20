package gov.va.api.health.healthwhere.service;

import java.util.ArrayList;
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

  public List<VaFacility> data() {
    if (data == null) {
      data = new ArrayList<>();
    }
    return data;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class VaFacility {
    private String id;

    private VaFacilityAttributes attributes;
  }
}

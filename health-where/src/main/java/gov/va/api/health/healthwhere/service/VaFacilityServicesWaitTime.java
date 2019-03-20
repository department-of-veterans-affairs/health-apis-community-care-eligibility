package gov.va.api.health.healthwhere.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VaFacilityServicesWaitTime {
  private String service;

  // TODO this isn't mapping correctly
  @JsonProperty("new")
  private int newPatient;

  private int established;
}

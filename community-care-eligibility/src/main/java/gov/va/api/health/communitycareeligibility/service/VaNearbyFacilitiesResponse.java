package gov.va.api.health.communitycareeligibility.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class VaNearbyFacilitiesResponse {
  private List<Facility> data;

  /** Lazy Getter. */
  public List<Facility> data() {
    if (data == null) {
      data = new ArrayList<>();
    }
    return data;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Facility {
    private String id;

    private Attributes attributes;

  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Attributes {
     @JsonProperty("min_time")
     Integer min;

     @JsonProperty("max_time")
     Integer max;
  }


}

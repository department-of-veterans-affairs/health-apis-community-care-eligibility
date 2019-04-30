package gov.va.api.health.communitycareeligibility.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
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
public final class BingResponse {
  private List<Resources> resourceSets;

  /** Lazy getter. */
  public List<Resources> resourceSets() {
    if (resourceSets == null) {
      resourceSets = new ArrayList<>();
    }
    return resourceSets;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Point {
    String type;

    List<Double> coordinates;

    /** Lazy getter. */
    public List<Double> coordinates() {
      if (coordinates == null) {
        coordinates = new ArrayList<>();
      }
      return coordinates;
    }
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Resource {
    String name;

    Point point;

    Integer travelDuration;

    // Integer travelDurationTraffic;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Resources {
    private List<Resource> resources;

    /** Lazy getter. */
    public List<Resource> resources() {
      if (resources == null) {
        resources = new ArrayList<>();
      }
      return resources;
    }
  }
}

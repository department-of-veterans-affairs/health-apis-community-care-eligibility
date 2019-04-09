package gov.va.api.health.communitycareeligibility.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class BingRoutesResponse {
  private String authenticationResultCode;

  private String brandLogoUri;

  private String copyright;

  private List<ResourceSet> resourceSets;

  private int statusCode;

  private String statusDescription;

  private String traceId;

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Instruction {
    String formattedText;

    String maneuverType;

    String text;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class ItineraryDetail {
    int compassDegrees;

    List<Integer> endPathIndices;

    List<String> locationCodes;

    String maneuverType;

    String mode;

    List<String> names;

    String roadType;

    List<Integer> startPathIndices;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class ItineraryItem {
    String compassDirection;

    List<ItineraryDetail> details;

    String exit;

    // hints

    String iconType;

    Instruction instruction;

    boolean isRealTimeTransit;

    Point maneuverPoint;

    int realTimeTransitDelay;

    String sideOfStreet;

    String tollZone;

    String towardsRoadName;

    String transitTerminus;

    double travelDistance;

    int travelDuration;

    String travelMode;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Point {
    String type;

    List<Double> coordinates;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Resource {
    @JsonProperty("__type")
    String type;

    List<Double> bbox;

    String id;

    String distanceUnit;

    String durationUnit;

    List<RouteLeg> routeLegs;

    String trafficCongestion;

    String trafficDataUsed;

    Double travelDistance;

    int travelDuration;

    int travelDurationTraffic;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class ResourceSet {
    private int estimatedTotal;

    private List<Resource> resources;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class RouteLeg {
    Point actualEnd;

    Point actualStart;

    List<String> alternateVias;

    int cost;

    String description;

    List<ItineraryItem> itineraryItems;

    String routeRegion;

    List<RouteSubLeg> routeSubLegs;

    double travelDistance;

    double travelDuration;
  }

  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  // @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class RouteSubLeg {}
}

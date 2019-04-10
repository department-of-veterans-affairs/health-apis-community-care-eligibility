package gov.va.api.health.communitycareeligibility.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
@JsonAutoDetect(
  fieldVisibility = JsonAutoDetect.Visibility.ANY,
  isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
// @Schema(
//      description =
//
// "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html",
//      example = "SWAGGER_EXAMPLE_ALLERGY_INTOLERANCE"
//    )
public final class CommunityCareEligibilityResponse {
  boolean communityCareEligible;

  List<Facility> facilities;

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Address {
    String street;

    String city;

    String state;

    String zip;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Coordinates {
    double latitude;

    double longitude;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Facility {
    String id;

    String name;

    Address address;

    Coordinates coordinates;

    String phoneNumber;

    WaitDays waitDays;

    int driveMinutes;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class WaitDays {
    int newPatient;

    int establishedPatient;
  }
}

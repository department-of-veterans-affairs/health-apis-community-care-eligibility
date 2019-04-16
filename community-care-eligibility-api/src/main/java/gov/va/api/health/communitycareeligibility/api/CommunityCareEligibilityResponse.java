package gov.va.api.health.communitycareeligibility.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.ArrayList;
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
  Boolean communityCareEligible;

  List<CommunityCareEligibilities> communityCareEligibilities;

  List<Facility> facilities;

  /** Javadoc. */
  public List<CommunityCareEligibilities> communityCareEligibilities() {
    if (communityCareEligibilities == null) {
      communityCareEligibilities = new ArrayList<>();
    }
    return communityCareEligibilities;
  }

  /** Lazy getter. */
  public List<Facility> facilities() {
    if (facilities == null) {
      facilities = new ArrayList<>();
    }
    return facilities;
  }

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
    Double latitude;

    Double longitude;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class CommunityCareEligibilities {
    String description;

    String effectiveDate;

    String code;
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

    Integer driveMinutes;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class WaitDays {
    Integer newPatient;

    Integer establishedPatient;
  }
}

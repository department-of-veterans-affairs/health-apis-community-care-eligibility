package gov.va.api.health.communitycareeligibility.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
public final class CommunityCareEligibilityResponse {

  PatientRequest patientRequest;

  CommunityCareEligibility communityCareEligibilities;

  List<Facility> facilities;

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
  public static final class CommunityCareEligibility {

    Boolean eligible;

    List<EligibilityCodes> eligibilityCodes;

    List<String> facilities;

    /** Lazy getter. */
    public List<EligibilityCodes> eligibilityCodes() {
      if (eligibilityCodes == null) {
        eligibilityCodes = new ArrayList<>();
      }
      return eligibilityCodes;
    }

    /** Lazy getter. */
    public List<String> facilities() {
      if (facilities == null) {
        facilities = new ArrayList<>();
      }
      return facilities;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class EligibilityCodes {

    String description;

    String code;
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
  public static final class PatientRequest {

    String patientIcn;

    Address patientAddress;

    Coordinates patientCoordinates;

    String serviceType;

    Boolean establishedPatient;

    Instant timeStamp;

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

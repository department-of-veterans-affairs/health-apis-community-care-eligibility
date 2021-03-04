package gov.va.api.health.communitycareeligibility.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
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
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@Schema(
    example =
        "${cce.communityCareEligibilityResponse:"
            + "gov.va.api.health.communitycareeligibility.swaggerexamples."
            + "SwaggerCommunityCareEligibilityResponse#communityCareEligibilityResponse}")
public final class CommunityCareEligibilityResponse {
  PatientRequest patientRequest;

  List<EligibilityCode> eligibilityCodes;

  Boolean grandfathered;

  Boolean noFullServiceVaMedicalFacility;

  Address patientAddress;

  Coordinates patientCoordinates;

  List<Facility> nearbyFacilities;

  Boolean eligible;

  ProcessingStatus processingStatus;

  String pactStatus;

  /** Lazy getter. */
  public List<EligibilityCode> eligibilityCodes() {
    if (eligibilityCodes == null) {
      eligibilityCodes = new ArrayList<>();
    }
    return eligibilityCodes;
  }

  /** Lazy getter. */
  public List<Facility> nearbyFacilities() {
    if (nearbyFacilities == null) {
      nearbyFacilities = new ArrayList<>();
    }
    return nearbyFacilities;
  }

  public enum ProcessingStatus {
    successful,
    @JsonProperty("geocoding-not-available")
    geocoding_not_available,
    @JsonProperty("geocoding-out-of-date")
    geocoding_out_of_date,
    @JsonProperty("geocoding-incomplete")
    geocoding_incomplete
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Address {
    String country;

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
    BigDecimal latitude;

    BigDecimal longitude;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DriveMinutes {
    Integer min;

    Integer max;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class EligibilityCode {
    String description;

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

    Address physicalAddress;

    Coordinates coordinates;

    DriveMinutes driveMinutes;

    String phoneNumber;

    String website;

    Boolean mobile;

    Boolean active;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class PatientRequest {
    String patientIcn;

    String serviceType;

    Integer extendedDriveMin;

    String timestamp;
  }
}

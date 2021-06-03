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

/** Defines response from Community Care Eligibility. */
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

  /**
   * The status result of a CCE process, which can be successful, geocoding not available, geocoding
   * out of date, or geocoding incomplete.
   */
  public enum ProcessingStatus {
    successful,
    @JsonProperty("geocoding-not-available")
    geocoding_not_available,
    @JsonProperty("geocoding-out-of-date")
    geocoding_out_of_date,
    @JsonProperty("geocoding-incomplete")
    geocoding_incomplete
  }

  /** Address object used for CCE response. */
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

  /** Coordinate object used for CCE response. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Coordinates {
    BigDecimal latitude;

    BigDecimal longitude;
  }

  /** Drive minutes object used for CCE response. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DriveMinutes {
    Integer min;

    Integer max;
  }

  /** Eligibility Code object used for CCE response. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class EligibilityCode {
    String description;

    String code;
  }

  /** Facility object used for CCE response. */
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

  /** Patient Request object used for CCE response. */
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

package gov.va.api.health.communitycareeligibility.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(example = "SWAGGER_EXAMPLE_COMMUNITY_CARE_ELIGIBILITY_RESPONSE")
public final class CommunityCareEligibilityResponse {

  PatientRequest patientRequest;

  List<EligibilityCode> eligibilityCodes;

  Boolean grandfathered;

  Boolean noFullServiceVaMedicalFacility;

  List<Facility> facilities;

  List<String> accessStandardsFacilities;

  Boolean eligible;

  /** Lazy getter. */
  public List<String> accessStandardFacilities() {
    if (accessStandardsFacilities == null) {
      accessStandardsFacilities = new ArrayList<>();
    }
    return accessStandardsFacilities;
  }

  /** Lazy getter. */
  public List<EligibilityCode> eligibilityCode() {
    if (eligibilityCodes == null) {
      eligibilityCodes = new ArrayList<>();
    }
    return eligibilityCodes;
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

    String phoneNumber;

    String website;

    Integer waitDays;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class PatientRequest {

    String patientIcn;

    Address patientAddress;

    String serviceType;

    String timestamp;
  }
}

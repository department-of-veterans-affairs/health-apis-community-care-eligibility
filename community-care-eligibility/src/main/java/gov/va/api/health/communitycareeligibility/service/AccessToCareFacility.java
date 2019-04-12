package gov.va.api.health.communitycareeligibility.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public final class AccessToCareFacility {
  @JsonProperty("facilityID")
  private String facilityId;

  @JsonProperty("VISN")
  private Integer visn;

  private String name;

  private Integer type;

  private String address;

  private String city;

  private String state;

  private String zip;

  private String phone;

  private String fax;

  private String url;

  private Double latitude;

  private Double longitude;

  @JsonProperty("ApptTypeName")
  private String apptTypeName;

  private Double estWaitTime;

  private Double newWaitTime;

  private String sliceEndDate;

  @JsonProperty("ED")
  private Boolean ed;

  @JsonProperty("UC")
  private Boolean uc;

  @JsonProperty("SameDayPC")
  private Boolean sameDayPc;

  @JsonProperty("SameDayMH")
  private Boolean sameDayMh;

  @JsonProperty("WalkInPC")
  private Boolean walkInPc;

  @JsonProperty("WalkInMH")
  private Boolean walkInMh;

  @JsonProperty("TelehealthPC")
  private Boolean telehealthPc;

  @JsonProperty("TelehealthMH")
  private Boolean telehealthMh;

  private Integer distance;
}

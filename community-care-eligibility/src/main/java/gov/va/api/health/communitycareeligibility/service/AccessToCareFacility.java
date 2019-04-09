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
  private int visn;

  private String name;

  private int type;

  private String address;

  private String city;

  private String state;

  private String zip;

  private String phone;

  private String fax;

  private String url;

  private double latitude;

  private double longitude;

  @JsonProperty("ApptTypeName")
  private String apptTypeName;

  private double estWaitTime;

  private double newWaitTime;

  private String sliceEndDate;

  @JsonProperty("ED")
  private boolean ed;

  @JsonProperty("UC")
  private boolean uc;

  @JsonProperty("SameDayPC")
  private boolean sameDayPc;

  @JsonProperty("SameDayMH")
  private boolean sameDayMh;

  @JsonProperty("WalkInPC")
  private boolean walkInPc;

  @JsonProperty("WalkInMH")
  private boolean walkInMh;

  @JsonProperty("TelehealthPC")
  private boolean telehealthPc;

  @JsonProperty("TelehealthMH")
  private boolean telehealthMh;

  private int distance;
}

package gov.va.api.health.communitycareeligibility.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class VaFacilitiesResponse {
  private List<Facility> data;

  /** Lazy Getter. */
  public List<Facility> data() {
    if (data == null) {
      data = new ArrayList<>();
    }
    return data;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Address {
    private PhysicalAddress physical;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Attributes {
    private String name;

    private BigDecimal lat;

    private BigDecimal lng;

    private Address address;

    private Phone phone;

    private String website;

    private Boolean mobile;

    @JsonProperty("active_status")
    private String active;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Facility {
    private String id;

    private Attributes attributes;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Phone {
    private String main;
  }

  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class PhysicalAddress {
    private String zip;

    private String city;

    private String state;

    @JsonProperty("address_1")
    private String address1;

    @JsonProperty("address_2")
    private String address2;

    @JsonProperty("address_3")
    private String address3;
  }
}

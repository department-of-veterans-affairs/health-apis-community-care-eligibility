package gov.va.api.health.healthwhere.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccessToCareFacility {
  private String facilityID;

  private String name;

  private String address;

  private String city;

  private String state;

  private String zip;

  private String phone;

  private double latitude;

  private double longitude;

  private double estWaitTime;

  private double newWaitTime;
}

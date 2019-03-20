package gov.va.api.health.healthwhere.service;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VaFacilityAttributes {
  private String name;

  private double lat;

  private double longg;

  private Address address;

  private Phone phone;

  private WaitTimes wait_times;

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Address {
    private PhysicalAddress physical;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Phone {
    private String main;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class PhysicalAddress {
    private String zip;

    private String city;

    private String state;

    private String address_1;

    private String address_2;

    private String address_3;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class WaitTime {
    private String service;

    private int neww;

    private int established;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class WaitTimes {
    private List<WaitTime> health;

    public List<WaitTime> health() {
      if (health == null) {
        health = new ArrayList<>();
      }
      return health;
    }
  }
}

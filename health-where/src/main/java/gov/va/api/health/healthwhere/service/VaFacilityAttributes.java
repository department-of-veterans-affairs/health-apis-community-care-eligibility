package gov.va.api.health.healthwhere.service;

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

  private Address address;

  private Phone phone;

  private Services services;

  private WaitTimes wait_times;

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class Address {
    private PhysicalAddress physical;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class Phone {
    private String main;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class PhysicalAddress {
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
  private static final class Services {
    private List<String> health;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class WaitTime {
    private String service;

    private int neww;

    private int established;
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class WaitTimes {
    private List<WaitTime> health;
  }
}

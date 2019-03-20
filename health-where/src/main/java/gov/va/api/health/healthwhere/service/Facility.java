package gov.va.api.health.healthwhere.service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Facility {

  @NotBlank String id;

  @NotBlank String name;

  @NotNull Address address;

  String phoneNumber;

  WaitDays waitDays;

  int driveMinutes;

}

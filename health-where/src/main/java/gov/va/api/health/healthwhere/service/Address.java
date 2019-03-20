package gov.va.api.health.healthwhere.service;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Address {

  @NotBlank String street;
  @NotBlank String city;
  @NotBlank String state;
  @NotBlank String zip;

}

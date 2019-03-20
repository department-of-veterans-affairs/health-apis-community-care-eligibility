package gov.va.api.health.healthwhere.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Address {

  @NotBlank String street;
  @NotBlank String city;
  @NotBlank String state;
  @NotBlank String zip;
}

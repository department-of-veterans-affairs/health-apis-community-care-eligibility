package gov.va.api.health.healthwhere.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WaitDays {

  int newPatient;
  int establishedPatient;

}

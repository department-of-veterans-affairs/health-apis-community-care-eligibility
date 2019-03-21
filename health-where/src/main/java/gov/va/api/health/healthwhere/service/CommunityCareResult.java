package gov.va.api.health.healthwhere.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CommunityCareResult {
  boolean communityCareEligible;
  List<Facility> facilities;
}

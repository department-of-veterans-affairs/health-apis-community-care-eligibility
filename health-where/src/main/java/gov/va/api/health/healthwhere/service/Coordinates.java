package gov.va.api.health.healthwhere.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Coordinates {
  double latitude;
  double longitude;

  public String toCoordinateString() {
    return "" + latitude + "," + longitude;
  }
}

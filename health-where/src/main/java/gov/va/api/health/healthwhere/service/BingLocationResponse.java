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
public class BingLocationResponse {

  private List<BingResourceSet> resourceSets;

  public Coordinates getBingResourceCoordinates() {

    //TODO: Add error checking
    Coordinates coordinates = new Coordinates(
        resourceSets.get(0).resources().get(0).point.coordinates[0],
        resourceSets.get(0).resources().get(0).point.coordinates[1]
    );

    return coordinates;
  }
}

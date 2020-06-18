package gov.va.api.health.communitycareeligibility.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;

public class RoundTrip {
  @SneakyThrows
  static void assertRoundTrip(Object object) {
    ObjectMapper mapper = new JacksonConfig().objectMapper();
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    Object evilTwin = mapper.readValue(json, object.getClass());
    assertEquals(evilTwin, object);
  }
}

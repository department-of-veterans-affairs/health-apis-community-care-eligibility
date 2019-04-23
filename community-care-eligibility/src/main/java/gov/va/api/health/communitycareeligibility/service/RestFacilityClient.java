package gov.va.api.health.communitycareeligibility.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import java.util.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class RestFacilityClient implements FacilityClient {

  private final String vaFacilitiesApiKey;

  private final RestTemplate restTemplate;

  /** Autowired constructor. */
  public RestFacilityClient(
      @Value("${va-facilities.api-key}") String vaFacilitiesApiKey,
      @Autowired RestTemplate restTemplate) {
    this.vaFacilitiesApiKey = vaFacilitiesApiKey;
    this.restTemplate = restTemplate;
  }

  /**
   * To support deserialization, recursively visit all descendant nodes and, for any nodes with a
   * bad key (e.g. "new"), add a good key (e.g. "neww") alongside.
   */
  private static void doHackForFieldRenaming(JsonNode node, String badName, String goodName) {
    if (node instanceof ObjectNode) {
      ObjectNode objNode = (ObjectNode) node;
      JsonNode newNode = node.get(badName);
      if (newNode != null) {
        objNode.set(goodName, newNode);
      }
    }
    for (JsonNode child : node) {
      doHackForFieldRenaming(child, badName, goodName);
    }
  }

  /** Ignore Unknown Properties When Deserialization. */
  public static ObjectMapper objectMapper() {
    return JacksonConfig.createMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  /** Javadoc PlaceHolder. */
  @SneakyThrows
  public VaFacilitiesResponse facilities(Coordinates coordinates, String serviceType) {
    String url =
        UriComponentsBuilder.fromHttpUrl(
                "https://dev-api.va.gov/services/va_facilities/v0/facilities")
            .queryParam("lat", coordinates.latitude())
            .queryParam("long", coordinates.longitude())
            .queryParam("type", "health")
            .queryParam("page", 1)
            .queryParam("per_page", 30)
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.add("apiKey", vaFacilitiesApiKey);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "va facilities api response: "
            + objectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper().readTree(body)));
    JsonNode root = objectMapper().readTree(body);
    doHackForFieldRenaming(root, "new", "neww");
    doHackForFieldRenaming(root, "long", "longg");
    VaFacilitiesResponse responseObject =
        objectMapper()
            .readValue(objectMapper().writeValueAsString(root), VaFacilitiesResponse.class);
    log.error(
        "va facilities response object: "
            + objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(responseObject));
    return responseObject;
  }
}

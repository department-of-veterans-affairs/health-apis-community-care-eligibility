package gov.va.api.health.healthwhere.service.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.healthwhere.service.Address;
import gov.va.api.health.healthwhere.service.BingResponse;
import gov.va.api.health.healthwhere.service.Coordinates;
import gov.va.api.health.healthwhere.service.Facility;
import gov.va.api.health.healthwhere.service.VaFacilitiesResponse;
import gov.va.api.health.healthwhere.service.WaitDays;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Controller
public class HomeController {

  private final RestTemplate restTemplate;

  private String bingApiKey;

  private String vaFacilitiesApiKey;

  public HomeController(
      @Value("${bing.api-key}") String bingApiKey,
      @Value("${va-facilities.api-key}") String vaFacilitiesApiKey,
      @Autowired RestTemplate restTemplate) {
    this.bingApiKey = bingApiKey;
    this.vaFacilitiesApiKey = vaFacilitiesApiKey;
    this.restTemplate = restTemplate;
  }

  private static List<Facility> generateTestFacilities() {
    Address addressOne =
        new Address("50 Irving Street, Northwest", "Washington", "DC", "20422-0001");
    WaitDays waitOne = new WaitDays(23, 2);
    Facility facilityOne =
        new Facility(
            "vha_688", "Washington VA Medical Center", addressOne, "202-745-8000", waitOne, 42);
    return Collections.singletonList(facilityOne);
  }

  @SneakyThrows
  private BingResponse bingDrivetimeSearch() {
    String url =
        UriComponentsBuilder.fromHttpUrl("http://dev.virtualearth.net/REST/V1/Routes")
            .queryParam("wp.0", "38.9311450072647,-77.010835000092")
            .queryParam("wp.1", "38.7048241100001,-77.14011033")
            .queryParam("key", bingApiKey)
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ObjectMapper objectMapper =
        JacksonConfig.createMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "Bing API response: "
            + objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(body)));
    BingResponse responseObject = objectMapper.readValue(body, BingResponse.class);
    log.error(
        "response object: "
            + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseObject));
    return responseObject;
  }

  @SneakyThrows
  private BingResponse bingLocationSearch(Address address) {
    String url =
        UriComponentsBuilder.fromHttpUrl("http://dev.virtualearth.net/REST/v1/Locations")
            .queryParam("countryRegion", "US")
            .queryParam("adminDistrict", address.state())
            .queryParam("locality", address.city())
            .queryParam("postalCode", address.zip())
            .queryParam("addressLine", address.street())
            .queryParam("maxResults", 1)
            .queryParam("key", bingApiKey)
            .build()
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ObjectMapper objectMapper =
        JacksonConfig.createMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "Bing API response: "
            + objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(body)));
    BingResponse responseObject = objectMapper.readValue(body, BingResponse.class);
    log.error(
        "response object: "
            + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseObject));
    return responseObject;
  }

  /**
   * To support deserialization, recursively visit all descendant nodes and, for any nodes with a
   * "new" key, add a "neww" key alongside.
   */
  private void doHackForFieldsNamedNew(JsonNode node) {
    if (node instanceof ObjectNode) {
      ObjectNode objNode = (ObjectNode) node;
      JsonNode newNode = node.get("new");
      if (newNode != null) {
        objNode.set("neww", newNode);
      }
    }
    for (JsonNode child : node) {
      doHackForFieldsNamedNew(child);
    }
  }

  private Coordinates getBingResourceCoordinates(BingResponse bingResponse) {
    // TODO: Add error checking, convert to lamda
    Coordinates coordinates =
        new Coordinates(
            bingResponse.resourceSets().get(0).resources().get(0).point().coordinates()[0],
            bingResponse.resourceSets().get(0).resources().get(0).point().coordinates()[1]);
    return coordinates;
  }

  /** Search by address and service type. */
  @GetMapping(value = {"/search"})
  @ResponseBody
  public List<Facility> search(
      @RequestParam(value = "street") String street,
      @RequestParam(value = "city") String city,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "zip") String zip,
      @RequestParam(value = "serviceType") String serviceType) {
    Address patientAddress = new Address(street, city, state, zip);
    bingDrivetimeSearch().resourceSets().get(0).resources().get(0).travelDuration();
    BingResponse bingResponse = bingLocationSearch(patientAddress);
    Coordinates patientCoordinates = getBingResourceCoordinates(bingResponse);
    VaFacilitiesResponse vaFacilitiesResponse = vaFacilitySearch(patientCoordinates, serviceType);
    return generateTestFacilities();
  }

  @SneakyThrows
  private VaFacilitiesResponse vaFacilitySearch(Coordinates coordinates, String serviceType) {
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
    ObjectMapper objectMapper =
        JacksonConfig.createMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "va facilities api response: "
            + objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(body)));
    JsonNode root = objectMapper.readTree(body);
    doHackForFieldsNamedNew(root);
    VaFacilitiesResponse responseObject =
        objectMapper.readValue(objectMapper.writeValueAsString(root), VaFacilitiesResponse.class);
    log.error(
        "va facilities response object: "
            + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseObject));
    return responseObject;
  }
}

package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Facility;
import gov.va.api.health.communitycareeligibility.service.BingResponse.Point;
import java.util.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class RestBingMapsClient implements BingMapsClient {
  private final String bingApiKey;

  private final RestTemplate restTemplate;

  public RestBingMapsClient(
      @Value("${bing-maps.api-key}") String bingApiKey, @Autowired RestTemplate restTemplate) {
    this.bingApiKey = bingApiKey;
    this.restTemplate = restTemplate;
  }

  private static String coordinateParam(Coordinates coordinates) {
    return coordinates.latitude() + "," + coordinates.longitude();
  }

  private static HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    return headers;
  }

  @SneakyThrows
  private Coordinates coordinates(Address address) {
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
    BingResponse responseObject =
        restTemplate
            .exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), BingResponse.class)
            .getBody();
    log.error(
        "response object: "
            + JacksonConfig.createMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseObject));
    if (responseObject == null) {
      throw new IllegalStateException();
    }
    Point point = responseObject.resourceSets().get(0).resources().get(0).point();
    return Coordinates.builder()
        .latitude(point.coordinates().get(0))
        .longitude(point.coordinates().get(1))
        .build();
  }

  @Override
  @SneakyThrows
  public BingResponse routes(Address patientAddress, Facility facility) {
    Coordinates patientCoordinates = coordinates(patientAddress);
    String url =
        UriComponentsBuilder.fromHttpUrl("http://dev.virtualearth.net/REST/V1/Routes")
            .queryParam("wp.0", coordinateParam(patientCoordinates))
            .queryParam("wp.1", coordinateParam(facility.coordinates()))
            .queryParam("key", bingApiKey)
            .toUriString();
    BingResponse responseObject =
        restTemplate
            .exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), BingResponse.class)
            .getBody();
    log.error(
        "response object: "
            + JacksonConfig.createMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseObject));
    if (responseObject == null) {
      throw new IllegalStateException();
    }
    return responseObject;
  }
}

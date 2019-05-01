package gov.va.api.health.communitycareeligibility.service;

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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class RestFacilitiesClient implements FacilitiesClient {
  private final String vaFacilitiesApiKey;

  private final String baseUrl;

  private final RestTemplate restTemplate;

  /** Autowired constructor. */
  public RestFacilitiesClient(
      @Value("${va-facilities.api-key}") String vaFacilitiesApiKey,
      @Value("${va-facilities.url}") String baseUrl,
      @Autowired RestTemplate restTemplate) {
    this.vaFacilitiesApiKey = vaFacilitiesApiKey;
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  @SneakyThrows
  public VaFacilitiesResponse facilities(Coordinates coordinates) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .queryParam("lat", coordinates.latitude())
            .queryParam("long", coordinates.longitude())
            .queryParam("type", "health")
            .queryParam("page", 1)
            .queryParam("per_page", 30)
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.add("apiKey", vaFacilitiesApiKey);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    VaFacilitiesResponse responseObject =
        restTemplate
            .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), VaFacilitiesResponse.class)
            .getBody();
    log.info("Va Facilities Response" + responseObject);
    if (responseObject == null) {
      throw new IllegalStateException();
    }
    return responseObject;
  }
}

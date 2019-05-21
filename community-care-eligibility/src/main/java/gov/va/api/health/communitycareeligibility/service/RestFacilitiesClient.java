package gov.va.api.health.communitycareeligibility.service;

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
  public VaFacilitiesResponse facilities(String state) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .queryParam("state", state)
            .queryParam("type", "health")
            .queryParam("page", 1)
            .queryParam("per_page", 500)
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.add("apiKey", vaFacilitiesApiKey);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    VaFacilitiesResponse responseObject;
    try {
      responseObject =
          restTemplate
              .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), VaFacilitiesResponse.class)
              .getBody();
    } catch (Exception e) {
      throw new Exceptions.FacilitiesUnavailableException(e);
    }
    log.info("Va Facilities Response" + responseObject);
    return responseObject;
  }
}

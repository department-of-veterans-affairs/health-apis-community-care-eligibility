package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import java.util.Collections;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.restTemplate = restTemplate;
  }

  @Override
  @SneakyThrows
  public VaFacilitiesResponse nearbyFacilities(Address address, int driveMins, String serviceType) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "v1/nearby")
            .queryParam("state", address.state())
            .queryParam("city", address.city())
            .queryParam("street_address", address.street())
            .queryParam("zip", address.zip())
            .queryParam("drive_time", driveMins)
            .queryParam("type", "health")
            .queryParam("services[]", serviceType)
            .queryParam("page", 1)
            .queryParam("per_page", 500)
            .build()
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.add("apiKey", vaFacilitiesApiKey);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    try {
      VaFacilitiesResponse responseObject =
          restTemplate
              .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), VaFacilitiesResponse.class)
              .getBody();
      return responseObject;
    } catch (Exception e) {
      throw new Exceptions.FacilitiesUnavailableException(e);
    }
  }
}

package gov.va.api.health.healthwhere.service;

import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BingClient {

  private final RestTemplate restTemplate;

  @NotNull String bingUrl;
  @NotNull String bingKey;
  @NotNull int maxResults = 1;

  public BingClient(
      @Value("${bing.url}") String bingUrl,
      @Value("${bing.key}") String bingKey,
      @Autowired RestTemplate restTemplate) {

    this.bingUrl = bingUrl;
    this.bingKey = bingKey;
    this.restTemplate = restTemplate;
  }

  public BingLocationResponse lookupAddress(Address address) {

    

    return null;
  }
}

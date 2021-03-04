package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.PcmmResponse;
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
public class RestPcmmClient implements PcmmClient {
  private static final String PCMM_URL_SUFFIX = "pcmmr_web/ws/patientSummary/icn/";

  private final String pcmmUsername;

  private final String pcmmPassword;

  private final String baseUrl;

  private final RestTemplate restTemplate;

  /** Autowired constructor. */
  public RestPcmmClient(
      @Value("${pcmm.username}") String pcmmUsername,
      @Value("${pcmm.password}") String pcmmPassword,
      @Value("${pcmm.url}") String baseUrl,
      @Autowired RestTemplate restTemplate) {
    this.pcmmUsername = pcmmUsername;
    this.pcmmPassword = pcmmPassword;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.restTemplate = restTemplate;
  }

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    // todo how to add password
    headers.add("todo", pcmmUsername + ":" + pcmmPassword);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    return headers;
  }

  @Override
  @SneakyThrows
  public PcmmResponse pactStatusByIcn(String patientIcn) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + PCMM_URL_SUFFIX + patientIcn)
            .build()
            .toUriString();
    try {
      return restTemplate
          .exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), PcmmResponse.class)
          .getBody();
    } catch (Exception e) {
      throw new Exceptions.PcmmUnavailableException(e);
    }
  }
}

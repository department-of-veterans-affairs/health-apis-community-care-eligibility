package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.PcmmResponse;
import java.nio.charset.Charset;
import java.util.Base64;
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
      @Value("${pcmm.header.username}") String pcmmUsername,
      @Value("${pcmm.header.password}") String pcmmPassword,
      @Value("${pcmm.endpoint.url}") String baseUrl,
      @Autowired RestTemplate restTemplate) {
    this.pcmmUsername = pcmmUsername;
    this.pcmmPassword = pcmmPassword;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.restTemplate = restTemplate;
  }

  @SneakyThrows
  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    String base64Credentials =
        Base64.getEncoder()
            .encodeToString((pcmmUsername + ":" + pcmmPassword).getBytes(Charset.defaultCharset()));
    headers.add("Authorization", "Basic " + base64Credentials);
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

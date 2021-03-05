package gov.va.api.health.communitycareeligibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.api.PcmmResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestPcmmClientTest {

  @Test
  @SuppressWarnings("unchecked")
  public void pactStatusByIcn() {
    ResponseEntity<PcmmResponse> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(PcmmResponse.builder().build());

    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq("http://foo/bar/v0/pcmmr_web/ws/patientSummary/icn/123"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(PcmmResponse.class)))
        .thenReturn(response);

    RestPcmmClient client = new RestPcmmClient("user", "pass", "http://foo/bar/v0/", restTemplate);

    assertThat(client.pactStatusByIcn("123")).isEqualTo(PcmmResponse.builder().build());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void pactStatusByIcnException() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq("http://foo/bar/v0/pcmmr_web/ws/patientSummary/icn/123"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(PcmmResponse.class)))
        .thenThrow(new RuntimeException());

    RestPcmmClient client = new RestPcmmClient("user", "pass", "http://foo/bar/v0/", restTemplate);

    assertThrows(Exceptions.PcmmUnavailableException.class, () -> client.pactStatusByIcn("123"));
  }
}

package gov.va.api.health.communitycareeligibility.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.service.RestFacilitiesClient;
import gov.va.api.health.communitycareeligibility.service.VaFacilitiesResponse;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestFacilitiesClientTest {

  @Test
  @SuppressWarnings("unchecked")
  public void facilities() {
    ResponseEntity<VaFacilitiesResponse> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(VaFacilitiesResponse.builder().build());

    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq("https://foo/bar?state=FL&type=health&page=1&per_page=500"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(VaFacilitiesResponse.class)))
        .thenReturn(response);

    RestFacilitiesClient client =
        new RestFacilitiesClient("fakeApiKey", "https://foo/bar", restTemplate);
    assertThat(client.facilities("FL")).isEqualTo(VaFacilitiesResponse.builder().build());
  }
}

package gov.va.api.health.communitycareeligibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import java.math.BigDecimal;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestFacilitiesClientTest {

  @Test
  @SuppressWarnings("unchecked")
  public void nearbyFacilities() {
    ResponseEntity<VaFacilitiesResponse> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(VaFacilitiesResponse.builder().build());

    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq(
                "http://foo/bar/v1/nearby?lat=0&lng=0&drive_time=30&type=health&services[]=PrimaryCare&page=1&per_page=500"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(VaFacilitiesResponse.class)))
        .thenReturn(response);

    RestFacilitiesClient client =
        new RestFacilitiesClient("fakeApiKey", "http://foo/bar", restTemplate);
    assertThat(
            client.nearbyFacilities(
                CommunityCareEligibilityResponse.Coordinates.builder()
                    .latitude(BigDecimal.ZERO)
                    .longitude(BigDecimal.ZERO)
                    .build(),
                30,
                "PrimaryCare"))
        .isEqualTo(VaFacilitiesResponse.builder().build());
  }
}

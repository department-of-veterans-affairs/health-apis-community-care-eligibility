package gov.va.api.health.communitycareeligibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
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
  public void facilitiesById() {
    ResponseEntity<VaFacilitiesResponse> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(VaFacilitiesResponse.builder().build());
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq("http://foo/bar/v0/facilities?ids=vha_675GD"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(VaFacilitiesResponse.class)))
        .thenReturn(response);
    RestFacilitiesClient client =
        new RestFacilitiesClient("fakeApiKey", "http://foo/bar", restTemplate);
    assertThat(client.facilitiesByIds(asList("vha_675GD")))
        .isEqualTo(VaFacilitiesResponse.builder().build());
  }

  @SuppressWarnings("unchecked")
  @Test(expected = Exceptions.FacilitiesUnavailableException.class)
  public void facilitiesByIdException() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq("http://foo/bar/v0/facilities?ids=vha_675GD"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(VaFacilitiesResponse.class)))
        .thenThrow(new RuntimeException());
    RestFacilitiesClient client =
        new RestFacilitiesClient("fakeApiKey", "http://foo/bar", restTemplate);
    client.facilitiesByIds(asList("vha_675GD"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void nearbyFacilities() {
    ResponseEntity<VaNearbyFacilitiesResponse> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(VaNearbyFacilitiesResponse.builder().build());
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq(
                "http://foo/bar/v0/nearby?lat=0&lng=0&drive_time=30&type=health&services[]=PrimaryCare&page=1&per_page=500"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(VaNearbyFacilitiesResponse.class)))
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
        .isEqualTo(VaNearbyFacilitiesResponse.builder().build());
  }

  @SuppressWarnings("unchecked")
  @Test(expected = Exceptions.FacilitiesUnavailableException.class)
  public void nearbyFacilitiesException() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq(
                "http://foo/bar/v0/nearby?lat=0&lng=0&drive_time=30&type=health&services[]=PrimaryCare&page=1&per_page=500"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(VaNearbyFacilitiesResponse.class)))
        .thenThrow(new RuntimeException());
    RestFacilitiesClient client =
        new RestFacilitiesClient("fakeApiKey", "http://foo/bar", restTemplate);
    client.nearbyFacilities(
        CommunityCareEligibilityResponse.Coordinates.builder()
            .latitude(BigDecimal.ZERO)
            .longitude(BigDecimal.ZERO)
            .build(),
        30,
        "PrimaryCare");
  }
}

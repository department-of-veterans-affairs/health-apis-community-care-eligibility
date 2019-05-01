package gov.va.api.health.communitycareeligibility.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.service.BingResponse;
import gov.va.api.health.communitycareeligibility.service.RestBingMapsClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestBingMapsClientTest {

  @Test
  @SuppressWarnings("unchecked")
  public void coordinates() {
    ResponseEntity<BingResponse> response = mock(ResponseEntity.class);
    List<Double> coordinates = new ArrayList<>();
    coordinates.add(1.0);
    coordinates.add(1.0);
    when(response.getBody())
        .thenReturn(
            BingResponse.builder()
                .resourceSets(
                    Collections.singletonList(
                        BingResponse.Resources.builder()
                            .resources(
                                Collections.singletonList(
                                    BingResponse.Resource.builder()
                                        .point(
                                            BingResponse.Point.builder()
                                                .coordinates(coordinates)
                                                .build())
                                        .build()))
                            .build()))
                .build());

    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq(
                "http://dev.virtualearth.net/REST/v1/Locations?countryRegion=US&adminDistrict=FL&locality=Melbourne&postalCode=32927&addressLine=505 North johns&maxResults=1&key=bingApiKey"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BingResponse.class)))
        .thenReturn(response);

    RestBingMapsClient client = new RestBingMapsClient("bingApiKey", restTemplate);
    assertThat(
            client.coordinates(
                CommunityCareEligibilityResponse.Address.builder()
                    .street("505 North johns")
                    .state("FL")
                    .city("Melbourne")
                    .zip("32927")
                    .build()))
        .isEqualTo(
            CommunityCareEligibilityResponse.Coordinates.builder()
                .longitude(1.0)
                .latitude(1.0)
                .build());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void routes() {
    ResponseEntity<BingResponse> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(BingResponse.builder().build());

    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(
            eq("http://dev.virtualearth.net/REST/V1/Routes?wp.0=1.0,1.0&wp.1=2.0,2.0&key=key"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BingResponse.class)))
        .thenReturn(response);

    RestBingMapsClient client = new RestBingMapsClient("key", restTemplate);
    assertThat(
            client.routes(
                CommunityCareEligibilityResponse.Coordinates.builder()
                    .latitude(1.0)
                    .longitude(1.0)
                    .build(),
                CommunityCareEligibilityResponse.Coordinates.builder()
                    .longitude(2.0)
                    .latitude(2.0)
                    .build()))
        .isEqualTo(BingResponse.builder().build());
  }
}

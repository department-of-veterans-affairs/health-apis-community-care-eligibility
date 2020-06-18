package gov.va.api.health.communitycareeligibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HealthControllerTest {
  @Test
  public void eeDown() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility(any())).thenThrow(new RuntimeException("oh noez"));
    HealthController controller =
        HealthController.builder()
            .eeClient(eeClient)
            .facilitiesClient(mock(FacilitiesClient.class))
            .build();
    Instant time = Instant.now();
    assertThat(controller.health(time))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                    Health.status(new Status("DOWN", "Downstream services"))
                        .withDetail("name", "All downstream services")
                        .withDetail(
                            "downstreamServices",
                            List.of(
                                Health.status(new Status("DOWN", "E&E"))
                                    .withDetail("name", "E&E")
                                    .withDetail("statusCode", 503)
                                    .withDetail("status", HttpStatus.SERVICE_UNAVAILABLE)
                                    .withDetail("time", time)
                                    .build(),
                                Health.status(new Status("UP", "Facilities"))
                                    .withDetail("name", "Facilities")
                                    .withDetail("statusCode", 200)
                                    .withDetail("status", HttpStatus.OK)
                                    .withDetail("time", time)
                                    .build()))
                        .withDetail("time", time)
                        .build()));
  }

  @Test
  public void facilitiesDown() {
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilitiesByIds(any())).thenThrow(new RuntimeException("oh noez"));
    HealthController controller =
        HealthController.builder()
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .facilitiesClient(facilitiesClient)
            .build();
    Instant time = Instant.now();
    assertThat(controller.health(time))
        .isEqualTo(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                    Health.status(new Status("DOWN", "Downstream services"))
                        .withDetail("name", "All downstream services")
                        .withDetail(
                            "downstreamServices",
                            List.of(
                                Health.status(new Status("UP", "E&E"))
                                    .withDetail("name", "E&E")
                                    .withDetail("statusCode", 200)
                                    .withDetail("status", HttpStatus.OK)
                                    .withDetail("time", time)
                                    .build(),
                                Health.status(new Status("DOWN", "Facilities"))
                                    .withDetail("name", "Facilities")
                                    .withDetail("statusCode", 503)
                                    .withDetail("status", HttpStatus.SERVICE_UNAVAILABLE)
                                    .withDetail("time", time)
                                    .build()))
                        .withDetail("time", time)
                        .build()));
  }

  @Test
  public void up() {
    HealthController controller =
        HealthController.builder()
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .facilitiesClient(mock(FacilitiesClient.class))
            .build();
    Instant time = Instant.now();
    assertThat(controller.health(time))
        .isEqualTo(
            ResponseEntity.ok()
                .body(
                    Health.status(new Status("UP", "Downstream services"))
                        .withDetail("name", "All downstream services")
                        .withDetail(
                            "downstreamServices",
                            List.of(
                                Health.status(new Status("UP", "E&E"))
                                    .withDetail("name", "E&E")
                                    .withDetail("statusCode", 200)
                                    .withDetail("status", HttpStatus.OK)
                                    .withDetail("time", time)
                                    .build(),
                                Health.status(new Status("UP", "Facilities"))
                                    .withDetail("name", "Facilities")
                                    .withDetail("statusCode", 200)
                                    .withDetail("status", HttpStatus.OK)
                                    .withDetail("time", time)
                                    .build()))
                        .withDetail("time", time)
                        .build()));
  }
}

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
  private void checkResponse(
      ResponseEntity<Health> response, Instant callTime, List<HealthServices> downServices) {
    HttpStatus expectedStatus =
        downServices.isEmpty() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
    String expectedStatusCode = downServices.isEmpty() ? "UP" : "DOWN";
    assertThat(response)
        .isEqualTo(
            ResponseEntity.status(expectedStatus)
                .body(
                    Health.status(new Status(expectedStatusCode, "Downstream services"))
                        .withDetail("name", "All downstream services")
                        .withDetail(
                            "downstreamServices",
                            List.of(
                                makeHealth(
                                    "E&E", callTime, downServices.contains(HealthServices.EE)),
                                makeHealth(
                                    "Facilities",
                                    callTime,
                                    downServices.contains(HealthServices.FACILITIES)),
                                makeHealth(
                                    "PCMM", callTime, downServices.contains(HealthServices.PCMM))))
                        .withDetail("time", callTime)
                        .build()));
  }

  @Test
  public void eeDown() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility(any()))
        .thenThrow(
            new Exceptions.EeUnavailableException(new RuntimeException("E&E Service Exception")));
    HealthController controller =
        HealthController.builder()
            .eeClient(eeClient)
            .facilitiesClient(mock(FacilitiesClient.class))
            .pcmmClient(mock(PcmmClient.class))
            .build();
    Instant currentTime = Instant.now();
    checkResponse(controller.health(currentTime), currentTime, List.of(HealthServices.EE));
  }

  @Test
  public void eeUnknownIcn() {
    EligibilityAndEnrollmentClient eeClient = mock(EligibilityAndEnrollmentClient.class);
    when(eeClient.requestEligibility(any()))
        .thenThrow(
            new Exceptions.UnknownPatientIcnException(
                "000", new RuntimeException("E&E Service Exception")));
    HealthController controller =
        HealthController.builder()
            .eeClient(eeClient)
            .facilitiesClient(mock(FacilitiesClient.class))
            .pcmmClient(mock(PcmmClient.class))
            .build();
    Instant currentTime = Instant.now();
    checkResponse(controller.health(currentTime), currentTime, List.of());
  }

  @Test
  public void facilitiesDown() {
    FacilitiesClient facilitiesClient = mock(FacilitiesClient.class);
    when(facilitiesClient.facilitiesByIds(any()))
        .thenThrow(
            new Exceptions.FacilitiesUnavailableException(
                new RuntimeException("Facilities service exception")));
    HealthController controller =
        HealthController.builder()
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .facilitiesClient(facilitiesClient)
            .pcmmClient(mock(PcmmClient.class))
            .build();
    Instant currentTime = Instant.now();
    checkResponse(controller.health(currentTime), currentTime, List.of(HealthServices.FACILITIES));
  }

  private Health makeHealth(String name, Instant callTime, boolean isDown) {
    return Health.status(new Status(isDown ? "DOWN" : "UP", name))
        .withDetail("name", name)
        .withDetail("statusCode", isDown ? 503 : 200)
        .withDetail("status", isDown ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.OK)
        .withDetail("time", callTime)
        .build();
  }

  @Test
  public void pcmmDown() {
    PcmmClient pcmmClient = mock(PcmmClient.class);
    when(pcmmClient.pactStatusByIcn(any()))
        .thenThrow(
            new Exceptions.PcmmUnavailableException(
                new RuntimeException("PCMM Service Exception")));
    HealthController controller =
        HealthController.builder()
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .facilitiesClient(mock(FacilitiesClient.class))
            .pcmmClient(pcmmClient)
            .build();
    Instant currentTime = Instant.now();
    checkResponse(controller.health(currentTime), currentTime, List.of(HealthServices.PCMM));
  }

  @Test
  public void up() {
    HealthController controller =
        HealthController.builder()
            .eeClient(mock(EligibilityAndEnrollmentClient.class))
            .facilitiesClient(mock(FacilitiesClient.class))
            .pcmmClient(mock(PcmmClient.class))
            .build();
    Instant currentTime = Instant.now();
    checkResponse(controller.health(currentTime), currentTime, List.of());
  }

  private enum HealthServices {
    EE,
    FACILITIES,
    PCMM
  }
}

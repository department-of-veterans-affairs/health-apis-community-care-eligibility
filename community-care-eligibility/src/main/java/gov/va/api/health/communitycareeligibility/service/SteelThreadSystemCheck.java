package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Coordinates;
import java.math.BigDecimal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Component
@Slf4j
public class SteelThreadSystemCheck implements HealthIndicator {
  private static final Coordinates COORDINATES =
      Coordinates.builder()
          .latitude(new BigDecimal("28.112506"))
          .longitude(new BigDecimal("-80.7000423"))
          .build();

  private static final int DRIVE_MINS = 60;

  private static final String SERVICE_TYPE = "PrimaryCare";

  private final EligibilityAndEnrollmentClient eeClient;

  private final FacilitiesClient facilitiesClient;

  private final String icn;

  private final SteelThreadSystemCheckLedger ledger;

  private final int consecutiveFailureThreshold;

  /** 'By hand' all args constructor is required to inject non-string values from our properties. */
  public SteelThreadSystemCheck(
      @Autowired EligibilityAndEnrollmentClient eeClient,
      @Autowired FacilitiesClient facilitiesClient,
      @Value("${health-check.icn}") String icn,
      @Autowired SteelThreadSystemCheckLedger ledger,
      @Value("${health-check.consecutive-failure-threshold}") int consecutiveFailureThreshold) {
    this.eeClient = eeClient;
    this.facilitiesClient = facilitiesClient;
    this.icn = icn;
    this.ledger = ledger;
    this.consecutiveFailureThreshold = consecutiveFailureThreshold;
  }

  @Override
  @SneakyThrows
  public Health health() {
    if ("skip".equals(icn)) {
      return Health.up().withDetail("skipped", true).build();
    }

    // The count is read and stored for consistency because there is another thread writing it.
    int consecutiveFails = ledger.getConsecutiveFailureCount();

    if (consecutiveFails < consecutiveFailureThreshold) {
      return Health.up().build();
    }

    return Health.down()
        .withDetail(
            "failures",
            String.format(
                "Error threshold of %d hit with %d consecutive failure(s).",
                consecutiveFailureThreshold, consecutiveFails))
        .build();
  }

  /**
   * Asynchronously perform the steel thread read and save the results for health check to use.
   * Frequency is configurable via properties.
   */
  @Scheduled(
    fixedDelayString = "${health-check.frequency-ms}",
    initialDelayString = "${health-check.frequency-ms}"
  )
  @SneakyThrows
  public void runSteelThreadCheckAsynchronously() {
    if ("skip".equals(icn)) {
      return;
    }

    log.info("Performing health check.");

    try {
      eeClient.requestEligibility(icn);
      facilitiesClient.nearbyFacilities(COORDINATES, DRIVE_MINS, SERVICE_TYPE);
      ledger.recordSuccess();
    } catch (HttpServerErrorException
        | HttpClientErrorException
        | ResourceAccessException
        | Exceptions.EeUnavailableException
        | Exceptions.FacilitiesUnavailableException e) {
      int consecutiveFailures = ledger.recordFailure();
      log.error("Failed to complete health check. Failure count is " + consecutiveFailures);
    } catch (Exception e) {
      int consecutiveFailures = ledger.recordFailure();
      log.error("Failed to complete health check. Failure count is " + consecutiveFailures, e);
      throw e;
    }
  }
}

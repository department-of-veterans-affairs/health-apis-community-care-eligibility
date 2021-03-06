package gov.va.api.health.communitycareeligibility.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health endpoint that reports on services health. */
@Slf4j
@Validated
@RestController
@RequestMapping(produces = "application/json")
public class HealthController {
  private final EligibilityAndEnrollmentClient eeClient;

  private final FacilitiesClient facilitiesClient;

  private final AtomicBoolean hasCachedRecently = new AtomicBoolean(false);

  @Builder
  HealthController(
      @Autowired EligibilityAndEnrollmentClient eeClient,
      @Autowired FacilitiesClient facilitiesClient) {
    this.eeClient = eeClient;
    this.facilitiesClient = facilitiesClient;
  }

  private static Health toHealth(
      @NonNull String name, @NonNull HttpStatus statusCode, @NonNull Instant time) {
    return Health.status(new Status(statusCode.is2xxSuccessful() ? "UP" : "DOWN", name))
        .withDetail("name", name)
        .withDetail("statusCode", statusCode.value())
        .withDetail("status", statusCode)
        .withDetail("time", time)
        .build();
  }

  /** Clear the cache every five minutes. */
  @Scheduled(cron = "0 */5 * * * *")
  @CacheEvict(value = "health")
  public void clearCacheScheduled() {
    if (hasCachedRecently.getAndSet(false)) {
      // Reduce log spam by only reporting cleared _after_ we've actually cached it
      log.info("Clearing downstream service health cache");
    }
  }

  private Health eeHealth(@NonNull Instant time) {
    try {
      eeClient.requestEligibility("1008679665V880686");
      return toHealth("E&E", HttpStatus.OK, time);
    } catch (Exception ex) {
      log.info("E&E exception", ex);
      return toHealth("E&E", HttpStatus.SERVICE_UNAVAILABLE, time);
    }
  }

  private Health facilitiesHealth(@NonNull Instant time) {
    try {
      facilitiesClient.facilitiesByIds(List.of("vha_675GA"));
      return toHealth("Facilities", HttpStatus.OK, time);
    } catch (Exception ex) {
      log.info("Facilities exception", ex);
      return toHealth("Facilities", HttpStatus.SERVICE_UNAVAILABLE, time);
    }
  }

  /**
   * Get health status of downstream systems. To limit load, Spring Cacheable is used to record the
   * result. The cache is cleared every five minutes.
   *
   * @see #clearCacheScheduled()
   */
  @Cacheable("health")
  @GetMapping(value = {"/health", "/v0/health"})
  public ResponseEntity<Health> health() {
    return health(Instant.now());
  }

  ResponseEntity<Health> health(@NonNull Instant time) {
    hasCachedRecently.set(true);
    List<Health> services = List.of(eeHealth(time), facilitiesHealth(time));
    String code = services.stream().anyMatch(d -> !d.getStatus().equals(Status.UP)) ? "DOWN" : "UP";
    Health health =
        Health.status(new Status(code, "Downstream services"))
            .withDetail("name", "All downstream services")
            .withDetail("downstreamServices", services)
            .withDetail("time", time)
            .build();
    log.info(health.toString());
    if (!health.getStatus().equals(Status.UP)) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
    }
    return ResponseEntity.ok(health);
  }
}

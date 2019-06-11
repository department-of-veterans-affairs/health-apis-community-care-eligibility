package gov.va.api.health.communitycareeligibility.service.healthcheck;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse;
import gov.va.api.health.communitycareeligibility.service.EligibilityAndEnrollmentClient;
import gov.va.api.health.communitycareeligibility.service.Exceptions;
import gov.va.api.health.communitycareeligibility.service.FacilitiesClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Component
@Slf4j
public class SteelThreadSystemCheck implements HealthIndicator {

  private final EligibilityAndEnrollmentClient eeClient;

  private final FacilitiesClient facilitiesClient;

  private final String icn;

  private final CommunityCareEligibilityResponse.Address address;

  private final int driveMinutes;

  /** 'By hand' all args constructor is required to inject non-string values from our properties. */
  public SteelThreadSystemCheck(
      @Autowired EligibilityAndEnrollmentClient eeClient,
      @Autowired FacilitiesClient facilitiesClient,
      @Value("${health-check.icn}") String icn,
      @Value("${health-check.address.street}") String street,
      @Value("${health-check.address.city}") String city,
      @Value("${health-check.address.state}") String state,
      @Value("${health-check.address.zip}") String zip,
      @Value("${health-check.drive-minutes}") int driveMinutes) {
    this.eeClient = eeClient;
    this.facilitiesClient = facilitiesClient;
    this.icn = icn;
    this.address = new CommunityCareEligibilityResponse.Address(street, city, state, zip);
    this.driveMinutes = driveMinutes;
  }

  @Override
  @SneakyThrows
  public Health health() {
    if ("skip".equals(icn)) {
      return Health.up().withDetail("skipped", true).build();
    }
    try {
      eeClient.requestEligibility(icn);
      facilitiesClient.nearby(address, driveMinutes);
      return Health.up().build();
    } catch (HttpServerErrorException
        | HttpClientErrorException
        | ResourceAccessException
        | Exceptions.EeUnavailableException
        | Exceptions.FacilitiesUnavailableException e) {

      return Health.down()
          .withDetail("exception", e.getClass())
          .withDetail("message", e.getMessage())
          .build();
    } catch (Exception e) {
      log.error("Failed to complete health check.", e);
      throw e;
    }
  }
}

package gov.va.api.health.communitycareeligibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Status;

public class SteelThreadSystemCheckTest {

  private final int failureThresholdForTests = 5;

  @Mock EligibilityAndEnrollmentClient eeClient;

  @Mock FacilitiesClient facilitiesClient;

  @Mock SteelThreadSystemCheckLedger ledger;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void healthCheckHappyPath() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, "123", ledger, failureThresholdForTests);
    when(ledger.getConsecutiveFailureCount()).thenReturn(failureThresholdForTests - 1);
    assertThat(test.health().getStatus()).isEqualTo(Status.UP);
  }

  @Test
  public void healthCheckSadPath() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, "123", ledger, failureThresholdForTests);
    when(ledger.getConsecutiveFailureCount()).thenReturn(failureThresholdForTests);
    assertThat(test.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void healthCheckSkip() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, "skip", ledger, failureThresholdForTests);
    when(ledger.getConsecutiveFailureCount()).thenReturn(failureThresholdForTests + 100);
    assertThat(test.health().getStatus()).isEqualTo(Status.UP);
  }

  /** Make sure that when the E&E calls are working, the happy event is getting kicked in ledger. */
  @Test
  public void runSteelThreadHappyPathEe() {
    GetEESummaryResponse root = new GetEESummaryResponse();
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, "123", ledger, failureThresholdForTests);
    when(eeClient.requestEligibility(Mockito.any())).thenReturn(root);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordSuccess();
  }

  /**
   * Make sure that when the Facilities calls are working, the happy event is getting kicked in
   * ledger.
   */
  @Test
  public void runSteelThreadHappyPathFacilities() {
    List<String> facilities = new ArrayList<>();
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, "123", ledger, failureThresholdForTests);
    when(facilitiesClient.nearby(Mockito.any(), Mockito.anyInt(), Mockito.any()))
        .thenReturn(facilities);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordSuccess();
  }

  /**
   * Make sure that when the E&E calls are not working, the failure event is getting kicked in
   * ledger.
   */
  @Test
  public void runSteelThreadSadPathEe() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, "123", ledger, failureThresholdForTests);
    when(eeClient.requestEligibility(Mockito.any()))
        .thenThrow(new Exceptions.EeUnavailableException(new Throwable()));
    when(ledger.recordFailure()).thenReturn(failureThresholdForTests);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordFailure();
  }

  /**
   * Make sure that when the Facilities calls are not working, the failure event is getting kicked
   * in ledger.
   */
  @Test
  public void runSteelThreadSadPathFacilities() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, "123", ledger, failureThresholdForTests);
    when(facilitiesClient.nearby(Mockito.any(), Mockito.anyInt(), Mockito.any()))
        .thenThrow(new Exceptions.FacilitiesUnavailableException(new Throwable()));
    when(ledger.recordFailure()).thenReturn(failureThresholdForTests);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordFailure();
  }
}

package gov.va.api.health.communitycareeligibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.queenelizabeth.ee.QueenElizabethService;
import gov.va.api.health.queenelizabeth.ee.exceptions.RequestFailed;
import gov.va.api.health.queenelizabeth.ee.handlers.BaseFaultSoapHandler;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Status;

public class SteelThreadSystemCheckTest {

  private static final String TEST_ICN = "123";

  private final int failureThresholdForTests = 5;

  @Mock QueenElizabethService eeClient;

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
            eeClient, facilitiesClient, TEST_ICN, ledger, failureThresholdForTests);
    when(ledger.getConsecutiveFailureCount()).thenReturn(failureThresholdForTests - 1);
    assertThat(test.health().getStatus()).isEqualTo(Status.UP);
  }

  @Test
  public void healthCheckSadPath() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, TEST_ICN, ledger, failureThresholdForTests);
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
            eeClient, facilitiesClient, TEST_ICN, ledger, failureThresholdForTests);
    when(eeClient.getEeSummary(TEST_ICN)).thenReturn(root);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordSuccess();
  }

  /**
   * Make sure that when the Facilities calls are working, the happy event is getting kicked in
   * ledger.
   */
  @Test
  public void runSteelThreadHappyPathFacilities() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(
            eeClient, facilitiesClient, TEST_ICN, ledger, failureThresholdForTests);
    when(facilitiesClient.nearbyFacilities(Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenReturn(VaFacilitiesResponse.builder().build());
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
            eeClient, facilitiesClient, TEST_ICN, ledger, failureThresholdForTests);
    when(eeClient.getEeSummary(TEST_ICN))
        .thenThrow(new RequestFailed(BaseFaultSoapHandler.FAULT_UNKNOWN_MESSAGE));
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
            eeClient, facilitiesClient, TEST_ICN, ledger, failureThresholdForTests);
    when(facilitiesClient.nearbyFacilities(Mockito.any(), Mockito.anyInt(), Mockito.anyString()))
        .thenThrow(new Exceptions.FacilitiesUnavailableException(new Throwable()));
    when(ledger.recordFailure()).thenReturn(failureThresholdForTests);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordFailure();
  }
}

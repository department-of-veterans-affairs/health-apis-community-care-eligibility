package gov.va.api.health.communitycareeligibility.service;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Mock EligibilityAndEnrollmentClient eeClient;
  @Mock FacilitiesClient facilitiesClient;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void healthCheckHappyPathEe() {
    GetEESummaryResponse root = new GetEESummaryResponse();
    SteelThreadSystemCheck test = new SteelThreadSystemCheck(eeClient, facilitiesClient, "123");
    when(eeClient.requestEligibility(Mockito.any())).thenReturn(root);
    assertThat(test.health().getStatus()).isEqualTo(Status.UP);
  }

  @Test
  public void healthCheckHappyPathFacilities() {
    List<String> facilities = new ArrayList<>();
    SteelThreadSystemCheck test = new SteelThreadSystemCheck(eeClient, facilitiesClient, "123");
    when(facilitiesClient.nearby(Mockito.any(), Mockito.anyInt(), Mockito.any()))
        .thenReturn(facilities);
    assertThat(test.health().getStatus()).isEqualTo(Status.UP);
  }

  @Test
  public void healthCheckSadPathEe() {

    SteelThreadSystemCheck test = new SteelThreadSystemCheck(eeClient, facilitiesClient, "123");
    when(eeClient.requestEligibility(Mockito.any()))
        .thenThrow(new Exceptions.EeUnavailableException(new Throwable()));
    assertThat(test.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void healthCheckSadPathFacilities() {

    SteelThreadSystemCheck test = new SteelThreadSystemCheck(eeClient, facilitiesClient, "123");
    when(facilitiesClient.nearby(Mockito.any(), Mockito.anyInt(), Mockito.any()))
        .thenThrow(new Exceptions.FacilitiesUnavailableException(new Throwable()));
    assertThat(test.health().getStatus()).isEqualTo(Status.DOWN);
  }
}

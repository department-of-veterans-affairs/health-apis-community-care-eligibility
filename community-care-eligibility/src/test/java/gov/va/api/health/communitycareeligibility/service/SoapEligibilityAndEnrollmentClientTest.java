package gov.va.api.health.communitycareeligibility.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;

import org.junit.Test;

import gov.va.med.esr.webservices.jaxws.schemas.EeSummaryPort;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import lombok.Value;
import lombok.experimental.Delegate;

public final class SoapEligibilityAndEnrollmentClientTest {
  @Test
  public void asdf() {
    Binding binding = mock(Binding.class);
    BindingProvider bindingProvider = mock(BindingProvider.class);
    when(bindingProvider.getBinding()).thenReturn(binding);
    EeSummaryPort port = new MockPort(mock(EeSummaryPort.class), bindingProvider);

    SoapEligibilityAndEnrollmentClient client =
        SoapEligibilityAndEnrollmentClient.builder()
            .eeSummaryPortSupplier(() -> port)
            .username("bobnelson")
            .password("12345")
            .endpointUrl("https://foo.bar")
            .keystorePath("xyz")
            .keystorePassword("12345")
            .build();

    GetEESummaryResponse response = client.requestEligibility("0V0");
    System.out.println(response);
    // initSsl()
    
    // two exceptions
    
    // securityhandler.handleMessage
  }

  @Value
  private static final class MockPort implements EeSummaryPort, BindingProvider {
    @Delegate private EeSummaryPort port;

    @Delegate private BindingProvider bindingProvider;
  }
}

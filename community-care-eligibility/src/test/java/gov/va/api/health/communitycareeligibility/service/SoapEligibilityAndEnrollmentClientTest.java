package gov.va.api.health.communitycareeligibility.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.junit.Test;

import gov.va.med.esr.webservices.jaxws.schemas.EeSummaryPort;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import lombok.SneakyThrows;
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
  }

  @Test
  @SneakyThrows
  public void securityHandler() {
    SOAPElement usernameToken = mock(SOAPElement.class);
    when(usernameToken.addChildElement(eq("Username"), eq("wsse")))
        .thenReturn(mock(SOAPElement.class));
    when(usernameToken.addChildElement(eq("Password"), eq("wsse")))
        .thenReturn(mock(SOAPElement.class));

    SOAPHeader header = mock(SOAPHeader.class);
    when(header.addChildElement(eq("Security"), eq("wsse"))).thenReturn(usernameToken);
    when(usernameToken.addChildElement(eq("UsernameToken"), eq("wsse"))).thenReturn(usernameToken);

    SOAPEnvelope envelope = mock(SOAPEnvelope.class);
    when(envelope.getHeader()).thenReturn(header);

    SOAPPart soapPart = mock(SOAPPart.class);
    when(soapPart.getEnvelope()).thenReturn(envelope);

    SOAPMessage msg = mock(SOAPMessage.class);
    when(msg.getSOAPPart()).thenReturn(soapPart);

    SOAPMessageContext msgContext = mock(SOAPMessageContext.class);
    when(msgContext.getMessage()).thenReturn(msg);
    assertThat(
            SoapEligibilityAndEnrollmentClient.SecurityHandler.builder()
                .username("bobnelson")
                .password("12345")
                .build()
                .handleMessage(msgContext))
        .isTrue();
  }

  @Value
  private static final class MockPort implements EeSummaryPort, BindingProvider {
    @Delegate private EeSummaryPort port;

    @Delegate private BindingProvider bindingProvider;
  }
}

package gov.va.api.health.communitycareeligibility.service;

import static java.util.Collections.emptySet;

import gov.va.med.esr.webservices.jaxws.schemas.EeSummaryPort;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryRequest;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SoapEligibilityAndEnrollmentClient implements EligibilityAndEnrollmentClient {
  private final Supplier<EeSummaryPort> eeSummaryPortSupplier;

  private final String username;

  private final String password;

  private final String endpointUrl;

  /** Autowired constructor. */
  @Builder
  public SoapEligibilityAndEnrollmentClient(
      @Autowired Supplier<EeSummaryPort> eeSummaryPortSupplier,
      @Value("${ee.header.username}") String username,
      @Value("${ee.header.password}") String password,
      @Value("${ee.endpoint.url}") String endpointUrl) {
    this.eeSummaryPortSupplier = eeSummaryPortSupplier;
    this.username = username;
    this.password = password;
    this.endpointUrl = endpointUrl.endsWith("/") ? endpointUrl : endpointUrl + "/";
  }

  /** Initialize SSL. */
  @SneakyThrows
  @EventListener(ApplicationStartedEvent.class)
  public boolean initSsl() {
    if (!endpointUrl.startsWith("https")) {
      return false;
    }

    log.info("Initializing SSL for E&E");
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(
        null,
        new TrustManager[] {
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
          }
        },
        new SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    return true;
  }

  @Override
  @SneakyThrows
  public GetEESummaryResponse requestEligibility(String patientIcn) {
    EeSummaryPort port = eeSummaryPortSupplier.get();
    BindingProvider bindingProvider = (BindingProvider) port;
    bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
    @SuppressWarnings("rawtypes")
    List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
    handlers.add(SecurityHandler.builder().username(username).password(password).build());
    bindingProvider.getBinding().setHandlerChain(handlers);

    try {
      return port.getEESummary(
          GetEESummaryRequest.builder().key(patientIcn).requestName("CommunityCareInfo").build());
    } catch (Exception e) {
      if (StringUtils.containsIgnoreCase(e.getMessage(), "PERSON_NOT_FOUND")) {
        throw new Exceptions.UnknownPatientIcnException(patientIcn, e);
      }
      throw new Exceptions.EeUnavailableException(e);
    }
  }

  @Builder
  static final class SecurityHandler implements SOAPHandler<SOAPMessageContext> {
    @NonNull private final String username;

    @NonNull private final String password;

    @Override
    public void close(MessageContext context) {}

    @Override
    public Set<QName> getHeaders() {
      return emptySet();
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
      return false;
    }

    @Override
    @SneakyThrows
    public boolean handleMessage(SOAPMessageContext context) {
      SOAPEnvelope env = context.getMessage().getSOAPPart().getEnvelope();
      env.addNamespaceDeclaration(
          "wsse",
          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
      SOAPElement usernameToken =
          env.getHeader()
              .addChildElement("Security", "wsse")
              .addChildElement("UsernameToken", "wsse");
      usernameToken.addChildElement("Username", "wsse").addTextNode(username);
      usernameToken.addChildElement("Password", "wsse").addTextNode(password);
      return true;
    }
  }
}

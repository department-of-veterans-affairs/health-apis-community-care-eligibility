package gov.va.api.health.communitycareeligibility.service;

import gov.va.med.esr.webservices.jaxws.schemas.EeSummaryPort;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummaryPortService;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryRequest;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.ws.Binding;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Slf4j
@Component
public class SoapEligibilityAndEnrollmentClient implements EligibilityAndEnrollmentClient {
  private final String username;

  private final String password;

  private final String endpointUrl;

  private final String keyStorePath;

  private final String keyStorePassword;

  /** Autowired constructor. */
  public SoapEligibilityAndEnrollmentClient(
      @Value("${ee.header.username}") String username,
      @Value("${ee.header.password}") String password,
      @Value("${ee.endpoint.url}") String endpointUrl,
      @Value("${ee.keystore.path}") String keyStorePath,
      @Value("${ee.keystore.password}") String keyStorePassword) {
    this.username = username;
    this.password = password;
    this.endpointUrl = endpointUrl.endsWith("/") ? endpointUrl : endpointUrl + "/";
    this.keyStorePath = keyStorePath;
    this.keyStorePassword = keyStorePassword;
  }

  private static String fileOrClasspath(String path) {
    if (StringUtils.startsWith(path, "file:") || StringUtils.startsWith(path, "classpath:")) {
      return path;
    }
    throw new IllegalArgumentException("Expected file or classpath resources. Got " + path);
  }

  /** Initialize SSL. */
  @SneakyThrows
  @EventListener(ApplicationStartedEvent.class)
  public void initSsl() {
    if (!endpointUrl.startsWith("https")) {
      return;
    }

    log.info("Initializing SSL");
    try (InputStream keyStoreInputStream =
        ResourceUtils.getURL(fileOrClasspath(keyStorePath)).openStream()) {
      KeyStore ts = KeyStore.getInstance("JKS");
      ts.load(keyStoreInputStream, keyStorePassword.toCharArray());
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(ts);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }
  }

  @Override
  @SneakyThrows
  public GetEESummaryResponse requestEligibility(String patientIcn) {
    // System.out.println("Requesting eligibilities: " + patientIcn);

    // final StopWatch watch = StopWatch.createStarted();
    EeSummaryPort port =
        new EeSummaryPortService(new URL(endpointUrl + "eeSummary.wsdl")).getEeSummaryPortSoap11();
    Binding binding = ((BindingProvider) port).getBinding();
    @SuppressWarnings("rawtypes")
    List<Handler> handlers = binding.getHandlerChain();
    System.out.println("initial handlers: " + handlers);
    handlers.add(SecurityHandler.builder().username(username).password(password).build());
    binding.setHandlerChain(handlers);
    // watch.stop();
    // System.out.println("took " + watch.getTime(TimeUnit.MILLISECONDS));

    try {
      // StopWatch watch2 = StopWatch.createStarted();
      GetEESummaryResponse response =
          port.getEESummary(
              GetEESummaryRequest.builder()
                  .key(patientIcn)
                  .requestName("CommunityCareInfo")
                  .build());
      // log.info(response.toString());
      // watch2.stop();
      // System.out.println("call took " + watch2.getTime(TimeUnit.MILLISECONDS));
      return response;
    } catch (Exception e) {
      if (StringUtils.containsIgnoreCase(e.getMessage(), "PERSON_NOT_FOUND")) {
        throw new Exceptions.UnknownPatientIcnException(patientIcn, e);
      } else {
        throw new Exceptions.EeUnavailableException(e);
      }
    }
  }

  @Builder
  private static final class SecurityHandler implements SOAPHandler<SOAPMessageContext> {
    @NonNull private final String username;

    @NonNull private final String password;

    @Override
    public void close(MessageContext context) {}

    @Override
    public Set<QName> getHeaders() {
      return Collections.emptySet();
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

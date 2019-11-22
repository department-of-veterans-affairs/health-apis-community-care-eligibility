package gov.va.api.health.communitycareeligibility.service;

import gov.va.med.esr.webservices.jaxws.schemas.EeSummaryPort;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummaryPortService;
import java.net.URL;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class EeSummaryPortSupplier implements Supplier<EeSummaryPort> {
  private final String endpointUrl;

  /** Autowired constructor. */
  public EeSummaryPortSupplier(@Value("${ee.endpoint.url}") String endpointUrl) {
    this.endpointUrl = endpointUrl.endsWith("/") ? endpointUrl : endpointUrl + "/";
  }

  @Override
  @SneakyThrows
  public EeSummaryPort get() {
    return new EeSummaryPortService(new URL(endpointUrl + "eeSummary.wsdl"))
        .getEeSummaryPortSoap11();
  }
}

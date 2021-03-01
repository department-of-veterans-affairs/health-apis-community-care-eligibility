package gov.va.api.health.communitycareeligibilitymockservices;

import static com.google.common.base.Preconditions.checkState;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.google.common.io.Resources;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.client.MockServerClient;
import org.mockserver.mockserver.MockServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a mock server that can support requests for CCE. This also includes a /help endpoint to
 * allow humans to see what requests are supported.
 */
@Slf4j
@Component
public class MockServices {
  public static final String PCMM_PATH = "/pcmmr_web/ws/patientSummary/icn/";

  /** All queries added to the mock server are listed here, except for /help. */
  private final List<String> supportedQueries = new ArrayList<>();

  @Autowired MockServicesOptions options;

  /** The mock server itself. */
  private MockServer ms;

  private static Header contentApplicationJson() {
    return new Header("Content-Type", "application/json");
  }

  private static Header contentApplicationXml() {
    return new Header("Content-Type", "application/xml");
  }

  private static Header contentTextPlain() {
    return new Header("Content-Type", "text/plain");
  }

  private void addFacilitiesIds(MockServerClient mock) {
    mock.when(addQuery("/v0/facilities?ids=vha_675GA"))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationJson())
                .withBody(contentOf("/facilities-ids.json")));
  }

  private void addFacilitiesNearby(MockServerClient mock) {
    mock.when(addQuery("/v0/nearby?lat=25.885108&lng=-97.510832"))
        .respond(
            response().withStatusCode(200).withHeader(contentApplicationJson()).withBody("{}"));
    mock.when(addQuery("/v0/nearby"))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationJson())
                .withBody(contentOf("/facilities-nearby.json")));
  }

  private void addHelp(MockServerClient mock) {
    mock.when(request().withPath("/help"))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTextPlain())
                .withBody(supportedQueries.stream().sorted().collect(Collectors.joining("\n"))));
    log.info("List of supported queries available at http://localhost:{}/help", options.getPort());
  }

  private void addPcmmStatusRequests(MockServerClient mock) {
    final String pactStatusNoDataIcn = "1012870703V135989";
    final String pactStatusPendingIcn = "1012667674V820648";
    final String pactStatusMultiDataStatusActiveIcn = "1013060957V646684";
    mock.when(addQuery(PCMM_PATH + pactStatusNoDataIcn))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationXml())
                .withBody(contentOf("/pcmm-no-pact-data.xml")));
    mock.when(addQuery(PCMM_PATH + pactStatusPendingIcn))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationXml())
                .withBody(contentOf("/pcmm-single-pact-data-status-pending.xml")));
    mock.when(addQuery(PCMM_PATH + pactStatusMultiDataStatusActiveIcn))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationXml())
                .withBody(contentOf("/pcmm-multiple-pact-data-status-active.xml")));
  }

  @SneakyThrows
  private HttpRequest addQuery(String path) {
    log.info("http://localhost:{}{}", options.getPort(), path);
    supportedQueries.add("http://localhost:" + options.getPort() + path);
    URL url = new URL("http://localhost" + path);
    HttpRequest request = request().withPath(url.getPath());
    if (url.getQuery() == null) {
      return request;
    }
    // Split the query portion of the path and each of the parameters individually
    // Note that the parameter value may also contain '='
    Stream.of(url.getQuery().split("&"))
        .forEach(
            q -> {
              var pv = q.split("=", 2);
              request.withQueryStringParameter(
                  pv[0], URLDecoder.decode(pv[1], StandardCharsets.UTF_8));
            });
    return request;
  }

  @SneakyThrows
  private String contentOf(String resource) {
    log.info("Loading resource {}", resource);
    return Resources.toString(getClass().getResource(resource), StandardCharsets.UTF_8);
  }

  /** Start the server and configure it to support requests. */
  public void start() {
    checkState(ms == null, "Mock Services have already been started");
    log.info("Starting mock services on port {}", options.getPort());
    ms = new MockServer(options.getPort());
    MockServerClient mock = new MockServerClient("localhost", options.getPort());
    addFacilitiesIds(mock);
    addFacilitiesNearby(mock);
    addPcmmStatusRequests(mock);
    addHelp(mock);
  }
}

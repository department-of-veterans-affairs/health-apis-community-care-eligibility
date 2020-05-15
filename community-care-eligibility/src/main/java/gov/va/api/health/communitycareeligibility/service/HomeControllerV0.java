package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v0/eligibility", produces = "application/json")
public class HomeControllerV0 {

  private final Resource openapi;

  @Autowired
  HomeControllerV0(@Value("classpath:/openapi.json") Resource openapi) {
    this.openapi = openapi;
  }

  /** REST endpoint for OpenAPI JSON + redirect. */
  @GetMapping(
      value = {"/", "/openapi.json"},
      produces = "application/json")
  @ResponseBody
  public Object openApiJson() throws IOException {
    try (InputStream is = openapi.getInputStream()) {
      String openapiContent = StreamUtils.copyToString(is, Charset.defaultCharset());
      return JacksonConfig.createMapper().readValue(openapiContent, Object.class);
    }
  }
}

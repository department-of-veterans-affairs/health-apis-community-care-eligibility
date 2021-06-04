package gov.va.api.health.communitycareeligibility.service;

import java.io.InputStream;
import java.nio.charset.Charset;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint for openapi.json and home redirect to open api. */
@RestController
@RequestMapping(value = "/v0/eligibility", produces = "application/json")
public class HomeControllerV0 {
  private final Resource openapi;

  @Autowired
  HomeControllerV0(@Value("classpath:/openapi.json") Resource openapi) {
    this.openapi = openapi;
  }

  /** REST endpoint for OpenAPI JSON + redirect. */
  @SneakyThrows
  @GetMapping(
      value = {"/", "/openapi.json"},
      produces = "application/json")
  @ResponseBody
  public Object openApiJson() {
    try (InputStream is = openapi.getInputStream()) {
      return StreamUtils.copyToString(is, Charset.defaultCharset());
    }
  }
}

package gov.va.api.health.communitycareeligibility.service;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v0/eligibility", produces = "application/json")
public class HomeControllerV0 {
  private static final YAMLMapper MAPPER = new YAMLMapper();

  /** REST endpoint for OpenAPI JSON + redirect. */
  @GetMapping(
    value = {"/", "/openapi.json", "/api/openapi.json"},
    produces = "application/json"
  )
  @ResponseBody
  public Object openApiJson() throws IOException {
    return MAPPER.readValue(openApiYamlContent(), Object.class);
  }

  /** REST endpoint OpenAPI YAML. */
  @GetMapping(
    value = {"/openapi.yaml", "/api/openapi.yaml"},
    produces = "application/vnd.oai.openapi"
  )
  @ResponseBody
  public String openApiYaml() throws IOException {
    return openApiYamlContent();
  }

  /** OpenAPI content in YAML form. */
  @Bean
  private String openApiYamlContent() throws IOException {
    try (InputStream is = new ClassPathResource("openapi.yaml").getInputStream()) {
      return StreamUtils.copyToString(is, Charset.defaultCharset());
    }
  }
}

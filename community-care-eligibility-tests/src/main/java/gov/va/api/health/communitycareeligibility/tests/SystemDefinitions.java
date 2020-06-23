package gov.va.api.health.communitycareeligibility.tests;

import static gov.va.api.health.sentinel.SentinelProperties.magicAccessToken;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.sentinel.BasicTestClient;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.SentinelProperties;
import gov.va.api.health.sentinel.ServiceDefinition;
import gov.va.api.health.sentinel.TestClient;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
class SystemDefinitions {
  static TestClient cceClient() {
    return BasicTestClient.builder()
        .service(SystemDefinitions.systemDefinition().cce())
        .mapper(JacksonConfig::createMapper)
        .contentType("application/json")
        .build();
  }

  private static SystemDefinition lab() {
    return SystemDefinition.builder()
        .cce(
            serviceDefinition(
                "community-care-eligibility",
                "https://blue.lab.lighthouse.va.gov",
                443,
                magicAccessToken(),
                "/community-care/"))
        .patient("1017283148V813263")
        .build();
  }

  private static SystemDefinition local() {
    return SystemDefinition.builder()
        .cce(serviceDefinition("community-care-eligibility", "http://localhost", 8090, null, "/"))
        .patient("1012853802V084487")
        .build();
  }

  private static SystemDefinition production() {
    return SystemDefinition.builder()
        .cce(
            serviceDefinition(
                "community-care-eligibility",
                "https://blue.production.lighthouse.va.gov",
                443,
                magicAccessToken(),
                "/community-care/"))
        .patient("1013294025V219497")
        .build();
  }

  private static SystemDefinition qa() {
    return SystemDefinition.builder()
        .cce(
            serviceDefinition(
                "community-care-eligibility",
                "https://blue.qa.lighthouse.va.gov",
                443,
                magicAccessToken(),
                "/community-care/"))
        .patient("1008679665V880686")
        .build();
  }

  private static ServiceDefinition serviceDefinition(
      String name, String url, int port, String accessToken, String apiPath) {
    return ServiceDefinition.builder()
        .url(SentinelProperties.optionUrl(name, url))
        .port(port)
        .apiPath(SentinelProperties.optionApiPath(name, apiPath))
        .accessToken(() -> Optional.ofNullable(accessToken))
        .build();
  }

  private static SystemDefinition staging() {
    return SystemDefinition.builder()
        .cce(
            serviceDefinition(
                "community-care-eligibility",
                "https://blue.staging.lighthouse.va.gov",
                443,
                magicAccessToken(),
                "/community-care/"))
        .patient("0000001008405009V205102000000")
        .build();
  }

  private static SystemDefinition stagingLab() {
    return SystemDefinition.builder()
        .cce(
            serviceDefinition(
                "community-care-eligibility",
                "https://blue.staging-lab.lighthouse.va.gov",
                443,
                magicAccessToken(),
                "/community-care/"))
        .patient("1017283148V813263")
        .build();
  }

  static SystemDefinition systemDefinition() {
    switch (Environment.get()) {
      case LOCAL:
        return local();
      case QA:
        return qa();
      case STAGING:
        return staging();
      case PROD:
        return production();
      case STAGING_LAB:
        return stagingLab();
      case LAB:
        return lab();
      default:
        throw new IllegalArgumentException(
            "Unsupported sentinel environment: " + Environment.get());
    }
  }

  @Value
  @Builder
  static final class SystemDefinition {
    @NonNull ServiceDefinition cce;

    @NonNull String patient;
  }
}

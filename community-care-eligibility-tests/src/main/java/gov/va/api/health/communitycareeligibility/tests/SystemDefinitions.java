package gov.va.api.health.communitycareeligibility.tests;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.SentinelProperties;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
class SystemDefinitions {
  private static SystemDefinition lab() {
    return SystemDefinition.builder()
        .cce(
            serviceDefinition(
                "community-care-eligibility",
                "https://blue.lab.lighthouse.va.gov",
                443,
                "/community-care/"))
        .patient("1017283148V813263")
        .build();
  }

  private static SystemDefinition local() {
    return SystemDefinition.builder()
        .cce(serviceDefinition("community-care-eligibility", "http://localhost", 8090, "/"))
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
                "/community-care/"))
        .patient("1008679665V880686")
        .build();
  }

  private static ServiceDefinition serviceDefinition(
      String name, String url, int port, String apiPath) {
    return ServiceDefinition.builder()
        .url(SentinelProperties.optionUrl(name, url))
        .port(port)
        .apiPath(SentinelProperties.optionApiPath(name, apiPath))
        .build();
  }

  private static SystemDefinition staging() {
    return SystemDefinition.builder()
        .cce(
            serviceDefinition(
                "community-care-eligibility",
                "https://blue.staging.lighthouse.va.gov",
                443,
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

  /** Defines particulars for interacting with a specific service. */
  @Value
  @Builder
  static final class ServiceDefinition {
    @NonNull String url;

    @NonNull Integer port;

    @NonNull String apiPath;

    /**
     * Return a url + path that adds / as necessary to produce a url that ends in a /.
     *
     * <p>For example: https://something.com/my/cool/api/
     */
    public String urlWithApiPath() {
      StringBuilder builder = new StringBuilder(url());
      if (!apiPath().startsWith("/")) {
        builder.append('/');
      }
      builder.append(apiPath());
      if (!apiPath.endsWith("/")) {
        builder.append('/');
      }
      return builder.toString();
    }
  }
}

package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client.EligibilityAndEnrollmentClient;
import gov.va.api.health.queenelizabeth.ee.Eligibilities;
import gov.va.api.health.queenelizabeth.ee.SoapMessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SoapEligibilityAndEnrollmentClient implements EligibilityAndEnrollmentClient {
  private final Eligibilities eligibilities;

  private final String eeUsername;

  private final String eePassword;

  private final String eeRequestName;

  /** Autowired constructor. */
  public SoapEligibilityAndEnrollmentClient(
      @Autowired Eligibilities eligibilities,
      @Value("${ee.header.username}") String eeUsername,
      @Value("${ee.header.password}") String eePassword,
      @Value("${ee.request.name}") String eeRequestName) {
    this.eligibilities = eligibilities;
    this.eeUsername = eeUsername;
    this.eePassword = eePassword;
    this.eeRequestName = eeRequestName;
  }

  @Override
  public String requestEligibility(String id) {
    return eligibilities.request(
        SoapMessageGenerator.builder()
            .id(id)
            .eeUsername(eeUsername)
            .eePassword(eePassword)
            .eeRequestName(eeRequestName)
            .build());
  }
}

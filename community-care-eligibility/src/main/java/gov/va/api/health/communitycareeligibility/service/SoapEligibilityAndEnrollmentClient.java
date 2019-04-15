package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client.EligibilityAndEnrollmentClient;
import gov.va.api.health.queenelizabeth.ee.Eligibilities;
import gov.va.api.health.queenelizabeth.ee.SoapMessageGenerator;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import lombok.SneakyThrows;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;

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
  public GetEESummaryResponse requestEligibility(String id) {
    return unmarshal(
        eligibilities.request(
            SoapMessageGenerator.builder()
                .id(id)
                .eeUsername(eeUsername)
                .eePassword(eePassword)
                .eeRequestName(eeRequestName)
                .build()),
        GetEESummaryResponse.class);
  }

  /** Unmarshal the XML string into the given class. */
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static <T> T unmarshal(String xml, Class<T> resultClass) {
    try (Reader reader = new StringReader(xml)) {
      return (T) JAXBContext.newInstance(resultClass).createUnmarshaller().unmarshal(reader);
    }
  }
}

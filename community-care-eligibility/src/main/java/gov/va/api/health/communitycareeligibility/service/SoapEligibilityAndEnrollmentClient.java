package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.queenelizabeth.ee.Eligibilities;
import gov.va.api.health.queenelizabeth.ee.SoapMessageGenerator;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

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

  /** Unmarshal the XML string into the given class. */
  @SneakyThrows
  @SuppressWarnings("cast")
  private static <T> T unmarshal(String xml, Class<T> resultClass) {
    // Secure XML parser configuration that prevents XXE injections. Satisfies Fortify requirements.
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    spf.setNamespaceAware(true);
    try (Reader reader = new StringReader(xml)) {
      Source source = new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource(reader));
      Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(resultClass).createUnmarshaller();
      JAXBElement<T> jaxbElement = (JAXBElement<T>) jaxbUnmarshaller.unmarshal(source, resultClass);
      return jaxbElement.getValue();
    }
  }

  @Override
  public GetEESummaryResponse requestEligibility(String patientIcn) {
    try {
      return unmarshal(
          eligibilities.request(
              SoapMessageGenerator.builder()
                  .id(patientIcn)
                  .eeUsername(eeUsername)
                  .eePassword(eePassword)
                  .eeRequestName(eeRequestName)
                  .build()),
          GetEESummaryResponse.class);
    } catch (Exception e) {
      if (StringUtils.containsIgnoreCase(e.getMessage(), "PERSON_NOT_FOUND")
          || StringUtils.containsIgnoreCase(e.getMessage(), "unknown patient")) {
        throw new Exceptions.UnknownPatientIcnException(patientIcn, e);
      } else {
        throw new Exceptions.EeUnavailableException(e);
      }
    }
  }
}

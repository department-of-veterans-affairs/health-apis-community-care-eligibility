package gov.va.api.health.communitycareeligibility.service.client;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.communitycareeligibility.service.SoapEligibilityAndEnrollmentClient;
import gov.va.api.health.queenelizabeth.ee.Eligibilities;
import gov.va.api.health.queenelizabeth.ee.SoapMessageGenerator;
import gov.va.med.esr.webservices.jaxws.schemas.CommunityCareEligibilityInfo;
import gov.va.med.esr.webservices.jaxws.schemas.EeSummary;
import gov.va.med.esr.webservices.jaxws.schemas.GetEESummaryResponse;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityCollection;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public final class RestSoapEligibilityAndEnrollmentClientTest {
  @SneakyThrows
  private static XMLGregorianCalendar parseXmlGregorianCalendar(String timestamp) {
    GregorianCalendar gCal = new GregorianCalendar();
    gCal.setTime(Date.from(Instant.parse(timestamp)));
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void requestEligibility() {
    Eligibilities eligibilities = mock(Eligibilities.class);
    when(eligibilities.request(
            (SoapMessageGenerator.builder()
                .id("123")
                .eePassword("eePassword")
                .eeRequestName("eeRequestName")
                .eeUsername("eeUsername")
                .build())))
        .thenReturn(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<getEESummaryResponse xmlns=\"http://jaxws.webservices.esr.med.va.gov/schemas\">\n"
                + "    <eesVersion>5.6.0.01001</eesVersion>\n"
                + "    <summary>\n"
                + "        <communityCareEligibilityInfo>\n"
                + "            <eligibilities>\n"
                + "                <eligibility>\n"
                + "                    <vceDescription>Urgent Care</vceDescription>\n"
                + "                    <vceEffectiveDate>2019-03-27T10:37:48.000-04:00</vceEffectiveDate>\n"
                + "                    <vceCode>U</vceCode>\n"
                + "                </eligibility>\n"
                + "            </eligibilities>\n"
                + "        </communityCareEligibilityInfo>\n"
                + "    </summary>\n"
                + "    <invocationDate>2019-03-27T10:37:48.000-04:00</invocationDate>\n"
                + "</getEESummaryResponse>");

    GetEESummaryResponse expected =
        GetEESummaryResponse.builder()
            .eesVersion("5.6.0.01001")
            .invocationDate(parseXmlGregorianCalendar("2019-03-27T14:37:48Z"))
            .summary(
                EeSummary.builder()
                    .communityCareEligibilityInfo(
                        CommunityCareEligibilityInfo.builder()
                            .eligibilities(
                                VceEligibilityCollection.builder()
                                    .eligibility(
                                        singletonList(
                                            VceEligibilityInfo.builder()
                                                .vceCode("U")
                                                .vceDescription("Urgent Care")
                                                .vceEffectiveDate(
                                                    parseXmlGregorianCalendar(
                                                        "2019-03-27T14:37:48Z"))
                                                .build()))
                                    .build())
                            .build())
                    .build())
            .build();
    SoapEligibilityAndEnrollmentClient client =
        new SoapEligibilityAndEnrollmentClient(
            eligibilities, "eeUsername", "eePassword", "eeRequestName");
    assertThat(client.requestEligibility("123")).isEqualTo(expected);
  }
}

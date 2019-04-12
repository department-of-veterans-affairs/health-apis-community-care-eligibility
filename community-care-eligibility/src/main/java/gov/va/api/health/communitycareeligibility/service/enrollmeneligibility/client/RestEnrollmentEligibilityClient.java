package gov.va.api.health.communitycareeligibility.service.enrollmeneligibility.client;

import gov.va.api.health.communitycareeligibility.service.EnrollmentEligibilityClient;
import gov.va.api.health.communitycareeligibility.service.config.WithJaxb;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/** Implementation of the Enrollment Eligibility Client. */
@Component
public class RestEnrollmentEligibilityClient implements EnrollmentEligibilityClient {

  private final RestTemplate restTemplate;

  private final String baseUrl;

  public RestEnrollmentEligibilityClient(
      @Value("${enrollmenteligibility.url}") String baseUrl,
      @Autowired @WithJaxb RestTemplate restTemplate) {
    this.baseUrl = baseUrl;
    this.restTemplate = restTemplate;
  }

  private HttpEntity<Void> requestEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    return new HttpEntity<>(headers);
  }

  @Override
  public <T> T search(Query<T> query) {
    try {
      ResponseEntity<T> responseEntity =
          restTemplate.exchange(
              urlOf(query),
              HttpMethod.GET,
              requestEntity(),
              ParameterizedTypeReference.forType(query.type()));
      return responseEntity.getBody();
    } catch (HttpClientErrorException.NotFound e) {
      throw new NotFound(query);
    } catch (HttpClientErrorException.BadRequest e) {
      throw new BadRequest(query);
    } catch (HttpStatusCodeException e) {
      throw new SearchFailed(query);
    }
  }

  private String urlOf(Query<?> query) {
    return baseUrl + "/api/v1/eligibilityandenrollment" + query.toQueryString();
  }
}

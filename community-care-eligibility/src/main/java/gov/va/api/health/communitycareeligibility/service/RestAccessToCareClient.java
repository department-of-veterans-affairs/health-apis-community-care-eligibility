package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.Address;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class RestAccessToCareClient implements AccessToCareClient {
  private final RestTemplate restTemplate;

  public RestAccessToCareClient(@Autowired RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static Map<String, Integer> appointmentTypeCodeMapping() {
    Map<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    map.put("AUDIOLOGY", 1);
    map.put("CARDIOLOGY", 2);
    map.put("WOMENSHEALTH", 3);
    map.put("DERMATOLOGY", 4);
    map.put("GASTROENTEROLOGY", 5);
    map.put("MENTALHEALTH", 6);
    map.put("MENTALHEALTHCARE", 6);
    map.put("GYNECOLOGY", 7);
    map.put("OPHTHALMOLOGY", 8);
    map.put("OPTOMETRY", 9);
    map.put("ORTHOPEDICS", 10);
    map.put("PRIMARYCARE", 12);
    map.put("UROLOGY", 14);
    map.put("UROLOGYCLINIC", 14);
    return map;
  }

  @Override
  public List<AccessToCareFacility> facilities(Address patientAddress, String serviceType) {
    String addressString =
        Stream.of(
                patientAddress.street(),
                patientAddress.city(),
                patientAddress.state(),
                patientAddress.zip())
            .collect(Collectors.joining(", "));
    Integer appointmentTypeCode = appointmentTypeCodeMapping().get(serviceType);
    // TODO handle unknown appointment type code
    String url =
        UriComponentsBuilder.fromHttpUrl("https://www.accesstocare.va.gov/PWT/getRawData")
            .queryParam("location", addressString)
            .queryParam("radius", "50")
            .queryParam("apptType", appointmentTypeCode)
            .queryParam("sortOrder", "Distance")
            .queryParam("format", "JSON")
            .build()
            .toUriString();
    List<AccessToCareFacility> accessToCareFacilities =
        restTemplate
            .exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<List<AccessToCareFacility>>() {})
            .getBody();
    log.info("Access-to-care facilities: " + accessToCareFacilities);
    return accessToCareFacilities;
  }
}

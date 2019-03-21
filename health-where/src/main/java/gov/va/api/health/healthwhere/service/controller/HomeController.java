package gov.va.api.health.healthwhere.service.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.healthwhere.service.AccessToCareFacility;
import gov.va.api.health.healthwhere.service.AccessToCareFacilityTransformer;
import gov.va.api.health.healthwhere.service.Address;
import gov.va.api.health.healthwhere.service.BingResponse;
import gov.va.api.health.healthwhere.service.CommunityCareResult;
import gov.va.api.health.healthwhere.service.Coordinates;
import gov.va.api.health.healthwhere.service.Facility;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Controller
public class HomeController {

  private final RestTemplate restTemplate;

  private String bingApiKey;

  private int maxDriveTime;

  private int maxWait;

  public HomeController(
      @Value("${bing.api-key}") String bingApiKey,
      @Value("${community-care.max-drive-time}") int maxDriveTime,
      @Value("${community-care.max-wait}") int maxWait,
      @Autowired RestTemplate restTemplate) {
    this.bingApiKey = bingApiKey;
    this.maxDriveTime = maxDriveTime;
    this.maxWait = maxWait;
    this.restTemplate = restTemplate;
  }

  private static Map<String, Integer> acessToCareAppointmentTypeCodeMapping() {
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

  private static ObjectMapper objectMapper() {
    return JacksonConfig.createMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @SneakyThrows
  private BingResponse bingDrivetimeSearch(
      Coordinates patientCoordinates, Coordinates facilityCoordinates) {
    String url =
        UriComponentsBuilder.fromHttpUrl("http://dev.virtualearth.net/REST/V1/Routes")
            .queryParam("wp.0", patientCoordinates.toCoordinateString())
            .queryParam("wp.1", facilityCoordinates.toCoordinateString())
            .queryParam("key", bingApiKey)
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "Bing API response: "
            + objectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper().readTree(body)));
    BingResponse responseObject = objectMapper().readValue(body, BingResponse.class);
    log.error(
        "response object: "
            + objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(responseObject));
    return responseObject;
  }

  @SneakyThrows
  private BingResponse bingLocationSearch(Address address) {
    String url =
        UriComponentsBuilder.fromHttpUrl("http://dev.virtualearth.net/REST/v1/Locations")
            .queryParam("countryRegion", "US")
            .queryParam("adminDistrict", address.state())
            .queryParam("locality", address.city())
            .queryParam("postalCode", address.zip())
            .queryParam("addressLine", address.street())
            .queryParam("maxResults", 1)
            .queryParam("key", bingApiKey)
            .build()
            .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "Bing API response: "
            + objectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper().readTree(body)));
    BingResponse responseObject = objectMapper().readValue(body, BingResponse.class);
    log.error(
        "response object: "
            + objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(responseObject));
    return responseObject;
  }

  @SneakyThrows
  private boolean checkIfEligibleForCommunityCare(
      Address patientAddress, boolean establishedPatient, List<Facility> facilities) {
    String[] automaticallyEligibleStates = {"AK", "AZ", "IA", "NM", "MN", "ND", "OK", "SD", "UT"};
    if (Arrays.stream(automaticallyEligibleStates).anyMatch(patientAddress.state()::equals)) {
      // No VAMC locations in these states, automatically eligible
      return true;
    } else {
      List<Facility> facilitiesMeetingRequirements =
          facilities
              .stream()
              .filter(
                  facility ->
                      (facility.driveMinutes() < maxDriveTime
                          && (establishedPatient
                              ? (facility.waitDays().establishedPatient() < maxWait)
                              : (facility.waitDays().newPatient() < maxWait))))
              .collect(Collectors.toList());
      return facilitiesMeetingRequirements.size()
          == // return false if NO facilities meet requirements
          0;
    }
  }

  private Coordinates getBingResourceCoordinates(BingResponse bingResponse) {
    Coordinates coordinates =
        new Coordinates(
            bingResponse.resourceSets().get(0).resources().get(0).point().coordinates()[0],
            bingResponse.resourceSets().get(0).resources().get(0).point().coordinates()[1]);
    return coordinates;
  }

  @SneakyThrows
  private List<AccessToCareFacility> retrieveAccessToCareFacilities(
      Address patientAddress, String serviceType) {
    String addressString =
        Stream.of(
                patientAddress.street(),
                patientAddress.city(),
                patientAddress.state(),
                patientAddress.zip())
            .collect(Collectors.joining(", "));
    Integer appointmentTypeCode = acessToCareAppointmentTypeCodeMapping().get(serviceType);
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
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "access-to-care api response: "
            + objectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper().readTree(body)));
    List<AccessToCareFacility> accessToCareFacilities =
        objectMapper().readValue(body, new TypeReference<List<AccessToCareFacility>>() {});
    log.error(
        "access-to-care response objects: "
            + objectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(accessToCareFacilities));
    return accessToCareFacilities;
  }

  /** Search by address and service type. */
  @ResponseBody
  @SneakyThrows
  @GetMapping(value = {"/search"})
  public CommunityCareResult search(
      @RequestParam(value = "street") String street,
      @RequestParam(value = "city") String city,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "zip") String zip,
      @RequestParam(value = "serviceType") String serviceType,
      @RequestParam(value = "establishedPatient") boolean establishedPatient) {
    Address patientAddress = new Address(street, city, state, zip);
    List<AccessToCareFacility> accessToCareFacilities =
        retrieveAccessToCareFacilities(patientAddress, serviceType);
    List<Facility> facilities =
        accessToCareFacilities
            .stream()
            .filter(Objects::nonNull)
            .map(
                accessToCareFacility ->
                    AccessToCareFacilityTransformer.builder()
                        .atcFacility(accessToCareFacility)
                        .build()
                        .toFacility())
            .collect(Collectors.toList());
    BingResponse bingResponse = bingLocationSearch(patientAddress);
    Coordinates patientCoordinates = getBingResourceCoordinates(bingResponse);
    facilities
        .parallelStream()
        .forEach(
            facility ->
                facility.driveMinutes(
                    bingDrivetimeSearch(patientCoordinates, facility.coordinates())
                            .resourceSets()
                            .get(0)
                            .resources()
                            .get(0)
                            .travelDurationTraffic()
                        / 60));
    boolean communityCareEligible =
        checkIfEligibleForCommunityCare(patientAddress, establishedPatient, facilities);
    return new CommunityCareResult(communityCareEligible, facilities);
  }
}

package gov.va.api.health.healthwhere.service.controller;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.healthwhere.service.Address;
import gov.va.api.health.healthwhere.service.BingClient;
import gov.va.api.health.healthwhere.service.BingLocationResponse;
import gov.va.api.health.healthwhere.service.Coordinates;
import gov.va.api.health.healthwhere.service.Facility;
import gov.va.api.health.healthwhere.service.VaFacilitiesResponse;
import gov.va.api.health.healthwhere.service.WaitDays;
import lombok.SneakyThrows;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Controller
public class HomeController {
  private String vaFacilitiesApiKey;

  private final RestTemplate restTemplate;

  public HomeController(
      @Value("${va-facilities.api-key}") String vaFacilitiesApiKey,
      @Autowired RestTemplate restTemplate) {
    this.vaFacilitiesApiKey = vaFacilitiesApiKey;
    this.restTemplate = restTemplate;
  }

  private static List<Facility> generateTestFacilities() {
    Address addressOne =
        new Address("50 Irving Street, Northwest", "Washington", "DC", "20422-0001");
    WaitDays waitOne = new WaitDays(23, 2);
    Facility facilityOne =
        new Facility(
            "vha_688", "Washington VA Medical Center", addressOne, "202-745-8000", waitOne, 42);
    return Collections.singletonList(facilityOne);
  }

  /** Search by address and service type. */
  @GetMapping(value = {"/search"})
  @ResponseBody
  public List<Facility> search(
      @RequestParam(value = "street") String street,
      @RequestParam(value = "city") String city,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "zip") String zip,
      @RequestParam(value = "serviceType") String serviceType) {
    Address patientAddress = new Address(street, city, state, zip);

    BingClient bingClient =
        new BingClient(
            "http://dev.virtualearth.net/REST/v1/Locations",
            "ApoyeQuWwOoDGnRxHQT9UpW-jE4XTZLzddpPJtRHzWmyHxzp71nZlpBPKWwh0wLC");
    BingLocationResponse bingLocationResponse = bingClient.lookupAddress(patientAddress);
    Coordinates patientCoordinates = bingLocationResponse.getBingResourceCoordinates();
    // Coordinates patientCoordinates = new Coordinates(38.9311137, -77.0109110499999);

    return vaFacilitySearch(patientCoordinates, serviceType);
  }

  @SneakyThrows
  private List<Facility> vaFacilitySearch(Coordinates coordinates, String serviceType) {
    String url =
        UriComponentsBuilder.fromHttpUrl(
                "https://dev-api.va.gov/services/va_facilities/v0/facilities")
            .queryParam("lat", coordinates.latitude())
            .queryParam("long", coordinates.longitude())
            .queryParam("type", "health")
            .queryParam("page", 1)
            .queryParam("per_page", 30)
            .toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.add("apiKey", vaFacilitiesApiKey);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);

    ObjectMapper objectMapper =
        JacksonConfig.createMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    ResponseEntity<String> entity =
        restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    String body = entity.getBody();
    log.error(
        "va facilities api response: "
            + objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(body)));

    VaFacilitiesResponse responseObject = objectMapper.readValue(body, VaFacilitiesResponse.class);
    log.error(
        "response object: "
            + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseObject));

    // TODO convert response object into list of facilities, filtered by serviceType

    // try {
    //    } catch (HttpClientErrorException.NotFound e) {
    //      throw new NotFound(query);
    //    } catch (HttpClientErrorException.BadRequest e) {
    //      throw new BadRequest(query);
    //    } catch (HttpStatusCodeException e) {
    //      throw new SearchFailed(query);
    //    }

    return generateTestFacilities();
  }
}

package gov.va.api.health.healthwhere.service.controller;

import gov.va.api.health.healthwhere.service.Address;
import gov.va.api.health.healthwhere.service.BingClient;
import gov.va.api.health.healthwhere.service.BingLocationResponse;
import gov.va.api.health.healthwhere.service.Coordinates;
import gov.va.api.health.healthwhere.service.Facility;
import gov.va.api.health.healthwhere.service.WaitDays;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class HomeController {

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

    BingClient bingClient = new BingClient("http://dev.virtualearth.net/REST/v1/Locations",
        "ApoyeQuWwOoDGnRxHQT9UpW-jE4XTZLzddpPJtRHzWmyHxzp71nZlpBPKWwh0wLC");

    BingLocationResponse bingLocationResponse = bingClient.lookupAddress(patientAddress);

    Coordinates patientCoordinates = bingLocationResponse.getBingResourceCoordinates();

    return vaFacilitySearch(patientCoordinates);
  }

  private List<Facility> generateTestFacilities() {
    List<Facility> facilities = new ArrayList<>();
    Address addressOne =
        new Address("50 Irving Street, Northwest", "Washington", "DC", "20422-0001");
    WaitDays waitOne = new WaitDays(23, 2);
    Facility facilityOne =
        new Facility(
            "vha_688", "Washington VA Medical Center", addressOne, "202-745-8000", waitOne, 42);
    facilities.add(facilityOne);
    return facilities;
  }

  private Coordinates lookupFacilityCoordinate(Address address) {
    return new Coordinates(38.9311137, -77.0109110499999);
  }

  private List<Facility> vaFacilitySearch(Coordinates coordinates) {
    return generateTestFacilities();
  }
}

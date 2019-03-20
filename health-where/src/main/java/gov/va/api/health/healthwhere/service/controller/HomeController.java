package gov.va.api.health.healthwhere.service.controller;

import gov.va.api.health.healthwhere.service.Address;
import gov.va.api.health.healthwhere.service.Coordinates;
import gov.va.api.health.healthwhere.service.Facility;
import gov.va.api.health.healthwhere.service.WaitDays;
import java.io.IOException;
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

  /**
   * Search by address and service type.
   */
  @GetMapping(value = {"/search"})
  @ResponseBody
  public List<Facility> search(
      @RequestParam(value = "street") String street,
      @RequestParam(value = "city") String city,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "zip") String zip,
      @RequestParam(value = "serviceType") String serviceType) throws IOException {

    Address patientAddress = new Address(street, city, state, zip);

    Coordinates patientCoordinates = lookupFacilityCoordinate(patientAddress);

    //TODO: use patient Coordinates to call va facilities api

    return generateTestFacilities();
  }

  private Coordinates lookupFacilityCoordinate(Address address) {

    //TODO Utilize Bing Maps API to to convert address to lat/long (required for VA facilities api)
    //http://dev.virtualearth.net/REST/v1/Locations/US/{adminDistrict}/{postalCode}/{locality}/{addressLine}
    // ?includeNeighborhood={includeNeighborhood}&include={includeValue}&maxResults={maxResults}&key={BingMapsAPIKey}

    return new Coordinates(38.9311137,-77.0109110499999);
  }

  private List<Facility> vaFacilitySearch(Coordinates coordinates) {

    //TODO: Utlize VA Facilities API to identify nearby facilities
    //https://dev-api.va.gov/services/va_facilities/v0/facilities
    //with parameters ?lat=____&long=____&facility_type=health

    return generateTestFacilities();
  }

  private List<Facility> generateTestFacilities() {

    List<Facility> facilities = new ArrayList<>();

    Address addressOne = new Address(
        "50 Irving Street, Northwest",
        "Washington",
        "DC",
        "20422-0001"
    );

    WaitDays waitOne = new WaitDays(23, 2);

    Facility facilityOne = new Facility(
        "vha_688",
        "Washington VA Medical Center",
        addressOne,
        "202-745-8000",
        waitOne,
        42
    );

    facilities.add(facilityOne);

    return facilities;
  }
}


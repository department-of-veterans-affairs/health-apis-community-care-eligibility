package gov.va.api.health.healthwhere.service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {HomeController.class})
public class HomeControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  @SneakyThrows
  public void badSearch() {

    mvc.perform(get("/search")).andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  public void search() {

    mvc.perform(
            get("/search")
                .param("street", "12 Irving Street")
                .param("city", "Washington")
                .param("state", "DC")
                .param("zip", "20422")
                .param("serviceType", "PrimaryCare"))
        .andExpect(status().isOk());
  }

  @Test
  @SneakyThrows
  public void callBingService() {

    mvc.perform(
        get("http://dev.virtualearth.net/REST/v1/Locations")
            .param("addressLine", "12 Irving Street")
            .param("locality", "Washington")
            .param("adminDistrict", "DC")
            .param("postalCode", "20422")
            .param("countryRegion", "US")
            .param("maxResults", "1")
            .param("key", "ApoyeQuWwOoDGnRxHQT9UpW-jE4XTZLzddpPJtRHzWmyHxzp71nZlpBPKWwh0wLC"))
        .andExpect(status().isOk());
  }

}

package gov.va.api.health.healthwhere.service.controller;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {HomeController.class})
public class HomeControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  @SneakyThrows
  public void helloWorld() {
    mvc.perform(get("/"))
        .andExpect(status().isOk());
  }
}

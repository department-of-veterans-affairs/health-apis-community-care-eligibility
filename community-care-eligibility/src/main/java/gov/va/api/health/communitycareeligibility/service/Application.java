package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.queenelizabeth.ee.impl.QueenElizabethConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SuppressWarnings("WeakerAccess")
@SpringBootApplication
@EnableScheduling
@Import({QueenElizabethConfig.class})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

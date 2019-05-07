package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.EligibilityCodes;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class EligibilityAndEnrollmentTransformer {
  @NonNull private final VceEligibilityInfo eligibilityInfo;

  EligibilityCodes toEligibilities() {
    String description = eligibilityInfo.getVceDescription();
    String code = eligibilityInfo.getVceCode();

    if (allBlank(description, code)) {
      return null;
    }
    Instant now = Instant.now();
    if (eligibilityInfo.getVceEffectiveDate().toGregorianCalendar().toInstant().isAfter(now)) {
      return null;
    }
    return EligibilityCodes.builder().code(code).description(description).build();
  }
}

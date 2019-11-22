package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.EligibilityCode;
import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class EligibilityAndEnrollmentTransformer {
  @NonNull private final VceEligibilityInfo eligibilityInfo;

  @NonNull private final Instant timestamp;

  EligibilityCode toEligibility() {
    String description = eligibilityInfo.getVceDescription();
    String code = eligibilityInfo.getVceCode();
    if (allBlank(description, code)) {
      return null;
    }

    if (eligibilityInfo.getVceEffectiveDate() != null
        && eligibilityInfo
            .getVceEffectiveDate()
            .toGregorianCalendar()
            .toInstant()
            .isAfter(timestamp)) {
      return null;
    }

    return EligibilityCode.builder().code(code).description(description).build();
  }
}

package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;
import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.CommunityCareEligibility;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Builder
final class EligibilityAndEnrollmentTransformer {

    CommunityCareEligibility toCommunityCareEligibilities(@NotNull Boolean eligible,@NotBlank String descript) {
    Boolean eligibility = eligible;
    String description = descript;
    if (allBlank(description, eligibility)) {
      return null;
    }
    return CommunityCareEligibility.builder()
        .description(description)
        .eligible(eligibility)
        .build();
  }
}

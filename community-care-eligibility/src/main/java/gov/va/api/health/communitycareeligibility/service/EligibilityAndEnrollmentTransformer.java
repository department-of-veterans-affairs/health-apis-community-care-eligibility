package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.CommunityCareEligibility;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class EligibilityAndEnrollmentTransformer {
  @NonNull private final CommunityCareEligibility communityCareEligibility;

  CommunityCareEligibility toCommunityCareEligibilities() {
    Boolean eligibility = communityCareEligibility.eligible();
    String description = communityCareEligibility.description();
    if (allBlank(description, eligibility)) {
      return null;
    }
    if (description.length() == 0) {
      return null;
    }
    return CommunityCareEligibility.builder()
        .description(description)
        .eligible(eligibility)
        .build();
  }
}

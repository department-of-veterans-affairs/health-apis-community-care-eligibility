package gov.va.api.health.communitycareeligibility.service;

import static gov.va.api.health.communitycareeligibility.service.Transformers.allBlank;

import gov.va.api.health.communitycareeligibility.api.CommunityCareEligibilityResponse.CommunityCareEligibilities;


import gov.va.med.esr.webservices.jaxws.schemas.VceEligibilityInfo;
import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;
import java.util.Date;

@Builder
final class EligibilityAndEnrollmentTransformer {
    @NonNull private final VceEligibilityInfo eligibilityInfo;
    CommunityCareEligibilities toCommunityCareEligibilities() {
        String description = eligibilityInfo.getVceDescription();
        String code = eligibilityInfo.getVceCode();

        String effectiveDate = (eligibilityInfo.getVceEffectiveDate()).toGregorianCalendar().getTime().toInstant().toString();
        if (allBlank(description, code, effectiveDate)) {
            return null;
        }
        return CommunityCareEligibilities.builder()
                .code(code)
                .description(description)
                .effectiveDate(effectiveDate)
                .build();
    }


}

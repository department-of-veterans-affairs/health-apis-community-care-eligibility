package gov.va.api.health.communitycareeligibility.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    security =
        @SecurityRequirement(
            name = "OauthFlow",
            scopes = {"patient/CommunityCareEligibility.read"}),
    info =
        @Info(
            title = "Community Care Eligibility",
            version = "v0",
            description =
                "## Background"
                    + "\n\n"
                    + "Community Care Eligibility is computed based on "
                    + "[rules defined in the VA MISSION Act of 2018](https://www.va.gov/COMMUNITYCARE/programs/veterans/General_Care.asp#Eligibility)"
                    + ", which includes criteria that can change depending on a "
                    + "Veteran's situation, such as their distance from a VA facility.\n"
                    + "The Community Care Eligibility (CCE) API uses the VA Facilities API, the "
                    + "VA's Enrollment & Eligibility (E&E) system and rules from the "
                    + "[VA MISSION Act of 2018](https://missionact.va.gov/) to determine "
                    + "eligibility. "
                    + "\n"
                    + "The Community Care Eligibility API allows consumers to provide Veterans the "
                    + "ability to know if they meet the following MISSION Act requirements:\n"
                    + "\n"
                    + "- If a Veteran needs a service (one of the services returned from VA's "
                    + "Facility API) it will show the nearest VA facility that meets the Veteran's "
                    + "needs, calculating average drive times. Please see the Facility endpoint "
                    + "for drive time calculation specifics\n"
                    + "- If a Veteran needs a service and there are no VA Facilities in the "
                    + "Veteran's state or territory, it will display the Veteran's community care "
                    + "eligibility\n"
                    + "- Using the E&E system it will identify if a Veteran is eligible for "
                    + "the \"Grandfather\" Provision\n"
                    + "- If VA cannot furnish care within certain designated access standards, "
                    + "it will display a Veteran's community care eligibility"
                    + "\n\n"
                    + "## Eligibility Determination"
                    + "\n\n"
                    + "The Community Care Eligibility API bases access standards on drive times "
                    + "only, not appointment wait times. The last two criteria, **best "
                    + "medical interest** and **quality standards**, are subjective criteria "
                    + "outside the scope of this API. Because the subjective criteria are not "
                    + "included, this API's eligibility decisions ***are not final***. A "
                    + "user-facing message based on the result of this API should stress that the "
                    + "patient is **probably** eligible or **probably not** eligible, and that no "
                    + "decision is final until they have consulted VA staff and scheduled their "
                    + "appointment."
                    + "\n\n"
                    + "## Authorization"
                    + "\n\n"
                    + "This API uses the [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html)"
                    + " standard (OAuth 2) to allow the "
                    + "person being confirmed to log in and provide digital consent. "
                    + "API requests are authorized using a Bearer token issued through "
                    + "an OpenID Connect service. The token should be submitted as an "
                    + "Authorization header in the form Bearer.\n\n"
                    + "See our [Authorization Guide](https://developer.va.gov/explore/health/docs/authorization)"
                    + " for more details."
                    + "\n\n"
                    + "## Partial Success States"
                    + "\n\n"
                    + "It should be noted that partial success states can occur. "
                    + "Partial success states occur when:\n"
                    + "- Address geocoding is not available\n"
                    + "- Address geocoding is out of date compared to the users address\n"
                    + "- Address geocoding is incomplete (e.g. missing latitude)"
                    + "\n\n"
                    + "## Additional Notes"
                    + "\n\n"
                    + "- Community Care Eligibility API does not check whether a Veteran is "
                    + "registered at a particular facility. A Veteran is ineligible (or not "
                    + "automatically eligible) if they are within 30/60 min drive of any VAMC that "
                    + "offers the requested service type.\n"
                    + "- For the Primary Care service type, a Veteran is also ineligible (or not "
                    + "automatically eligible) if they have an 'Active' or 'Pending' Patient-"
                    + "Aligned Care Team (PACT) status.\n"
                    + "- If the patient has any of the G/N/H codes (grandfathered, state with no "
                    + "full-service VA med facility, or hardship), they are eligible by code. If "
                    + "they have X (Ineligible), they are ineligible by code. Any other codes "
                    + "(such as B/Basic), or no code, indicates that eligibility by code is "
                    + "indeterminate, so the drive time calculation is performed."),
    externalDocs =
        @ExternalDocumentation(
            description = "GitHub",
            url =
                "https://github.com/department-of-veterans-affairs/health-apis-community-care-eligibility"))
@SecurityScheme(
    type = SecuritySchemeType.OAUTH2,
    name = "OauthFlow",
    flows =
        @OAuthFlows(
            authorizationCode =
                @OAuthFlow(
                    authorizationUrl = "https://sandbox-api.va.gov/oauth2/authorization",
                    tokenUrl = "https://sandbox-api.va.gov/oauth2/token",
                    scopes = {
                      @OAuthScope(
                          name = "patient/CommunityCareEligibility.read",
                          description = "Community Care Eligibility")
                    })))
@Path("/")
public interface CommunityCareEligibilityService {
  @Operation(
      summary =
          "Compute community care eligibility by patient ICN and desired medical service type",
      tags = "Search")
  @GET
  @Path("search")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CommunityCareEligibilityResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.BadRequest.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.NotFound.class)))
  CommunityCareEligibilityResponse search(
      @Parameter(
              in = ParameterIn.QUERY,
              required = true,
              name = "patient",
              description = "The patient ICN")
          @NotBlank
          String patientIcn,
      @Parameter(
              in = ParameterIn.QUERY,
              required = true,
              name = "serviceType",
              description = "Patient's desired medical service type for community care",
              schema =
                  @Schema(
                      allowableValues = {
                        "Audiology",
                        "Cardiology",
                        "Dermatology",
                        "Gastroenterology",
                        "Gynecology",
                        "MentalHealthCare",
                        "Nutrition",
                        "Ophthalmology",
                        "Optometry",
                        "Orthopedics",
                        "Podiatry",
                        "PrimaryCare",
                        "Urology",
                        "WomensHealth"
                      }))
          @NotBlank
          String serviceType,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "extendedDriveMin",
              description =
                  "Optional extended drive-radius to include more VA medical facilities in response"
                      + " (Does not change overall eligibility."
                      + " Must exceed standard drive time for service-type.)")
          @Max(90)
          Integer driveMin);
}

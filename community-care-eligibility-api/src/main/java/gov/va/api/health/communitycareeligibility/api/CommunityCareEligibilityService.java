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
                    + "Community care eligibility is computed based on "
                    + "[rules defined in the MISSION Act](https://www.va.gov/COMMUNITYCARE/programs/veterans/General_Care.asp#Eligibility)"
                    + ", which includes criteria that can change depending on a "
                    + "Veteran's situation, such as their distance from a VA facility."
                    + " Because of this, the Community Care Eligibility"
                    + " API does not provide a **final** eligibility decision. "
                    + "Any message based on this API's results should say that the patient "
                    + " is **probably** eligible or **probably not** eligible, "
                    + "and that no decision is final until VA staff is consulted and an"
                    + " appointment is scheduled."
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
                    + "Partial success states occur due to:\n"
                    + "- Address geocoding is not available\n"
                    + "- Address geocoding is out of date compared to the users address\n"
                    + "- Address geocoding is incomplete (e.g. missing latitude)"),
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

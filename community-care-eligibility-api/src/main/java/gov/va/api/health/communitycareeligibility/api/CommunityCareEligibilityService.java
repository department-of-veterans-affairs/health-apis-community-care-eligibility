package gov.va.api.health.communitycareeligibility.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
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
              scopes = {
                      "patient/Audiology",
                      "patient/Cardiology",
                      "patient/Dermatology",
                      "patient/Gastroenterology",
                      "patient/Gynecology",
                      "patient/MentalHealthCare",
                      "patient/Ophthalmology",
                      "patient/Optometry",
                      "patient/Orthopedics",
                      "patient/PrimaryCare",
                      "patient/Urology",
                      "patient/WomensHealth"
              }
      ),
  info =
      @Info(
        title = "Community Care Eligibility",
        version = "v0",
        description =
            "Compute community care eligibility under the **objective** criteria"
                + " of the MISSION Act. Because MISSION Act also includes"
                + " subjective criteria, this API does not provide a **final**"
                + " eligibility decision. Any user-facing message based on these"
                + " results should indicate that the patient is *probably* eligible"
                + " or *probably not* eligible, and that no decision is final until"
                + " they have consulted VA staff and"
                + " scheduled their appointment."
      ),
  externalDocs =
      @ExternalDocumentation(
        description = "GitHub",
        url =
            "https://github.com/department-of-veterans-affairs/health-apis-community-care-eligibility"
      )
)

@SecurityScheme(
    type = SecuritySchemeType.OAUTH2,
    name = "OauthFlow",
    in = SecuritySchemeIn.HEADER,
        flows =
                @OAuthFlows(
                        implicit =
                                @OAuthFlow(
                        authorizationUrl = "https://dev-api.va.gov/oauth2/authorization",
                        tokenUrl = "https://dev-api.va.gov/services/fhir/v0/dstu2/token",
                        scopes = {
                                @OAuthScope(name = "patient/Audiology", description = "Eligibility for Audiology"),
                                @OAuthScope(name = "patient/Cardiology", description = "Eligibility for Cardiology"),
                                @OAuthScope(name = "patient/Dermatology", description = "Eligibility for Dermatology"),
                                @OAuthScope(name = "patient/Gastroenterology", description = "Eligibility for Gastroenterology"),
                                @OAuthScope(name = "patient/Gynecology", description = "Eligibility for Gynecology"),
                                @OAuthScope(name = "patient/MentalHealthCare", description = "Eligibility for MentalHealthCare"),
                                @OAuthScope(name = "patient/Ophthalmology", description = "Eligibility for Ophthalmology"),
                                @OAuthScope(name = "patient/Optometry", description = "Eligibility for Optometry"),
                                @OAuthScope(name = "patient/Orthopedics", description = "Eligibility for Orthopedics"),
                                @OAuthScope(name = "patient/PrimaryCare", description = "Eligibility for PrimaryCare"),
                                @OAuthScope(name = "patient/Urology", description = "Eligibility for Urology"),
                                @OAuthScope(name = "patient/WomensHealth", description = "Eligibility for WomensHealth")
                        }
                    )
                )
)

@Path("/")
public interface CommunityCareEligibilityService {
  @Operation(
    summary = "Compute community care eligibility by patient ICN and desired medical service type",
    tags = "Search"
  )
  @GET
  @Path("search")
  @ApiResponse(
    responseCode = "200",
    description = "Record found",
    content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = CommunityCareEligibilityResponse.class)
        )
  )
  @ApiResponse(
    responseCode = "400",
    description = "Bad request",
    content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.BadRequest.class)
        )
  )
  @ApiResponse(
    responseCode = "404",
    description = "Not found",
    content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.NotFound.class)
        )
  )
  CommunityCareEligibilityResponse search(
      String optSessionIdHeader,
      @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            name = "patient",
            description = "The patient ICN"
          )
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
                    "Ophthalmology",
                    "Optometry",
                    "Orthopedics",
                    "PrimaryCare",
                    "Urology",
                    "WomensHealth"
                  }
                )
          )
          @NotBlank
          String serviceType,
      @Parameter(
            in = ParameterIn.QUERY,
            name = "extendedDriveMin",
            description =
                "Optional extended drive-radius to include more VA medical facilities in response"
                    + " (Does not change overall eligibility."
                    + " Must exceed standard drive time for service-type.)"
          )
          @Max(90)
          Integer driveMin);
}

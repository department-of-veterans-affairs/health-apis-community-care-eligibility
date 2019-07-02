package gov.va.api.health.communitycareeligibility.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@OpenAPIDefinition(
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
                    "Nutrition",
                    "Optometry",
                    "Podiatry",
                    "PrimaryCare"
                  }
                )
          )
          @NotBlank
          String serviceType);
}

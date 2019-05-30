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
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@OpenAPIDefinition(
  info =
      @Info(
        title = "Community Care Eligibility",
        version = "v0",
        description =
            "Compute *objective* community care eligibility under the criteria of the MISSION Act."
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
    summary =
        "Compute community care eligibility by patient ICN, patient home address,"
            + " desired medical service type, and patient establishment."
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
    description = "Not found",
    content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class)
        )
  )
  @ApiResponse(
    responseCode = "404",
    description = "Bad request",
    content =
        @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class)
        )
  )
  CommunityCareEligibilityResponse search(
      @Parameter(in = ParameterIn.QUERY, required = true, name = "patient") String patientIcn,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "street") String street,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "city") String city,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "state") String state,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "zip") String zip,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "serviceType") String serviceType,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "establishedPatient")
          Boolean establishedPatient);
}

package gov.va.api.health.communitycareeligibility.api;

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
        version = "v1",
        description = "Community care eligibility under the MISSION Act."
      )
)
@Path("/")
public interface CommunityCareEligibilityService {
  @Operation(
    summary = "Community Care Eligibility Search",
    tags = {"Community Care Eligibility"}
  )
  @GET
  @Path("CommunityCareEligibility")
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
  CommunityCareEligibilityResponse communityCareEligibilitySearch(
      @Parameter(in = ParameterIn.QUERY, required = true, name = "serviceType") String serviceType,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "patient") String patientIcn,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "street") String street,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "city") String city,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "state") String state,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "zip") String zip);
}

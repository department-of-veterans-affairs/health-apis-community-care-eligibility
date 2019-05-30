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
            name = "street",
            description = "Street of patient's home address"
          )
          @NotBlank
          String street,
      @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            name = "city",
            description = "City of patient's home address"
          )
          @NotBlank
          String city,
      @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            name = "state",
            description = "State of patient's home address"
          )
          @NotBlank
          String state,
      @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            name = "zip",
            description = "ZIP code of patient's home address"
          )
          @NotBlank
          String zip,
      @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            name = "serviceType",
            description = "The patient's desired medical service type",
            schema =
                @Schema(
                  allowableValues = {
                    "Audiology",
                    "Cardiology",
                    "Dermatology",
                    "EmergencyCare",
                    "Gastroenterology",
                    "Gynecology",
                    "MentalHealthCare",
                    "Ophthalmology",
                    "Optometry",
                    "Orthopedics",
                    "PrimaryCare",
                    "UrgentCare",
                    "Urology",
                    "WomensHealth"
                  }
                )
          )
          @NotBlank
          String serviceType,
      @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            name = "establishedPatient",
            description = "Whether or not this is an established patient"
          )
          Boolean establishedPatient);
}

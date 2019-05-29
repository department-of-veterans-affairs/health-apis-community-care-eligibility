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
        description = "Community care eligibility under the MISSION Act."
        // FHIR (Fast Healthcare Interoperability Resources) specification defines a
        // set of "Resources" that represent granular clinical, financial, and administrative
        // concepts.  This CoverageEligibilityResponse resource is compliant with FHIR version R4
        // standards.

        // The Community Care Eligibility API utilizes the Facility API to determine Veteran wait
        // and drive time, and computes a Veteran's community care eligibility status under the
        // criteria of the MISSION Act. This API provides access via an EHR-agnostic RESTful web
        // services abstraction layer.

        //        This API is a [Spring Boot](https://spring.io/projects/spring-boot) microservice
        //        	that computes *objective* overall community-care eligibility by combining
        // eligibility codes
        //        	from the Eligibility and Enrollment System (E&E) with wait- and drive-time access
        //        	standards.
        //        	(Average historical wait times are provided by Facilities API. Average drive
        // times are computed by Bing Maps. )
        //
        //        	Bing Maps is expected to be removed in the near future, when average drive times
        // are supplied
        //        	by the Facilities API. Historical wait times from the Facilities API are expected
        // to be replaced
        //        	with live wait times from the VA Online Scheduling System.
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
        "Compute community care eligibility by patient ICN, patient home address, desired medical service type, and patient establishment."
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
  CommunityCareEligibilityResponse communityCareEligibilitySearch(
      @Parameter(in = ParameterIn.QUERY, required = true, name = "serviceType") String serviceType,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "patient") String patientIcn,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "street") String street,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "city") String city,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "state") String state,
      @Parameter(in = ParameterIn.QUERY, required = true, name = "zip") String zip);
}

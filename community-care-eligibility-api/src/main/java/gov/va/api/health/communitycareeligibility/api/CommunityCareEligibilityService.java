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
//  servers = {
//    @Server(
//      url = "https://dev-api.va.gov/services/argonaut/v0/",
//      description = "Development server"
//    )
//  },
//  externalDocs =
//      @ExternalDocumentation(
//        description = "Argonaut Data Query Implementation Guide",
//        url = "http://www.fhir.org/guides/argonaut/r2/index.html"
//      )
@Path("/")
public interface CommunityCareEligibilityService {
  //  ConditionApi,
  //  DiagnosticReportApi,
  //  ImmunizationApi,
  //  MedicationOrderApi,
  //  MedicationApi,
  //  MedicationStatementApi,
  //  MetadataApi,
  //  ObservationApi,
  //  PatientApi,
  //  ProcedureApi

  //  @Operation(
  //    summary = "Allergy Intolerance Read",
  //    description =
  //
  // "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html",
  //    tags = {"Allergy Intolerance"}
  //  )
  //  @GET
  //  @Path("AllergyIntolerance/{id}")
  //  @ApiResponse(
  //    responseCode = "200",
  //    description = "Record found",
  //    content =
  //        @Content(
  //          mediaType = "application/json+fhir",
  //          schema = @Schema(implementation = AllergyIntolerance.class)
  //        )
  //  )
  //  @ApiResponse(
  //    responseCode = "400",
  //    description = "Not found",
  //    content =
  //        @Content(
  //          mediaType = "application/json+fhir",
  //          schema = @Schema(implementation = OperationOutcome.class)
  //        )
  //  )
  //  @ApiResponse(
  //    responseCode = "404",
  //    description = "Bad request",
  //    content =
  //        @Content(
  //          mediaType = "application/json+fhir",
  //          schema = @Schema(implementation = OperationOutcome.class)
  //        )
  //  )
  //  AllergyIntolerance allergyIntoleranceRead(
  //      @Parameter(in = ParameterIn.PATH, name = "id", required = true) String id);

  @Operation(
    summary = "Community Care Eligibility Search",
    // description =
    // "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html",
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

  class CommunityCareEligibilityServiceException extends RuntimeException {
    CommunityCareEligibilityServiceException(String message) {
      super(message);
    }
  }

  //  class SearchFailed extends CommunityCareEligibilityServiceException {
  //    @SuppressWarnings("WeakerAccess")
  //    public SearchFailed(String id, String reason) {
  //      super(id + " Reason: " + reason);
  //    }
  //  }

  //  class UnknownResource extends CommunityCareEligibilityServiceException {
  //    @SuppressWarnings("WeakerAccess")
  //    public UnknownResource(String id) {
  //      super(id);
  //    }
  //  }
}

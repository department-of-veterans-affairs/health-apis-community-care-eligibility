package gov.va.api.health.communitycareeligibility.api;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JacksonXmlRootElement(localName = "PatientSummary")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PcmmResponse {

  @Builder.Default
  public List<PatientAssignmentsAtStation> patientAssignmentsAtStations = new ArrayList<>();

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class PatientAssignmentsAtStation {
    List<PrimaryCareAssignment> primaryCareAssignments;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class PrimaryCareAssignment {
    @JacksonXmlProperty PactStatus assignmentStatus;

    public enum PactStatus {
      None,
      Pending,
      Active
    }
  }

  // many used fields.
  // Only PACT status is retrieved from the "assignmentStatus" field
}

package gov.va.api.health.communitycareeligibility.tests;

public class TempPcmmTest {

  //
  //    public static void main(String[] args) throws IOException, SAXException,
  // ParserConfigurationException {
  //
  ////        PcmmResponse response =
  ////                new XmlMapper()
  ////                        .registerModule(new StringTrimModule())
  ////                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  ////                        .readValue(new
  // URL("http://localhost:8319/pcmmr_web/ws/patientSummary/icn/1012667674V820648"),
  // PcmmResponse.class);
  //
  //        PcmmResponse response =
  //
  //            ExpectedResponse.of(
  //                    RestAssured.given()
  //                            .contentType(ContentType.XML)
  //                        .relaxedHTTPSValidation()
  //                        .request(
  //                            Method.GET,
  //
  // "http://localhost:8319/pcmmr_web/ws/patientSummary/icn/1013060957V646684"))
  //                    .mapper(
  //                            new XmlMapper()
  //                                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  //                    )
  //                    .expectValid(PcmmResponse.class);
  //        System.out.println("The response: " + response);
  //
  //        PcmmResponse testSerializer =
  //                PcmmResponse.builder()
  //                        .patientAssignmentsAtStation(
  //                            List.of(
  //                                PcmmResponse.PatientAssignmentsAtStation.builder()
  //                                    .primaryCareAssignment(
  //                                        List.of(
  //                                            PcmmResponse.PrimaryCareAssignment.builder()
  //                                                    .assignmentStatus("Active")
  //                                            .build()
  //                                        )
  //                                    )
  //                                .build()
  //                            )
  //                        )
  //                .build();
  //
  //        System.out.println("PCMM response  :   " + testSerializer);
  //
  //        System.out.println("pcmm Serialized:   " +
  //                new
  // XmlMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writeValueAsString(testSerializer));
  //
  //        System.out.println("blank:        " +
  //                new
  // XmlMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).writeValueAsString(PcmmResponse.builder().build()));
  //
  //        // todo multiple levels of null checking
  //        if (response.patientAssignmentsAtStation != null) {
  //            for (PcmmResponse.PatientAssignmentsAtStation p :
  // response.patientAssignmentsAtStation) {
  //              for (PcmmResponse.PrimaryCareAssignment pca : p.primaryCareAssignment()) {
  //                System.out.println("Found: " + pca.assignmentStatus());
  //              }
  //            }
  //        }
  //
  ////        for(PcmmResponse.PatientAssignmentsAtStations p :
  // response.patientAssignmentsAtStation) {
  ////            for(PcmmResponse.PrimaryCareAssignment pca : p.primaryCareAssignment()) {
  ////                System.out.println("Found: " + pca.assignmentStatus());
  ////            }
  ////        }
  //
  ////        String url =
  ////                UriComponentsBuilder.fromHttpUrl(
  ////
  // "http://localhost:8319/pcmmr_web/ws/patientSummary/icn/1012667674V820648")
  ////                        .build().toUriString();
  //
  ////        String response = insecureRestTemplate.exchange(url, HttpMethod.GET, new
  // HttpEntity<>(new HttpHeaders()), String.class)
  ////                .getBody();
  //
  ////        List<PcmmResponse.PatientAssignmentsAtStation> patientAssignmentsAtStations =
  ////                new XmlMapper()
  ////                .registerModule(new StringTrimModule())
  ////                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  ////                .readValue(response, PcmmResponse.class).patientAssignmentsAtStation();
  ////
  ////        for(PcmmResponse.PatientAssignmentsAtStation p : patientAssignmentsAtStations) {
  ////            for(PcmmResponse.PrimaryCareAssignment pca : p.primaryCareAssignment()) {
  ////                System.out.println("Found: " + pca.assignmentStatus());
  ////            }
  ////        }
  //
  //    }
  //
  //    private static final class StringTrimModule extends SimpleModule {
  //        StringTrimModule() {
  //            addDeserializer(
  //                    String.class,
  //                    new StdScalarDeserializer<>(String.class) {
  //                        @Override
  //                        @SneakyThrows
  //                        public String deserialize(JsonParser p, DeserializationContext ctxt) {
  //                            return trimToNull(p.getValueAsString());
  //                        }
  //                    });
  //        }
  //    }

}

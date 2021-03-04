package gov.va.api.health.communitycareeligibility.service;

import gov.va.api.health.communitycareeligibility.api.PcmmResponse;

public interface PcmmClient {
  PcmmResponse pactStatusByIcn(String patientIcn);
}

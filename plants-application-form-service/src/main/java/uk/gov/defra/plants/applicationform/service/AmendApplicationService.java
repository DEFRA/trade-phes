package uk.gov.defra.plants.applicationform.service;

import static java.util.Collections.singletonList;
import static uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus.SUBMITTED;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class AmendApplicationService {

  private final BackendServiceAdapter backendServiceAdapter;
  private final HealthCertificateServiceAdapter healthCertificateServiceAdapter;
  private final ApplicationFormRepository applicationFormRepository;
  private final ApplicationFormDAO applicationFormDAO;
  private static final String INSPECTION_COMPLETE_STATUS = "InspectionComplete";

  public void checkApplicationAmendable(Long applicationFormId) {

    PersistentApplicationForm persistentApplicationForm =
        applicationFormRepository.load(applicationFormDAO, applicationFormId);
    if (persistentApplicationForm.getStatus().equals(SUBMITTED)
        && !isAllowedToAmend(persistentApplicationForm)) {
      throw new ForbiddenException(
          "Amend is not allowed for application with id " + persistentApplicationForm.getId());
    }
  }

  private boolean isAllowedToAmend(PersistentApplicationForm persistentApplicationForm) {

    HealthCertificate healthCertificate =
        getHealthCertificate(persistentApplicationForm.getEhcNumber());

    if (healthCertificate.isAmendable()) {
      Map<Long, ApplicationTradeStatus> caseStatusMap =
          backendServiceAdapter.getCaseStatusesForApplications(
              singletonList(persistentApplicationForm.getId()), 1,
              persistentApplicationForm.getExporterOrganisation());
      return caseStatusMap
          .get(persistentApplicationForm.getId())
          .getTradeApiStatus()
          .equalsIgnoreCase(INSPECTION_COMPLETE_STATUS);
    }
    return false;
  }

  private HealthCertificate getHealthCertificate(final String ehcName) {
    return healthCertificateServiceAdapter
        .getHealthCertificate(ehcName)
        .orElseThrow(
            () ->
                new NotFoundException(
                    String.format("Could not retrieve healthCertificate: %s", ehcName)));
  }
}

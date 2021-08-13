package uk.gov.defra.plants.applicationform.service;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus.CANCELLATION_REQUESTED;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.EXPORTER_ACTION;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.PREPARING_PHYTO;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.PROCESSING;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.RETURNED;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.SCHEDULED;
import static uk.gov.defra.plants.common.constants.ApplicationStatus.WITH_INSPECTOR;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormDAO;
import uk.gov.defra.plants.applicationform.dao.ApplicationFormRepository;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.backend.adapter.BackendServiceAdapter;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.common.constants.ApplicationStatus;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ApplicationService {

  private final Jdbi jdbi;
  private final BackendServiceAdapter backendServiceAdapter;
  private final ApplicationFormRepository applicationFormRepository;

  public void cancelApplication(final Long applicationId) {
    jdbi.useTransaction(
        h -> {
          final PersistentApplicationForm applicationForm =
              applicationFormRepository.load(
                  h.attach(ApplicationFormDAO.class), applicationId);

          switch (applicationForm.getStatus()) {
            case DRAFT:
              throw new BadRequestException(
                  format("Application %s cannot be cancelled as it is in DRAFT status.",
                      applicationId));
            case CANCELLATION_REQUESTED:
              LOGGER.debug("Application {} is already in CANCELLATION_REQUESTED status!",
                  applicationId);
              break;
            default:
              if (isValidStatusForCancellation(applicationId, applicationForm.getExporterOrganisation())) {
                backendServiceAdapter.cancelApplication(applicationId);
                applicationFormRepository
                    .update(h.attach(ApplicationFormDAO.class),
                        applicationForm.toBuilder().status(CANCELLATION_REQUESTED).build());
              }
          }

        });
  }

  private boolean isValidStatusForCancellation(final Long applicationId, UUID organisationId) {
    Map<Long, ApplicationTradeStatus> caseStatusMap = backendServiceAdapter
        .getCaseStatusesForApplications(singletonList(applicationId), 1, organisationId);

    ApplicationStatus applicationStatus = caseStatusMap.get(applicationId).getApplicationStatus();

    final List<ApplicationStatus> validStatusesForCancellation = Arrays
        .asList(PROCESSING, WITH_INSPECTOR, SCHEDULED, EXPORTER_ACTION, PREPARING_PHYTO, RETURNED);

    return validStatusesForCancellation.contains(applicationStatus);
  }

}

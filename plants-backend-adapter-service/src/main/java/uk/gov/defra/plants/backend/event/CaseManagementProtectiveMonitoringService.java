package uk.gov.defra.plants.backend.event;

import static uk.gov.defra.plants.common.constants.RequestTracing.CORRELATION_HEADER;
import static uk.gov.defra.plants.common.constants.RequestTracing.REMOTE_HOST;
import static uk.gov.defra.plants.common.eventhub.model.ComponentName.CASE_MANAGEMENT;
import static uk.gov.defra.plants.common.eventhub.model.EventPriority.NORMAL;

import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.eventhub.service.EventCreator;
import uk.gov.defra.plants.common.eventhub.service.EventHubService;
import uk.gov.defra.plants.common.security.User;

@Slf4j
public class CaseManagementProtectiveMonitoringService {

  private final EventHubService eventHubService;
  private EventCreator eventCreator;

  @Inject
  public CaseManagementProtectiveMonitoringService(final EventHubService eventHubService,
      final EventCreator eventCreator) {
    this.eventHubService = eventHubService;
    this.eventCreator = eventCreator;
  }

  public void  publishCaseEvents(
      @NonNull final User user,
      final CaseProcessingResult caseProcessingResult,
      final String additionalInfo) {
      eventHubService.publishEvents(ImmutableList.of(
        getCaseEvent(user, caseProcessingResult, additionalInfo)));
  }

  private Event getCaseEvent(
      final User user, final CaseProcessingResult caseProcessingResult, final String additionalInfo) {
    return eventCreator.getEvent(
        user,
        MDC.get(CORRELATION_HEADER),
        CASE_MANAGEMENT,
        NORMAL,
        caseProcessingResult.getPmcCode(),
        caseProcessingResult.getMessage(),
        MDC.get(REMOTE_HOST),
        additionalInfo);
  }

}

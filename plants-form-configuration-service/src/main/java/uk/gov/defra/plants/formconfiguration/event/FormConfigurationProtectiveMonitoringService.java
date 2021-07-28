package uk.gov.defra.plants.formconfiguration.event;

import static uk.gov.defra.plants.common.constants.RequestTracing.CORRELATION_HEADER;
import static uk.gov.defra.plants.common.constants.RequestTracing.REMOTE_HOST;
import static uk.gov.defra.plants.common.eventhub.model.ComponentName.FORM_CONFIG;
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
public class FormConfigurationProtectiveMonitoringService {

  private final EventHubService eventHubService;
  private final EventCreator eventCreator;

  @Inject
  public FormConfigurationProtectiveMonitoringService(final EventHubService eventHubService,
      final EventCreator eventCreator) {
    this.eventHubService = eventHubService;
    this.eventCreator = eventCreator;
  }

  public void publishFormEvents(
      @NonNull final User user,
      final FormProcessingResult formProcessingResult,
      final String message) {
    eventHubService.publishEvents(ImmutableList.of(
        getFormEvent(user, formProcessingResult, message)));
  }

  private Event getFormEvent(
      final User user, final FormProcessingResult formProcessingResult, final String message) {
    return eventCreator.getEvent(
        user,
        MDC.get(CORRELATION_HEADER),
        FORM_CONFIG,
        NORMAL,
        formProcessingResult.getPmcCode(),
        formProcessingResult.getMessage(),
        MDC.get(REMOTE_HOST),
        message);
  }

}

package uk.gov.defra.plants.filestorage.service;

import static uk.gov.defra.plants.common.constants.RequestTracing.CORRELATION_HEADER;
import static uk.gov.defra.plants.common.constants.RequestTracing.REMOTE_HOST;
import static uk.gov.defra.plants.common.eventhub.model.ComponentName.FILE_STORAGE;

import java.util.List;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.eventhub.service.EventCreator;
import uk.gov.defra.plants.common.eventhub.service.EventHubService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.enums.FileEvent;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class FileStorageProtectiveMonitoringService {

  private final EventCreator eventCreator;
  private final EventHubService eventHubService;

  public Event getFileStorageEvent(User user, String fileName, String additionalInfo,
      FileEvent event) {
    String file = "File: ";
    return eventCreator.getEvent(
        user,
        MDC.get(CORRELATION_HEADER),
        FILE_STORAGE,
        event.getPriority(),
        event.getEventCode(),
        file + fileName + " " + event.getName(),
        MDC.get(REMOTE_HOST),
        additionalInfo);
  }

  public void publishFileStorageEvents(List<Event> events, String eventsStageName) {
    boolean result = eventHubService.publishEvents(events);
    if (result) {
      LOGGER.debug("{} have been sent successfully.", eventsStageName);
    } else {
      LOGGER.warn("Failed to send {} to event hub", eventsStageName);
    }
  }
}

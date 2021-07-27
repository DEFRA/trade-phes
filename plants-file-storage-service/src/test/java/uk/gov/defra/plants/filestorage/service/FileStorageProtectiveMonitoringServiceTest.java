package uk.gov.defra.plants.filestorage.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.RequestTracing.CORRELATION_HEADER;
import static uk.gov.defra.plants.common.constants.RequestTracing.REMOTE_HOST;
import static uk.gov.defra.plants.common.eventhub.model.PMCCode.UPLOAD_FILE;
import static uk.gov.defra.plants.common.security.UserRoles.EXPORTER_ROLE;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.apache.log4j.MDC;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.common.constants.RequestTracing;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.eventhub.model.EventDetails;
import uk.gov.defra.plants.common.eventhub.model.EventPriority;
import uk.gov.defra.plants.common.eventhub.service.EventCreator;
import uk.gov.defra.plants.common.eventhub.service.EventHubService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.filestorage.enums.FileEvent;

@RunWith(MockitoJUnitRunner.class)
public class FileStorageProtectiveMonitoringServiceTest {

  private final UUID USER_ID = UUID.randomUUID();
  private final UUID SESSION_ID = UUID.randomUUID();

  @Mock
  private EventHubService eventHubService;
  @Mock
  private Clock clock;
  private final User user = User.builder().userId(USER_ID).role(EXPORTER_ROLE).build();
  private FileStorageProtectiveMonitoringService fileStorageProtectiveMonitoringService;

  private String currentUTCTimeInString;

  @Before
  public void before() {
    MDC.clear();
    MDC.put(CORRELATION_HEADER, SESSION_ID.toString());
    MDC.put(RequestTracing.REMOTE_HOST, REMOTE_HOST);
    Instant now = Instant.now();
    when(clock.instant()).thenReturn(now);
    when(clock.withZone(ZoneId.of("UTC"))).thenReturn(clock);
    currentUTCTimeInString = now.toString();
    fileStorageProtectiveMonitoringService = new FileStorageProtectiveMonitoringService(
        new EventCreator(clock, "OTH"), eventHubService);
  }

  @Test
  public void testGetFileStorageEvent() {

    Event event = fileStorageProtectiveMonitoringService
        .getFileStorageEvent(user, "test.pdf", "test additional info", FileEvent.UPLOAD_STARTED);

    verifyEvent(event, UPLOAD_FILE.getCode(), UPLOAD_FILE.getTransactionCode(),
        "File: test.pdf upload started",
        EventPriority.NORMAL, "test additional info");
  }

  private void verifyEvent(
      Event actualEvent,
      String expectedPmcCode,
      String expectedTransactionId,
      String expectedMessage,
      EventPriority expectedPriority,
      String expectedAddInfo) {
    Event expectedEvent =
        Event.builder()
            .user("IDM/" + USER_ID)
            .sessionId(SESSION_ID.toString())
            .utcDateTimeString(currentUTCTimeInString)
            .version("1.1")
            .environment("OTH")
            .application("DESPEU02")
            .component("FileStorageService")
            .ip(REMOTE_HOST)
            .pmcCode(expectedPmcCode)
            .priority(expectedPriority)
            .details(
                EventDetails.builder()
                    .transactionCode(expectedTransactionId)
                    .message(expectedMessage)
                    .additionalInfo(expectedAddInfo)
                    .build())
            .build();

    assertEquals(expectedEvent, actualEvent);
  }
}

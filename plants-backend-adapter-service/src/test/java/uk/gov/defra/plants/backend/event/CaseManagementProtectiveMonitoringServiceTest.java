package uk.gov.defra.plants.backend.event;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_CLOSED;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_CREATED;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_DECISION_UPDATED;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_STATUS_UPDATED;
import static uk.gov.defra.plants.backend.event.CaseProcessingResult.CASE_UPDATED;
import static uk.gov.defra.plants.common.constants.RequestTracing.CORRELATION_HEADER;
import static uk.gov.defra.plants.common.constants.RequestTracing.REMOTE_HOST;
import static uk.gov.defra.plants.common.eventhub.model.EventPriority.NORMAL;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;
import uk.gov.defra.plants.common.eventhub.model.Event;
import uk.gov.defra.plants.common.eventhub.model.EventDetails;
import uk.gov.defra.plants.common.eventhub.model.EventPriority;
import uk.gov.defra.plants.common.eventhub.service.EventCreator;
import uk.gov.defra.plants.common.eventhub.service.EventHubService;
import uk.gov.defra.plants.common.security.User;

@RunWith(JUnitParamsRunner.class)
public class CaseManagementProtectiveMonitoringServiceTest {

  private final String HOST = "1.1.1.1";
  private UUID SESSION_ID =UUID.randomUUID();
  private UUID USER_ID = UUID.randomUUID();
  private final User user = User.builder().userId(USER_ID).build();
  private final String MESSAGE = "TEST MESSAGE";
  private EventHubService eventHubService = Mockito.mock(EventHubService.class);
  private Clock clock = Mockito.mock(Clock.class);

  private ArgumentCaptor<ArrayList<Event>> eventListCaptor = ArgumentCaptor.forClass(ArrayList.class);

  private CaseManagementProtectiveMonitoringService service;

  private String currentUTCTimeInString;

  @Before
  public void setUp() {
    initMocks(this);
    when(eventHubService.publishEvents(anyList())).thenReturn(true);
    Instant now = Instant.now();
    when(clock.instant()).thenReturn(now);
    when(clock.withZone(ZoneId.of("UTC"))).thenReturn(clock);

    currentUTCTimeInString = now.toString();
    MDC.put(CORRELATION_HEADER,SESSION_ID.toString());
    MDC.put(REMOTE_HOST,HOST);
    service = new CaseManagementProtectiveMonitoringService(eventHubService, new EventCreator(
        clock, "OTH"));
  }

  @Test
  @Parameters(method = "testRequestParameters")
  public void publishCaseEvents(final CaseProcessingResult caseProcessingResult,
      String expectedPmcCode, String expectedTransactionId, String expectedMessage ) {
    service.publishCaseEvents(user,caseProcessingResult, MESSAGE);
    verify(eventHubService).publishEvents(eventListCaptor.capture());
    List<Event> passedList = eventListCaptor.getValue();
    verifyEvent(passedList.get(0), expectedPmcCode, expectedTransactionId,
        expectedMessage,
        NORMAL, MESSAGE);
  }

  private void verifyEvent(
      Event actualEvent,
      String expectedPmcCode,
      String expectedTransactionId,
      String expectedMessage,
      EventPriority expectecPriority,
      String expectedAddInfo) {
    Event expectedEvent =
        Event.builder()
            .user("AAD/" + USER_ID)
            .sessionId(SESSION_ID.toString())
            .utcDateTimeString(currentUTCTimeInString)
            .version("1.1")
            .environment("OTH")
            .application("DESPEU02")
            .component("CaseManagementService")
            .ip(HOST)
            .pmcCode(expectedPmcCode)
            .priority(expectecPriority)
            .details(
                EventDetails.builder()
                    .transactionCode(expectedTransactionId)
                    .message(expectedMessage)
                    .additionalInfo(expectedAddInfo)
                    .build())
            .build();

    assertEquals(expectedEvent, actualEvent);
  }
  @SuppressWarnings("unused")
  private static Object[][] testRequestParameters() {
    return new Object[][]{
        {CASE_CREATED, "0706", "0101", "Application submitted"},
        {CASE_CLOSED, "0706", "0104", "Closed certificate"},
        {CASE_UPDATED, "0706", "0102", "Application updated"},
        {CASE_DECISION_UPDATED, "0706", "0103", "Certificate Decision is recorded"},
        {CASE_STATUS_UPDATED, "0706", "0105", "Updated certificate status"}
    };
  }
}

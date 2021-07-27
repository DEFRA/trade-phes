package uk.gov.defra.plants.backend.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.ApplicationTradeStatus;
import uk.gov.defra.plants.backend.representation.TraderApplication;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.backend.service.TradeAPIApplicationService;
import uk.gov.defra.plants.common.constants.ApplicationStatus;
import uk.gov.defra.plants.common.security.EnrolledOrganisation;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.factory.ResourceTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIApplicationResourceTest {

  private static final UUID TEST_TRADER_APPLICATION_ID = UUID.randomUUID();
  private static final UUID TEST_APPLICANT_ID = UUID.randomUUID();
  private static final UUID TEST_ORGANISATION_ID = UUID.fromString("a5276cd0-fec0-40a7-a1a9-4e16360e447c");
  private static final String SEARCH_TYPE_APPLICANT = "APPLICANT";
  private static final User TEST_USER = User.builder()
      .userId(UUID.randomUUID())
      .selectedOrganisation(Optional.of(EnrolledOrganisation
          .builder()
          .exporterOrganisationId(TEST_ORGANISATION_ID)
          .build()))
      .build();


  private static final TradeAPIApplicationService TRADE_API_APPLICATION_SERVICE =
      mock(TradeAPIApplicationService.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestFactory.buildRule(
          TEST_USER, new TradeAPIApplicationResource(TRADE_API_APPLICATION_SERVICE));

  private static final TraderApplication TRADER_APPLICATION =
      TraderApplication.builder()
          .traderApplicationId(TEST_TRADER_APPLICATION_ID)
          .applicationId(1L)
          .countryCode("FR")
          .countryName("FRANCE")
          .applicantId(TEST_USER.getUserId())
          .referenceNumber("test_ref")
          .status("DISPATCHED")
          .organisationId(TEST_ORGANISATION_ID.toString())
          .build();

  private static final TraderApplicationsSummary TRADER_APPLICATIONS_SUMMARY =
      TraderApplicationsSummary.builder()
          .data(Collections.singletonList(TRADER_APPLICATION))
          .build();

  @Before
  public void setUp() {
    reset(TRADE_API_APPLICATION_SERVICE);
  }

  @Test
  public void testGetTraderApplications() {
    when(TRADE_API_APPLICATION_SERVICE.getTraderApplications(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(TRADER_APPLICATIONS_SUMMARY);

    final Response response =
        resources
            .target("/application-forms/list")
            .queryParam("pageNumber", 1)
            .queryParam("count", 5)
            .queryParam("contactIdToSearch", TEST_APPLICANT_ID)
            .queryParam("userSearchType", SEARCH_TYPE_APPLICANT)

            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.readEntity(String.class)).isNotEmpty();
    verify(TRADE_API_APPLICATION_SERVICE, times(1))
        .getTraderApplications(TEST_USER, null, Collections.emptyList(), 1,
            5, TEST_APPLICANT_ID, SEARCH_TYPE_APPLICANT);
  }

  @Test
  public void testGetStatusesForApplications() {
    final ApplicationTradeStatus applicationTradeStatus = ApplicationTradeStatus.builder()
        .applicationStatus(ApplicationStatus.PROCESSING)
        .tradeApiStatus("Submitted")
        .build();

    Map<Long, ApplicationTradeStatus> applicationStatusesMap = Map.of(
        3L, applicationTradeStatus, 4L, applicationTradeStatus);

    UUID organisationId = UUID.randomUUID();
    when(TRADE_API_APPLICATION_SERVICE
        .getStatusesForApplications(Arrays.asList(1L, 2L), 30, organisationId, TEST_USER))
        .thenReturn(applicationStatusesMap);

    Map<Long, ApplicationTradeStatus> applicationStatusResponseMap =
        resources
            .target("/application-forms/application-statuses")
            .queryParam("applicationFormIds", 1L)
            .queryParam("applicationFormIds", 2L)
            .queryParam("pageSize", 30)
            .queryParam("organisationId", organisationId)
            .request()
            .get(new GenericType<Map<Long, ApplicationTradeStatus>>() {});

    assertThat(applicationStatusResponseMap).isEqualTo(applicationStatusesMap);
    verify(TRADE_API_APPLICATION_SERVICE, times(1))
        .getStatusesForApplications(Arrays.asList(1L, 2L), 30, organisationId, TEST_USER);
  }
}

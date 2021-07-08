package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.http.NameValuePair;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import uk.gov.defra.plants.backend.representation.TraderApplication;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.common.json.ItemsMapper;

@RunWith(MockitoJUnitRunner.class)
public class TradeApiAdapterTest {

  private static final String APPLICATIONS_URI = "/trade-application-store/v1";
  private static final String APPLICATIONS_RESOURCE_NAME = "/application";

  private static final String ZERO = "0";
  @Mock
  private Response response;
  @Mock
  private TradeApiRequestFactory tradeApiRequestFactory;
  @Mock
  private TradeApiGet tradeApiGet;
  @Mock
  private TradeApiRequestProcessor tradeApiRequestProcessor;

  private TradeApiAdapter tradeApiAdapter;

  private static final UUID TEST_TRADER_APPLICATION_ID = UUID.randomUUID();
  private static final UUID TEST_APPLICANT_ID = UUID.randomUUID();

  private static final TraderApplication TRADER_APPLICATION =
      TraderApplication.builder()
          .traderApplicationId(TEST_TRADER_APPLICATION_ID)
          .applicantId(TEST_APPLICANT_ID)
          .applicationId(1L)
          .countryCode("FR")
          .countryName("FRANCE")
          .referenceNumber("test_ref")
          .status("DISPATCHED")
          .build();

  @Before
  public void before() {
    // establish MDC entries are set by ContainerLoggingFilter
    MDC.put("defra-exports-correlation-depth", ZERO);
    MDC.put("defra-exports-correlation-count", ZERO);
    MDC.put("defra-exports-correlation-count-this-service", ZERO);

    when(tradeApiRequestProcessor.execute(tradeApiGet)).thenReturn(response);
    tradeApiAdapter = new TradeApiAdapter(tradeApiRequestFactory, tradeApiRequestProcessor);
  }

  @Test
  public void testQueryList_success() {

    TraderApplicationsSummary traderApplicationsSummary =
        TraderApplicationsSummary.builder()
            .data(Collections.singletonList(TRADER_APPLICATION))
            .build();

    stubQueryEndpoint(traderApplicationsSummary);

    List<NameValuePair> nameValuePairs = new ArrayList<>();

    when(tradeApiRequestFactory.createGet(APPLICATIONS_URI, APPLICATIONS_RESOURCE_NAME, nameValuePairs)).thenReturn(tradeApiGet);
    List<TraderApplication> traderApplications = tradeApiAdapter.getTraderApplicationSummary(nameValuePairs).getData();

    Assertions.assertThat(traderApplications.get(0)).isEqualTo(TRADER_APPLICATION);
  }

  private void stubQueryEndpoint(
      TraderApplicationsSummary traderApplicationsSummary) {
    when(response.readEntity(String.class))
        .thenReturn(ItemsMapper.toJson(traderApplicationsSummary));
  }
}
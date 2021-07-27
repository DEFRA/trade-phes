package uk.gov.defra.plants.backend.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.referencedata.BotanicalItem;
import uk.gov.defra.plants.backend.service.TradeAPIReferenceDataService;
import uk.gov.defra.plants.common.security.User;
import uk.gov.defra.plants.commontest.factory.ResourceTestFactory;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIReferenceDataResourceTest {

  private static final User TEST_USER = User.builder().userId(UUID.randomUUID()).build();

  private static final TradeAPIReferenceDataService TRADE_API_REFERENCE_DATA_SERVICE =
      mock(TradeAPIReferenceDataService.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestFactory.buildRule(
          TEST_USER, new TradeAPIReferenceDataResource(TRADE_API_REFERENCE_DATA_SERVICE));

  private List<BotanicalItem> botanicalItems = List.of(BotanicalItem.builder().build());

  @Before
  public void setUp() {
    reset(TRADE_API_REFERENCE_DATA_SERVICE);
  }

  @Test
  public void testGetEppoInfo() {
    when(TRADE_API_REFERENCE_DATA_SERVICE.getEppoInformation())
        .thenReturn(botanicalItems);

    final Response response =
        resources
            .target("/referencedata/botanical-info")
            .request()
            .get();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.readEntity(String.class)).isNotEmpty();
    verify(TRADE_API_REFERENCE_DATA_SERVICE, times(1))
        .getEppoInformation();
  }
}
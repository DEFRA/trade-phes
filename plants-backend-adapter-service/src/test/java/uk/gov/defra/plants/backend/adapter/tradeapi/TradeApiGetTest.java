package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TradeApiGetTest {
  private static final String TRADE_API_SUBSCRIPTION_KEY_NAME = "";
  private static URI TRADE_API_SUBSCRIPTION_KEY = UriBuilder.fromUri("TRADE_API_SUBSCRIPTION_KEY").build();
  private static URI SERVER_URI = UriBuilder.fromUri("http://test.com").build();

  @Mock
  private Client client;
  @Mock
  private WebTarget webTarget;
  @Mock
  private Builder builder;
  @Mock
  private Response response;

  private TradeApiGet tradeApiGet;
  private Response returnedResponse;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void performsGet() {
    givenATradeApiGet();
    whenIExecuteTheGet();
    thenTheResponseIsReturned();
  }

  private void givenATradeApiGet() {
    when(client.target(SERVER_URI)).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
    when(builder.header(TRADE_API_SUBSCRIPTION_KEY_NAME, TRADE_API_SUBSCRIPTION_KEY)).thenReturn(builder);
    when(builder.get()).thenReturn(response);
    tradeApiGet = new TradeApiGet(client, TRADE_API_SUBSCRIPTION_KEY_NAME, TRADE_API_SUBSCRIPTION_KEY,
        SERVER_URI);
  }

  private void whenIExecuteTheGet() {
    returnedResponse = tradeApiGet.execute();
  }

  private void thenTheResponseIsReturned() {
    assertThat(returnedResponse, is(response));
  }
}
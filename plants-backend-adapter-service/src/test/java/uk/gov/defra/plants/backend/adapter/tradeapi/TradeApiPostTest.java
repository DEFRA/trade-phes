package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TradeApiPostTest {
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
  @Mock
  private Entity entity;

  private TradeApiPost tradeApiPost;
  private Response returnedResponse;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void performsPost() {
    givenATradeApiPost();
    whenIExecuteThePost();
    thenTheResponseIsReturned();
  }


  private void givenATradeApiPost() {
    when(client.target(SERVER_URI)).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
    when(builder.header(TRADE_API_SUBSCRIPTION_KEY_NAME, TRADE_API_SUBSCRIPTION_KEY)).thenReturn(builder);
    when(builder.post(entity, Response.class)).thenReturn(response);
    tradeApiPost = new TradeApiPost(client, TRADE_API_SUBSCRIPTION_KEY_NAME, TRADE_API_SUBSCRIPTION_KEY,
        SERVER_URI, entity);
  }

  private void whenIExecuteThePost() {
    returnedResponse = tradeApiPost.execute();
  }

  private void thenTheResponseIsReturned() {
    assertThat(returnedResponse, is(response));
  }
}
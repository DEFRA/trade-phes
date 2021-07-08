package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.NameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TradeApiRequestFactoryTest {
  private static final String RESOURCE_URI = "RESOURCE_URI";
  private static final String RESOURCE_NAME = "RESOURCE_NAME";
  private static final List<NameValuePair> QUERY_PARAMS = new ArrayList<>();

  private static final String TRADE_API_SUBSCRIPTION_KEY_NAME = "";
  private static java.net.URI TRADE_API_SUBSCRIPTION_KEY = UriBuilder.fromUri("TRADE_API_SUBSCRIPTION_KEY").build();
  private static URI SERVER_URI = UriBuilder.fromUri("http://test.com").build();

  @Mock
  private Client client;

  @Mock private Entity data;

  private TradeApiRequestFactory factory;
  private TradeApiGet tradeApiGet;
  private TradeApiPost tradeApiPost;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void createsGet() {
    givenAFactory();
    whenICreateGet();
    thenTheTradeApiGetIsCreated();
  }

  @Test
  public void createsPost() {
    givenAFactory();
    whenICreatePost();
    thenTheTradeApiPostIsCreated();
  }

  private void givenAFactory() {
    factory = new TradeApiRequestFactory(
      client,
      TRADE_API_SUBSCRIPTION_KEY_NAME,
      TRADE_API_SUBSCRIPTION_KEY,
        SERVER_URI);
  }

  private void whenICreateGet() {
    tradeApiGet = factory.createGet(RESOURCE_URI, RESOURCE_NAME, QUERY_PARAMS);
  }

  private void whenICreatePost() {
    tradeApiPost = factory.createPost(RESOURCE_URI, RESOURCE_NAME, QUERY_PARAMS, data);
  }

  private void thenTheTradeApiGetIsCreated() {
    assertThat(tradeApiGet, is(notNullValue()));
  }

  private void thenTheTradeApiPostIsCreated() {
    assertThat(tradeApiPost, is(notNullValue()));
  }
}
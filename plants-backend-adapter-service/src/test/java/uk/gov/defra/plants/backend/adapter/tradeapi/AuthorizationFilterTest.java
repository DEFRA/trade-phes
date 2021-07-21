package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class AuthorizationFilterTest {

  private static final String AUTHENTICATION_TOKEN = "AUTHENTICATION_TOKEN";
  private static final MultivaluedMap<String, Object> HEADERS = new MultivaluedHashMap<>();

  @Mock
  private TradeApiAuthenticationAdapter tradeApiAuthenticationAdapter;
  @Mock
  private ClientRequestContext clientRequestContext;

  private AuthorizationFilter filter;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void filters() {
    givenAFilter();
    whenIFilter();
    thenTheTokenIsSetOnTheHeaders();
  }

  private void givenAFilter() {
    when(clientRequestContext.getHeaders()).thenReturn(HEADERS);
    when(tradeApiAuthenticationAdapter.authenticate()).thenReturn(AUTHENTICATION_TOKEN);
    filter = new AuthorizationFilter(tradeApiAuthenticationAdapter);
  }

  private void whenIFilter() {
    filter.filter(clientRequestContext);
  }

  private void thenTheTokenIsSetOnTheHeaders() {
    assertThat(HEADERS.getFirst(HttpHeaders.AUTHORIZATION), is("Bearer " + AUTHENTICATION_TOKEN));
  }
}
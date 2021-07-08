package uk.gov.defra.plants.backend.adapter.tradeapi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class CommonHeadersTest {

  private static final MultivaluedMap<String, Object> HEADERS = new MultivaluedHashMap<>();

  @Mock
  private  ClientRequestContext clientRequestContext;

  private CommonHeaders commonHeaders;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void filters() {
    givenACommonHeaders();
    whenIFilter();
    thenTheHeadersAreSet();
  }

  private void givenACommonHeaders() {
    when(clientRequestContext.getHeaders()).thenReturn(HEADERS);
    commonHeaders = new CommonHeaders();
  }

  private void whenIFilter() {
    commonHeaders.filter(clientRequestContext);
  }

  private void thenTheHeadersAreSet() {
    assertThat(HEADERS.getFirst("OData-MaxVersion"), is("4.0"));
    assertThat(HEADERS.getFirst("OData-Version"), is("4.0"));
    assertThat(HEADERS.getFirst(HttpHeaders.ACCEPT), is(MediaType.APPLICATION_JSON));
  }
}
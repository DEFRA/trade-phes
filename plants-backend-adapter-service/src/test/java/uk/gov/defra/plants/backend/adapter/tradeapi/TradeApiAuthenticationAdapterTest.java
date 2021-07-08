package uk.gov.defra.plants.backend.adapter.tradeapi;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.FieldSetter.setField;
import static uk.gov.defra.plants.common.adapter.BaseAdapterCached.Singleton.SINGLETON;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import uk.gov.defra.plants.common.adapter.BaseAdapterCached.Singleton;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.logging.ClientLoggingFilter;
import uk.gov.defra.plants.dynamics.representation.TradeAPIAccessToken;

@RunWith(MockitoJUnitRunner.class)
public class TradeApiAuthenticationAdapterTest {

  @Mock
  private Client authClient;
  @Mock
  private WebTarget target;
  @Mock
  private Response response;
  @Mock
  private Builder builder;

  static final String TEST_URL = "/6f504113-6b64-43f2-ade9-242e05780007/oauth2/token";
  private static final String ZERO = "0";
  private static final String HTTP_LOCALHOST = "http://localhost:";
  private static final String CACHED_TOKEN = "cachedToken";
  private static final long TIME_ONE_HOUR_ADVANCED =
      LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

  private TradeApiAuthenticationAdapter tradeApiAuthenticationAdapter;
  private static final String TEST_TOKEN =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCIsImtpZCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCJ9.eyJhdWQiOiJhcGk6Ly9kZXYtZnV0dXJldHJhZGUtaW50LmRlZnJhLmdvdi51ayIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0L2M5ZDc0MDkwLWI0ZTYtNGIwNC05ODFkLWU2NzU3YTE2MDgxMi8iLCJpYXQiOjE2MDM5MDA0NzYsIm5iZiI6MTYwMzkwMDQ3NiwiZXhwIjoxNjAzOTA0Mzc2LCJhaW8iOiJFMlJnWUZqMThrbVpzdVhkV3c4NVZOK1VOV2QwQVFBPSIsImFwcGlkIjoiOWFiOGE5NTYtOWM1Yy00NmFmLWI0NzgtNDMxOWMyNDlmYTA1IiwiYXBwaWRhY3IiOiIxIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvYzlkNzQwOTAtYjRlNi00YjA0LTk4MWQtZTY3NTdhMTYwODEyLyIsIm9pZCI6ImNmMWI5ZWU4LWQxNGQtNDdmNy04ZWUwLTIwZDM1OWUyMDkyYiIsInJoIjoiMC5BQUFBa0VEWHllYTBCRXVZSGVaMWVoWUlFbGFwdUpwY25LOUd0SGhER2NKSi1nVkhBQUEuIiwicm9sZXMiOlsiVHJhZGUuU3RhdHVzLlJlYWQiXSwic3ViIjoiY2YxYjllZTgtZDE0ZC00N2Y3LThlZTAtMjBkMzU5ZTIwOTJiIiwidGlkIjoiYzlkNzQwOTAtYjRlNi00YjA0LTk4MWQtZTY3NTdhMTYwODEyIiwidXRpIjoiSUdGVUU2d3hra3lyS0c5TUVZMlRBQSIsInZlciI6IjEuMCJ9.HWBGgcBvmmNZYutPl1wN9e1-8pxVfAfPqxsuj5b-6VfC1wv9Dxrh0SlPcHv0M4dee2fSOuNZA6BNspVRIcZ8x9FZsTT-Ue4qPguHX79yiUvQ9tlB7w84_S_Bb6n-XMFAVflGELrNXkRe8JAQ7_Yy4yKgUl4MomXiPvcxHvRSgojfXUZRJWpffHKGEayvNEZOk6dGX5o3IFvbtkG44zTB7vFY4jOI9O9nHFqKCbvJdWkTd8Vg3C970ZJJ5Ln4p5NCD86Zm5GQB5liL0YIEai1KbyKh0blDE7GrvGpj07hEWpP69z2aj6SqLxyz1HwkgtuLgtkOECNANDewyu-xLpLiw";
  private Long TOKEN_TIMESTAMP = 1603904376L;

  private static final TradeAPIAccessToken TEST_VALID_TOKEN =
      TradeAPIAccessToken.builder()
          .accessToken(TEST_TOKEN)
          .expiresOn(TIME_ONE_HOUR_ADVANCED)
          .build();
  private static final TradeAPIAccessToken TOKEN_WITH_INVALID_VALUE =
      TradeAPIAccessToken.builder().expiresOn(TIME_ONE_HOUR_ADVANCED).build();
  private final Cache<Singleton, TradeAPIAccessToken> cachedToken =
      CacheBuilder.newBuilder().expireAfterAccess(59, TimeUnit.MINUTES).build();

  @Before
  public void before() throws Exception {
    MDC.put("defra-exports-correlation-depth", ZERO);
    MDC.put("defra-exports-correlation-count", ZERO);
    MDC.put("defra-exports-correlation-count-this-service", ZERO);

    final TradeApiAdapterConfiguration dynamicsAdapterConfiguration =
        TradeApiAdapterConfiguration.builder()
            .accessTokenUrl(URI.create(HTTP_LOCALHOST + "1234" + TEST_URL))
            .refreshTokenThresholdInSecondsBeforeExpires(30)
            .tradeAPISubscriptionKeyName("trade-api-subscription-key")
            .tradeAPISubscriptionKey(URI.create("trade-api-subscription-key"))
            .build();

    when(authClient.register(any(ClientLoggingFilter.class))).thenReturn(authClient);
    tradeApiAuthenticationAdapter =
        new TradeApiAuthenticationAdapter(authClient, dynamicsAdapterConfiguration);
    setField(
        tradeApiAuthenticationAdapter,
        tradeApiAuthenticationAdapter.getClass().getDeclaredField(CACHED_TOKEN),
        cachedToken);
  }

  @Test
  public void testAuthenticationWithAccessToken() {
    stubExpectedAuthenticationTokenEndpoint(authClient, TEST_VALID_TOKEN);

    String result = tradeApiAuthenticationAdapter.authenticate();

    assertThat(result).isEqualTo(TEST_TOKEN);
  }

  @Test
  public void testAuthenticationWithValidSavedTokenShouldReturnSavedToken() throws Exception {
    stubExpectedAuthenticationTokenEndpoint(authClient, TEST_VALID_TOKEN);
    cachedToken.put(SINGLETON, TEST_VALID_TOKEN);
    String result = tradeApiAuthenticationAdapter.authenticate();

    assertThat(result).isEqualTo(TEST_TOKEN);
  }

  @Test
  public void testAuthenticationWithNoTokenShouldForceToFetch() throws Exception {
    cachedToken.invalidate(SINGLETON);
    stubExpectedAuthenticationTokenEndpoint(authClient, TEST_VALID_TOKEN);

    String result = tradeApiAuthenticationAdapter.authenticate();

    assertThat(result).isEqualTo(TEST_TOKEN);
  }

  @Test
  public void testAuthenticationWithSavedInValidTokenShouldForceToFetch() throws Exception {
    cachedToken.put(SINGLETON, TOKEN_WITH_INVALID_VALUE);
    stubExpectedAuthenticationTokenEndpoint(authClient, TEST_VALID_TOKEN);

    String result = tradeApiAuthenticationAdapter.authenticate();

    assertThat(result).isEqualTo(TEST_TOKEN);
  }

  @Test
  public void testDecodeAccessTokenAndFetchTimeStamp() {
    stubExpectedAuthenticationTokenEndpoint(authClient, TEST_VALID_TOKEN);
    String result = tradeApiAuthenticationAdapter.authenticate();
    assertThat(tradeApiAuthenticationAdapter.getTimeStampFromToken(result))
        .isEqualTo(TOKEN_TIMESTAMP);
  }

  private void stubExpectedAuthenticationTokenEndpoint(Client authClient,
      TradeAPIAccessToken token) {
    when(response.getStatusInfo()).thenReturn(OK);
    when(response.readEntity(String.class)).thenReturn(ItemsMapper.toJson(token));
    when(authClient.target(any(URI.class))).thenReturn(target);
    when(target.request()).thenReturn(builder);
    when(builder.acceptEncoding("gzip", "deflate")).thenReturn(builder);
    when(builder.property(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED))
        .thenReturn(builder);
    when(builder.cacheControl(any(CacheControl.class))).thenReturn(builder);
    when(builder.post(any())).thenReturn(response);
  }

  @Test
  public void testAuthenticationWithInvalidAccess() {
    stubFaultyResponse(authClient);
    assertThatExceptionOfType(InternalServerErrorException.class)
        .isThrownBy(tradeApiAuthenticationAdapter::authenticate);
  }

  private void stubFaultyResponse(Client authClient) {
    when(response.getStatusInfo()).thenReturn(UNAUTHORIZED);
    when(authClient.target(any(URI.class))).thenReturn(target);
    when(target.request()).thenReturn(builder);
    when(builder.acceptEncoding("gzip", "deflate")).thenReturn(builder);
    when(builder.property(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED))
        .thenReturn(builder);
    when(builder.cacheControl(any(CacheControl.class))).thenReturn(builder);
    when(builder.post(any())).thenReturn(response);
  }
}

package uk.gov.defra.plants.backend.adapter.tradeapi;

import static uk.gov.defra.plants.common.adapter.BaseAdapterCached.Singleton.SINGLETON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import uk.gov.defra.plants.common.adapter.BaseAdapterCached;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.common.logging.ClientLoggingFilter;
import uk.gov.defra.plants.dynamics.representation.TradeAPIAccessToken;

@Slf4j
public class TradeApiAuthenticationAdapter extends BaseAdapterCached {

  private static final CacheControl DISABLED_CACHE = new CacheControl();
  private static final String GZIP = "gzip";
  private static final String DEFLATE = "deflate";
  private final Cache<Singleton, TradeAPIAccessToken> cachedToken =
      buildCacheWith(59, TimeUnit.MINUTES);

  static {
    DISABLED_CACHE.setNoCache(true);
    DISABLED_CACHE.setNoStore(true);
  }

  private final Client client;
  private final URI authenticationUrl;
  private final Form authenticationRequestForm;
  private final int refreshTokenThresholdInSecondsBeforeExpires;

  public TradeApiAuthenticationAdapter(
      final Client client, final TradeApiAdapterConfiguration tradeApiAdapterConfiguration) {
    this.client = client.register(new ClientLoggingFilter(LOGGER));
    this.authenticationUrl = tradeApiAdapterConfiguration.getAccessTokenUrl();
    this.refreshTokenThresholdInSecondsBeforeExpires =
        tradeApiAdapterConfiguration.getRefreshTokenThresholdInSecondsBeforeExpires();
    this.authenticationRequestForm =
        new Form()
            .param("client_id", tradeApiAdapterConfiguration.getClientId())
            .param("client_secret", tradeApiAdapterConfiguration.getClientSecret())
            .param("grant_type", tradeApiAdapterConfiguration.getGrantType())
            .param("scope", tradeApiAdapterConfiguration.getScope());
  }

  public String authenticate() {
    final TradeAPIAccessToken token =
        Optional.ofNullable(getToken())
            .filter(tkn -> tkn.hasValidValues(refreshTokenThresholdInSecondsBeforeExpires))
            .orElseGet(
                () -> {
                  cachedToken.invalidate(SINGLETON);
                  return getToken();
                });
    return Optional.ofNullable(token)
        .map(TradeAPIAccessToken::getAccessToken)
        .orElseThrow(
            () -> new InternalServerErrorException("Failed to authenticate with trader API"));
  }

  private TradeAPIAccessToken getToken() {
    try {
      return cachedToken.get(SINGLETON, this::fetchToken);
    } catch (Exception e) {
      throw new InternalServerErrorException(e.getCause());
    }
  }

  @SneakyThrows
  private TradeAPIAccessToken fetchToken() {
    final Response response =
        client
            .target(authenticationUrl)
            .request()
            .acceptEncoding(GZIP, DEFLATE)
            .property(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            .cacheControl(DISABLED_CACHE)
            .post(Entity.form(authenticationRequestForm));
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      try {
        TradeAPIAccessToken token =
            ItemsMapper.fromJson(response.readEntity(String.class), TradeAPIAccessToken.class);
        return token.toBuilder().expiresOn(getTimeStampFromToken(token.getAccessToken())).build();
      } catch (ProcessingException pe) {
        throw new InternalServerErrorException(
            "Exception occurred while reading the token : " + pe);
      }
    }
    final String message =
        String.format(
            "Failed to authenticate with Trader API, status=%s response=%s",
            response.getStatus(), response.readEntity(String.class));
    LOGGER.error(message);
    throw new InternalServerErrorException(message);
  }

  @SneakyThrows
  protected Long getTimeStampFromToken(String accessToken) {
    final Jwt decodedJwt = JwtHelper.decode(accessToken);
    final Map<Object, Object> authClaims =
        new ObjectMapper().readValue(decodedJwt.getClaims(), Map.class);
    if (authClaims != null && authClaims.get("exp") != null) {
      String expiryTimestamp = authClaims.get("exp").toString();
      return Long.valueOf(expiryTimestamp);
    }
    throw new InternalServerErrorException("No expiry timestamp returned from auth call");
  }
}

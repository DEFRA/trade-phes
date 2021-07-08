package uk.gov.defra.plants.backend.adapter.tradeapi;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

public class AuthorizationFilter implements ClientRequestFilter {

  private TradeApiAuthenticationAdapter tradeApiAuthenticationAdapter;

  public AuthorizationFilter(final TradeApiAuthenticationAdapter tradeApiAuthenticationAdapter) {
    this.tradeApiAuthenticationAdapter = tradeApiAuthenticationAdapter;
  }

  @Override
  public void filter(final ClientRequestContext clientRequestContext) {
    final String authenticationToken = tradeApiAuthenticationAdapter.authenticate();

    clientRequestContext
        .getHeaders()
        .putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationToken);
  }
}

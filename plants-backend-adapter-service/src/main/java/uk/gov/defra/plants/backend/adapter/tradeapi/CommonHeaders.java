package uk.gov.defra.plants.backend.adapter.tradeapi;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class CommonHeaders implements ClientRequestFilter {
  @Override
  public void filter(final ClientRequestContext clientRequestContext) {
    clientRequestContext.getHeaders().putSingle("OData-MaxVersion", "4.0");
    clientRequestContext.getHeaders().putSingle("OData-Version", "4.0");
    clientRequestContext.getHeaders().putSingle(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
  }
}
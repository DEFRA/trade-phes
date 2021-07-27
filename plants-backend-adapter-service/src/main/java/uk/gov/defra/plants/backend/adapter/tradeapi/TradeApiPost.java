package uk.gov.defra.plants.backend.adapter.tradeapi;

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradeApiPost implements TradeApiRequest {
  private final Client client;
  private final String tradeAPISubscriptionKeyName;
  private final URI tradeAPISubscriptionKey;
  private final URI serverUri;
  private final Entity<?> data;

  public TradeApiPost(Client client, String tradeAPISubscriptionKeyName,
      URI tradeAPISubscriptionKey, URI serverUri, Entity<?> data) {
    this.client = client;
    this.tradeAPISubscriptionKeyName = tradeAPISubscriptionKeyName;
    this.tradeAPISubscriptionKey = tradeAPISubscriptionKey;
    this.serverUri = serverUri;
    this.data = data;
  }

  @SneakyThrows
  public Response execute() {
    return client
        .target(serverUri)
        .request()
        .header(tradeAPISubscriptionKeyName, tradeAPISubscriptionKey)
        .post(data, Response.class);
  }
}

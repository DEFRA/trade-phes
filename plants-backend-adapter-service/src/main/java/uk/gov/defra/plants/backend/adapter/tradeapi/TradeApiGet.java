package uk.gov.defra.plants.backend.adapter.tradeapi;

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradeApiGet implements TradeApiRequest {
  private final Client client;
  private final String tradeAPISubscriptionKeyName;
  private final URI tradeAPISubscriptionKey;
  private final URI serverUri;

  public TradeApiGet(Client client, String tradeAPISubscriptionKeyName,
      URI tradeAPISubscriptionKey, URI serverUri) {
    this.client = client;
    this.tradeAPISubscriptionKeyName = tradeAPISubscriptionKeyName;
    this.tradeAPISubscriptionKey = tradeAPISubscriptionKey;
    this.serverUri = serverUri;
  }

  @SneakyThrows
  public Response execute() {
      return
          client
              .target(serverUri)
              .request()
              .header(
                  tradeAPISubscriptionKeyName, tradeAPISubscriptionKey)
              .get();
  }
}

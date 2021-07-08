package uk.gov.defra.plants.backend.adapter.tradeapi;

import java.net.URI;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

@AllArgsConstructor(onConstructor = @__({@Inject}))
@Slf4j
public class TradeApiRequestFactory {
  private final Client client;
  private final String tradeAPISubscriptionKeyName;
  private final URI tradeAPISubscriptionKey;
  private final URI serverUri;

  @SneakyThrows
  public TradeApiGet createGet(
      String resourceUri,
      String resourceName,
      List<NameValuePair> queryParams) {

    return new TradeApiGet(
        client,
        tradeAPISubscriptionKeyName,
        tradeAPISubscriptionKey,
        new URIBuilder(serverUri + resourceUri + resourceName)
                    .addParameters(queryParams)
                    .build());
  }

  @SneakyThrows
  public TradeApiPost createPost(
      String resourceUri,
      String resourceName,
      List<NameValuePair> queryParams,
      Entity<?> data) {

    return new TradeApiPost(
        client,
        tradeAPISubscriptionKeyName,
        tradeAPISubscriptionKey,
        new URIBuilder(serverUri + resourceUri + resourceName)
            .addParameters(queryParams)
            .build(),
        data);
  }
}

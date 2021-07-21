package uk.gov.defra.plants.backend.adapter.tradeapi;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import uk.gov.defra.plants.backend.representation.TraderApplicationsSummary;
import uk.gov.defra.plants.common.json.ItemsMapper;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeApiAdapter {

  private static final String APPLICATIONS_URI = "/trade-application-store/v1";
  private static final String APPLICATIONS_RESOURCE_NAME = "/application";

  private final TradeApiRequestFactory tradeApiRequestFactory;
  private final TradeApiRequestProcessor tradeApiRequestProcessor;

  public TraderApplicationsSummary getTraderApplicationSummary(List<NameValuePair> queryParams) {
    final TradeApiGet tradeApiGet =
        tradeApiRequestFactory.createGet(APPLICATIONS_URI, APPLICATIONS_RESOURCE_NAME, queryParams);
    Response response = tradeApiRequestProcessor.execute(tradeApiGet);
    return ItemsMapper.fromJson(response.readEntity(String.class), TraderApplicationsSummary.class);
  }
}

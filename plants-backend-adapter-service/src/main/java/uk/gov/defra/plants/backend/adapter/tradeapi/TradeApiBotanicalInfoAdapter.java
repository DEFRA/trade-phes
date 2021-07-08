package uk.gov.defra.plants.backend.adapter.tradeapi;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItemPagedResult;
import uk.gov.defra.plants.common.json.ItemsMapper;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeApiBotanicalInfoAdapter {

  private static final String REFERENCE_DATA_URI = "/trade-reference-data/v1";
  private static final String EPPO_RESOURCE_NAME = "/eppo/plant/genus-and-species";

  private final TradeApiRequestFactory tradeApiRequestFactory;
  private final TradeApiRequestProcessor tradeApiRequestProcessor;

  public EppoItemPagedResult getEppoInformation(List<NameValuePair> queryParams) {
    final TradeApiGet tradeApiGet =
        tradeApiRequestFactory.createGet(REFERENCE_DATA_URI, EPPO_RESOURCE_NAME, queryParams);
    Response response = tradeApiRequestProcessor.execute(tradeApiGet);
    return ItemsMapper.fromJson(response.readEntity(String.class), EppoItemPagedResult.class);
  }
}

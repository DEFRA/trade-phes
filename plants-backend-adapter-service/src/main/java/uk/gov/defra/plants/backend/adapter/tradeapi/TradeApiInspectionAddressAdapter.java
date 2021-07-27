package uk.gov.defra.plants.backend.adapter.tradeapi;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;
import uk.gov.defra.plants.common.json.ItemsMapper;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeApiInspectionAddressAdapter {

  private static final String ADDRESSES_URI = "/trade-customer-extension/1-internal";
  private static final String INSPECTION_ADDRESSES_RESOURCE_NAME = "/address";
  private static final String APPROVER_TYPE_PHEATS = "Pheats";


  private final TradeApiRequestFactory tradeApiRequestFactory;
  private final TradeApiRequestProcessor tradeApiRequestProcessor;

  public List<InspectionAddress> getInspectionAddresses(final UUID userId, boolean pheatsApplication) {

    List<NameValuePair> queryParams = new ArrayList<>();
    queryParams.add(new BasicNameValuePair("partyIdentifier", userId.toString()));
    queryParams.add(new BasicNameValuePair("partyContactPointTypeCode", "InspAddr"));
    if(pheatsApplication) {
      queryParams.add(new BasicNameValuePair("approverType", APPROVER_TYPE_PHEATS));
    }

    try {
      final TradeApiGet tradeApiGet =
          tradeApiRequestFactory.createGet(
              ADDRESSES_URI, INSPECTION_ADDRESSES_RESOURCE_NAME, queryParams);
      Response response = tradeApiRequestProcessor.execute(tradeApiGet);
      return ItemsMapper.fromJson(
          response.readEntity(String.class), new TypeReference<List<InspectionAddress>>() {});
    } catch (NotFoundException e) {
      return Collections.emptyList();
    }
  }

  public InspectionAddress getInspectionAddress(final UUID selectedLocationId) {
      final TradeApiGet tradeApiGet =
          tradeApiRequestFactory.createGet(
              ADDRESSES_URI,
              INSPECTION_ADDRESSES_RESOURCE_NAME + "/" + selectedLocationId.toString(),
              Collections.emptyList());
      Response response = tradeApiRequestProcessor.execute(tradeApiGet);
      return ItemsMapper.fromJson(response.readEntity(String.class), InspectionAddress.class);
  }

}

package uk.gov.defra.plants.backend.dao;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiBotanicalInfoAdapter;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItemPagedResult;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class TradeAPIReferenceDataDao {

  private static final String PAGE_NUMBER = "pageNumber";
  private static final String PAGE_SIZE = "pageSize";
  private static final Integer DEFAULT_BATCH_SIZE = 1000;
  private final TradeApiBotanicalInfoAdapter tradeApiBotanicalInfoAdapter;

  public EppoItemPagedResult getEppoInformation(Integer pageNumber, Integer pageSize) {

    if (pageNumber == null || pageNumber == 0) {
      pageNumber = 1;
    }
    if (pageSize == null || pageSize == 0) {
      pageSize = DEFAULT_BATCH_SIZE;
    }

    List<NameValuePair> nameValuePairs = new ArrayList<>();
    addPageNumberToQueryParam(pageNumber, nameValuePairs);
    addPageSizeToQueryParam(pageSize, nameValuePairs);

    return tradeApiBotanicalInfoAdapter.getEppoInformation(nameValuePairs);
  }

  private void addPageNumberToQueryParam(Integer pageNumber, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair(PAGE_NUMBER, pageNumber.toString()));
  }

  private void addPageSizeToQueryParam(Integer pageSize, List<NameValuePair> queryParams) {
    queryParams.add(new BasicNameValuePair(PAGE_SIZE, pageSize.toString()));
  }

}

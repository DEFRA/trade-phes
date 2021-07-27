package uk.gov.defra.plants.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.adapter.tradeapi.TradeApiBotanicalInfoAdapter;
import uk.gov.defra.plants.backend.representation.referencedata.EppoCommonName;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItemPagedResult;

@RunWith(MockitoJUnitRunner.class)
public class TradeAPIReferenceDataDaoTest {

  @Mock
  private TradeApiBotanicalInfoAdapter tradeApiBotanicalInfoAdapter;

  @InjectMocks
  private TradeAPIReferenceDataDao tradeAPIReferenceDataDao;

  @Test
  public void testGetEppoInformation() {
    EppoItemPagedResult eppoItemPagedResult =
        EppoItemPagedResult.builder()
            .data(getDataItems())
            .build();

    when(tradeApiBotanicalInfoAdapter.getEppoInformation(any())).thenReturn(eppoItemPagedResult);
    EppoItemPagedResult actual = tradeAPIReferenceDataDao.getEppoInformation(1, 5);

    assertEquals(eppoItemPagedResult, actual);
  }

  @Test
  public void testGetEppoInformationWithNoPageNumber() {
    EppoItemPagedResult eppoItemPagedResult =
        EppoItemPagedResult.builder()
            .data(getDataItems())
            .build();

    when(tradeApiBotanicalInfoAdapter.getEppoInformation(any())).thenReturn(eppoItemPagedResult);

    EppoItemPagedResult actual = tradeAPIReferenceDataDao.getEppoInformation(null, 5);
    assertEquals(eppoItemPagedResult, actual);

    actual = tradeAPIReferenceDataDao.getEppoInformation(0, 5);
    assertEquals(eppoItemPagedResult, actual);
  }

  @Test
  public void testGetEppoInformationWithNoPageSize() {
    EppoItemPagedResult eppoItemPagedResult =
        EppoItemPagedResult.builder()
            .data(getDataItems())
            .build();

    when(tradeApiBotanicalInfoAdapter.getEppoInformation(any())).thenReturn(eppoItemPagedResult);

    EppoItemPagedResult actual = tradeAPIReferenceDataDao.getEppoInformation(1, null);
    assertEquals(eppoItemPagedResult, actual);

    actual = tradeAPIReferenceDataDao.getEppoInformation(1, 0);
    assertEquals(eppoItemPagedResult, actual);
  }

  private List<EppoItem> getDataItems() {
    return Arrays.asList(
        buildEppoItem("51028", "1AARG", "Plant", "Genus", "Gastrococos"),
        buildEppoItem("51029", "1AASG", "Plant", "Species", "Tyranosaurus Rex")
    );
  }

  private EppoItem buildEppoItem(String codeId, String eppoCode, String dataGroup,
      String taxonomicLevel, String fullName) {
    return EppoItem.builder()
        .codeId(codeId)
        .eppoCode(eppoCode)
        .dataGroup(dataGroup)
        .taxonomicLevel(taxonomicLevel)
        .commonName(EppoCommonName.builder()
            .fullName(fullName)
            .build())
        .build();
  }
}
package uk.gov.defra.plants.backend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.plants.backend.representation.referencedata.BotanicalItem;
import uk.gov.defra.plants.backend.representation.referencedata.EppoCommonName;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.service.cache.EppoDataCacheKey;
import uk.gov.defra.plants.backend.service.cache.EppoDataCachePopulator;
import uk.gov.defra.plants.backend.service.cache.EppoDataCacheService;
import uk.gov.defra.plants.backend.service.cache.EppoListCacheService;

public class TradeAPIReferenceDataServiceTest {

  private static final String EPPO_CODE = "EPPO_CODE";
  private static final EppoItem EXPECTED_EPPO_ITEM = EppoItem.builder().eppoCode(EPPO_CODE).preferredName("BRIAN").build();
  public static final String PREFERRED_NAME_1 = "PREFERRED_NAME_1";
  public static final String PREFERRED_NAME_2 = "PREFERRED_NAME_2";
  public static final String EPPO_CODE1 = "1AARG";
  public static final String EPPO_CODE2 = "1AASG";
  public static final String FULL_NAME_1 = "Plant1";
  public static final String FULL_NAME_2 = "Plant2";

  public static final List<BotanicalItem> EPPO_INFORMATION = Arrays.asList(
      BotanicalItem.builder()
          .eppoCode(EPPO_CODE1)
          .preferredName(PREFERRED_NAME_1)
          .commonNames(
              Arrays.asList(EppoCommonName.builder()
                  .fullName(FULL_NAME_1)
                  .build())).build(),
      BotanicalItem.builder()
          .preferredName(PREFERRED_NAME_2)
          .eppoCode(EPPO_CODE2)
          .commonNames(Arrays.asList(EppoCommonName.builder()
              .fullName(FULL_NAME_2)
              .build())).build()
  );

  private static final EppoDataCacheKey DATA_KEY = EppoDataCacheKey.builder().eppoCode(EPPO_CODE).build();

  @Mock
  private EppoDataCacheService eppoDataCacheService;
  @Mock
  private EppoListCacheService eppoListCacheService;
  @Mock
  private EppoDataCachePopulator eppoDataCachePopulator;

  private TradeAPIReferenceDataService tradeAPIReferenceDataService;
  private EppoItem eppoItem;
  private List<BotanicalItem> botanicalItems;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void getsEppoNameForCode() {
    givenAService();
    whenIGetEppoItemByCode(EPPO_CODE);
    thenTheEppoItemIsReturned();
  }

  @Test
  public void getsEppoNameForCodeAndPopulatesCachIfEmpty() {
    givenAService();
    when(eppoDataCacheService.getEppoItem(EPPO_CODE)).thenReturn(null).thenReturn(EXPECTED_EPPO_ITEM);
    whenIGetEppoItemByCode(EPPO_CODE);
    thenTheCacheIsRepopulated();
    thenTheEppoItemIsReturned();
  }

  @Test
  public void getsEppoInformation() {
    givenAService();
    whenIGetEppoInformation();
    thenTheEppoInformationIsReturned();
  }

  @Test
  public void getsEppoInformationAndPopulatesCacheIfEmpty() {
    givenAService();
    when(eppoListCacheService.getEppoList()).thenReturn(null).thenReturn(getDataItems());
    whenIGetEppoInformation();
    thenTheCacheIsRepopulated();
    thenTheEppoInformationIsReturned();
  }

  private void givenAService() {
    when(eppoDataCacheService.asKey(EPPO_CODE)).thenReturn(DATA_KEY);
    when(eppoDataCacheService.getEppoItem(EPPO_CODE)).thenReturn(EXPECTED_EPPO_ITEM);
    when(eppoListCacheService.getEppoList()).thenReturn(getDataItems());
    tradeAPIReferenceDataService = new TradeAPIReferenceDataService(
        eppoDataCacheService,
        eppoListCacheService,
        eppoDataCachePopulator);
  }

  private void whenIGetEppoItemByCode(String eppoCode) {
    eppoItem = tradeAPIReferenceDataService.getEppoNameForCode(eppoCode);
  }

  private void whenIGetEppoInformation() {
    botanicalItems = tradeAPIReferenceDataService.getEppoInformation();
  }

  private void thenTheEppoInformationIsReturned() {
    assertEquals(EPPO_INFORMATION, botanicalItems);
  }

  private void thenTheEppoItemIsReturned() {
    assertThat( eppoItem, is(EXPECTED_EPPO_ITEM));
  }


  private void thenTheCacheIsRepopulated() {
    verify(eppoDataCachePopulator).populate();
  }



  private List<EppoItem> getDataItems() {
    return Arrays.asList(
        buildEppoItem(PREFERRED_NAME_1, EPPO_CODE1, FULL_NAME_1),
        buildEppoItem(PREFERRED_NAME_2, EPPO_CODE2, FULL_NAME_2)
    );
  }

  private EppoItem buildEppoItem(String preferredName, String eppoCode, String fullName) {
    return EppoItem.builder()
        .eppoCode(eppoCode)
        .preferredName(preferredName)
        .commonName(EppoCommonName.builder()
            .fullName(fullName)
            .build())
        .build();
  }

}
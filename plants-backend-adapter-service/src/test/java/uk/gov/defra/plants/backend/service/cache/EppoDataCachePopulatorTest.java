package uk.gov.defra.plants.backend.service.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.cache.Cache;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.MDC;
import uk.gov.defra.plants.backend.dao.TradeAPIReferenceDataDao;
import uk.gov.defra.plants.backend.representation.referencedata.EppoCommonName;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItemPagedResult;
import uk.gov.defra.plants.common.constants.RequestTracing;

public class EppoDataCachePopulatorTest {
  private static final String EPPO_CODE = "EPPO_CODE";
  private static final EppoItem EXPECTED_EPPO_ITEM = EppoItem.builder().eppoCode(EPPO_CODE).preferredName("BRIAN").build();
  public static final String PREFERRED_NAME_1 = "PREFERRED_NAME_1";
  public static final String PREFERRED_NAME_2 = "PREFERRED_NAME_2";
  public static final String EPPO_CODE1 = "1AARG";
  public static final String EPPO_CODE2 = "1AASG";
  public static final String FULL_NAME_1 = "Plant1";
  public static final String FULL_NAME_2 = "Plant2";

  private static final int PAGE_SIZE = 2000;
  private static final Integer PAGE_NUMBER = 1;
  public static final int TOTAL_PAGES = 1;
  private static final String PREFERRED_NAME = "PREFERRED_NAME";
  private static final String COMMON_NAME = "COMMON_NAME";
  private static final EppoItem EPPO_ITEM = EppoItem.builder()
      .eppoCode(EPPO_CODE)
      .preferredName(PREFERRED_NAME)
      .commonNames(
          Arrays.asList(
              EppoCommonName.builder()
                  .fullName(COMMON_NAME)
                  .build()))
      .build();

  private static final EppoItemPagedResult EPPO_PAGED_RESULT = EppoItemPagedResult.builder()
      .totalPages(TOTAL_PAGES).data(Arrays.asList(EPPO_ITEM)).build();
  private static final EppoDataCacheKey DATA_KEY = EppoDataCacheKey.builder().eppoCode(EPPO_CODE).build();

  @Mock
  private TradeAPIReferenceDataDao tradeAPIReferenceDataDao;

  @Mock
  private EppoDataCacheService eppoDataCacheService;
  @Mock
  private EppoListCacheService eppoListCacheService;
  @Mock
  private Cache<EppoDataCacheKey, EppoItem> eppoDataCache;

  private EppoDataCachePopulator loader;

  @Before
  public void beforeEachTest() {
    initMocks(this);
  }

  @Test
  public void populatesEppoCache() {
    givenALoader();
    whenICallPopulate();
    thenTheCacheIsPopulated();
  }

  private void givenALoader() {
    when(tradeAPIReferenceDataDao
        .getEppoInformation(PAGE_NUMBER, PAGE_SIZE)).thenReturn(EPPO_PAGED_RESULT);
    when(eppoDataCacheService.asKey(EPPO_CODE)).thenReturn(DATA_KEY);
    when(eppoDataCacheService.getEppoItem(EPPO_CODE)).thenReturn(EXPECTED_EPPO_ITEM);
    when(eppoListCacheService.getEppoList()).thenReturn(getDataItems());
    loader = new EppoDataCachePopulator(
        tradeAPIReferenceDataDao,
        eppoDataCache,
        eppoDataCacheService,
        eppoListCacheService);
  }

  private void whenICallPopulate() {
    loader.populate();
  }

  private void thenTheCacheIsPopulated() {

    assertThat(MDC.get(RequestTracing.CORRELATION_COUNT), is("0"));
    assertThat(MDC.get(RequestTracing.CORRELATION_HEADER), is(notNullValue()));

    verify(eppoDataCache).put(DATA_KEY, EPPO_ITEM);
    verify(eppoListCacheService).populate(Arrays.asList(EPPO_ITEM));

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
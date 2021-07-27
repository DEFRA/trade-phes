package uk.gov.defra.plants.backend.service.cache;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;

@RunWith(MockitoJUnitRunner.class)
public class EppoDataCacheServiceTest {

  @Mock
  private Cache<EppoDataCacheKey, EppoItem> eppoDataCache;

  @InjectMocks
  private EppoDataCacheService eppoDataCacheService;

  @Test
  public void testGetEppoItem() {
    EppoDataCacheKey testKey = eppoDataCacheService.asKey("testKey");
    EppoItem expectedEppoItem = EppoItem.builder().preferredName("preferredName").build();
    when(eppoDataCache.getIfPresent(testKey)).thenReturn(expectedEppoItem);

    EppoItem actual = eppoDataCacheService.getEppoItem("testKey");

    assertThat(actual, is(expectedEppoItem));
  }

  @Test(expected = NullPointerException.class)
  public void whenNoEppoCodeSupplied_ThrowsException() {
    eppoDataCacheService.getEppoItem(null);
  }
}
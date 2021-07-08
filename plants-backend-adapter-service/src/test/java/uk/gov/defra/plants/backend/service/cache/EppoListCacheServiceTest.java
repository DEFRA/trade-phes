package uk.gov.defra.plants.backend.service.cache;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;

@RunWith(MockitoJUnitRunner.class)
public class EppoListCacheServiceTest {

  private static final String KEY_NAME = "eppoList";

  @Mock
  private Cache<EppoListCacheKey, List<EppoItem>> eppoListCache;

  @InjectMocks
  private EppoListCacheService eppoListCacheService;

  @Test
  public void testGetEppoItem() {
    EppoListCacheKey testKey = eppoListCacheService.asKey(KEY_NAME);
    List<EppoItem> expectedEppoItems = List.of(EppoItem.builder().preferredName("preferredName").build());
    when(eppoListCache.getIfPresent(testKey)).thenReturn(expectedEppoItems);

    List<EppoItem> actual = eppoListCacheService.getEppoList();

    assertThat(actual, is(expectedEppoItems));
  }

  @Test
  public void testPopulate() {
    List<EppoItem> expectedEppoItems = List.of(EppoItem.builder().preferredName("preferredName").build());
    EppoListCacheKey eppoListCacheKey = eppoListCacheService.asKey(KEY_NAME);

    eppoListCacheService.populate(expectedEppoItems);
    verify(eppoListCache).put(eppoListCacheKey, expectedEppoItems);
  }


  @Test(expected = NullPointerException.class)
  public void whenNoKeynameSupplied_ThrowsException() {
    eppoListCacheService.asKey(null);
  }
}
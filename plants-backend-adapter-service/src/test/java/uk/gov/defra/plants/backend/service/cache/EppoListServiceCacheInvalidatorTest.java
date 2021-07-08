package uk.gov.defra.plants.backend.service.cache;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;

import com.google.common.cache.Cache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.common.redis.RedisCacheKey;

@RunWith(MockitoJUnitRunner.class)

public class EppoListServiceCacheInvalidatorTest {
  @Mock
  private Cache<EppoListCacheKey, EppoItem> eppoListCache;

  @InjectMocks
  private EppoListServiceCacheInvalidator eppoListServiceCacheInvalidator;

  @Test
  public void testInvalidateEppoItem() {
    eppoListServiceCacheInvalidator.invalidateEppoList("eppoCode");

    verify(eppoListCache).invalidate(refEq(RedisCacheKey.asKey("eppoCode")));
  }

  @Test(expected = NullPointerException.class)
  public void whenNoKeyNameProvided_ThrowsException() {
    eppoListServiceCacheInvalidator.invalidateEppoList(null);
  }
}
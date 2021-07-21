package uk.gov.defra.plants.backend.service.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import io.dropwizard.util.Duration;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.common.configuration.CacheConfiguration;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.common.redis.RedisCache;

@RunWith(MockitoJUnitRunner.class)
public class EppoListCacheFactoryTest {

  @Mock
  private Redis redis;

  @Mock
  private CaseManagementServiceConfiguration configuration;

  @InjectMocks
  private EppoListCacheFactory eppoListCacheFactory;

  @Mock
  private CacheConfiguration eppoListCache;

  private RedisCache<EppoListCacheKey, List<EppoItem>> createdCache;

  @Before
  public void before() {
    when(configuration.getEppoListCache()).thenReturn(eppoListCache);
    when(configuration.getEppoListCache().getClientErrorExpiry()).thenReturn(Duration.minutes(5));
    when(eppoListCache.getExpiry()).thenReturn(Duration.minutes(5));
  }

  @Test
  public void providesCorrectCache() {
    whenICreateACache();

    assertThat(createdCache, is(not(nullValue())));
    assertThat(redis, is(not(nullValue())));
  }

  @Test
  public void testDispose() {
    whenICreateACache();

    eppoListCacheFactory.dispose(createdCache);
  }

  private void whenICreateACache() {
    createdCache = (RedisCache<EppoListCacheKey, List<EppoItem>>) eppoListCacheFactory.provide();
  }
  
}
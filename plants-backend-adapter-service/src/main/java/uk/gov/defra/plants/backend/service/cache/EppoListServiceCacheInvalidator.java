package uk.gov.defra.plants.backend.service.cache;

import static uk.gov.defra.plants.backend.bundle.EppoDataCacheBundle.EPPO_DATA_CACHE;

import com.google.common.cache.Cache;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.common.redis.RedisCacheKey;

public class EppoListServiceCacheInvalidator {

  private final Cache<EppoDataCacheKey, List<EppoItem>> eppoListCache;

  @Inject
  public EppoListServiceCacheInvalidator(
      @Named(EPPO_DATA_CACHE) final Cache<EppoDataCacheKey, List<EppoItem>> eppoListCache) {
    this.eppoListCache = eppoListCache;
  }

  public void invalidateEppoList(@NonNull final String eppoCode) {
    eppoListCache.invalidate(RedisCacheKey.asKey(eppoCode));
  }

}

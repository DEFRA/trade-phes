package uk.gov.defra.plants.backend.service.cache;

import static uk.gov.defra.plants.backend.bundle.EppoDataCacheBundle.EPPO_DATA_CACHE;

import com.google.common.cache.Cache;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.common.redis.RedisCacheKey;

public class EppoDataServiceCacheInvalidator {

  private final Cache<EppoDataCacheKey, EppoItem> eppoDataCache;

  @Inject
  public EppoDataServiceCacheInvalidator(
      @Named(EPPO_DATA_CACHE) final Cache<EppoDataCacheKey, EppoItem> eppoDataCache) {
    this.eppoDataCache = eppoDataCache;
  }

  public void invalidateEppoItem(@NonNull final String eppoCode) {
    eppoDataCache.invalidate(RedisCacheKey.asKey(eppoCode));
  }

}

package uk.gov.defra.plants.backend.service.cache;

import static uk.gov.defra.plants.backend.bundle.EppoDataCacheBundle.EPPO_DATA_CACHE;

import com.google.common.cache.Cache;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;

public class EppoDataCacheService {
  private final Cache<EppoDataCacheKey, EppoItem> eppoDataCache;

  @Inject
  public EppoDataCacheService(
      @Named(EPPO_DATA_CACHE) final Cache<EppoDataCacheKey, EppoItem> eppoDataCache) {
    this.eppoDataCache = eppoDataCache;
  }

  public EppoItem getEppoItem(@NonNull final String eppoCode) {
    return eppoDataCache.getIfPresent(asKey(eppoCode));
  }

  public EppoDataCacheKey asKey(@NonNull String eppoCode) {
    return EppoDataCacheKey.builder()
        .eppoCode(eppoCode)
        .build();
  }

}

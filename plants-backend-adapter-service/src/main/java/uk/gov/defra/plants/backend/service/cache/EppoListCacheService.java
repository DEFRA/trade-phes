package uk.gov.defra.plants.backend.service.cache;

import static uk.gov.defra.plants.backend.bundle.EppoDataCacheBundle.EPPO_LIST_CACHE;

import com.google.common.cache.Cache;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;

public class EppoListCacheService {

  private static final String EPPO_LIST_KEYNAME = "eppoList";
  private final Cache<EppoListCacheKey, List<EppoItem>> eppoListCache;

  @Inject
  public EppoListCacheService(
      @Named(EPPO_LIST_CACHE) final Cache<EppoListCacheKey, List<EppoItem>> eppoListCache) {
    this.eppoListCache = eppoListCache;
  }

  public List<EppoItem> getEppoList() {
    return eppoListCache.getIfPresent(asKey(EPPO_LIST_KEYNAME));
  }

  public EppoListCacheKey asKey(@NonNull String keyName) {
    return EppoListCacheKey.builder()
        .eppoListKey(keyName)
        .build();
  }

  public void populate(List<EppoItem> eppoItems) {
    eppoListCache.put(asKey(EPPO_LIST_KEYNAME), eppoItems);
  }
}

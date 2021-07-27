package uk.gov.defra.plants.backend.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.api.Factory;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.common.redis.CachedValueMapper;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.common.redis.RedisCache;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EppoDataCacheFactory implements Factory<Cache<EppoDataCacheKey, EppoItem>> {
  private final Redis redis;
  private final CaseManagementServiceConfiguration configuration;

  @Override
  public Cache<EppoDataCacheKey, EppoItem> provide() {

    return RedisCache.<EppoDataCacheKey, EppoItem>builder()
        .redis(redis)
        .keyPrefix("ed")
        .cachedValueMapper(new CachedValueMapper<>(new TypeReference<>() {
        }))
        .valueIsCachable((key, eppoItem) -> true)
        .expirySeconds((int) configuration.getEppoDataCache().getExpiry().toSeconds())
        .clientErrorExpirySeconds(
            (int) configuration.getEppoDataCache().getClientErrorExpiry().toSeconds())
        .build();
  }

  @Override
  public void dispose(final Cache<EppoDataCacheKey, EppoItem> cache) {
    // nothing to dispose, adding this comment to make SonarQube happy
  }
}

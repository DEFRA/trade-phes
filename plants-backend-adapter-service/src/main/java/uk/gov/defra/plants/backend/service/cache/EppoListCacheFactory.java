package uk.gov.defra.plants.backend.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.api.Factory;
import uk.gov.defra.plants.backend.configuration.CaseManagementServiceConfiguration;
import uk.gov.defra.plants.backend.representation.referencedata.EppoItem;
import uk.gov.defra.plants.common.redis.CachedValueMapper;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.common.redis.RedisCache;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EppoListCacheFactory implements Factory<Cache<EppoListCacheKey, List<EppoItem>>> {
  private final Redis redis;
  private final CaseManagementServiceConfiguration configuration;

  @Override
  public Cache<EppoListCacheKey, List<EppoItem>> provide() {
    return RedisCache.<EppoListCacheKey, List<EppoItem>>builder()
        .redis(redis)
        .keyPrefix("ed")
        .cachedValueMapper(new CachedValueMapper<>(new TypeReference<>() {
        }))
        .valueIsCachable((key, eppoItem) -> true)
        .expirySeconds((int) configuration.getEppoListCache().getExpiry().toSeconds())
        .clientErrorExpirySeconds(
            (int) configuration.getEppoListCache().getClientErrorExpiry().toSeconds())
        .build();
  }

  @Override
  public void dispose(final Cache<EppoListCacheKey,  List<EppoItem>> cache) {
    // nothing to dispose, adding this comment to make SonarQube happy
  }
}

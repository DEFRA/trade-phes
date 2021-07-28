package uk.gov.defra.plants.formconfiguration.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import java.net.URI;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.api.Factory;
import uk.gov.defra.plants.common.redis.CachedValueMapper;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.common.redis.RedisCache;
import uk.gov.defra.plants.common.redis.RedisCacheKey;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ActiveMergedFormCacheFactory implements Factory<Cache<RedisCacheKey, URI>> {
  private final Redis redis;
  private final FormConfigurationServiceConfiguration configuration;

  @Override
  public Cache<RedisCacheKey, URI> provide() {
    return RedisCache.<RedisCacheKey, URI>builder()
        .redis(redis)
        .keyPrefix("amf")
        .cachedValueMapper(new CachedValueMapper<>(new TypeReference<>() {}))
        .expirySeconds((int) configuration.getActiveMergedFormsCache().getExpiry().toSeconds())
        .clientErrorExpirySeconds(
            (int) configuration.getActiveMergedFormsCache().getClientErrorExpiry().toSeconds())
        .build();
  }

  @Override
  public void dispose(final Cache<RedisCacheKey, URI> redisCacheKeyURICache) {
    // nothing to dispose, adding this comment to make SonarQube happy
  }
}

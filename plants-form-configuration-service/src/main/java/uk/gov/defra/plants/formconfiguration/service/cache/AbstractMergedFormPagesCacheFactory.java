package uk.gov.defra.plants.formconfiguration.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import java.util.List;
import org.glassfish.hk2.api.Factory;
import uk.gov.defra.plants.common.redis.CachedValueMapper;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.common.redis.RedisCache;
import uk.gov.defra.plants.common.security.UserRoles;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;
import uk.gov.defra.plants.formconfiguration.model.MergedFormCacheKey;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;


public abstract class AbstractMergedFormPagesCacheFactory
    implements Factory<Cache<MergedFormCacheKey, List<MergedFormPage>>> {
  private final Redis redis;
  private final FormConfigurationServiceConfiguration configuration;
  private final String keyPrefix;

  protected AbstractMergedFormPagesCacheFactory(Redis redis,
      FormConfigurationServiceConfiguration configuration, String keyPrefix) {
    this.redis = redis;
    this.configuration = configuration;
    this.keyPrefix = keyPrefix;
  }

  @Override
  public Cache<MergedFormCacheKey, List<MergedFormPage>> provide() {
    return RedisCache.<MergedFormCacheKey, List<MergedFormPage>>builder()
        .redis(redis)
        .keyPrefix(keyPrefix)
        .cachedValueMapper(new CachedValueMapper<>(new TypeReference<>() {}))
        .valueIsCachable(
            (key, mergedFormPages) -> !key.getUserRoles().contains(UserRoles.ADMIN_ROLE))
        .expirySeconds((int) configuration.getMergedFormPagesCache().getExpiry().toSeconds())
        .clientErrorExpirySeconds(
            (int) configuration.getMergedFormPagesCache().getClientErrorExpiry().toSeconds())
        .build();
  }

  @Override
  public void dispose(final Cache<MergedFormCacheKey, List<MergedFormPage>> cache) {
    // nothing to dispose, adding this comment to make SonarQube happy
  }
}

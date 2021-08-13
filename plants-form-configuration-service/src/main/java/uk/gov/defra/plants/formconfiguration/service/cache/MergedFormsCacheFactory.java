package uk.gov.defra.plants.formconfiguration.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.api.Factory;
import uk.gov.defra.plants.common.redis.CachedValueMapper;
import uk.gov.defra.plants.common.redis.Redis;
import uk.gov.defra.plants.common.redis.RedisCache;
import uk.gov.defra.plants.formconfiguration.FormConfigurationServiceConfiguration;
import uk.gov.defra.plants.formconfiguration.model.MergedFormCacheKey;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MergedFormsCacheFactory implements Factory<Cache<MergedFormCacheKey, MergedForm>> {
  private final Redis redis;
  private final FormConfigurationServiceConfiguration configuration;

  @Override
  public Cache<MergedFormCacheKey, MergedForm> provide() {
    return RedisCache.<MergedFormCacheKey, MergedForm>builder()
        .redis(redis)
        .keyPrefix("mf")
        .cachedValueMapper(new CachedValueMapper<>(new TypeReference<>() {}))
        .valueIsCachable((key, mergedForm) -> mergedForm.getEhcFormStatus() != FormStatus.DRAFT)
        .expirySeconds((int) configuration.getMergedFormsCache().getExpiry().toSeconds())
        .clientErrorExpirySeconds(
            (int) configuration.getMergedFormsCache().getClientErrorExpiry().toSeconds())
        .build();
  }

  @Override
  public void dispose(final Cache<MergedFormCacheKey, MergedForm> cache) {
    // nothing to dispose, adding this comment to make SonarQube happy
  }
}

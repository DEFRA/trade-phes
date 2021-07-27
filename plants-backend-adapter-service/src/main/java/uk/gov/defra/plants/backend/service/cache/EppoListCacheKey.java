package uk.gov.defra.plants.backend.service.cache;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.common.redis.RedisCacheKey;

@Value
@Builder
public class EppoListCacheKey implements RedisCacheKey {
  @NonNull private final String eppoListKey;

  @Override
  public String toKey() {
    return String.join("_", eppoListKey);
  }

  @Override
  public String toInvalidationKey() {
    return eppoListKey;
  }
}

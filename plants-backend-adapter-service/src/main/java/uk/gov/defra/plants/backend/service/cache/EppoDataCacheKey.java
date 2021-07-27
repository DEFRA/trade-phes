package uk.gov.defra.plants.backend.service.cache;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import uk.gov.defra.plants.common.redis.RedisCacheKey;

@Value
@Builder
public class EppoDataCacheKey implements RedisCacheKey {
  @NonNull private final String eppoCode;

  @Override
  public String toKey() {
    return String.join("_", eppoCode);
  }

  @Override
  public String toInvalidationKey() {
    return eppoCode;
  }
}

package uk.gov.defra.plants.formconfiguration.model;

import java.util.SortedSet;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.apache.commons.lang3.ArrayUtils;
import uk.gov.defra.plants.common.redis.RedisCacheKey;

@Value
@Builder
public class MergedFormCacheKey implements RedisCacheKey {
  @NonNull private final String ehcNumber;
  private final String exaNumber;
  private final String ehcVersion;
  private final String exaVersion;
  @Singular private final SortedSet<String> userRoles;
  private boolean ignoreQuestionScope;

  @Override
  public String toKey() {
    return String.join(
        "_",
        ArrayUtils.addAll(
            new String[] {exaNumber, ehcNumber, exaVersion, ehcVersion, String.valueOf(ignoreQuestionScope)},
            userRoles.toArray(new String[] {})));
  }

  @Override
  public String toInvalidationKey() {
    return exaNumber + "_" + ehcNumber;
  }
}

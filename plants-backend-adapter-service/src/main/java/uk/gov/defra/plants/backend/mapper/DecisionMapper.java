package uk.gov.defra.plants.backend.mapper;

import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.defra.plants.backend.representation.Decision;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DecisionMapper {
  private static final Map<Decision, Integer> DECISION_TO_VALUE =
      new EnumMap<>(
          ImmutableMap.of(
              Decision.APPROVED, 814_250_000,
              Decision.REJECTED, 814_250_001,
              Decision.CANCELLED, 814_250_004));

  static Integer toDynamicsValue(final Decision decision) {
    return Objects.requireNonNull(DECISION_TO_VALUE.get(decision));
  }
}

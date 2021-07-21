package uk.gov.defra.plants.backend.mapper;

import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AvailabilityStatusMapper {
  private static final Map<AvailabilityStatus, Integer> AVAILABILITY_STATUS_MAP =
      new EnumMap<>(
          ImmutableMap.of(
              AvailabilityStatus.ON_HOLD, 814_250_001,
              AvailabilityStatus.RESTRICTED, 814_250_003,
              AvailabilityStatus.UNRESTRICTED, 814_250_000,
              AvailabilityStatus.WITHDRAWN, 814_250_002));

  static Integer toDynamicsValue(final AvailabilityStatus availabilityStatus) {
    return Objects.requireNonNull(AVAILABILITY_STATUS_MAP.get(availabilityStatus));
  }
}

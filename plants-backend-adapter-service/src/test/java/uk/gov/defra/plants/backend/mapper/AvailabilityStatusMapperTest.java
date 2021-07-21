package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Arrays;
import java.util.Objects;
import org.junit.Test;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;

public class AvailabilityStatusMapperTest {
  @Test
  public void testAvailabilityStatusCodes() {
    assertThat(AvailabilityStatusMapper.toDynamicsValue(AvailabilityStatus.ON_HOLD))
        .isEqualTo(814_250_001);
    assertThat(AvailabilityStatusMapper.toDynamicsValue(AvailabilityStatus.RESTRICTED))
        .isEqualTo(814_250_003);
    assertThat(AvailabilityStatusMapper.toDynamicsValue(AvailabilityStatus.UNRESTRICTED))
        .isEqualTo(814_250_000);
    assertThat(AvailabilityStatusMapper.toDynamicsValue(AvailabilityStatus.WITHDRAWN))
        .isEqualTo(814_250_002);
  }

  @Test
  public void testThatAllAvailabilityStatusesMapped() {
    assertThatCode(
            () ->
                Arrays.stream(AvailabilityStatus.values())
                    .map(AvailabilityStatusMapper::toDynamicsValue)
                    .anyMatch(Objects::isNull))
        .doesNotThrowAnyException();
  }
}

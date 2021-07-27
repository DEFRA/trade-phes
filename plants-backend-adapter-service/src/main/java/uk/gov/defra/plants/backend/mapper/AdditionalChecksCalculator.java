package uk.gov.defra.plants.backend.mapper;

import java.util.Arrays;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.AdditionalChecks;

public class AdditionalChecksCalculator {
  @AllArgsConstructor
  private enum AdditionalCheck {
    JOURNEY_LOG(AdditionalChecks::getJourneyLog, 2),
    OTHERS(AdditionalChecks::getOthers, 16),
    DISEASE_CLEARANCE_REQUIRED(AdditionalChecks::getDiseaseClearanceRequired, 32),
    APPROVALS(AdditionalChecks::getApprovals, 64);

    private final Function<AdditionalChecks, Boolean> isEnabled;
    private final Integer packedValue;
  }

  public static Integer calculateBitmask(@NonNull final AdditionalChecks additionalChecks) {
    return Arrays.stream(AdditionalCheck.values())
        .filter(ac -> Boolean.TRUE.equals(ac.isEnabled.apply(additionalChecks)))
        .mapToInt(ac -> ac.packedValue)
        .sum();
  }
}

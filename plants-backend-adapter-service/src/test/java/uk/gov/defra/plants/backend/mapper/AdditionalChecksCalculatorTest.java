package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.AdditionalChecks;

public class AdditionalChecksCalculatorTest {
  @Test
  public void testAllFalse() {
    final AdditionalChecks acs = AdditionalChecks.builder().build();
    assertThat(AdditionalChecksCalculator.calculateBitmask(acs)).isEqualTo(0);
  }

  @Test
  public void testJourneyLog() {
    final AdditionalChecks acs = AdditionalChecks.builder().journeyLog(true).build();
    assertThat(AdditionalChecksCalculator.calculateBitmask(acs)).isEqualTo(2);
  }

  @Test
  public void testApprovalsCheck() {
    final AdditionalChecks acs = AdditionalChecks.builder().approvals(true).build();
    assertThat(AdditionalChecksCalculator.calculateBitmask(acs)).isEqualTo(64);
  }

  @Test
  public void testOthers() {
    final AdditionalChecks acs = AdditionalChecks.builder().others(true).build();
    assertThat(AdditionalChecksCalculator.calculateBitmask(acs)).isEqualTo(16);
  }

  @Test
  public void testDiseaseClearanceRequired() {
    final AdditionalChecks acs = AdditionalChecks.builder().diseaseClearanceRequired(true).build();
    assertThat(AdditionalChecksCalculator.calculateBitmask(acs)).isEqualTo(32);
  }

  @Test
  public void testMix() {
    final AdditionalChecks acs =
        AdditionalChecks.builder().journeyLog(true).approvals(true).others(true).build();
    assertThat(AdditionalChecksCalculator.calculateBitmask(acs)).isEqualTo(82);
  }

  @Test
  public void testAll() {
    final AdditionalChecks acs =
        AdditionalChecks.builder()
            .journeyLog(true)
            .approvals(true)
            .others(true)
            .diseaseClearanceRequired(true)
            .build();
    assertThat(AdditionalChecksCalculator.calculateBitmask(acs)).isEqualTo(114);
  }
}

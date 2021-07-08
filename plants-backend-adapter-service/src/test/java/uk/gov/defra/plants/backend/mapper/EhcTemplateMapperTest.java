package uk.gov.defra.plants.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.defra.plants.dynamics.representation.EhcTemplate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataPaperType;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.AdditionalChecks;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;

public class EhcTemplateMapperTest {
  @Test
  public void testMapToDynamicsEHCTemplate() {
    final UUID expectedUUID = UUID.randomUUID();
    final HealthCertificate healthCertificate =
        HealthCertificate.builder()
            .ehcNumber("EHC1234")
            .ehcGUID(expectedUUID)
            .availabilityStatus(AvailabilityStatus.ON_HOLD)
            .healthCertificateMetadata(
                HealthCertificateMetadata.builder()
                    .vetSignature(true)
                    .counterSignature(true)
                    .paperType(HealthCertificateMetadataPaperType.CROWN_VELLUM)
                    .maxEhc(22)
                    .preCheck(true)
                    .additionalChecks(AdditionalChecks.builder().build())
                    .build())
            .build();

    final String version = "1.2";

    final EhcTemplate ehcTemplate =
        DynamicsEHCTemplateMapper.asDynamicsEHCTemplate(healthCertificate, version);

    assertThat(ehcTemplate.getEhcNumber()).isEqualTo("EHC1234");
    assertThat(ehcTemplate.getEhcGuid()).isEqualTo(expectedUUID);
    assertThat(ehcTemplate.getStatus()).isEqualTo(814_250_001);
    assertThat(ehcTemplate.getVetSignature()).isTrue();
    assertThat(ehcTemplate.getCounterSignature()).isTrue();
    assertThat(ehcTemplate.getCrownVellumPaper()).isTrue();
    assertThat(ehcTemplate.getPreCheckRequired()).isTrue();
    assertThat(ehcTemplate.getAdditionalChecks()).isEqualTo(0);
  }
}

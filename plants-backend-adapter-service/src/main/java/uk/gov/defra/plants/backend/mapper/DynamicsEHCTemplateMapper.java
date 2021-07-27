package uk.gov.defra.plants.backend.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.defra.plants.dynamics.representation.EhcTemplate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataPaperType;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicsEHCTemplateMapper {

  public static EhcTemplate asDynamicsEHCTemplate(
      @NonNull final HealthCertificate healthCertificate, final String version) {
    final HealthCertificateMetadata healthCertificateMetadata =
        healthCertificate.getHealthCertificateMetadata();

    final Integer availabilityStatus =
        AvailabilityStatusMapper.toDynamicsValue(healthCertificate.getAvailabilityStatus());

    final Integer additionalChecks =
        AdditionalChecksCalculator.calculateBitmask(
            healthCertificateMetadata.getAdditionalChecks());

    final Boolean isCrownVellumPaper = isCrownVellumPaper(healthCertificateMetadata);

    return EhcTemplate.builder()
        .ehcNumber(healthCertificate.getEhcNumber())
        .ehcGuid(healthCertificate.getEhcGUID())
        .status(availabilityStatus)
        .version(version)
        .vetSignature(healthCertificateMetadata.getVetSignature())
        .counterSignature(healthCertificateMetadata.getCounterSignature())
        .additionalChecks(additionalChecks)
        .crownVellumPaper(isCrownVellumPaper)
        .preCheckRequired(healthCertificateMetadata.getPreCheck())
        .build();
  }

  private static boolean isCrownVellumPaper(HealthCertificateMetadata healthCertificateMetadata) {
    return healthCertificateMetadata.getPaperType()
        == HealthCertificateMetadataPaperType.CROWN_VELLUM;
  }
}

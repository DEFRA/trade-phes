package uk.gov.defra.plants.formconfiguration.helper;

import static uk.gov.defra.plants.common.constants.PageType.SINGULAR;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks.BLOCK_APPLICATON;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType.APPLICATION_LEVEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotFoundException;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomPageTitleHint;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;

public class CustomPagesBuilder {

  private final List<MergedFormPage> customMergedFormPages;
  private final NameAndVersion ehc;
  private final HealthCertificateService healthCertificateService;

  public CustomPagesBuilder(NameAndVersion ehc,
      HealthCertificateService healthCertificateService) {
    this.customMergedFormPages = new ArrayList<>();
    this.ehc = ehc;
    this.healthCertificateService = healthCertificateService;
  }

  public List<MergedFormPage> build() {
    return this.customMergedFormPages;
  }

  public void addCustomPage(
      MergedFormQuestion mergedFormQuestion, CustomPageTitleHint customPageTitleHint,
      Long formPageId) {
    Optional<MergedFormPage> existingMergedFormPage =
        customMergedFormPages.stream()
            .filter(mfp -> mfp.getPageNumber().equals(mergedFormQuestion.getPageNumber()))
            .findFirst();

    MergedFormPage newMergedFormPage;

    if (existingMergedFormPage.isPresent()) {
      customMergedFormPages.remove(existingMergedFormPage.get());
      newMergedFormPage =
          existingMergedFormPage.get().toBuilder().question(mergedFormQuestion).build();
    } else {
      newMergedFormPage =
          MergedFormPage.builder()
              .pageNumber(mergedFormQuestion.getPageNumber())
              .pageOccurrences(1)
              .pageType(SINGULAR)
              .title(customPageTitleHint.getTitle())
              .hint(
                  BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION
                      .getQuestionId()
                      .equals(mergedFormQuestion.getFormQuestionId())
                      ? String
                      .format(customPageTitleHint.getHint(), getMaximumCertificatesForBlock(ehc))
                      : customPageTitleHint.getHint())
              .question(mergedFormQuestion)
              .formPageId(formPageId)
              .build();
    }
    this.customMergedFormPages.add(newMergedFormPage.toBuilder().mergedFormPageType(APPLICATION_LEVEL).build());
  }

  private int getMaximumCertificatesForBlock(NameAndVersion ehc) {
    HealthCertificate healthCertificate = loadHealthCertificate(ehc.getName());
    int maxNumberOfCerts = 0;
    if (healthCertificate != null
        && healthCertificate.getHealthCertificateMetadata() != null
        && healthCertificate
        .getHealthCertificateMetadata()
        .getMultipleBlocks()
        .equals(BLOCK_APPLICATON)) {
      maxNumberOfCerts = healthCertificate.getHealthCertificateMetadata().getMaxEhc();
    }
    return maxNumberOfCerts;
  }

  private HealthCertificate loadHealthCertificate(final String ehcNumber) {
    return healthCertificateService
        .getByEhcNumber(ehcNumber)
        .orElseThrow(
            () ->
                new NotFoundException("Could not find health certificate with name=" + ehcNumber));
  }
}


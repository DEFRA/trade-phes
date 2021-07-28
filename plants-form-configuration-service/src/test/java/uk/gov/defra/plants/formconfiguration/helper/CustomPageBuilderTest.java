package uk.gov.defra.plants.formconfiguration.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks.BLOCK_APPLICATON;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.HealthCertificateTestData;
import uk.gov.defra.plants.formconfiguration.representation.AvailabilityStatus;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomPageTitleHint;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;

@RunWith(MockitoJUnitRunner.class)
public class CustomPageBuilderTest {

  @Mock
  private HealthCertificateService healthCertificateService;

  static final Form EHC_FORM = Form.builder().name("ehcName").version("1.0").build();

  @Test
  public void addCustomPages() {
    CustomPagesBuilder customPagesBuilder = new CustomPagesBuilder(EHC_FORM.getNameAndVersion(), healthCertificateService);

    HealthCertificate HEALTH_CERTIFICATE =
        HealthCertificate.builder()
            .ehcNumber(HealthCertificateTestData.EHC_NUMBER)
            .exaNumber(HealthCertificateTestData.EXA_NUMBER)
            .availabilityStatus(AvailabilityStatus.ON_HOLD)
            .healthCertificateMetadata(
                HealthCertificateMetadata.builder()
                    .maxEhc(100)
                    .multipleBlocks(BLOCK_APPLICATON).build())
            .destinationCountry("country")
            .ehcTitle(HealthCertificateTestData.EHC_TITLE)
            .build();

    when(healthCertificateService.getByEhcNumber("ehcName"))
        .thenReturn(Optional.of(HEALTH_CERTIFICATE));

    MergedFormQuestion blockQuestion = MergedFormQuestion.builder()
        .pageNumber(-5)
        .formQuestionId(-5L)
        .questionId(-5L)
        .questionOrder(1)
        .build();

    MergedFormQuestion groupReferenceQuestion = MergedFormQuestion.builder()
        .pageNumber(-6)
        .formQuestionId(-6L)
        .questionId(-6L)
        .questionOrder(1)
        .build();

    customPagesBuilder.addCustomPage(blockQuestion, CustomPageTitleHint.BLOCKS_NUMBER_OF_CERTIFICATES_PAGE,
        CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormPageId());
    customPagesBuilder.addCustomPage(groupReferenceQuestion, CustomPageTitleHint.APPLICANT_REFERENCE_NUMBER_PAGE,
        CustomQuestions.APPLICANT_REFERENCE_NUMBER_QUESTION.getFormPageId());
    List<MergedFormPage> mergedFormPages = customPagesBuilder.build();
    assertThat(mergedFormPages).isNotEmpty().hasSize(2);
  }
}
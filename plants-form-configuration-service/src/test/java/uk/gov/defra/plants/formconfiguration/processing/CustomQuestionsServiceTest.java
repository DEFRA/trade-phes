package uk.gov.defra.plants.formconfiguration.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.common.constants.PageType.SINGULAR;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks.BLOCK_APPLICATON;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks.MULTIPLE_APPLICATION;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataPaperType.PLAIN_PAPER;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.UPLOAD_QUESTION;
import static uk.gov.defra.plants.formconfiguration.service.FormTestData.TEMPLATE_FILE_REFERENCE;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.AdditionalChecks;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomPageTitleHint;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestionOption;
import uk.gov.defra.plants.formconfiguration.service.FormService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;
import uk.gov.defra.plants.reference.representation.LocationType;

public class CustomQuestionsServiceTest {

  private HealthCertificateService healthCertificateService = mock(HealthCertificateService.class);
  private ReferenceDataServiceAdapter referenceDataServiceAdapter =
      mock(ReferenceDataServiceAdapter.class);
  private FormService formService = mock(FormService.class);

  private CustomQuestionsService customQuestionsService;

  private static final HealthCertificateMetadata HEALTH_CERTIFICATE_METADATA =
      HealthCertificateMetadata.builder()
          .maxEhc(1)
          .multipleBlocks(BLOCK_APPLICATON)
          .vetSignature(false)
          .counterSignature(false)
          .preCertAllowed(false)
          .preCheck(false)
          .paperType(PLAIN_PAPER)
          .fullyTyped(false)
          .additionalChecks(
              AdditionalChecks.builder()
                  .diseaseClearanceRequired(true)
                  .journeyLog(false)
                  .others(false)
                  .approvals(false)
                  .build())
          .build();

  private static final HealthCertificate HEALTH_CERTIFICATE_BLOCK =
      HealthCertificate.builder()
          .ehcNumber("foo")
          .exaNumber("bar")
          .amendable(false)
          .healthCertificateMetadata(HEALTH_CERTIFICATE_METADATA)
          .build();

  private static final Form FORM_WITH_COUNTRY_TEMPLATE =
      Form.builder().countryTemplateFile("FR", TEMPLATE_FILE_REFERENCE).build();

  @Before
  public void before() {
    customQuestionsService =
        new CustomQuestionsService(
            healthCertificateService, referenceDataServiceAdapter, formService);

    when(formService.get(any(), any())).thenReturn(Optional.of(FORM_WITH_COUNTRY_TEMPLATE));
  }

  @Test
  public void getCustomQuestionShouldReturnManualUploadQuestion() {
    Optional<MergedFormQuestion> customQuestion =
        customQuestionsService.getCustomQuestionForPage(
            CustomQuestionTestData.EXA, CustomQuestionTestData.EHC_OFFLINE, UPLOAD_QUESTION);
    assertThat(customQuestion).contains(CustomQuestionTestData.TEST_UPLOAD_QUESTION);
  }

  @Test
  public void getAllCustomPagesShouldReturnAllCustomPagesWithQuestionsInOrder_manualEhc() {
    setHealthCertificate();
    setDestinationCountries();
    List<MergedFormPage> customPages =
        customQuestionsService.getAllCustomPages(
            CustomQuestionTestData.EXA, CustomQuestionTestData.EHC_OFFLINE);
    assertThat(customPages)
        .containsExactly(
            getCustomPage(
                CustomPageTitleHint.UPLOAD_QUESTION,
                UPLOAD_QUESTION.getFormPageId(),
                CustomQuestionTestData.TEST_UPLOAD_QUESTION));
  }

  @Test
  public void getCustomQuestionShouldReturnNumberOfCertsBlockQuestion() {
    when(healthCertificateService.getByEhcNumber(anyString()))
        .thenReturn(Optional.of(HEALTH_CERTIFICATE_BLOCK));
    Optional<MergedFormQuestion> customQuestion =
        customQuestionsService.getCustomQuestionForPage(
            CustomQuestionTestData.EXA,
            CustomQuestionTestData.EHC,
            BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION);
    assertThat(customQuestion).contains(CustomQuestionTestData.TEST_BLOCK_QUESTION);
  }

  private void setDestinationCountries() {
    when(referenceDataServiceAdapter.getCountriesListByCodes(anyList()))
        .thenReturn(
            ImmutableList.of(
                Country.builder()
                    .code("AF")
                    .name("Afghanistan")
                    .locationType(LocationType.COUNTRY)
                    .build(),
                Country.builder()
                    .code("AL")
                    .name("Algeria")
                    .locationType(LocationType.COUNTRY)
                    .build()));
  }

  private void setHealthCertificate() {
    when(healthCertificateService.getByEhcNumber(anyString()))
        .thenReturn(
            Optional.of(
                HealthCertificate.builder()
                    .amendable(false)
                    .commodityGroup("PLANTS_PRODUCTS")
                    .secondaryDestinations(ImmutableList.of("AF", "AL"))
                    .build()));
  }

  private MergedFormPage getCustomPage(
      CustomPageTitleHint pageTitleHint,
      Long formPageId,
      MergedFormQuestion... mergedFormQuestion) {
    return MergedFormPage.builder()
        .pageNumber(mergedFormQuestion[0].getPageNumber())
        .pageOccurrences(1)
        .pageType(SINGULAR)
        .title(pageTitleHint.getTitle())
        .hint(pageTitleHint.getHint())
        .questions(Arrays.asList(mergedFormQuestion))
        .formPageId(formPageId)
        .mergedFormPageType(MergedFormPageType.APPLICATION_LEVEL)
        .build();
  }
}

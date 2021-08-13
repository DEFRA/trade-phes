package uk.gov.defra.plants.applicationform.service.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EHC;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_EXA;
import static uk.gov.defra.plants.applicationform.ApplicationFormTestData.TEST_PERSISTENT_APPLICATION_FORM_DRAFT;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.mapper.ResponseItemMapper;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationForm;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;
import uk.gov.defra.plants.applicationform.model.PersistentConsignmentData;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.common.constants.PageType;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedForm;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFormAnswerMigrationServiceTest {

  public static final NameAndVersion NEW_EXA_UPDATED_ON_PUBLISHED_EHC =
      NameAndVersion.builder().name("someNewExa").version("1.0").build();

  private List<MergedFormQuestion> EXA_V1_QUESTIONS =
      ImmutableList.of(
          mergedFormQuestionWithQuestionIdFormNamePageNumber(1L, TEST_EXA.getName(), 1),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(1L, TEST_EXA.getName(), 1),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(2L, TEST_EXA.getName(), 1),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(3L, TEST_EXA.getName(), 1));

  private List<MergedFormQuestion> NEW_EXA_V1_QUESTIONS =
      ImmutableList.of(
          mergedFormQuestionWithQuestionIdFormNamePageNumber(
              2L, NEW_EXA_UPDATED_ON_PUBLISHED_EHC.getName(), 1),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(
              3L, NEW_EXA_UPDATED_ON_PUBLISHED_EHC.getName(), 1),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(
              3L, NEW_EXA_UPDATED_ON_PUBLISHED_EHC.getName(), 1));

  private List<MergedFormQuestion> EHC_V1_QUESTIONS =
      ImmutableList.of(
          mergedFormQuestionWithQuestionIdFormNamePageNumber(3L, TEST_EHC.getName(), 2),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(3L, TEST_EHC.getName(), 2),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(4L, TEST_EHC.getName(), 2),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(5L, TEST_EHC.getName(), 2));

  private URI EXA_V1_EHC_V1_MERGED_FORM_PAGES_URI = URI.create("http://exaV1ehcV1Pages.com");

  private MergedForm MERGED_FORM_EXA_V1_EHC_V1 =
      MergedForm.builder()
          .exa(TEST_EXA)
          .ehc(TEST_EHC)
          .mergedFormPagesUri(EXA_V1_EHC_V1_MERGED_FORM_PAGES_URI)
          .build();

  private List<MergedFormQuestion> EXA_V2_QUESTIONS =
      ImmutableList.of(
          mergedFormQuestionWithQuestionIdFormNamePageNumber(2L, TEST_EXA.getName(), 1),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(3L, TEST_EXA.getName(), 1),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(3L, TEST_EXA.getName(), 1));

  private List<MergedFormQuestion> EHC_V2_QUESTIONS =
      ImmutableList.of(
          mergedFormQuestionWithQuestionIdFormNamePageNumber(4L, TEST_EHC.getName(), 2),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(5L, TEST_EHC.getName(), 2),
          mergedFormQuestionWithQuestionIdFormNamePageNumber(5L, TEST_EHC.getName(), 2));

  private List<MergedFormQuestion> EHC_V3_QUESTIONS =
      ImmutableList.of(
          mergedFormQuestionWithQuestionIdFormNamePageNumber(4L, TEST_EHC.getName(), 2));

  private URI EXA_V2_EHC_V2_MERGED_FORM_PAGES_URI = URI.create("http://exaV2ehcV2Pages.com");

  private URI NEW_EXA_V1_EHC_V2_MERGED_FORM_PAGES_URI =
      URI.create("http://someNewExaV1ehcV2Pages.com");

  private MergedForm MERGED_FORM_EXA_V2_EHC_V2 =
      MergedForm.builder()
          .exa(TEST_EXA.toBuilder().version("2").build())
          .ehc(TEST_EHC.toBuilder().version("2").build())
          .mergedFormPagesUri(EXA_V2_EHC_V2_MERGED_FORM_PAGES_URI)
          .build();

  private MergedForm MERGED_FORM_NEW_EXA_V1_EHC_V2 =
      MergedForm.builder()
          .exa(NEW_EXA_UPDATED_ON_PUBLISHED_EHC.toBuilder().version("1").build())
          .ehc(TEST_EHC.toBuilder().version("2").build())
          .mergedFormPagesUri(NEW_EXA_V1_EHC_V2_MERGED_FORM_PAGES_URI)
          .build();

  private MergedForm MERGED_FORM_EXA_V2_OFFLINE_EHC =
      MergedForm.builder()
          .exa(TEST_EXA.toBuilder().version("2").build())
          .ehc(TEST_EHC.toBuilder().version("OFFLINE").build())
          .build();

  private List<ApplicationFormItem> EXA_RESPONSE_ITEMS =
      ImmutableList.of(
          // answers to qID 1 will not be copied over due to duplicate questionId on the existing
          // response items
          responseItemWithQuestionIdAnswerFormNamePageNumber(1L, "1ans-exa", TEST_EXA.getName(), 1),
          responseItemWithQuestionIdAnswerFormNamePageNumber(
              1L, "1ans-exa-2", TEST_EXA.getName(), 1),
          // answer to qID 11 will not be copied over due to non existent questionId on the updated
          // EXA
          responseItemWithQuestionIdAnswerFormNamePageNumber(
              11L, "11ans-exa", TEST_EXA.getName(), 1),
          responseItemWithQuestionIdAnswerFormNamePageNumber(2L, "2ans-exa", TEST_EXA.getName(), 1),
          // answer to qID 3 will not be copied over due to duplicate questionId on the updated EXA
          responseItemWithQuestionIdAnswerFormNamePageNumber(
              3L, "3ans-exa", TEST_EXA.getName(), 1));

  private List<ApplicationFormItem> EHC_RESPONSE_ITEMS =
      ImmutableList.of(
          // answers to qID 3 will not be copied over due to duplicate questionId on the existing
          // response items
          responseItemWithQuestionIdAnswerFormNamePageNumber(3L, "3ans-ehc", TEST_EHC.getName(), 2),
          responseItemWithQuestionIdAnswerFormNamePageNumber(
              3L, "3ans-ehc-2", TEST_EHC.getName(), 2),
          // answer to qID 33 will not be copied over due to non existent questionId on the updated
          // EXA
          responseItemWithQuestionIdAnswerFormNamePageNumber(
              33L, "33ans-ehc", TEST_EHC.getName(), 2),
          responseItemWithQuestionIdAnswerFormNamePageNumber(4L, "4ans-ehc", TEST_EHC.getName(), 2),
          // answer to qID 5 will not be copied over due to duplicate questionId on the updated EHC
          responseItemWithQuestionIdAnswerFormNamePageNumber(
              5L, "5ans-ehc", TEST_EHC.getName(), 2));

  private List<ApplicationFormItem> CERTIFICATE_RESPONSE_ITEMS =
      ImmutableList.of(
          responseItemWithQuestionIdAnswerFormNamePageNumber(
              4L, "4cert-ans-ehc", TEST_EHC.getName(), 2));

  private PersistentApplicationForm APPLICATION_FORM =
      TEST_PERSISTENT_APPLICATION_FORM_DRAFT
          .toBuilder()
          .data(
              TEST_PERSISTENT_APPLICATION_FORM_DRAFT
                  .getData()
                  .toBuilder()
                  .clearResponseItems()
                  .responseItems(EXA_RESPONSE_ITEMS)
                  .responseItems(EHC_RESPONSE_ITEMS)
                  .build())
          .build();

  private PersistentApplicationForm APPLICATION_FORM_WITH_CERTIFICATE =
      TEST_PERSISTENT_APPLICATION_FORM_DRAFT
          .toBuilder()
          .persistentConsignments(
              List.of(
                  PersistentConsignment.builder()
                      .data(
                          PersistentConsignmentData.builder()
                              .responseItems(CERTIFICATE_RESPONSE_ITEMS)
                              .build())
                      .build()))
          .data(
              TEST_PERSISTENT_APPLICATION_FORM_DRAFT
                  .getData()
                  .toBuilder()
                  .clearResponseItems()
                  .responseItems(EXA_RESPONSE_ITEMS)
                  .responseItems(EHC_RESPONSE_ITEMS)
                  .build())
          .build();

  private List<Form> allForms =
      ImmutableList.of(
          Form.builder()
              .name(TEST_EHC.getName())
              .status(FormStatus.ACTIVE)
              .version(TEST_EHC.getVersion())
              .build(),
          Form.builder()
              .name(TEST_EHC.getName())
              .version("5.0")
              .status(FormStatus.INACTIVE)
              .build());

  private ResponseItemMapper responseItemMapper = new ResponseItemMapper();

  @Mock private FormConfigurationServiceAdapter mockFormConfigurationServiceAdapter;

  private ApplicationFormAnswerMigrationService applicationFormAnswerMigrationService;

  @Before
  public void before() {
    applicationFormAnswerMigrationService =
        new ApplicationFormAnswerMigrationService(
            mockFormConfigurationServiceAdapter,
            responseItemMapper,
            new MergedFormPageNormaliser());

    when(mockFormConfigurationServiceAdapter.getMergedFormPages(
            TEST_EHC.getName(), TEST_EHC.getVersion(), TEST_EXA.getName(), TEST_EXA.getVersion()))
        .thenReturn(
            ImmutableList.of(
                MergedFormPage.builder().pageNumber(1).questions(EXA_V1_QUESTIONS).build(),
                MergedFormPage.builder().pageNumber(2).questions(EHC_V1_QUESTIONS).build()));

    when(mockFormConfigurationServiceAdapter.getMergedFormPages(
            MERGED_FORM_NEW_EXA_V1_EHC_V2.getEhc().getName(),
            MERGED_FORM_NEW_EXA_V1_EHC_V2.getEhc().getVersion(),
            MERGED_FORM_NEW_EXA_V1_EHC_V2.getExa().getName(),
            MERGED_FORM_NEW_EXA_V1_EHC_V2.getExa().getVersion()))
        .thenReturn(
            ImmutableList.of(
                MergedFormPage.builder().pageNumber(1).questions(NEW_EXA_V1_QUESTIONS).build(),
                MergedFormPage.builder().pageNumber(2).questions(EHC_V2_QUESTIONS).build()));

    when(mockFormConfigurationServiceAdapter.getMergedFormPages(
            MERGED_FORM_EXA_V2_EHC_V2.getEhc().getName(),
            MERGED_FORM_EXA_V2_EHC_V2.getEhc().getVersion(),
            MERGED_FORM_EXA_V2_EHC_V2.getExa().getName(),
            MERGED_FORM_EXA_V2_EHC_V2.getExa().getVersion()))
        .thenReturn(
            ImmutableList.of(
                MergedFormPage.builder().pageNumber(1).questions(EXA_V2_QUESTIONS).build(),
                MergedFormPage.builder().pageNumber(2).questions(EHC_V2_QUESTIONS).build()));

    when(mockFormConfigurationServiceAdapter.getAllVersions(TEST_EHC.getName()))
        .thenReturn(allForms);
  }

  @Test
  public void shouldNotChangeApplicationFormIfItIsUpToDate() {

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(MERGED_FORM_EXA_V1_EHC_V1);

    PersistentApplicationForm updatedForm =
        applicationFormAnswerMigrationService
            .migrateAnswersToLatestFormVersion(APPLICATION_FORM)
            .get();

    assertThat(updatedForm).isEqualToIgnoringGivenFields(APPLICATION_FORM, "data");

    assertThat(updatedForm.getData())
        .isEqualToIgnoringGivenFields(APPLICATION_FORM.getData(), "responseItems");

    assertThat(
            CollectionUtils.isEqualCollection(
                updatedForm.getData().getResponseItems(),
                APPLICATION_FORM.getData().getResponseItems()))
        .isTrue();
  }

  @Test
  public void shouldNotChangeEXAResponseItemsIfExaIsUpToDateAndEhcIsNot() {

    MergedForm changedOnlyEhcMergedForm =
        MERGED_FORM_EXA_V1_EHC_V1
            .toBuilder()
            .ehc(TEST_EHC.toBuilder().version("differentVersion").build())
            .build();

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(changedOnlyEhcMergedForm);

    PersistentApplicationForm updatedForm =
        applicationFormAnswerMigrationService
            .migrateAnswersToLatestFormVersion(APPLICATION_FORM)
            .get();

    List<ApplicationFormItem> exaResponseItems =
        responseItemsForFormName(updatedForm, TEST_EXA.getName());

    assertThat(CollectionUtils.isEqualCollection(exaResponseItems, EXA_RESPONSE_ITEMS)).isTrue();
  }

  @Test
  public void shouldNotChangeEHCResponseItemsIfEhcIsUpToDateAndExaIsNot() {

    MergedForm changedOnlyExaMergedForm =
        MERGED_FORM_EXA_V1_EHC_V1
            .toBuilder()
            .exa(TEST_EXA.toBuilder().version("differentVersion").build())
            .build();

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(changedOnlyExaMergedForm);

    PersistentApplicationForm updatedForm =
        applicationFormAnswerMigrationService
            .migrateAnswersToLatestFormVersion(APPLICATION_FORM)
            .get();

    List<ApplicationFormItem> ehcResponseItems =
        responseItemsForFormName(updatedForm, TEST_EHC.getName());

    assertThat(CollectionUtils.isEqualCollection(ehcResponseItems, EHC_RESPONSE_ITEMS)).isTrue();
  }

  @Test
  public void shouldCorrectlyCarryOverAnswersWhenMultiplesEhcFormIsUpdated() {

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(MERGED_FORM_EXA_V2_EHC_V2);

    PersistentApplicationForm updatedForm =
        applicationFormAnswerMigrationService
            .migrateAnswersToLatestFormVersion(APPLICATION_FORM_WITH_CERTIFICATE)
            .get();

    testEHCAndEXAAnswers(updatedForm);

    ApplicationFormItem expectedCertificateResponseItem =
        responseItemMapper.getApplicationFormItem(
            mergedFormQuestionWithQuestionIdFormNamePageNumber(4L, TEST_EHC.getName(), 2),
            "4cert-ans-ehc");

    assertThat(certificateResponseItems(updatedForm)).containsOnly(expectedCertificateResponseItem);
  }

  @Test
  public void shouldCorrectlyCarryOverAnswersWhenEhcFormIsUpdated() {

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(MERGED_FORM_EXA_V2_EHC_V2);

    PersistentApplicationForm updatedForm =
        applicationFormAnswerMigrationService
            .migrateAnswersToLatestFormVersion(APPLICATION_FORM)
            .get();
    testEHCAndEXAAnswers(updatedForm);
  }

  @Test
  public void shouldCorrectlyCarryOverRepeatablePageAnswersWhenEhcFormIsUpdated() {
    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(MERGED_FORM_EXA_V2_EHC_V2);

    when(mockFormConfigurationServiceAdapter.getMergedFormPages(
            MERGED_FORM_EXA_V2_EHC_V2.getEhc().getName(),
            MERGED_FORM_EXA_V2_EHC_V2.getEhc().getVersion(),
            MERGED_FORM_EXA_V2_EHC_V2.getExa().getName(),
            MERGED_FORM_EXA_V2_EHC_V2.getExa().getVersion()))
        .thenReturn(
            ImmutableList.of(
                MergedFormPage.builder().pageNumber(1).questions(EXA_V2_QUESTIONS).build(),
                MergedFormPage.builder()
                    .pageNumber(2)
                    .pageOccurrences(2)
                    .pageType(PageType.REPEATABLE)
                    .questions(EHC_V3_QUESTIONS)
                    .build()));

    // response item on page occurrence 1
    ApplicationFormItem responseItemPageOccurrence1 =
        responseItemWithQuestionIdAnswerFormNamePageNumber(4L, "4ans-ehc", TEST_EHC.getName(), 2)
            .toBuilder()
            .pageOccurrence(1)
            .build();
    // add this response item to application response items.
    PersistentApplicationForm persistentApplicationFormWithRepeatableAnswers =
        APPLICATION_FORM
            .toBuilder()
            .data(
                APPLICATION_FORM
                    .getData()
                    .toBuilder()
                    .responseItem(responseItemPageOccurrence1)
                    .build())
            .build();
    PersistentApplicationForm updatedForm =
        applicationFormAnswerMigrationService
            .migrateAnswersToLatestFormVersion(persistentApplicationFormWithRepeatableAnswers)
            .get();

    ApplicationFormItem expectedEHCResponseItemPageOccurrence0 =
        responseItemMapper.getApplicationFormItem(
            mergedFormQuestionWithQuestionIdFormNamePageNumber(4L, TEST_EHC.getName(), 2),
            "4ans-ehc");

    ApplicationFormItem expectedEHCResponseItemPageOccurrence1 =
        expectedEHCResponseItemPageOccurrence0.toBuilder().pageOccurrence(1).build();

    assertThat(responseItemsForFormName(updatedForm, TEST_EHC.getName()))
        .contains(expectedEHCResponseItemPageOccurrence0, expectedEHCResponseItemPageOccurrence1);
  }

  @Test
  public void shouldCorrectlyCarryOverAnswersWhenExaFormIsUpdated() {

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(MERGED_FORM_NEW_EXA_V1_EHC_V2);

    PersistentApplicationForm updatedForm =
        applicationFormAnswerMigrationService
            .migrateAnswersToLatestFormVersion(APPLICATION_FORM)
            .get();

    /*NOTE: only one EXA and EHC answer carried over as others either i) have a duplicated question
    id on the
    original answers or on the EXA or EHC or ii) do not have their question id on the updated EXA
    or EHC form. */
    ApplicationFormItem expectedEXAResponseItem =
        responseItemMapper.getApplicationFormItem(
            mergedFormQuestionWithQuestionIdFormNamePageNumber(
                2L, NEW_EXA_UPDATED_ON_PUBLISHED_EHC.getName(), 1),
            "2ans-exa");

    ApplicationFormItem expectedEHCResponseItem =
        responseItemMapper.getApplicationFormItem(
            mergedFormQuestionWithQuestionIdFormNamePageNumber(4L, TEST_EHC.getName(), 2),
            "4ans-ehc");

    assertThat(responseItemsForFormName(updatedForm, NEW_EXA_UPDATED_ON_PUBLISHED_EHC.getName()))
        .containsOnly(expectedEXAResponseItem);

    assertThat(responseItemsForFormName(updatedForm, TEST_EHC.getName()))
        .containsOnly(expectedEHCResponseItem);
  }

  @Test
  public void shouldUpdateEhcAndExaVersions() {

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(MERGED_FORM_EXA_V2_EHC_V2);

    Optional<PersistentApplicationForm> updatedForm =
        applicationFormAnswerMigrationService.migrateAnswersToLatestFormVersion(APPLICATION_FORM);

    assertThat(updatedForm).isPresent();
    assertThat(updatedForm.get().getData().getExa().getVersion()).isEqualTo("2");
    assertThat(updatedForm.get().getData().getEhc().getVersion()).isEqualTo("2");
  }

  @Test
  public void shouldNotUpdatePrivateForm() {

    when(mockFormConfigurationServiceAdapter.getAllVersions(TEST_EHC.getName()))
        .thenReturn(
            ImmutableList.of(
                Form.builder()
                    .name(TEST_EHC.getName())
                    .version(TEST_EHC.getVersion())
                    .status(FormStatus.PRIVATE)
                    .build()));

    Optional<PersistentApplicationForm> updatedForm =
        applicationFormAnswerMigrationService.migrateAnswersToLatestFormVersion(APPLICATION_FORM);

    assertThat(updatedForm).isEmpty();
  }

  @Test
  public void shouldUpdatePrivateFormToLatest() {

    prepareForUpdatedPrivateMergedForm();

    Optional<PersistentApplicationForm> updatedForm =
        applicationFormAnswerMigrationService.migrateAnswersToLatestFormVersion(APPLICATION_FORM);

    testUpdatedPrivateForm(updatedForm);
  }

  @Test
  public void shouldUpdatePrivateMultipleFormToLatest() {

    prepareForUpdatedPrivateMergedForm();

    Optional<PersistentApplicationForm> updatedForm =
        applicationFormAnswerMigrationService.migrateAnswersToLatestFormVersion(
            APPLICATION_FORM_WITH_CERTIFICATE);

    testUpdatedPrivateForm(updatedForm);
    assertThat(
            updatedForm
                .get()
                .getPersistentConsignments()
                .get(0)
                .getData()
                .getResponseItems()
                .get(0)
                .getAnswer())
        .isEqualTo("4cert-ans-ehc");
  }

  private void prepareForUpdatedPrivateMergedForm() {
    when(mockFormConfigurationServiceAdapter.getAllVersions(TEST_EHC.getName()))
        .thenReturn(
            ImmutableList.of(
                Form.builder()
                    .name(TEST_EHC.getName())
                    .version("8.0")
                    .privateCode(10)
                    .status(FormStatus.PRIVATE)
                    .build()));

    when(mockFormConfigurationServiceAdapter.getActiveMergedForm(TEST_EHC.getName()))
        .thenReturn(MERGED_FORM_EXA_V2_OFFLINE_EHC);

    when(mockFormConfigurationServiceAdapter.getPrivateMergedForm(TEST_EHC.getName(), "10"))
        .thenReturn(MERGED_FORM_EXA_V2_EHC_V2);
  }

  private MergedFormQuestion mergedFormQuestionWithQuestionIdFormNamePageNumber(
      Long questionId, String formName, int pageNumber) {

    return MergedFormQuestion.builder()
        .questionId(questionId)
        .formName(formName)
        .pageNumber(pageNumber)
        .build();
  }

  private ApplicationFormItem responseItemWithQuestionIdAnswerFormNamePageNumber(
      Long questionId, String answer, String formName, int pageNumber) {

    // NOTE: only questionId and answer matter for this test
    return ApplicationFormItem.builder()
        .questionId(questionId)
        .answer(answer)
        .formName(formName)
        .formQuestionId(1L)
        .text("doesntMatter")
        .pageNumber(pageNumber)
        .questionOrder(1)
        .build();
  }

  private List<ApplicationFormItem> responseItemsForFormName(
      PersistentApplicationForm applicationForm, String formName) {
    return applicationForm.getData().getResponseItems().stream()
        .filter(ri -> ri.getFormName().equals(formName))
        .collect(Collectors.toList());
  }

  private List<ApplicationFormItem> certificateResponseItems(
      PersistentApplicationForm applicationForm) {
    return applicationForm.getPersistentConsignments().get(0).getData().getResponseItems().stream()
        .collect(Collectors.toList());
  }

  private void testUpdatedPrivateForm(Optional<PersistentApplicationForm> updatedForm) {
    assertThat(updatedForm).isPresent();
    verify(mockFormConfigurationServiceAdapter).getPrivateMergedForm(TEST_EHC.getName(), "10");
    assertThat(updatedForm.get().getData().getExa().getVersion()).isEqualTo("2");
    assertThat(updatedForm.get().getData().getEhc().getVersion()).isEqualTo("2");
  }

  private void testEHCAndEXAAnswers(PersistentApplicationForm updatedForm) {
    /*NOTE: only one EXA and EHC answer carried over as others either i) have a duplicated question
    id on the
    original answers or on the EXA or EHC or ii) do not have their question id on the updated EXA
    or EHC form. */
    ApplicationFormItem expectedEXAResponseItem =
        responseItemMapper.getApplicationFormItem(
            mergedFormQuestionWithQuestionIdFormNamePageNumber(2L, TEST_EXA.getName(), 1),
            "2ans-exa");

    ApplicationFormItem expectedEHCResponseItem =
        responseItemMapper.getApplicationFormItem(
            mergedFormQuestionWithQuestionIdFormNamePageNumber(4L, TEST_EHC.getName(), 2),
            "4ans-ehc");

    assertThat(responseItemsForFormName(updatedForm, TEST_EXA.getName()))
        .containsOnly(expectedEXAResponseItem);

    assertThat(responseItemsForFormName(updatedForm, TEST_EHC.getName()))
        .containsOnly(expectedEHCResponseItem);
  }
}

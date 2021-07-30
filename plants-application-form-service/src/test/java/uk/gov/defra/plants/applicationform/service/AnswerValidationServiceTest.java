package uk.gov.defra.plants.applicationform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionType.DATE;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionType.MULTI_SELECT;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionType.NUMBER;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionType.SINGLE_SELECT;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionType.TEXT;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionType.TEXTAREA;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import lombok.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.applicationform.ApplicationFormTestData;
import uk.gov.defra.plants.applicationform.model.MultiplesApplicationValidationErrors;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationType;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ConsignmentStatus;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

@RunWith(MockitoJUnitRunner.class)
public class AnswerValidationServiceTest {

  private static final NameAndVersion EHC =
      NameAndVersion.builder().name("EHC123").version("1.0").build();
  private static final NameAndVersion EXA =
      NameAndVersion.builder().name("EXA456").version("1.0").build();
  private static final String RANGE_VALUE = "Enter a number between 1 and 100";

  private static final AnswerConstraint REQUIRED_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.REQUIRED)
          .message("Answer is required")
          .rule("true")
          .build();

  private static final AnswerConstraint MIN_SIZE_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.MIN_SIZE)
          .message("Answer is too short")
          .rule("3")
          .build();

  private static final AnswerConstraint MAX_SIZE_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.MAX_SIZE)
          .message("Answer is too long")
          .rule("5")
          .build();

  private static final AnswerConstraint MIN_VALUE_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.MIN_VALUE)
          .message(RANGE_VALUE)
          .rule("1")
          .build();

  private static final AnswerConstraint MAX_VALUE_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.MAX_VALUE)
          .message(RANGE_VALUE)
          .rule("100")
          .build();

  private static final AnswerConstraint LOWER_DATE_BOUNDARY_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.LOWER_DATE_BOUNDARY)
          .message("Date must be at least two days from now")
          .rule("2")
          .build();
  private static final AnswerConstraint UPPER_DATE_BOUNDARY_CONSTRAINT =
      AnswerConstraint.builder()
          .type(AnswerConstraintType.UPPER_DATE_BOUNDARY)
          .message("Date must be at most two days from now")
          .rule("2")
          .build();

  private static final String ONE_OR_MORE_FROM_THE_OPTIONS_AVAILABLE =
      "should select one or more from the options available";
  private static final String FROM_THE_OPTIONS_AVAILABLE =
      "should select one from the options available";

  private static final String AEROPLANE = "Aeroplane";
  private static final String SHIP = "Ship";
  private static final String ROAD_VEHICLE = "Road vehicle";
  private static final String QUOTES = "\"";

  private static final Long QUESTION_ID = 1L;

  private final List<MergedFormQuestionOption> questionOptions =
      ImmutableList.of(
          MergedFormQuestionOption.builder().order(1).text(AEROPLANE).build(),
          MergedFormQuestionOption.builder().order(2).text(SHIP).build(),
          MergedFormQuestionOption.builder().order(3).text(ROAD_VEHICLE).build());

  @Mock private FormConfigurationServiceAdapter formConfigurationServiceAdapter;
  @Mock private HealthCertificateServiceAdapter healthCertificateServiceAdapter;

  private List<ValidationError> errorMessages;
  private MultiplesApplicationValidationErrors errors;
  private AnswerValidationService answerValidationService;
  private Optional<HealthCertificate> healthCertificate =
      Optional.of(
          HealthCertificate.builder()
              .healthCertificateMetadata(HealthCertificateMetadata.WITH_DEFAULTS)
              .applicationType(ApplicationType.PHYTO.getApplicationTypeName())
              .build());

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    errorMessages = null;
    answerValidationService =
        new AnswerValidationService(
            formConfigurationServiceAdapter, healthCertificateServiceAdapter);
  }

  @Test
  public void formNotFoundThrowsNotFoundException() {
    when(formConfigurationServiceAdapter.getMergedFormPages(any(), any(), any(), any()))
        .thenThrow(new NotFoundException());
    ApplicationForm applicationForm = applicationFormWithAnswers(NUMBER, "");
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(
            () ->
                answerValidationService.validateComplete(
                    applicationForm, healthCertificate, Collections.emptyList()));
  }

  @Test
  public void testNoConstraints() {
    givenAFormExistsWithConstraints(NUMBER, question(QUESTION_ID));

    whenCompleteApplicationFormIsValidatedWithAnswers(NUMBER, "");
    thenThereAreNoErrorMessages();

    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "");
    thenThereAreNoErrorMessages();
  }

  @Test
  public void testRequiredMinSizeMaxSize_null() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, new String[] {null});
    thenOnlyErrorHasMessage(REQUIRED_CONSTRAINT.getMessage());
  }

  @Test
  public void testRequiredMinSizeMaxSize_empty() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, new String[] {null});
    thenOnlyErrorHasMessage(REQUIRED_CONSTRAINT.getMessage());
  }

  @Test
  public void testRequiredMinSizeMaxSize_short() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "12");
    thenOnlyErrorHasMessage(MIN_SIZE_CONSTRAINT.getMessage());
  }

  @Test
  public void testRequiredMinSizeMaxSize_long() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "123456");
    thenOnlyErrorHasMessage(MAX_SIZE_CONSTRAINT.getMessage());
  }

  @Test
  public void testRequiredMinSizeMaxSize_ok() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "12345");
    thenThereAreNoErrorMessages();
  }

  @Test
  public void testRequiredMinSizeMaxSize_twoQuestions_ok() {
    givenAFormExistsWithConstraints(
        TEXT,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT),
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(TEXT, "i'mok", "metoo");
    thenThereAreNoErrorMessages();
  }

  @Test
  public void testRequiredMinSizeMaxSize_twoQuestions_bothFail() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT),
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_SIZE_CONSTRAINT, MAX_SIZE_CONSTRAINT));

    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "12", "123456");

    assertThat(errorMessages)
        .hasSize(2)
        .first()
        .hasFieldOrPropertyWithValue("message", MIN_SIZE_CONSTRAINT.getMessage())
        .hasFieldOrPropertyWithValue("constraintType", AnswerConstraintType.MIN_SIZE);
    assertThat(errorMessages)
        .last()
        .hasFieldOrPropertyWithValue("message", MAX_SIZE_CONSTRAINT.getMessage())
        .hasFieldOrPropertyWithValue("constraintType", AnswerConstraintType.MAX_SIZE);
  }

  @Test
  public void testRequiredMinValueMaxValue_withinRange() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "50");
    thenThereAreNoErrorMessages();
  }

  @Test
  public void testRequiredMinValueMaxValue_belowRange() {
    givenAFormExistsWithConstraints(
        TEXT,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(TEXT, "-1");
    thenOnlyErrorHasMessage(MIN_VALUE_CONSTRAINT.getMessage());
  }

  @Test
  public void testRequiredNumber_belowRange() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "-1");
    thenOnlyErrorHasMessage("The value must be a whole number");
  }

  @Test
  public void testRequiredMinValueMaxValue_aboveRange() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(NUMBER, "101");
    thenOnlyErrorHasMessage(MAX_VALUE_CONSTRAINT.getMessage());
  }

  @Test
  public void testBoundedDate_withinRange() {
    givenAFormExistsWithConstraints(
        DATE,
        question(QUESTION_ID, LOWER_DATE_BOUNDARY_CONSTRAINT, UPPER_DATE_BOUNDARY_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(DATE, LocalDate.now().plusDays(2).toString());
    thenThereAreNoErrorMessages();
  }

  @Test
  public void testBoundedDate_beforeRange() {
    givenAFormExistsWithConstraints(
        DATE,
        question(QUESTION_ID, LOWER_DATE_BOUNDARY_CONSTRAINT, UPPER_DATE_BOUNDARY_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(DATE, LocalDate.now().plusDays(1).toString());
    thenOnlyErrorHasMessage("Date must be at least two days from now");
  }

  @Test
  public void testBoundedDate_afterRange() {
    givenAFormExistsWithConstraints(
        DATE,
        question(QUESTION_ID, LOWER_DATE_BOUNDARY_CONSTRAINT, UPPER_DATE_BOUNDARY_CONSTRAINT));
    whenPartialApplicationFormIsValidatedWithAnswers(DATE, LocalDate.now().plusDays(3).toString());
    thenOnlyErrorHasMessage("Date must be at most two days from now");
  }

  @Test
  public void testSingleChoiceQuestionWithAnAnswer() {
    givenAFormExistsWithConstraints(SINGLE_SELECT, question(QUESTION_ID));
    whenPartialApplicationFormIsValidatedWithAnswers(SINGLE_SELECT, AEROPLANE);
    thenThereAreNoErrorMessages();
  }

  @Test
  public void testSingleChoiceQuestionWithAWrongAnswer() {
    givenAFormExistsWithConstraints(SINGLE_SELECT, question(QUESTION_ID));
    whenPartialApplicationFormIsValidatedWithAnswers(SINGLE_SELECT, "WRONG");
    thenOnlyErrorHasMessage(FROM_THE_OPTIONS_AVAILABLE);
  }

  @Test
  public void testMultiChoiceQuestionWithAnAnswer() {
    givenAFormExistsWithConstraints(MULTI_SELECT, question(QUESTION_ID));
    whenPartialApplicationFormIsValidatedWithAnswers(MULTI_SELECT, AEROPLANE, SHIP);
    thenThereAreNoErrorMessages();
  }

  @Test
  public void testMultiChoiceQuestionWithAWrongAnswer() {
    givenAFormExistsWithConstraints(MULTI_SELECT, question(QUESTION_ID));
    whenPartialApplicationFormIsValidatedWithAnswers(MULTI_SELECT, "WRONG");
    thenOnlyErrorHasMessage(ONE_OR_MORE_FROM_THE_OPTIONS_AVAILABLE);
  }

  @Test
  public void testValidationApplicationNotAllQuestionsAnswered() {
    givenAFormExistsWithConstraints(
        TEXT,
        question(QUESTION_ID, REQUIRED_CONSTRAINT),
        question(QUESTION_ID, REQUIRED_CONSTRAINT));
    whenCompleteApplicationFormIsValidatedWithAnswers(TEXT, "an answer", "");
    thenOnlyErrorHasMessage("Answer is required");
  }

  @Test
  public void testValidationApplicationCertificatesNotAddedForMultiple() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT),
        question(QUESTION_ID, REQUIRED_CONSTRAINT));
    whenCompleteMultipleApplicationFormIsValidatedWithAnswers(NUMBER, List.of(), "an answer", "");
    assertThat(errors.getCommonErrors().get().get(0))
        .isEqualTo("You need to add at least one certificate to this application");
  }

  @Test
  public void testValidationApplicationCertificatesAddedMoreThanRequiredForMultiple() {
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT),
        question(QUESTION_ID, REQUIRED_CONSTRAINT));
    whenCompleteMultipleApplicationFormIsValidatedWithAnswers(
        NUMBER, List.of(getACertificate("CERT-1"), getACertificate("CERT-2")), "an answer", "");
    assertThat(errors.getCommonErrors().get().get(0))
        .isEqualTo("You can only add 1 certificates to an application");
  }

  private Consignment getACertificate(String certificateRef) {
    return Consignment.builder()
        .responseItem(
            ApplicationFormItem.builder().formQuestionId(-8L).answer(certificateRef).build())
        .applicationFormId(UUID.randomUUID())
        .applicationId(0L)
        .status(ConsignmentStatus.CLOSED)
        .responseItems(List.of())
        .build();
  }

  @Test
  public void testValidationApplicationOnlyWithCommodityTypeShouldNotThrowError() {
    givenAFormExistsWithConstraints(
        TEXT,
        question(QUESTION_ID, REQUIRED_CONSTRAINT),
        question(QUESTION_ID, REQUIRED_CONSTRAINT));
    whenCompleteApplicationFormOnlyWithCommodityTypesAndAnswersForOtherQuestions(
        TEXT, "an answer", "");
    thenOnlyErrorHasMessage("Answer is required");
  }

  @Test
  public void testValidationApplicationNotAllCustomQuestionsAnswered() {
    givenAFormCustomQuestionExistsWithConstraints(
        TEXT, question(-1L, REQUIRED_CONSTRAINT), question(-1L, REQUIRED_CONSTRAINT));
    whenCompleteApplicationFormIsValidatedWithAnswers(NUMBER, "an answer", "");
    thenOnlyErrorHasMessage("Answer is required");
  }

  @Test
  public void testPullExporterQuestionsForExporterApplicationOnValidateComplete() {
    UUID exporter = UUID.randomUUID();
    ApplicationForm applicationForm =
        applicationFormWithAnswers(TEXT, "Trader name", "Country of Export")
            .toBuilder()
            .applicant(exporter)
            .build();
    answerValidationService.validateComplete(
        applicationForm, healthCertificate, Collections.emptyList());
    verify(formConfigurationServiceAdapter).getMergedFormPages(any(), any(), any(), any());
  }

  @Test
  public void testExporterValidatesCertificateWithErrors() {
    UUID exporter = UUID.randomUUID();
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT),
        question(2L, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));

    ApplicationForm applicationForm =
        applicationFormWithAnswers(NUMBER, "101").toBuilder().id(1L).applicant(exporter).build();

    Consignment certificate = certificateWithAnswers(applicationForm.getId(), NUMBER, "0");

    List<ValidationError> errors =
        answerValidationService.validateConsignment(applicationForm, certificate);

    assertThat(errors).hasSize(2);
    assertThat(
            errors.stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.toUnmodifiableList()))
        .contains(MIN_VALUE_CONSTRAINT.getMessage());
    assertThat(
            errors.stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.toUnmodifiableList()))
        .contains(REQUIRED_CONSTRAINT.getMessage());
  }

  @Test
  public void testExporterValidatesCertificateNoErrors() {
    UUID exporter = UUID.randomUUID();
    givenAFormExistsWithConstraints(
        NUMBER,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));

    ApplicationForm applicationForm =
        applicationFormWithAnswers(NUMBER, "0").toBuilder().id(1L).applicant(exporter).build();

    Consignment certificate =
        certificateWithAnswers(applicationForm.getId(), NUMBER, "44", "Country of Export");

    List<ValidationError> errors =
        answerValidationService.validateConsignment(applicationForm, certificate);

    assertThat(errors).isEmpty();
  }

  @Test
  public void testExporterValidatesCertificateSharedAnswersWithErrors() {
    UUID exporter = UUID.randomUUID();
    givenAFormExistsWithConstraints(
        NUMBER,
        MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES,
        question(QUESTION_ID, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT),
        question(2L, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));

    ApplicationForm applicationForm =
        applicationFormWithAnswers(NUMBER, "0").toBuilder().id(1L).applicant(exporter).build();

    Consignment certificate = certificateWithAnswers(applicationForm.getId(), NUMBER);

    List<ValidationError> errors =
        answerValidationService.validateConsignment(applicationForm, certificate);

    assertThat(errors).hasSize(2);
    assertThat(
            errors.stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.toUnmodifiableList()))
        .contains(MIN_VALUE_CONSTRAINT.getMessage());
    assertThat(
            errors.stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.toUnmodifiableList()))
        .contains(REQUIRED_CONSTRAINT.getMessage());
  }

  @Test
  public void testExporterValidatesCertificateSharedAnswersNoErrors() {
    UUID exporter = UUID.randomUUID();
    givenAFormExistsWithConstraints(
        NUMBER,
        MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES,
        question(1L, REQUIRED_CONSTRAINT, MIN_VALUE_CONSTRAINT, MAX_VALUE_CONSTRAINT));

    ApplicationForm applicationForm =
        applicationFormWithAnswers(NUMBER, "1").toBuilder().id(2L).applicant(exporter).build();

    Consignment emptyCertificate = certificateWithAnswers(1L, NUMBER);
    List<ValidationError> errors =
        answerValidationService.validateConsignment(applicationForm, emptyCertificate);

    assertThat(errors).isEmpty();
  }

  private void givenAFormExistsWithConstraints(
      final QuestionType questionType, final FormQuestion... formQuestions) {
    givenAFormExistsWithConstraints(
        questionType, MergedFormPageType.CERTIFICATE_LEVEL, formQuestions);
  }

  /** @param formQuestions note that all will be assigned to the ehcForm, exaForm will have none. */
  private void givenAFormExistsWithConstraints(
      final QuestionType questionType,
      final MergedFormPageType mergedFormPageType,
      final FormQuestion... formQuestions) {
    when(formConfigurationServiceAdapter.getMergedFormPages(any(), any(), any(), any()))
        .thenReturn(
            Collections.singletonList(
                MergedFormPage.builder()
                    .pageNumber(1)
                    .mergedFormPageType(mergedFormPageType)
                    .questions(
                        IntStream.range(0, formQuestions.length)
                            .mapToObj(i -> formQuestions[i].toBuilder().id(i + 1L).build())
                            .map(
                                fq ->
                                    fromFormQuestion(
                                        fq,
                                        "EHC123",
                                        questionType,
                                        1,
                                        (questionType.equals(SINGLE_SELECT)
                                                || questionType.equals(MULTI_SELECT))
                                            ? questionOptions
                                            : Collections.emptyList()))
                            .collect(Collectors.toList()))
                    .build()));
  }

  private void givenAFormCustomQuestionExistsWithConstraints(
      final QuestionType questionType, final FormQuestion... formQuestions) {
    when(formConfigurationServiceAdapter.getMergedFormPages(any(), any(), any(), any()))
        .thenReturn(
            Collections.singletonList(
                MergedFormPage.builder()
                    .pageNumber(1)
                    .questions(
                        Collections.singletonList(
                            fromFormQuestion(
                                formQuestions[0],
                                "EHC123",
                                questionType,
                                1,
                                (questionType.equals(TEXT) || questionType.equals(TEXTAREA))
                                    ? questionOptions
                                    : Collections.emptyList())))
                    .build()));
  }

  private FormQuestion question(Long questionId, final AnswerConstraint... answerConstraints) {
    return FormQuestion.builder()
        .id(questionId)
        .constraints(Arrays.asList(answerConstraints))
        .templateField(FormFieldDescriptor.builder().name("Tx").type(FormFieldType.TEXT).build())
        .build();
  }

  private void whenPartialApplicationFormIsValidatedWithAnswers(
      final QuestionType questionType, final String... answers) {
    NameAndVersion ehc = NameAndVersion.builder().name("EHC").version("1.0").build();
    NameAndVersion exa = NameAndVersion.builder().name("EXA").version("1.0").build();
    errorMessages =
        answerValidationService.validatePartial(
            ApplicationForm.builder()
                .id(1L)
                .ehc(ehc)
                .exa(exa)
                .applicant(UUID.randomUUID())
                .responseItems(asApplicationFormItems(questionType, answers))
                .build(),
            asApplicationFormItems(questionType, answers));
  }

  private void whenCompleteApplicationFormIsValidatedWithAnswers(
      final QuestionType questionType, final String... answers) {

    ApplicationForm applicationForm = applicationFormWithAnswers(questionType, answers);

    errorMessages =
        answerValidationService.validateComplete(
            applicationForm, healthCertificate, Collections.emptyList());
  }

  private void whenCompleteMultipleApplicationFormIsValidatedWithAnswers(
      final QuestionType questionType, List<Consignment> consignments, final String... answers) {

    ApplicationForm applicationForm =
        multipleApplicationFormWithAnswers(questionType, consignments, answers);

    errorMessages =
        answerValidationService.validateComplete(
            applicationForm, healthCertificate, Collections.emptyList());
    errors =
        answerValidationService
            .validateMultipleApplication(applicationForm, HealthCertificateMetadata.WITH_DEFAULTS)
            .get();
  }

  private void whenCompleteApplicationFormOnlyWithCommodityTypesAndAnswersForOtherQuestions(
      final QuestionType questionType, final String... answers) {

    ApplicationForm applicationForm =
        applicationFormOnlyWithCommodityTypesAndAnswersForOtherQuestions(questionType, answers);

    errorMessages =
        answerValidationService.validateComplete(
            applicationForm, healthCertificate, Collections.emptyList());
  }

  private List<String> getAsJSONArrayString(final String... answers) {
    return Collections.singletonList(
        Stream.of(answers)
            .map(answer -> QUOTES + answer + QUOTES)
            .collect(Collectors.joining(",", "[", "]")));
  }

  private ApplicationForm applicationFormWithAnswers(
      final QuestionType questionType, final String... answers) {
    final List<ApplicationFormItem> items = asApplicationFormItems(questionType, answers);

    return ApplicationForm.builder()
        .ehc(EHC)
        .exa(EXA)
        .destinationCountry(ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE)
        .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
        .responseItems(items)
        .build();
  }

  private ApplicationForm multipleApplicationFormWithAnswers(
      final QuestionType questionType, List<Consignment> consignments, final String... answers) {
    return applicationFormWithAnswers(questionType, answers)
        .toBuilder()
        .consignments(consignments)
        .build();
  }

  private Consignment certificateWithAnswers(
      final Long applicationId, final QuestionType questionType, final String... answers) {

    final List<ApplicationFormItem> items = asApplicationFormItems(questionType, answers);

    return Consignment.builder()
        .consignmentId(UUID.randomUUID())
        .applicationId(applicationId)
        .applicationFormId(UUID.randomUUID())
        .status(ConsignmentStatus.OPEN)
        .responseItems(items)
        .build();
  }

  private ApplicationForm applicationFormOnlyWithCommodityTypesAndAnswersForOtherQuestions(
      final QuestionType questionType, final String... answers) {
    final List<ApplicationFormItem> items = asApplicationFormItems(questionType, answers);

    return ApplicationForm.builder()
        .ehc(EHC)
        .exa(EXA)
        .destinationCountry(ApplicationFormTestData.TEST_DESTINATION_COUNTRY_CODE)
        .commodityGroup(ApplicationFormTestData.TEST_COMMODITY)
        .responseItems(items)
        .build();
  }

  private List<ApplicationFormItem> asApplicationFormItems(
      final QuestionType questionType, final String... answers) {
    String[] processedAnswers =
        (questionType.equals(MULTI_SELECT) && answers.length > 0)
            ? getAsJSONArrayString(answers).toArray(new String[0])
            : answers;

    return IntStream.range(0, processedAnswers.length)
        .mapToObj(
            i ->
                ApplicationFormItem.builder()
                    .formQuestionId(i + 1L)
                    .answer(processedAnswers[i])
                    .pageNumber(1)
                    .pageOccurrence(0)
                    .build())
        .collect(Collectors.toList());
  }

  private void thenThereAreNoErrorMessages() {
    assertThat(errorMessages).isEmpty();
  }

  private void thenOnlyErrorHasMessage(final String message) {
    assertThat(errorMessages).hasSize(1).first().hasFieldOrPropertyWithValue("message", message);
  }

  private static MergedFormQuestion fromFormQuestion(
      @NonNull final FormQuestion formQuestion,
      @NonNull final String formName,
      @NonNull final QuestionType questionType,
      @NonNull final Integer pageNumber,
      @NonNull final List<MergedFormQuestionOption> questionOptions) {
    return MergedFormQuestion.builder()
        .formQuestionId(formQuestion.getId())
        .questionScope(formQuestion.getQuestionScope())
        .formName(formName)
        .questionOptions(questionOptions)
        .questionType(questionType)
        .questionId(formQuestion.getQuestionId())
        .constraints(formQuestion.getConstraints())
        .templateFields(formQuestion.getTemplateFields())
        .pageNumber(pageNumber)
        .questionOrder(formQuestion.getQuestionOrder())
        .build();
  }
}

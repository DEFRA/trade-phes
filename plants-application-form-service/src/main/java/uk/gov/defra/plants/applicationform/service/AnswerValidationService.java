package uk.gov.defra.plants.applicationform.service;

import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.UPLOAD_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type.EHC;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.applicationform.model.MultiplesApplicationValidationErrors;
import uk.gov.defra.plants.applicationform.representation.ApplicationForm;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.applicationform.representation.ValidationError;
import uk.gov.defra.plants.applicationform.validation.answers.AnswerValidator;
import uk.gov.defra.plants.common.representation.CertificateApplicationError;
import uk.gov.defra.plants.formconfiguration.adapter.FormConfigurationServiceAdapter;
import uk.gov.defra.plants.formconfiguration.adapter.HealthCertificateServiceAdapter;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadata;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;

@Slf4j
public class AnswerValidationService {

  private static final long CERTIFICATE_REFERENCE_NUMBER_QUESTION_ID = -8;
  private final FormConfigurationServiceAdapter formConfigurationServiceAdapter;

  @Inject
  public AnswerValidationService(
      final FormConfigurationServiceAdapter formConfigurationServiceAdapter,
      final HealthCertificateServiceAdapter healthCertificateServiceAdapter) {
    this.formConfigurationServiceAdapter = formConfigurationServiceAdapter;
  }

  List<ValidationError> validatePartial(
      @NonNull ApplicationForm applicationForm, @NonNull List<ApplicationFormItem> answers) {

    final List<Long> answerFormQuestionIds =
        answers.stream().map(ApplicationFormItem::getFormQuestionId).collect(Collectors.toList());

    final Map<Long, MergedFormQuestion> questions =
        getQuestionsForForm(applicationForm).stream()
            .filter(fq -> answerFormQuestionIds.contains(fq.getFormQuestionId()))
            .collect(Collectors.toMap(MergedFormQuestion::getFormQuestionId, Functions.identity()));

    return validateAnswers(applicationForm, answers, questions, Lists.newArrayList());
  }

  Optional<MultiplesApplicationValidationErrors> validateMultipleApplication(
      @NonNull ApplicationForm applicationForm,
      @NonNull HealthCertificateMetadata healthCertificateMetadata) {
    List<String> commonErrors = new ArrayList<>();
    List<CertificateApplicationError> certificateApplicationErrors = new ArrayList<>();

    final List<Consignment> consignments = applicationForm.getConsignments();
    if (consignments == null) {
      populateCommonErrors(healthCertificateMetadata, commonErrors, null);
    } else {
      populateCommonErrors(healthCertificateMetadata, commonErrors, consignments);

      consignments.forEach(
          certificateApplication -> {
            List<ApplicationFormItem> responseItems = certificateApplication.getResponseItems();
            List<ValidationError> certificateValidationErrors =
                getCertificateErrors(applicationForm, certificateApplication);
            if (!certificateValidationErrors.isEmpty()) {
              populateCertificateValidationErrors(
                  certificateApplicationErrors,
                  certificateApplication,
                  responseItems,
                  certificateValidationErrors);
            }
          });
    }

    if (certificateApplicationErrors.isEmpty() && commonErrors.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        MultiplesApplicationValidationErrors.builder()
            .certificateApplicationErrors(
                certificateApplicationErrors.isEmpty()
                    ? Optional.empty()
                    : Optional.of(certificateApplicationErrors))
            .commonErrors(commonErrors.isEmpty() ? Optional.empty() : Optional.of(commonErrors))
            .build());
  }

  private void populateCertificateValidationErrors(
      List<CertificateApplicationError> certificateApplicationErrors,
      Consignment consignment,
      List<ApplicationFormItem> responseItems,
      List<ValidationError> certificateValidationErrors) {
    Optional<String> certificateReference =
        responseItems.stream()
            .filter(
                responseItem ->
                    responseItem.getFormQuestionId() == CERTIFICATE_REFERENCE_NUMBER_QUESTION_ID)
            .findFirst()
            .map(ApplicationFormItem::getAnswer);
    certificateApplicationErrors.add(
        CertificateApplicationError.builder()
            .certificateId(consignment.getConsignmentId())
            .certificateReference(certificateReference.orElse(""))
            .numberOfErrors(certificateValidationErrors.size())
            .build());
  }

  private void populateCommonErrors(
      @NonNull HealthCertificateMetadata healthCertificateMetadata,
      List<String> commonErrors,
      List<Consignment> consignments) {
    if (consignments == null || consignments.isEmpty()) {
      commonErrors.add("You need to add at least one certificate to this application");
    }
    if (consignments != null && consignments.size() > healthCertificateMetadata.getMaxEhc()) {
      commonErrors.add(
          String.format(
              "You can only add %s certificates to an application",
              healthCertificateMetadata.getMaxEhc()));
    }
  }

  public List<ValidationError> getCertificateErrors(
      @NonNull ApplicationForm applicationForm, @NonNull Consignment consignment) {
    return new ArrayList<>(validateConsignment(applicationForm, consignment));
  }

  List<ValidationError> validateConsignment(
      @NonNull ApplicationForm applicationForm, @NonNull Consignment consignment) {

    final List<Long> answerFormQuestionIds =
        consignment.getResponseItems().stream()
            .map(ApplicationFormItem::getFormQuestionId)
            .collect(Collectors.toList());

    List<MergedFormQuestion> questions = getQuestionsForCertificate(applicationForm);

    List<ApplicationFormItem> responseItems = new ArrayList<>(consignment.getResponseItems());

    List<Long> commonlyAnsweredQuestions =
        getCommonQuestions(applicationForm, answerFormQuestionIds);
    answerFormQuestionIds.addAll(commonlyAnsweredQuestions);

    responseItems.addAll(
        applicationForm.getResponseItems().stream()
            .filter(answer -> commonlyAnsweredQuestions.contains(answer.getFormQuestionId()))
            .collect(Collectors.toList()));

    final Map<Long, MergedFormQuestion> questionsById =
        questions.stream()
            .filter(fq -> answerFormQuestionIds.contains(fq.getFormQuestionId()))
            .collect(Collectors.toMap(MergedFormQuestion::getFormQuestionId, Functions.identity()));

    return new ArrayList<>(
        validateAnswers(
            applicationForm,
            responseItems,
            questionsById,
            getValidationMessagesForMissingAnswers(questions, questionsById)));
  }

  private List<Long> getCommonQuestions(
      final ApplicationForm applicationForm, final List<Long> answerFormQuestionIds) {

    List<MergedFormPage> mergedFormPages =
        formConfigurationServiceAdapter.getMergedFormPages(
            applicationForm.getEhc().getName(),
            applicationForm.getEhc().getVersion(),
            applicationForm.getExa().getName(),
            applicationForm.getExa().getVersion());

    List<Long> commonQuestions =
        mergedFormPages.stream()
            .filter(
                page ->
                    page.getMergedFormPageType() == MergedFormPageType.COMMON_FOR_ALL_CERTIFICATES)
            .flatMap(
                page -> page.getQuestions().stream().map(MergedFormQuestion::getFormQuestionId))
            .collect(Collectors.toList());

    return applicationForm.getResponseItems().stream()
        .filter(
            answer ->
                !answerFormQuestionIds.contains(answer.getFormQuestionId())
                    && commonQuestions.contains(answer.getFormQuestionId()))
        .map(ApplicationFormItem::getFormQuestionId)
        .collect(Collectors.toList());
  }

  List<ValidationError> validateComplete(
      @NonNull ApplicationForm applicationForm,
      Optional<HealthCertificate> healthCertificate,
      List<MergedFormPage> mergedFormPages) {

    final List<Long> answerFormQuestionIds =
        applicationForm.getResponseItems().stream()
            .map(ApplicationFormItem::getFormQuestionId)
            .collect(Collectors.toList());

    List<MergedFormQuestion> questions =
        new ArrayList<>(
            getQuestionsForFormOnly(applicationForm, healthCertificate, mergedFormPages));

    final Map<Long, MergedFormQuestion> questionsById =
        questions.stream()
            .filter(fq -> answerFormQuestionIds.contains(fq.getFormQuestionId()))
            .collect(Collectors.toMap(MergedFormQuestion::getFormQuestionId, Functions.identity()));

    return validateAnswers(
        applicationForm,
        applicationForm.getResponseItems(),
        questionsById,
        getValidationMessagesForMissingAnswers(questions, questionsById));
  }

  private List<ValidationError> validateAnswers(
      ApplicationForm applicationForm,
      List<ApplicationFormItem> answers,
      Map<Long, MergedFormQuestion> questionsById,
      List<ValidationError> errorMessages) {
    answers.stream()
        .filter(a -> !UPLOAD_QUESTION.getFormQuestionId().equals(a.getFormQuestionId()))
        .filter(a -> questionsById.get(a.getFormQuestionId()) != null)
        .map(a -> validate(applicationForm, questionsById.get(a.getFormQuestionId()), a))
        .forEachOrdered(errorMessage -> errorMessage.ifPresent(errorMessages::add));

    return errorMessages;
  }

  private List<ValidationError> getValidationMessagesForMissingAnswers(
      List<MergedFormQuestion> questions, Map<Long, MergedFormQuestion> questionsById) {

    return questions.stream()
        .filter(fq -> !questionsById.containsValue(fq))
        .filter(fq -> getRequiredConstraint(fq).isPresent())
        .map(
            fq ->
                ValidationError.builder()
                    .formQuestionId(fq.getFormQuestionId())
                    .message(getRequiredConstraint(fq).map(AnswerConstraint::getMessage).get())
                    .build())
        .collect(Collectors.toList());
  }

  private Optional<AnswerConstraint> getRequiredConstraint(MergedFormQuestion fq) {

    return fq.getConstraints().stream()
        .filter(ac -> ac.getType().equals(AnswerConstraintType.REQUIRED))
        .findFirst();
  }

  private List<MergedFormQuestion> getQuestionsForFormOnly(
      final ApplicationForm applicationForm,
      Optional<HealthCertificate> healthCertificate,
      List<MergedFormPage> mergedFormPages) {

    if (mergedFormPages.isEmpty()) {
      mergedFormPages = getMergedFormPages(applicationForm);
    }

    if (isAMultipleApplication(healthCertificate)) {
      mergedFormPages =
          mergedFormPages.stream()
              .filter(mergedFormPage -> mergedFormPage.getType() != EHC)
              .collect(Collectors.toUnmodifiableList());
    }
    return mergedFormPages.stream()
        .flatMap(mfp -> mfp.getQuestions().stream())
        .collect(Collectors.toList());
  }

  private List<MergedFormPage> getMergedFormPages(final ApplicationForm applicationForm) {

    return formConfigurationServiceAdapter.getMergedFormPages(
        applicationForm.getEhc().getName(),
        applicationForm.getEhc().getVersion(),
        applicationForm.getExa().getName(),
        applicationForm.getExa().getVersion());
  }

  private List<MergedFormQuestion> getQuestionsForCertificate(
      final ApplicationForm applicationForm) {
    return getMergedQuestions(applicationForm, true);
  }

  private List<MergedFormQuestion> getMergedQuestions(
      final ApplicationForm applicationForm, boolean removeExaAndCustomPages) {
    List<MergedFormPage> mergedFormPages = getMergedFormPages(applicationForm);
    if (removeExaAndCustomPages) {
      return mergedFormPages.stream()
          .filter(
              mergedFormPage ->
                  mergedFormPage.getMergedFormPageType() != MergedFormPageType.APPLICATION_LEVEL)
          .flatMap(mfp -> mfp.getQuestions().stream())
          .collect(Collectors.toList());
    } else {
      return mergedFormPages.stream()
          .flatMap(mfp -> mfp.getQuestions().stream())
          .collect(Collectors.toList());
    }
  }

  private List<MergedFormQuestion> getQuestionsForForm(final ApplicationForm applicationForm) {
    return getMergedQuestions(applicationForm, false);
  }

  private boolean isAMultipleApplication(Optional<HealthCertificate> healthCertificate) {

    return healthCertificate
        .map(
            hc ->
                hc.getHealthCertificateMetadata()
                    .getMultipleBlocks()
                    .equals(HealthCertificateMetadataMultipleBlocks.MULTIPLE_APPLICATION))
        .orElseThrow();
  }

  private Optional<ValidationError> validate(
      final ApplicationForm applicationForm,
      final MergedFormQuestion formQuestion,
      final ApplicationFormItem answer) {
    List<AnswerConstraint> questionTypeSpecificConstraints =
        AnswerTypeConstraints.getQuestionTypeSpecificConstraints(formQuestion);
    return Stream.concat(
            questionTypeSpecificConstraints.stream(), formQuestion.getConstraints().stream())
        .filter(
            ac -> !AnswerValidator.isValid(applicationForm, answer.getAnswer(), ac, formQuestion))
        .findFirst()
        .map(answerConstraint -> asErrorMessage(formQuestion, answerConstraint));
  }

  private ValidationError asErrorMessage(
      final MergedFormQuestion formQuestion, final AnswerConstraint answerConstraint) {
    return ValidationError.builder()
        .formQuestionId(formQuestion.getFormQuestionId())
        .message(answerConstraint.getMessage())
        .constraintType(answerConstraint.getType())
        .build();
  }
}

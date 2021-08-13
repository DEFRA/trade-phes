package uk.gov.defra.plants.formconfiguration.processing;

import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_SIZE;
import static uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificateMetadataMultipleBlocks.BLOCK_APPLICATON;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.APPLICANT_REFERENCE_NUMBER_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.SELECT_CERTIFIER_ORGANISATION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.UPLOAD_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope.APPLICANT;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.helper.CustomPagesBuilder;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.form.Form;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomPageTitleHint;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestionOption;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.service.FormService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@AllArgsConstructor(onConstructor = @__({@Inject}))
@Slf4j
public class CustomQuestionsService {

  private final HealthCertificateService healthCertificateService;
  private final ReferenceDataServiceAdapter referenceDataServiceAdapter;
  private final FormService formService;

  // package level access, allows access into internal methods for unit testing
  Optional<MergedFormQuestion> getCustomQuestionForPage(
      NameAndVersion exa, NameAndVersion ehc, CustomQuestions customQuestion) {
    Optional<MergedFormQuestion> retVal = Optional.empty();

    if (UPLOAD_QUESTION.equals(customQuestion)) {
      retVal = getManualUploadQuestion(exa, ehc);
    } else if (BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.equals(customQuestion)) {
      retVal = getNumberOfCertificatesCustomQuestionForBlocks(exa, ehc);
    } else if (APPLICANT_REFERENCE_NUMBER_QUESTION.equals(customQuestion)) {
      retVal = getApplicationReferenceCustomQuestion(exa);
    } else if (SELECT_CERTIFIER_ORGANISATION.equals(customQuestion)) {
      retVal = getSelectCertifierOrganisationQuestion(ehc);
    }
    return retVal;
  }

  private Optional<MergedFormQuestion> getApplicationReferenceCustomQuestion(NameAndVersion exa) {
    return Optional.of(
        MergedFormQuestion.builder()
            .questionId(APPLICANT_REFERENCE_NUMBER_QUESTION.getQuestionId())
            .text(APPLICANT_REFERENCE_NUMBER_QUESTION.getQuestionText())
            .formName(exa.getName())
            .formQuestionId(APPLICANT_REFERENCE_NUMBER_QUESTION.getFormQuestionId())
            .questionOrder(1)
            .questionScope(APPLICANT)
            .pageNumber(APPLICANT_REFERENCE_NUMBER_QUESTION.getPageNumber())
            .isCustomQuestion(true)
            .constraint(
                AnswerConstraint.builder()
                    .type(MAX_SIZE)
                    .rule("20")
                    .message(APPLICANT_REFERENCE_NUMBER_QUESTION.getValidationMessage())
                    .build())
            .type(Type.EXA)
            .hint(APPLICANT_REFERENCE_NUMBER_QUESTION.getHint())
            .build());
  }

  private Optional<MergedFormQuestion> getNumberOfCertificatesCustomQuestionForBlocks(
      NameAndVersion exa, NameAndVersion ehc) {

    HealthCertificate healthCertificate = loadHealthCertificate(ehc.getName());

    if (healthCertificate != null
        && healthCertificate.getHealthCertificateMetadata() != null
        && healthCertificate
        .getHealthCertificateMetadata()
        .getMultipleBlocks()
        .equals(BLOCK_APPLICATON)) {

      int maxNumberOfCerts = healthCertificate.getHealthCertificateMetadata().getMaxEhc();

      String validationMessage = "Enter a number between 1 and " + maxNumberOfCerts;

      return Optional.of(
          MergedFormQuestion.builder()
              .questionId(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getQuestionId())
              .text(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getQuestionText())
              .formName(exa.getName())
              .formQuestionId(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormQuestionId())
              .questionOrder(1)
              .questionScope(APPLICANT)
              .isCustomQuestion(true)
              .pageNumber(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getPageNumber())
              .questionType(QuestionType.TEXT)
              .type(Type.EXA)
              .hint(
                  String.format(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getHint(), maxNumberOfCerts))
              .constraint(
                  AnswerConstraint.builder()
                      .type(AnswerConstraintType.REQUIRED)
                      .rule(Boolean.TRUE.toString())
                      .message(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getValidationMessage())
                      .build())
              .constraint(
                  AnswerConstraint.builder()
                      .type(AnswerConstraintType.WHOLE_NUMBER)
                      .message(validationMessage)
                      .build())
              .constraint(
                  AnswerConstraint.builder()
                      .type(AnswerConstraintType.MIN_VALUE)
                      .rule("1")
                      .message(validationMessage)
                      .build())
              .constraint(
                  AnswerConstraint.builder()
                      .type(AnswerConstraintType.MAX_VALUE)
                      .rule("" + maxNumberOfCerts)
                      .message(validationMessage)
                      .build())
              .build());
    } else {
      return Optional.empty();
    }
  }

  private Optional<MergedFormQuestion> getManualUploadQuestion(
      NameAndVersion exa, NameAndVersion ehc) {
    if (ehc.isOffline()) {
      return Optional.of(
          MergedFormQuestion.builder()
              .questionId(UPLOAD_QUESTION.getQuestionId())
              .text(UPLOAD_QUESTION.getQuestionText())
              .formQuestionId(UPLOAD_QUESTION.getFormQuestionId())
              .formName(exa.getName())
              .questionOrder(1)
              .isCustomQuestion(true)
              .templateField(
                  FormFieldDescriptor.builder().name("UPLOAD").type(FormFieldType.TEXT).build())
              .questionScope(APPLICANT)
              .hint(UPLOAD_QUESTION.getHint())
              .pageNumber(UPLOAD_QUESTION.getPageNumber())
              .questionType(QuestionType.UPLOAD)
              .type(Type.EHC)
              .constraint(
                  AnswerConstraint.builder()
                      .type(AnswerConstraintType.REQUIRED)
                      .rule(Boolean.TRUE.toString())
                      .message(
                          UPLOAD_QUESTION.getValidationMessage())
                      .build())
              .build());
    } else {
      return Optional.empty();
    }
  }

  private Optional<MergedFormQuestion> getSelectCertifierOrganisationQuestion(NameAndVersion ehc) {
    return Optional.of(
        MergedFormQuestion.builder()
            .questionId(SELECT_CERTIFIER_ORGANISATION.getQuestionId())
            .text(SELECT_CERTIFIER_ORGANISATION.getQuestionText())
            .formQuestionId(SELECT_CERTIFIER_ORGANISATION.getFormQuestionId())
            .formName(ehc.getName())
            .questionOrder(1)
            .questionScope(APPLICANT)
            .hint(SELECT_CERTIFIER_ORGANISATION.getHint())
            .pageNumber(SELECT_CERTIFIER_ORGANISATION.getPageNumber())
            .questionType(QuestionType.SELECT_CERTIFIER_ORGANISATION)
            .isCustomQuestion(true)
            .type(Type.EHC)
            .templateField(
                FormFieldDescriptor.builder()
                    .name("Certifier Organisation")
                    .type(FormFieldType.TEXT)
                    .build())
            .constraint(
                AnswerConstraint.builder()
                    .type(AnswerConstraintType.REQUIRED)
                    .rule(Boolean.TRUE.toString())
                    .message(
                        SELECT_CERTIFIER_ORGANISATION.getValidationMessage())
                    .build())
            .build());
  }

  public List<MergedFormPage> getAllCustomPages(NameAndVersion exa, NameAndVersion ehc) {
    CustomPagesBuilder customPagesBuilder = new CustomPagesBuilder(ehc,
        healthCertificateService);
    if (exa != null
        && StringUtils.isNotEmpty(exa.getName())
        && StringUtils.isNotEmpty(exa.getVersion())) {
      getNumberOfCertificatesCustomQuestionForBlocks(exa, ehc)
          .ifPresent(
              mergedFormQuestion ->
                  customPagesBuilder.addCustomPage(
                      mergedFormQuestion,
                      CustomPageTitleHint.BLOCKS_NUMBER_OF_CERTIFICATES_PAGE,
                      BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormPageId()));
      getManualUploadQuestion(exa, ehc)
          .ifPresent(
              mergedFormQuestion ->
                  customPagesBuilder.addCustomPage(
                      mergedFormQuestion,
                      CustomPageTitleHint.UPLOAD_QUESTION,
                      UPLOAD_QUESTION.getFormPageId()));
    }

    return customPagesBuilder.build();
  }

  private HealthCertificate loadHealthCertificate(final String ehcNumber) {
    return healthCertificateService
        .getByEhcNumber(ehcNumber)
        .orElseThrow(
            () ->
                new NotFoundException("Could not find health certificate with name=" + ehcNumber));
  }
}

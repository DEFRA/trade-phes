package uk.gov.defra.plants.formconfiguration.processing;

import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.constants.PageType;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormPage.MergedFormPageType;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;

@AllArgsConstructor(onConstructor = @__({@Inject}))
@Slf4j
public class CustomPagesService {

  private HealthCertificateService healthCertificateService;

  public MergedFormPage getCertificateReferenceNumberPage(String ehcName,
      Integer mergedFormPageNumber) {
    MergedFormQuestion certificateReferenceNumberQuestion = getCertificateReferenceNumberQuestion(
        ehcName, mergedFormPageNumber);

    String ehcTitle = healthCertificateService.getByEhcNumber(ehcName).orElseThrow().getEhcTitle();

    return MergedFormPage.builder()
        .title("Add a certificate to this application")
        .subtitle(ehcTitle)
        .hint(
            "Enter a reference to help you manage the certificates in this application, such as the animal name.")
        .pageNumber(mergedFormPageNumber)
        .question(certificateReferenceNumberQuestion)
        .pageOccurrences(1)
        .pageType(PageType.SINGULAR)
        .type(Type.EHC)
        .formPageId(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getFormPageId())
        .mergedFormPageType(MergedFormPageType.CERTIFICATE_LEVEL)
        .build();
  }

  private MergedFormQuestion getCertificateReferenceNumberQuestion(String ehcName,
      Integer mergedFormPageNumber) {

    String certificateReferenceValidationMessage = "Enter a reference for this certificate";

    return MergedFormQuestion.builder()
        .questionId(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getQuestionId())
        .text(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getQuestionText())
        .formName(ehcName)
        .formQuestionId(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getFormQuestionId())
        .questionOrder(1)
        .isCustomQuestion(true)
        .questionScope(QuestionScope.APPLICANT)
        .pageNumber(mergedFormPageNumber)
        .questionType(QuestionType.TEXT)
        .constraint(
            AnswerConstraint.builder()
                .type(AnswerConstraintType.MAX_SIZE)
                .rule("20")
                .message(
                    CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getValidationMessage())
                .build())
        .constraint(
            AnswerConstraint.builder()
                .type(AnswerConstraintType.REQUIRED)
                .rule(Boolean.TRUE.toString())
                .message(certificateReferenceValidationMessage)
                .build()
        )
        .type(Type.EHC)
        .hint(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getHint())
        .build();
  }
}

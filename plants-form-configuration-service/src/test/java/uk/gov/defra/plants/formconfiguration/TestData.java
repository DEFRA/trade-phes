package uk.gov.defra.plants.formconfiguration;

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

public class TestData {

  static String certificateReferenceValidationMessage = "Enter a reference for this certificate";

  public static final MergedFormQuestion TEST_CERTIFICATE_REFERENCE_QUESTION = MergedFormQuestion
      .builder()
      .questionId(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getQuestionId())
      .text(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getQuestionText())
      .formName("ehcName")
      .formQuestionId(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getFormQuestionId())
      .questionOrder(1)
      .isCustomQuestion(true)
      .questionScope(QuestionScope.APPLICANT)
      .pageNumber(2)
      .questionType(QuestionType.TEXT)
      .constraint(
          AnswerConstraint.builder()
              .type(AnswerConstraintType.MAX_SIZE)
              .rule("20")
              .message(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getValidationMessage())
              .build())
      .constraint(
          AnswerConstraint.builder()
              .type(AnswerConstraintType.REQUIRED)
              .rule(Boolean.TRUE.toString())
              .message(certificateReferenceValidationMessage)
              .build())
      .type(Type.EHC)
      .hint(CustomQuestions.CERTIFICATE_REFERENCE_NUMBER_QUESTION.getHint())
      .build();

  public static final MergedFormPage TEST_CERTIFICATE_REFERENCE_PAGE = MergedFormPage.builder()
      .title("Add a certificate to this application")
      .subtitle("ehcTitle")
      .hint(
          "Enter a reference to help you manage the certificates in this application, such as the animal name.")
      .pageNumber(2)
      .question(TEST_CERTIFICATE_REFERENCE_QUESTION)
      .pageOccurrences(1)
      .pageType(PageType.SINGULAR)
      .type(Type.EHC)
      .formPageId(-4L)
      .mergedFormPageType(MergedFormPageType.CERTIFICATE_LEVEL)
      .build();

}

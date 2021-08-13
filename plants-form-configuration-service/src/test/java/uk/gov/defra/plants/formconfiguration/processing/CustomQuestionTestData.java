package uk.gov.defra.plants.formconfiguration.processing;

import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_SIZE;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.APPLICANT_REFERENCE_NUMBER_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.SELECT_CERTIFIER_ORGANISATION;
import static uk.gov.defra.plants.formconfiguration.representation.mergedform.CustomQuestions.UPLOAD_QUESTION;
import static uk.gov.defra.plants.formconfiguration.representation.question.QuestionScope.APPLICANT;

import uk.gov.defra.plants.certificate.representation.FormFieldDescriptor;
import uk.gov.defra.plants.certificate.representation.FormFieldType;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType;
import uk.gov.defra.plants.formconfiguration.representation.NameAndVersion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.mergedform.MergedFormQuestion.Type;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;

public class CustomQuestionTestData {

  private static final String RANGE_VALUE = "Enter a number between 1 and 1";

  public static NameAndVersion EHC =
      NameAndVersion.builder().name("Test-EHC").version("1.0").build();
  public static NameAndVersion EHC_OFFLINE =
      NameAndVersion.builder().name("Test-EHC").version("OFFLINE").build();
  public static NameAndVersion EXA =
      NameAndVersion.builder().name("Test-EXA").version("1.0").build();
  public static final MergedFormQuestion TEST_UPLOAD_QUESTION =
      MergedFormQuestion.builder()
          .questionId(UPLOAD_QUESTION.getQuestionId())
          .questionScope(APPLICANT)
          .text(UPLOAD_QUESTION.getQuestionText())
          .formName(EXA.getName())
          .formQuestionId(UPLOAD_QUESTION.getFormQuestionId())
          .questionOrder(1)
          .isCustomQuestion(true)
          .hint(UPLOAD_QUESTION.getHint())
          .templateField(
              FormFieldDescriptor.builder().name("UPLOAD").type(FormFieldType.TEXT).build())
          .pageNumber(UPLOAD_QUESTION.getPageNumber())
          .questionType(QuestionType.UPLOAD)
          .type(Type.EHC)
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.REQUIRED)
                  .rule("true")
                  .message(
                      "You must upload an Export Health Certificate (EHC) to continue your application")
                  .build())
          .build();

  public static final MergedFormQuestion TEST_BLOCK_QUESTION =
      MergedFormQuestion.builder()
          .questionId(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getQuestionId())
          .questionScope(APPLICANT)
          .text(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getQuestionText())
          .formName(EXA.getName())
          .formQuestionId(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getFormQuestionId())
          .questionOrder(1)
          .isCustomQuestion(true)
          .pageNumber(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getPageNumber())
          .questionType(QuestionType.TEXT)
          .type(Type.EXA)
          .hint(String.format(BLOCKS_NUMBER_OF_CERTIFICATES_QUESTION.getHint(), 1))
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.REQUIRED)
                  .rule(Boolean.TRUE.toString())
                  .message("Enter number of certificates required")
                  .build())
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.WHOLE_NUMBER)
                  .message(RANGE_VALUE)
                  .build())
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.MIN_VALUE)
                  .rule("1")
                  .message(RANGE_VALUE)
                  .build())
          .constraint(
              AnswerConstraint.builder()
                  .type(AnswerConstraintType.MAX_VALUE)
                  .rule("1")
                  .message(RANGE_VALUE)
                  .build())
          .build();
}

package uk.gov.defra.plants.formconfiguration.validation;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;

@Slf4j
public class FormTextQuestionRequireSizesValidator extends AbstractValidator<FormQuestion>
    implements ConstraintValidator<TextQuestionsRequireSizes, FormQuestion> {

  private final SizeValidator sizeValidator = new SizeValidator();

  @Inject
  private QuestionService questionService;

  @Override
  public boolean isValid(FormQuestion question, ConstraintValidatorContext context) {

    boolean result = true;

    if (question.getQuestionId() != null && isTextAnswer(question)) {
      result = isTextQuestionValid(question);
    }

    if (!result) {
      addViolation(context, "The minimum and maximum size for mandatory fields must be set");
    }

    return result;
  }

  private boolean isTextAnswer(FormQuestion value) {
    Question question =
        questionService
            .getQuestion(value.getQuestionId())
            .orElseThrow(
                () ->
                    new BadRequestException("Cant find question with id:" + value.getQuestionId()));
    return QuestionType.isTextQuestionType(question.getQuestionType());
  }

  private boolean isTextQuestionValid(FormQuestion question) {
    boolean result = true;

    if (!sizeValidator.hasMinSizeBeenSet(question.getConstraints())
        || !sizeValidator.hasMaxSizeBeenSet(question.getConstraints())) {

      LOGGER.error(
          "Question {} failed validation.",
          questionService
              .getQuestion(question.getQuestionId())
              .orElseThrow(
                  () ->
                      new BadRequestException(
                          "Cant find question with id:" + question.getQuestionId())));
      result = false;
    }

    return result;
  }

  @Override
  public void initialize(TextQuestionsRequireSizes constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }
}

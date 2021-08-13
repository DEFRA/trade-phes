package uk.gov.defra.plants.formconfiguration.validation;

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_SIZE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MIN_SIZE;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.REQUIRED;

import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;
import uk.gov.defra.plants.formconfiguration.validation.FormTextQuestionRequireSizesValidator;

@RunWith(MockitoJUnitRunner.class)
public class FormTextQuestionRequireSizesValidatorTest {
  @Mock private QuestionService questionService;
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintViolationBuilder builder;

  @InjectMocks
  public FormTextQuestionRequireSizesValidator validator;

  private Question notTextQuestion =
      Question.builder().questionType(QuestionType.SINGLE_SELECT).build();
  private Question textQuestion = Question.builder().questionType(QuestionType.TEXT).build();

  @Before
  public void setup() {
    doNothing().when(context).disableDefaultConstraintViolation();
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
  }

  @Test
  public void givenNotText_whenIsValidCalled_thenReturnsTrue() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(notTextQuestion));
    FormQuestion fq =
        FormQuestion.builder().questionId(1L).constraints(Collections.emptyList()).build();
    assertThat(validator.isValid(fq, context)).isTrue();
  }

  @Test
  public void givenTextQuestionAndNeitherMinOrMaxSet_whenIsValidCalled_thenReturnsFalse() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(textQuestion));
    FormQuestion fq =
        FormQuestion.builder()
            .questionId(1L)
            .constraints(of(AnswerConstraint.builder().type(REQUIRED).rule("true").build()))
            .build();
    assertThat(validator.isValid(fq, context)).isFalse();
  }

  @Test
  public void givenTextQuestionAndMinNotSet_whenIsValidCalled_thenReturnsFalse() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(textQuestion));
    FormQuestion fq =
        FormQuestion.builder()
            .questionId(1L)
            .constraints(
                of(
                    AnswerConstraint.builder().type(REQUIRED).rule("true").build(),
                    AnswerConstraint.builder().type(MAX_SIZE).rule("1").build()))
            .build();
    assertThat(validator.isValid(fq, context)).isFalse();
  }

  @Test
  public void givenTextQuestionAndMaxNotSet_whenIsValidCalled_thenReturnsFalse() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(textQuestion));
    FormQuestion fq =
        FormQuestion.builder()
            .questionId(1L)
            .constraints(
                of(
                    AnswerConstraint.builder().type(REQUIRED).rule("true").build(),
                    AnswerConstraint.builder().type(MIN_SIZE).rule("1").build()))
            .build();
    assertThat(validator.isValid(fq, context)).isFalse();
  }

  @Test
  public void givenTextQuestionMinAndMaxSet_whenIsValidCalled_thenReturnsTrue() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(textQuestion));
    FormQuestion fq =
        FormQuestion.builder()
            .questionId(1L)
            .constraints(
                of(
                    AnswerConstraint.builder().type(REQUIRED).rule("true").build(),
                    AnswerConstraint.builder().type(MIN_SIZE).rule("1").build(),
                    AnswerConstraint.builder().type(MAX_SIZE).rule("1").build()))
            .build();
    assertThat(validator.isValid(fq, context)).isTrue();
  }
}

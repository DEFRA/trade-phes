package uk.gov.defra.plants.formconfiguration.validation;

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.defra.plants.formconfiguration.representation.AnswerConstraintType.MAX_CARRIAGE_RETURN;
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
import uk.gov.defra.plants.formconfiguration.representation.AnswerConstraint;
import uk.gov.defra.plants.formconfiguration.representation.form.FormQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;
import uk.gov.defra.plants.formconfiguration.validation.TextQuestionsRequireMaxCarriageReturnsValidator;

@RunWith(MockitoJUnitRunner.class)
public class TextQuestionsRequireMaxCarriageReturnsValidatorTest {
  @Mock private QuestionService questionService;
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintViolationBuilder builder;

  @InjectMocks
  public TextQuestionsRequireMaxCarriageReturnsValidator validator;

  private Question notTextQuestion =
      Question.builder().questionType(QuestionType.SINGLE_SELECT).build();
  private Question textAreaQuestion = Question.builder().questionType(QuestionType.TEXTAREA).build();

  @Before
  public void setup() {
    doNothing().when(context).disableDefaultConstraintViolation();
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
  }

  @Test
  public void givenNotTextWhenIsValidCalledThenReturnsTrue() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(notTextQuestion));
    FormQuestion fq =
        FormQuestion.builder().questionId(1L).constraints(Collections.emptyList()).build();
    assertThat(validator.isValid(fq, context)).isTrue();
  }

  @Test
  public void givenTextAreaQuestionAndMaxCarriageNotSetWhenIsValidCalled_thenReturnsFalse() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(textAreaQuestion));
    FormQuestion fq =
        FormQuestion.builder()
            .questionId(1L)
            .constraints(of(AnswerConstraint.builder().type(REQUIRED).rule("true").build()))
            .build();
    assertThat(validator.isValid(fq, context)).isFalse();
  }

  @Test
  public void givenTextAreaQuestionAndMaxCarriageSetWhenIsValidCalled_thenReturnsTrue() {
    when(questionService.getQuestion(anyLong())).thenReturn(Optional.of(textAreaQuestion));
    FormQuestion fq =
        FormQuestion.builder()
            .questionId(1L)
            .constraints(
                of(
                    AnswerConstraint.builder().type(REQUIRED).rule("true").build(),
                    AnswerConstraint.builder().type(MAX_CARRIAGE_RETURN).rule("1").build()))
            .build();
    assertThat(validator.isValid(fq, context)).isTrue();
  }
}

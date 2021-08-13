package uk.gov.defra.plants.formconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOrder;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionFormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionType;
import uk.gov.defra.plants.formconfiguration.dao.QuestionDAO;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOption;
import uk.gov.defra.plants.formconfiguration.service.QuestionService;

@RunWith(MockitoJUnitRunner.class)
public class QuestionServiceTest {
  private static final PersistentQuestion PERSISTENT_EXA_QUESTION =
      PersistentQuestion.builder()
          .formType(QuestionFormType.EXA)
          .formTypeText(QuestionFormType.EXA.getFriendlyName())
          .text("What is the Export date?")
          .questionType(QuestionType.DATE)
          .questionTypeText("Date")
          .data(PersistentQuestionData.builder().hint("Enter date in format 01 01 2001").build())
          .build();

  private static final PersistentQuestion PERSISTENT_EHC_QUESTION =
      PersistentQuestion.builder()
          .formType(QuestionFormType.EHC)
          .formTypeText(QuestionFormType.EHC.getFriendlyName())
          .text("What is your Gender?")
          .questionType(QuestionType.SINGLE_SELECT)
          .questionTypeText("Single Select")
          .data(
              PersistentQuestionData.builder()
                  .hint("Enter either Male or Female")
                  .option(QuestionOption.builder().text("Male").order(1).build())
                  .option(QuestionOption.builder().text("Female").order(2).build())
                  .build())
          .build();

  private static final PersistentQuestion PERSISTENT_EHC_QUESTION_MULTI =
      PersistentQuestion.builder()
          .formType(QuestionFormType.EHC)
          .formTypeText(QuestionFormType.EHC.getFriendlyName())
          .text("What is your transit countries?")
          .questionType(QuestionType.MULTI_SELECT)
          .questionTypeText("Multi Select")
          .data(
              PersistentQuestionData.builder()
                  .hint("Choose transit countries")
                  .option(QuestionOption.builder().text("Spain").order(1).build())
                  .option(QuestionOption.builder().text("France").order(2).build())
                  .build())
          .build();

  private static final PersistentQuestion PERSISTENT_IN_USE_EHC_QUESTION =
      PersistentQuestion.builder()
          .formType(QuestionFormType.EHC)
          .formTypeText(QuestionFormType.EHC.getFriendlyName())
          .text("What is your Gender?")
          .questionType(QuestionType.SINGLE_SELECT)
          .questionTypeText("Single Select")
          .data(
              PersistentQuestionData.builder()
                  .hint("Enter either Male or Female")
                  .option(QuestionOption.builder().text("Male").order(1).build())
                  .option(QuestionOption.builder().text("Female").order(2).build())
                  .build())
          .formId(1L)
          .build();

  private static final List<PersistentQuestion> PERSISTENT_QUESTIONS =
      Arrays.asList(PERSISTENT_EXA_QUESTION, PERSISTENT_EHC_QUESTION, PERSISTENT_EHC_QUESTION_MULTI);

  private static final Question EXA_QUESTION =
      Question.builder()
          .formType(QuestionFormType.EXA)
          .formTypeText(QuestionFormType.EXA.getFriendlyName())
          .text("What is the Export date?")
          .questionType(QuestionType.DATE)
          .questionTypeText("Date")
          .hint("Enter date in format 01 01 2001")
          .build();

  private static final Question EHC_QUESTION =
      Question.builder()
          .formType(QuestionFormType.EHC)
          .formTypeText(QuestionFormType.EHC.getFriendlyName())
          .text("What is your Gender?")
          .questionType(QuestionType.SINGLE_SELECT)
          .questionTypeText("Single Select")
          .hint("Enter either Male or Female")
          .option(QuestionOption.builder().text("Male").order(1).build())
          .option(QuestionOption.builder().text("Female").order(2).build())
          .build();

  private static final Question EHC_QUESTION_MULTI =
      Question.builder()
          .formType(QuestionFormType.EHC)
          .formTypeText(QuestionFormType.EHC.getFriendlyName())
          .text("What is your transit countries?")
          .questionType(QuestionType.MULTI_SELECT)
          .questionTypeText("Multi Select")
          .hint("Choose transit countries")
          .option(QuestionOption.builder().text("Spain").order(1).build())
          .option(QuestionOption.builder().text("France").order(2).build())
          .build();

  private static final Question IN_USE_EHC_QUESTION =
      Question.builder()
          .formType(QuestionFormType.EHC)
          .formTypeText(QuestionFormType.EHC.getFriendlyName())
          .text("What is your Gender?")
          .questionType(QuestionType.SINGLE_SELECT)
          .questionTypeText("Single Select")
          .hint("Enter either Male or Female")
          .option(QuestionOption.builder().text("Male").order(1).build())
          .option(QuestionOption.builder().text("Female").order(2).build())
          .formId(1L)
          .build();

  @Mock QuestionDAO questionDAO;

  @InjectMocks private QuestionService questionService;

  @Test
  public void shouldRemoveById() {
    // ARRANGE
    when(questionDAO.deleteByQuestionId(1L)).thenReturn(1);
    // ACT
    questionService.deleteByQuestionId(1L);
    // ASSERT
    verify(questionDAO).deleteByQuestionId(1L);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowExceptionWhenZeroRowsAffected() {
    when(questionDAO.deleteByQuestionId(any(Long.class))).thenReturn(0);
    questionService.deleteByQuestionId(1L);
    verify(questionDAO).deleteByQuestionId(1L);
  }

  @Test
  public void shouldInsertQuestion() {
    // ARRANGE
    when(questionDAO.insert(any(PersistentQuestion.class))).thenReturn(1L);
    // ACT
    Long id = questionService.insert(EXA_QUESTION);
    // ASSERT
    assertThat(id).isEqualTo(1L);
  }

  @Test
  public void shouldUpdateQuestion() {
    // ARRANGE
    when(questionDAO.update(any(PersistentQuestion.class))).thenReturn(1);
    // ACT
    questionService.update(EXA_QUESTION);
    // ASSERT
    verify(questionDAO).update(any(PersistentQuestion.class));
  }

  @Test
  public void shouldGetQuestion() {
    Mockito.when(questionDAO.getQuestion(1L)).thenReturn(PERSISTENT_EXA_QUESTION);

    // ACT
    Optional<Question> question = questionService.getQuestion(1L);

    // ASSERT
    assertThat(question).isPresent().contains(EXA_QUESTION);
    verify(questionDAO).getQuestion(1L);
  }

  @Test
  public void shouldGetQuestionInUse() {
    Mockito.when(questionDAO.getQuestion(1L)).thenReturn(PERSISTENT_IN_USE_EHC_QUESTION);

    // ACT
    Optional<Question> question = questionService.getQuestion(1L);

    // ASSERT
    assertThat(question).isPresent().contains(IN_USE_EHC_QUESTION);
    verify(questionDAO).getQuestion(1L);
  }

  @Test
  public void shouldGetAllQuestions() {
    Mockito.when(questionDAO.getQuestions(null, null, null, null, null))
        .thenReturn(PERSISTENT_QUESTIONS);

    // ACT
    List<Question> questions = questionService.getQuestions(null, null, null, null, null);

    // ASSERT
    assertThat(questions).hasSize(3).containsExactlyInAnyOrder(EXA_QUESTION, EHC_QUESTION, EHC_QUESTION_MULTI);
    verify(questionDAO).getQuestions(null, null, null, null, null);
  }

  @Test
  public void shouldGetAllQuestions_whenParametersProvided() {
    Mockito.when(questionDAO.getQuestions(QuestionOrder.ID, "ASC", "horse", 0, 10))
        .thenReturn(PERSISTENT_QUESTIONS);

    // ACT
    List<Question> questions =
        questionService.getQuestions(QuestionOrder.ID, "ASC", "horse", 0, 10);

    // ASSERT
    assertThat(questions).hasSize(3).containsExactlyInAnyOrder(EXA_QUESTION, EHC_QUESTION, EHC_QUESTION_MULTI);
    verify(questionDAO).getQuestions(QuestionOrder.ID, "ASC", "horse", 0, 10);
  }

  @Test
  public void shouldGetAllQuestionsForEXA() {
    Mockito.when(
            questionDAO.getQuestionsForFormType(
                QuestionFormType.EXA, null, null, null, null, null))
        .thenReturn(Collections.singletonList(PERSISTENT_EXA_QUESTION));

    // ACT
    List<Question> questions =
        questionService.getQuestionsForFormType(
            QuestionFormType.EXA, null, null, null, null, null);

    // ASSERT
    assertThat(questions).hasSize(1).containsOnly(EXA_QUESTION);
    verify(questionDAO)
        .getQuestionsForFormType(QuestionFormType.EXA, null, null, null, null, null);
  }

  @Test
  public void shouldGetAllQuestionsForEHC() {
    Mockito.when(
            questionDAO.getQuestionsForFormType(
                QuestionFormType.EHC, null, null, null, null, null))
        .thenReturn(Collections.singletonList(PERSISTENT_EHC_QUESTION));

    // ACT
    List<Question> questions =
        questionService.getQuestionsForFormType(
            QuestionFormType.EHC, null, null, null, null, null);

    // ASSERT
    assertThat(questions).hasSize(1).containsOnly(EHC_QUESTION);
    verify(questionDAO)
        .getQuestionsForFormType(QuestionFormType.EHC, null, null, null, null, null);
  }

  @Test
  public void shouldGetCountWithFilter() {
    // ARRANGE
    Mockito.when(questionDAO.count("horse")).thenReturn(10);

    // ACT
    Integer count = questionService.count("horse");

    // ASSERT
    assertThat(count).isEqualTo(10);
    verify(questionDAO).count("horse");
  }

  @Test
  public void shouldGetCountWithNoFilter() {
    // ARRANGE
    Mockito.when(questionDAO.count(null)).thenReturn(10);

    // ACT
    Integer count = questionService.count(null);

    // ASSERT
    assertThat(count).isEqualTo(10);
    verify(questionDAO).count(null);
  }
}

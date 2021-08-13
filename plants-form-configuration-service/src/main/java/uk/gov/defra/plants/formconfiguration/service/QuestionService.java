package uk.gov.defra.plants.formconfiguration.service;

import static java.lang.String.format;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import uk.gov.defra.plants.common.jdbi.DbHelper;
import uk.gov.defra.plants.formconfiguration.dao.QuestionDAO;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestion;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestionData;
import uk.gov.defra.plants.formconfiguration.representation.question.Question;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionFormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOrder;

@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class QuestionService {
  private final QuestionDAO questionDAO;

  public Long insert(@NonNull final Question question) {
    final PersistentQuestion persistentQuestion = asPersistentQuestion(question);

    return DbHelper.doSqlInsert(
        () -> questionDAO.insert(persistentQuestion), () -> "insert new question");
  }

  public void update(@NonNull final Question question) {
    final PersistentQuestion persistentQuestion = asPersistentQuestion(question);

    DbHelper.doSqlUpdate(
        () -> questionDAO.update(persistentQuestion),
        () -> "update question with id=" + question.getId(),
        DbHelper.NOT_ONE_ROW_THROWS_BAD_REQUEST_EXCEPTION);
  }

  public void deleteByQuestionId(@NonNull final Long id) {
    DbHelper.doSqlUpdate(
        () -> questionDAO.deleteByQuestionId(id),
        () -> "delete question with id=" + id,
        DbHelper.ZERO_ROWS_THROWS_NOT_FOUND_EXCEPTION);
  }

  public List<Question> getQuestions(
      final QuestionOrder sort,
      final String direction,
      final String filter,
      final Integer offset,
      final Integer limit) {

    return DbHelper.doSqlQuery(
            () -> questionDAO.getQuestions(sort, direction, filter, offset, limit),
            () -> "questions")
        .stream()
        .map(this::asQuestion)
        .collect(Collectors.toList());
  }

  public List<Question> getQuestionsForFormType(
      final QuestionFormType formType,
      final QuestionOrder sort,
      final String direction,
      final String filter,
      final Integer offset,
      final Integer limit) {

    return DbHelper.doSqlQuery(
            () ->
                questionDAO.getQuestionsForFormType(
                    formType, sort, direction, filter, offset, limit),
            () -> "questionsByFormType")
        .stream()
        .map(this::asQuestion)
        .collect(Collectors.toList());
  }

  public Optional<Question> getQuestion(@NonNull final Long questionId) {
    return DbHelper.doSqlQuery(
            () -> Optional.ofNullable(questionDAO.getQuestion(questionId)),
            () -> "question by id=" + questionId)
        .map(this::asQuestion);
  }

  public List<Question> getQuestions(
      @NonNull final String name, @NonNull final String version, Handle h) {
    return DbHelper.doSqlQuery(
            () -> h.attach(QuestionDAO.class).getByForm(name, version),
            () -> format("get all questions for form name=%s version=%s", name, version))
        .stream()
        .map(this::asQuestion)
        .collect(Collectors.toList());
  }

  public Integer count(final String filter) {
    return DbHelper.doSqlQuery(() -> questionDAO.count(filter), () -> "question by id=" + filter);
  }

  private PersistentQuestion asPersistentQuestion(Question question) {
    return PersistentQuestion.builder()
        .id(question.getId())
        .formType(question.getFormType())
        .text(question.getText())
        .questionType(question.getQuestionType())
        .questionTypeText(question.getQuestionTypeText())
        .formTypeText(question.getFormTypeText())
        .data(
            PersistentQuestionData.builder()
                .hint(question.getHint())
                .dataMapping(question.getDataMapping())
                .options(question.getOptions())
                .build())
        .build();
  }

  private Question asQuestion(final PersistentQuestion persistentQuestion) {
    return Question.builder()
        .id(persistentQuestion.getId())
        .text(persistentQuestion.getText())
        .hint(persistentQuestion.getData().getHint())
        .formType(persistentQuestion.getFormType())
        .dataMapping(persistentQuestion.getData().getDataMapping())
        .questionType(persistentQuestion.getQuestionType())
        .questionTypeText(persistentQuestion.getQuestionTypeText())
        .formTypeText(persistentQuestion.getFormTypeText())
        .options(persistentQuestion.getData().getOptions())
        .formId(persistentQuestion.getFormId())
        .build();
  }
}

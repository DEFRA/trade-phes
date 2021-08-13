package uk.gov.defra.plants.formconfiguration.dao;

import java.util.List;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.formconfiguration.dao.mapper.PersistentQuestionDataColumnMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentQuestion;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionFormType;
import uk.gov.defra.plants.formconfiguration.representation.question.QuestionOrder;

@RegisterConstructorMapper(PersistentQuestion.class)
@RegisterColumnMapper(PersistentQuestionDataColumnMapper.class)
public interface QuestionDAO {
  @SqlUpdate(
      "INSERT INTO question (formType,data,text,questionType) VALUES (:formType,:data,:text,:questionType)")
  @GetGeneratedKeys
  Long insert(@BindBean PersistentQuestion persistentQuestion);

  @SqlUpdate("DELETE FROM question WHERE id = :id")
  Integer deleteByQuestionId(@Bind("id") Long id);

  @SqlUpdate(
      "UPDATE question set data = :data, text = :text, questionType = :questionType, formType = :formType WHERE id = :id")
  Integer update(@BindBean PersistentQuestion persistentQuestion);

  /**
   * SQL query adds 1 to offset, as SQL ROW_NUM starts from 1 but pagination would be expected to
   * start from 0
   *
   * @param order which column to order by
   * @param direction ASC or DESC
   * @param filter search ID, questionType and text columns for values LIKE %filter%
   * @param offset row to start return set from
   * @param limit number of rows to return
   * @return
   */
  @SqlQuery(
      "SELECT id, formType, formTypeText, data, text, questionType, questionTypeText FROM  ( SELECT ROW_NUMBER() OVER ( ORDER BY "
          + "CASE WHEN :order = 'text' AND :direction != 'ASC' THEN q.text END DESC,"
          + "CASE WHEN :order = 'text' AND :direction = 'ASC' THEN q.text END ASC,"
          + "CASE WHEN :order = 'id' AND :direction != 'ASC' THEN CAST(id AS INT) END DESC,"
          + "CASE WHEN :order = 'id' AND :direction = 'ASC' THEN CAST (id AS INT) END ASC,"
          + "CASE WHEN :order = 'form_type' AND :direction != 'ASC' THEN ft.text END DESC,"
          + "CASE WHEN :order = 'form_type' AND :direction = 'ASC' THEN ft.text END ASC,"
          + "CASE WHEN :order = 'question_type' AND :direction != 'ASC' THEN qt.text END DESC,"
          + "CASE WHEN :order = 'question_type' AND :direction = 'ASC' THEN qt.text END ASC"
          + ") as RowNum, q.id, q.formType, ft.text as formTypeText, q.data, q.text, q.questionType, qt.text as questionTypeText FROM question q "
          + "LEFT JOIN formType ft ON q.formType = ft.code "
          + "LEFT JOIN questionType qt ON q.questionType = qt.code "
          + "WHERE (:filter IS NULL OR q.id LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(qt.text) LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(q.text) LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(ft.text) LIKE '%'+:filter+'%')) AS RowConstrainedResult"
          + " WHERE RowNum >="
          + "CASE WHEN :offset IS NULL THEN 1 ELSE (:offset + 1) END AND RowNum < "
          + "CASE WHEN :limit IS NULL THEN 10000 ELSE (:limit + ISNULL(:offset, 0) + 1) END ORDER BY RowNum")
  List<PersistentQuestion> getQuestions(
      @Bind("order") final QuestionOrder order,
      @Bind("direction") final String direction,
      @Bind("filter") final String filter,
      @Bind("offset") final Integer offset,
      @Bind("limit") final Integer limit);

  /**
   * SQL query adds 1 to offset, as SQL ROW_NUM starts from 1 but pagination would be expected to
   * start from 0
   *
   * @param formType search where form type column = formType
   * @param order which column to order by
   * @param direction ASC or DESC
   * @param filter search ID, questionType and text columns for values LIKE %filter%
   * @param offset row to start return set from
   * @param limit number of rows to return
   * @return
   */
  @SqlQuery(
      "SELECT id, formType, formTypeText, data, text, questionType, questionTypeText FROM  ( SELECT ROW_NUMBER() OVER ( ORDER BY "
          + "CASE WHEN :order = 'question_type' AND :direction = 'ASC' THEN qt.text END ASC,"
          + "CASE WHEN :order = 'question_type' AND :direction != 'ASC' THEN qt.text END DESC,"
          + "CASE WHEN :order = 'text' AND :direction != 'ASC' THEN q.text END DESC,"
          + "CASE WHEN :order = 'text' AND :direction = 'ASC' THEN q.text END ASC,"
          + "CASE WHEN :order = 'id' AND :direction != 'ASC' THEN CAST (id AS INT) END DESC,"
          + "CASE WHEN :order = 'id' AND :direction = 'ASC' THEN CAST (id AS INT) END ASC, q.formType"
          + ") as RowNum, q.id, q.formType, ft.text as formTypeText, q.data, q.text, q.questionType, qt.text as questionTypeText FROM question q "
          + "LEFT JOIN formType ft ON q.formType = ft.code "
          + "LEFT JOIN questionType qt ON q.questionType = qt.code "
          + "WHERE q.formType in (:formType, 'BOTH') AND ((:filter IS NULL OR q.id LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(qt.text) LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(ft.text) LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(q.text) LIKE '%'+:filter+'%'))) AS RowConstrainedResult"
          + " WHERE RowNum >="
          + "CASE WHEN :offset IS NULL THEN 1 ELSE (:offset + 1) END AND RowNum < "
          + "CASE WHEN :limit IS NULL THEN 10000 ELSE (:limit + ISNULL(:offset, 0) + 1) END ORDER BY RowNum")
  List<PersistentQuestion> getQuestionsForFormType(
      @Bind("formType") QuestionFormType formType,
      @Bind("order") final QuestionOrder order,
      @Bind("direction") final String direction,
      @Bind("filter") final String filter,
      @Bind("offset") final Integer offset,
      @Bind("limit") final Integer limit);

  @SqlQuery(
      "SELECT"
          + " q.id,"
          + " q.formType,"
          + " ft.text as formTypeText,"
          + " q.data,"
          + " q.text,"
          + " q.questionType,"
          + " qt.text as questionTypeText,"
          + " fp.formId"
          + " FROM"
          + " question q"
          + " LEFT JOIN"
          + " formQuestion fq"
          + " ON q.id=fq.questionId"
          + " LEFT JOIN "
          + " formPage fp"
          + " ON fq.formPageId=fp.id"
          + " LEFT JOIN"
          + " questionType qt"
          + " ON q.questionType = qt.code"
          + " LEFT JOIN"
          + " formType ft"
          + " ON q.formType = ft.code"
          + " WHERE q.id = :id")
  @SingleValue
  PersistentQuestion getQuestion(@Bind("id") Long id);

  @SqlQuery(
      "SELECT"
          + " q.id,"
          + " q.formType,"
          + " ft.text as formTypeText,"
          + " q.data,"
          + " q.text,"
          + " q.questionType,"
          + " qt.text as questionTypeText"
          + " FROM"
          + " form f"
          + " INNER JOIN"
          + " formPage fp"
          + " on f.id = fp.formId"
          + " INNER JOIN "
          + " formQuestion fq"
          + " ON fp.id=fq.formPageId"
          + " INNER JOIN"
          + " question q"
          + " ON q.id=fq.questionId"
          + " LEFT JOIN"
          + " questionType qt"
          + " ON q.questionType = qt.code"
          + " LEFT JOIN"
          + " formType ft"
          + " ON q.formType = ft.code"
          + " WHERE f.name=:name AND f.version=:version"
          + " ORDER BY fp.pageOrder ASC, questionOrder ASC")
  List<PersistentQuestion> getByForm(
      @Bind("name") final String name, @Bind("version") final String version);

  @SqlQuery(
      "SELECT COUNT(*) FROM question "
          + "WHERE (:filter IS NULL OR id LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(questionType) LIKE '%'+:filter+'%')"
          + "OR (:filter IS NULL OR lower(text) LIKE '%'+:filter+'%')")
  int count(@Bind("filter") final String filter);
}

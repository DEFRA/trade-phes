package uk.gov.defra.plants.formconfiguration.dao;

import java.util.List;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.formconfiguration.dao.mapper.PersistentFormQuestionDataColumnMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormQuestion;

@RegisterConstructorMapper(PersistentFormQuestion.class)
@RegisterColumnMapper(PersistentFormQuestionDataColumnMapper.class)
public interface FormQuestionDAO {

  @SqlQuery("SELECT" + " *" + " FROM" + " formQuestion" + " WHERE id=:id")
  @SingleValue
  PersistentFormQuestion getById(@Bind("id") final Long id);

  @SqlQuery(
      "SELECT"
          + " *"
          + " FROM"
          + " formQuestion"
          + " WHERE formPageId=:formPageId"
          + " ORDER by questionOrder ASC")
  List<PersistentFormQuestion> getByFormPageId(@Bind("formPageId") final Long formPageId);

  @SqlQuery(
      "SELECT"
          + " fq.*"
          + " FROM"
          + " form f"
          + " INNER JOIN"
          + " formPage fp "
          + " ON fp.formId = f.id"
          + " INNER JOIN "
          + " formQuestion fq"
          + " ON fp.id=fq.formPageId"
          + " WHERE f.name=:name AND f.version=:version"
          + " ORDER BY fp.pageOrder ASC, questionOrder ASC")
  List<PersistentFormQuestion> get(
      @Bind("name") final String name, @Bind("version") final String version);

  @SqlQuery(
      "SELECT"
          + " COUNT(fq.id)"
          + " FROM"
          + " form f"
          + " INNER JOIN"
          + " formPage fp "
          + " ON fp.formId = f.id"
          + " INNER JOIN "
          + " formQuestion fq"
          + " ON fp.id=fq.formPageId"
          + " WHERE f.name=:name AND f.version=:version")
  Integer getQuestionCount(@Bind("name") final String name, @Bind("version") final String version);

  @SqlBatch(
      "INSERT INTO"
          + " formQuestion ("
          + " questionId,"
          + " questionOrder,"
          + " questionScope,"
          + " questionEditable,"
          + " data,"
          + " formPageId"
          + " ) VALUES ("
          + " :questionId,"
          + " :questionOrder,"
          + " :questionScope,"
          + " :questionEditable,"
          + " :data,"
          + " :formPageId"
          + " )")
  int[] insert(@BindBean final List<PersistentFormQuestion> persistentFormQuestions);

  @SqlBatch("DELETE FROM" + " formQuestion" + " WHERE " + " id=:id")
  int[] remove(@Bind("id") final List<Long> ids);

  @SqlUpdate(
      "DELETE"
          + " fq"
          + " FROM"
          + " form f"
          + " INNER JOIN"
          + " formPage fp "
          + " ON fp.formId = f.id"
          + " INNER JOIN "
          + " formQuestion fq"
          + " ON fp.id=fq.formPageId"
          + " WHERE"
          + " f.name=:name"
          + " AND f.version=:version")
  Integer deleteVersion(@Bind("name") final String name, @Bind("version") final String version);

  @SqlUpdate(
      "DELETE"
          + " fq"
          + " FROM"
          + " form f"
          + " INNER JOIN"
          + " formPage fp "
          + " ON fp.formId = f.id"
          + " INNER JOIN "
          + " formQuestion fq"
          + " ON fp.id=fq.formPageId"
          + " WHERE"
          + " f.name=:name")
  Integer delete(@Bind("name") final String name);

  @SqlBatch(
      "UPDATE"
          + " formQuestion"
          + " SET"
          + " questionId = :questionId,"
          + " questionScope = :questionScope, "
          + " questionEditable = :questionEditable, "
          + " formPageId = :formPageId, "
          + " data = :data, "
          + " lastUpdated = GETUTCDATE() "
          + " WHERE"
          + " id = :id")
  int[] update(@BindBean final List<PersistentFormQuestion> formQuestions);

  @SqlUpdate("DELETE " + "FROM formQuestion " + "WHERE formPageId=:formPageId")
  Integer deleteForPage(@Bind("formPageId") final Long formPageId);

  @SqlBatch(
      "UPDATE " + " formQuestion" + " SET" + " questionOrder=:questionOrder" + " WHERE id=:id")
  int[] updateQuestionOrder(@BindBean List<PersistentFormQuestion> persistentFormQuestions);
}

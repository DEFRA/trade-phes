package uk.gov.defra.plants.formconfiguration.dao;

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormPage;

@RegisterConstructorMapper(PersistentFormPage.class)
public interface FormPageDAO {

  @SqlQuery(
      "SELECT formPage.* "
          + "FROM formPage "
          + "INNER JOIN form ON form.id = formPage.formId "
          + "WHERE form.name=:name "
          + "AND form.version=:version "
          + "ORDER BY pageOrder asc ")
  List<PersistentFormPage> getFormPages(
      @Bind("name") final String name, @Bind("version") final String version);

  @SqlQuery("SELECT * FROM formPage where formId =:formId ORDER BY pageOrder asc ")
  List<PersistentFormPage> getFormPagesByFormId(@Bind("formId") final Long formId);

  @SqlUpdate(
      "INSERT INTO formPage"
          + " (formId,title,subtitle,hint,pageOrder,repeatForEachCertificateInApplication,lastUpdated,created)"
          + " VALUES "
          + "(:formId,:title,:subtitle,:hint,:pageOrder,:repeatForEachCertificateInApplication,GETUTCDATE(),GETUTCDATE())")
  @GetGeneratedKeys
  Long insert(@BindBean final PersistentFormPage formPage);

  @SqlQuery("SELECT * FROM formPage WHERE id=:id")
  @SingleValue
  PersistentFormPage getById(@Bind("id") final Long id);

  @SqlBatch(
      "UPDATE"
          + " formPage"
          + " SET"
          + " title = :title,"
          + " subtitle = :subtitle, "
          + " hint = :hint, "
          + " pageOrder = :pageOrder, "
          + " repeatForEachCertificateInApplication = :repeatForEachCertificateInApplication, "
          + " lastUpdated = GETUTCDATE() "
          + " WHERE"
          + " id = :id")
  int[] update(@BindBean final List<PersistentFormPage> formPages);

  @SqlUpdate("DELETE FROM formPage WHERE id = :id")
  Integer delete(@Bind("id") final Long id);

  @SqlUpdate(
      "DELETE"
          + " fp"
          + " FROM"
          + " form f"
          + " INNER JOIN"
          + " formPage fp"
          + " ON"
          + " f.id=fp.formId"
          + " WHERE"
          + " f.name = :formName")
  Integer deleteByFormName(@Bind("formName") final String formName);

  @SqlUpdate(
      "DELETE"
          + " fp"
          + " FROM"
          + " form f"
          + " INNER JOIN"
          + " formPage fp"
          + " ON"
          + " f.id=fp.formId"
          + " WHERE"
          + " f.name = :formName"
          + " AND"
          + " f.version =:version")
  Integer deleteByFormNameAndVersion(
      @Bind("formName") final String formName, @Bind("version") final String version);

  @SqlQuery("SELECT" + " MAX(pageOrder)" + " FROM formPage" + " WHERE" + " formId =:formId")
  Integer getMaxPageOrderForForm(@Bind("formId") Long formId);

  @SqlQuery("SELECT MAX(questionOrder) FROM formQuestion WHERE formPageId=:formPageId")
  Optional<Integer> getMaxQuestionOrderForFormPage(@Bind("formPageId") Long formPageId);

}

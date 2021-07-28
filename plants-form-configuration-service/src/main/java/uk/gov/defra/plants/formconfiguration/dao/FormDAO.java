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
import uk.gov.defra.plants.formconfiguration.dao.mapper.PersistentFormDataColumnMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentForm;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;
import uk.gov.defra.plants.formconfiguration.representation.form.FormStatus;

@RegisterConstructorMapper(PersistentForm.class)
@RegisterColumnMapper(PersistentFormDataColumnMapper.class)
public interface FormDAO {

  @SqlQuery("SELECT" + " *" + " FROM" + " form" + " WHERE id=:id")
  @SingleValue
  PersistentForm getById(@Bind("id") final Long id);

  @SqlQuery("SELECT * FROM form WHERE name=:name ORDER BY created DESC")
  List<PersistentForm> getVersions(@Bind("name") final String name);

  @SqlQuery("SELECT TOP 1 * FROM form WHERE name=:name AND status='ACTIVE' ORDER BY created DESC")
  PersistentForm getActiveVersion(@Bind("name") final String name);

  @SqlQuery("SELECT TOP 1 * FROM form WHERE name=:name AND status='PRIVATE'")
  PersistentForm getPrivateVersion(@Bind("name") final String name);

  @SqlQuery("SELECT * FROM form WHERE name=:name AND version=:version")
  @SingleValue
  PersistentForm get(@Bind("name") final String name, @Bind("version") final String version);

  @SqlUpdate(
      "INSERT INTO"
          + " form ("
          + " name,"
          + " version,"
          + " formType,"
          + " status,"
          + " data,"
          + " created,"
          + " lastUpdated,"
          + " privateCode"
          + " ) VALUES ("
          + " :name,"
          + " :version,"
          + " :formType,"
          + " :status,"
          + " :data,"
          + " GETUTCDATE(),"
          + " GETUTCDATE(),"
          + " :privateCode"
          + ")")
  @GetGeneratedKeys
  Long insert(@BindBean final PersistentForm form);

  @SqlUpdate("UPDATE form SET lastUpdated=GETUTCDATE() WHERE id=:id")
  Integer updateLastUpdated(@Bind("id") final Long id);

  @SqlUpdate(
      "UPDATE"
          + " form"
          + " SET"
          + " data=:data,"
          + " status=:newStatus,"
          + " lastUpdated=GETUTCDATE()"
          + " WHERE"
          + " name=:name"
          + " AND version=:version")
  Integer updateStatus(
      @BindBean final PersistentForm form, @Bind("newStatus") final FormStatus newStatus);

  @SqlUpdate(
      "UPDATE"
          + " form"
          + " SET"
          + " data=:data,"
          + " lastUpdated=GETUTCDATE()"
          + " WHERE"
          + " name=:name"
          + " AND version=:version")
  Integer updateFormData(
      @Bind("data") final PersistentFormData data,
      @Bind("name") final String name,
      @Bind("version") final String version);

  @SqlUpdate("DELETE FROM form WHERE name=:name")
  Integer delete(@Bind("name") final String name);

  @SqlUpdate("DELETE FROM form WHERE name=:name AND version=:version")
  Integer deleteVersion(@Bind("name") final String name, @Bind("version") final String version);

  @SqlUpdate("UPDATE"
      + " form"
      + " SET"
      + " privateCode = :newPrivateCode"
      + " WHERE"
      + " name=:name"
      + " AND status='PRIVATE'")
  Integer regeneratePrivateLink(@Bind("name") final String name,
      @Bind("newPrivateCode") final Integer newPrivateCode);
}

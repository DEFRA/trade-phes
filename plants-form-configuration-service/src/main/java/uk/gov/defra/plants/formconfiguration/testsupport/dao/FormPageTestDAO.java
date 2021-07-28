package uk.gov.defra.plants.formconfiguration.testsupport.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FormPageTestDAO {

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
          + " f.name LIKE :formName")
  Integer clearTests(@Bind("formName") final String formName);
}

package uk.gov.defra.plants.formconfiguration.testsupport.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FormTestDAO {

  @SqlUpdate("DELETE FROM form WHERE name LIKE :name")
  Integer clearTests(@Bind("name") final String name);

}

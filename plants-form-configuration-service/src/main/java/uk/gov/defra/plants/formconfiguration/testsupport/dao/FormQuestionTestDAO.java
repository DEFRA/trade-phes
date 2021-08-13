package uk.gov.defra.plants.formconfiguration.testsupport.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FormQuestionTestDAO {

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
          + " f.name LIKE :name")
  Integer clearTests(@Bind("name") final String name);

}

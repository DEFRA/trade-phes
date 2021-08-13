package uk.gov.defra.plants.applicationform.dao;

import java.util.List;
import java.util.UUID;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.defra.plants.applicationform.dao.mapper.PersistentConsignmentDataColumnMapper;
import uk.gov.defra.plants.applicationform.model.PersistentConsignment;

@RegisterConstructorMapper(PersistentConsignment.class)
@RegisterColumnMapper(PersistentConsignmentDataColumnMapper.class)
public interface ConsignmentDAO {

  public static final String SELECT_CONSIGNMENT_QUERY_BASE =
      "SELECT c.id, c.applicationId, c.data, c.status FROM consignment c";

  @SqlUpdate(
      "INSERT INTO"
          + " consignment ("
          + " applicationId, "
          + " data "
          + " ) VALUES ("
          + " :applicationId, "
          + " :data"
          + " ) ")
  @GetGeneratedKeys
  UUID insertConsignment(@BindBean PersistentConsignment persistentConsignment);

  @SqlUpdate("DELETE FROM consignment WHERE id=:id")
  Integer deleteConsignment(@Bind("id") UUID id);

  @SqlUpdate("UPDATE consignment SET data = :data WHERE id = :id ")
  Integer updateConsignment(@BindBean PersistentConsignment persistentConsignment);

  @SqlQuery(SELECT_CONSIGNMENT_QUERY_BASE + " WHERE " + " c.id = :id")
  @SingleValue
  PersistentConsignment getConsignment(@Bind("id") UUID id);

  @SqlQuery(
      SELECT_CONSIGNMENT_QUERY_BASE
          + " WHERE "
          + " c.applicationId = :applicationFormId"
          + " ORDER by c.created ASC")
  List<PersistentConsignment> getConsignmentsForAppForm(
      @Bind("applicationFormId") Long applicationFormId);
}

package uk.gov.defra.plants.applicationform.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.defra.plants.applicationform.model.PersistentApplicationFormData;
import uk.gov.defra.plants.common.json.ItemsMapper;

public class PersistentApplicationFormDataColumnMapper implements ColumnMapper<PersistentApplicationFormData> {
  @Override
  public PersistentApplicationFormData map(ResultSet r, int columnNumber, StatementContext ctx)
      throws SQLException {
    return ItemsMapper.fromJson(r.getString(columnNumber), PersistentApplicationFormData.class);
  }
}

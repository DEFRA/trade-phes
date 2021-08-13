package uk.gov.defra.plants.formconfiguration.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.defra.plants.common.json.ItemsMapper;
import uk.gov.defra.plants.formconfiguration.model.PersistentFormData;

public class PersistentFormDataColumnMapper implements ColumnMapper<PersistentFormData> {
  @Override
  public PersistentFormData map(ResultSet r, int columnNumber, StatementContext ctx)
      throws SQLException {
    return ItemsMapper.fromJson(r.getString(columnNumber), PersistentFormData.class);
  }
}
